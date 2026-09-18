package com.yaxinaz.lostfound.dto;

import com.yaxinaz.lostfound.LostFoundStatus;
import com.yaxinaz.lostfound.LostFoundType;

import java.time.Instant;

public record LostFoundItemResponse(
        Long id,
        Long communityId,
        LostFoundType type,
        String title,
        String description,
        String imageUrl,
        String location,
        LostFoundStatus status,
        Long createdBy,
        String reporterName,
        Instant createdAt
) {
}
