package com.yaxinaz.servicerequest.dto;

import com.yaxinaz.servicerequest.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateServiceRequestStatusRequest(
        @NotNull ServiceRequestStatus status
) {
}
