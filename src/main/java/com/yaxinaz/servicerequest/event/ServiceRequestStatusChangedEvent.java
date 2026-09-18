package com.yaxinaz.servicerequest.event;

import com.yaxinaz.servicerequest.ServiceRequestStatus;

public record ServiceRequestStatusChangedEvent(
        Long serviceRequestId,
        Long customerId,
        Long providerId,
        String providerBusinessName,
        ServiceRequestStatus newStatus
) {
}
