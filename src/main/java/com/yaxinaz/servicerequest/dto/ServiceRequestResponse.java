package com.yaxinaz.servicerequest.dto;

import com.yaxinaz.provider.ServiceCategory;
import com.yaxinaz.servicerequest.ServiceRequestStatus;

import java.time.Instant;

public record ServiceRequestResponse(
        Long id,
        Long customerId,
        String customerName,
        Long providerId,
        String providerBusinessName,
        ServiceCategory category,
        String description,
        ServiceRequestStatus status,
        Instant requestedAt,
        Instant updatedAt,
        Instant completedAt
) {
}
