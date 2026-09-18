package com.yaxinaz.notification;

import com.yaxinaz.config.properties.FeatureProperties;
import com.yaxinaz.exception.NotificationNotFoundException;
import com.yaxinaz.notification.dto.NotificationResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FeatureProperties featureProperties;

    /**
     * REQUIRES_NEW is deliberate, not defensive boilerplate: this is called from
     * {@link NotificationEventListener}'s @TransactionalEventListener(AFTER_COMMIT) methods, where
     * the publishing transaction's resources can still be bound to the thread during Spring's
     * afterCommit() callback phase (cleanup/unbinding happens slightly later). A plain REQUIRED
     * propagation would join that already-committing transaction instead of opening a fresh one,
     * and the write would silently never get its own commit. Confirmed experimentally: without
     * REQUIRES_NEW, `NOTIFICATION_CREATED` logged and `save()` returned normally, but the row was
     * not visible to a subsequent request in the same test.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(Long userId, NotificationType type, String title, String message, Long referenceId) {
        Notification notification = notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .read(false)
                .build());

        log.info("NOTIFICATION_CREATED userId={} type={} referenceId={}", userId, type, referenceId);

        if (featureProperties.websocketEnabled()) {
            try {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, toResponse(notification));
            } catch (Exception ex) {
                log.warn("Failed to push WebSocket notification to user {}: {}", userId, ex.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> listMine(Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        Page<Notification> page = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(SecurityUtils.currentUserId());
    }

    @Transactional
    public void markRead(Long id) {
        Long userId = SecurityUtils.currentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead() {
        notificationRepository.markAllReadForUser(SecurityUtils.currentUserId());
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getType().name(), notification.getTitle(),
                notification.getMessage(), notification.getReferenceId(), notification.isRead(),
                notification.getCreatedAt());
    }
}
