package com.yaxinaz.event.dto;

import com.yaxinaz.event.AttendanceStatus;

import java.time.Instant;

public record EventResponse(
        Long id,
        Long communityId,
        String title,
        String description,
        String location,
        Instant startTime,
        Instant endTime,
        Integer capacity,
        long goingCount,
        long maybeCount,
        long notGoingCount,
        AttendanceStatus myStatus,
        Instant createdAt
) {
}
