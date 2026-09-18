package com.yaxinaz.notification;

import com.yaxinaz.community.CommunityRepository;
import com.yaxinaz.community.event.MembershipStatusChangedEvent;
import com.yaxinaz.issue.event.IssueEscalatedEvent;
import com.yaxinaz.issue.event.IssueStatusChangedEvent;
import com.yaxinaz.servicerequest.event.ServiceRequestStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the domain events other modules already publish and turns the relevant ones into
 * Notification rows (spec sections 54-55). Deliberately decoupled: IssueService,
 * IssueMaintenanceScheduler, CommunityService, and ServiceRequestService all publish events without
 * knowing this listener - or notifications at all - exist. Uses AFTER_COMMIT so a notification is
 * never created for a change that then rolls back.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final CommunityRepository communityRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIssueStatusChanged(IssueStatusChangedEvent event) {
        notificationService.create(
                event.reporterUserId(),
                NotificationType.ISSUE_STATUS_UPDATED,
                "Issue status updated",
                "Your issue is now " + event.newStatus().name().replace('_', ' ') + ".",
                event.issueId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIssueEscalated(IssueEscalatedEvent event) {
        communityRepository.findByIdAndDeletedFalse(event.communityId()).ifPresent(community ->
                notificationService.create(
                        community.getCreatedBy(),
                        NotificationType.CRITICAL_ALERT,
                        "Issue escalated",
                        event.reason(),
                        event.issueId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMembershipStatusChanged(MembershipStatusChangedEvent event) {
        NotificationType type = switch (event.newStatus()) {
            case APPROVED -> NotificationType.MEMBERSHIP_APPROVED;
            case REJECTED, BLOCKED -> NotificationType.MEMBERSHIP_REJECTED;
            case PENDING -> null;
        };
        if (type == null) {
            return;
        }
        String message = type == NotificationType.MEMBERSHIP_APPROVED
                ? "Your request to join " + event.communityName() + " was approved."
                : "Your request to join " + event.communityName() + " was not approved.";
        notificationService.create(event.userId(), type, "Community membership update", message, event.communityId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onServiceRequestStatusChanged(ServiceRequestStatusChangedEvent event) {
        switch (event.newStatus()) {
            case ACCEPTED -> notificationService.create(
                    event.customerId(), NotificationType.PROVIDER_REQUEST_ACCEPTED,
                    "Service request accepted",
                    event.providerBusinessName() + " accepted your service request.",
                    event.serviceRequestId());
            case COMPLETED -> notificationService.create(
                    event.customerId(), NotificationType.SERVICE_REQUEST_COMPLETED,
                    "Service completed",
                    event.providerBusinessName() + " marked your service request as completed. Consider leaving a review.",
                    event.serviceRequestId());
            default -> log.debug("No notification mapped for service request status {}", event.newStatus());
        }
    }
}
