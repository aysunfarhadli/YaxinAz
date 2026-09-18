package com.yaxinaz.community.dto;

import com.yaxinaz.community.CommunityType;

import java.time.Instant;

public record CommunityResponse(
        Long id,
        String name,
        String description,
        CommunityType type,
        String city,
        String district,
        String address,
        String coverImageUrl,
        long memberCount,
        Instant createdAt
) {
}
