package com.yaxinaz.servicerequest.dto;

import com.yaxinaz.provider.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateServiceRequestRequest(
        @NotNull Long providerId,
        @NotNull ServiceCategory category,
        @NotBlank @Size(max = 2000) String description
) {
}
