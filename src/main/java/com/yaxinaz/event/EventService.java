package com.yaxinaz.event;

import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.event.dto.CreateEventRequest;
import com.yaxinaz.event.dto.EventResponse;
import com.yaxinaz.exception.EventCapacityExceededException;
import com.yaxinaz.exception.EventNotFoundException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final CommunityEventRepository eventRepository;
    private final EventAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CommunityService communityService;

    @Transactional
    public EventResponse createEvent(Long communityId, CreateEventRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Community community = communityService.getCommunityOrThrow(communityId);
        communityService.requireApprovedMember(userId, communityId);

        CommunityEvent event = eventRepository.save(CommunityEvent.builder()
                .community(community)
                .title(request.title().trim())
                .description(request.description())
                .location(request.location())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .capacity(request.capacity())
                .createdBy(userId)
                .build());

        return toResponse(event, userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> listEvents(Long communityId, Boolean upcoming, Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);

        Page<CommunityEvent> page;
        if (Boolean.TRUE.equals(upcoming)) {
            page = eventRepository.findAllByCommunityIdAndDeletedFalseAndStartTimeAfterOrderByStartTimeAsc(
                    communityId, Instant.now(), pageable);
        } else if (Boolean.FALSE.equals(upcoming)) {
            page = eventRepository.findAllByCommunityIdAndDeletedFalseAndStartTimeBeforeOrderByStartTimeDesc(
                    communityId, Instant.now(), pageable);
        } else {
            page = eventRepository.findAllByCommunityIdAndDeletedFalseOrderByStartTimeAsc(communityId, pageable);
        }
        return PagedResponse.of(page, event -> toResponse(event, userId));
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long communityId, Long eventId) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);
        return toResponse(getEventOrThrow(communityId, eventId), userId);
    }

    @Transactional
    public EventResponse setAttendance(Long communityId, Long eventId, AttendanceStatus status) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);
        CommunityEvent event = getEventOrThrow(communityId, eventId);

        var existing = attendanceRepository.findByEventIdAndUserId(eventId, userId);

        if (status == AttendanceStatus.GOING && event.getCapacity() != null) {
            long currentGoing = attendanceRepository.countByEventIdAndStatus(eventId, AttendanceStatus.GOING);
            boolean alreadyGoing = existing.isPresent() && existing.get().getStatus() == AttendanceStatus.GOING;
            if (!alreadyGoing && currentGoing >= event.getCapacity()) {
                throw new EventCapacityExceededException("This event has reached its capacity");
            }
        }

        if (existing.isPresent()) {
            existing.get().setStatus(status);
        } else {
            User user = userRepository.findByIdAndDeletedFalse(userId)
                    .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));
            attendanceRepository.save(EventAttendance.builder().event(event).user(user).status(status).build());
        }

        return toResponse(event, userId);
    }

    private CommunityEvent getEventOrThrow(Long communityId, Long eventId) {
        return eventRepository.findByIdAndCommunityIdAndDeletedFalse(eventId, communityId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private EventResponse toResponse(CommunityEvent event, Long currentUserId) {
        Map<AttendanceStatus, Long> counts = attendanceRepository.countByEventIdGroupByStatus(event.getId()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        EventAttendanceRepository.AttendanceStatusCount::getStatus,
                        EventAttendanceRepository.AttendanceStatusCount::getTotal));

        AttendanceStatus myStatus = attendanceRepository.findByEventIdAndUserId(event.getId(), currentUserId)
                .map(EventAttendance::getStatus)
                .orElse(null);

        return new EventResponse(
                event.getId(), event.getCommunity().getId(), event.getTitle(), event.getDescription(),
                event.getLocation(), event.getStartTime(), event.getEndTime(), event.getCapacity(),
                counts.getOrDefault(AttendanceStatus.GOING, 0L),
                counts.getOrDefault(AttendanceStatus.MAYBE, 0L),
                counts.getOrDefault(AttendanceStatus.NOT_GOING, 0L),
                myStatus, event.getCreatedAt());
    }
}
