package com.yaxinaz.event;

import com.yaxinaz.event.dto.AttendanceRequest;
import com.yaxinaz.event.dto.CreateEventRequest;
import com.yaxinaz.event.dto.EventResponse;
import com.yaxinaz.response.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities/{communityId}/events")
@RequiredArgsConstructor
@Tag(name = "Events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> create(@PathVariable Long communityId, @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(communityId, request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> list(
            @PathVariable Long communityId,
            @RequestParam(required = false) Boolean upcoming,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(eventService.listEvents(communityId, upcoming, pageable));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> get(@PathVariable Long communityId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(communityId, eventId));
    }

    @PostMapping("/{eventId}/attendance")
    public ResponseEntity<EventResponse> attendance(
            @PathVariable Long communityId, @PathVariable Long eventId, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(eventService.setAttendance(communityId, eventId, request.status()));
    }
}
