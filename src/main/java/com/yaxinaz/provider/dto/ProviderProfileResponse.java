package com.yaxinaz.provider.dto;

import com.yaxinaz.provider.ServiceCategory;

import java.time.Instant;
import java.util.Set;

public record ProviderProfileResponse(
        Long id,
        Long userId,
        String businessName,
        String bio,
        String serviceArea,
        boolean verified,
        double averageRating,
        int reviewCount,
        Set<ServiceCategory> categories,
        Instant createdAt
) {
}
