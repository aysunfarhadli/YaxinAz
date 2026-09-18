package com.yaxinaz.review.dto;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long serviceRequestId,
        Long authorId,
        String authorName,
        Long providerId,
        int rating,
        String comment,
        Instant createdAt
) {
}
