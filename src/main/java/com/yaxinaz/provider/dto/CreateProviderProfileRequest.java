package com.yaxinaz.provider.dto;

import com.yaxinaz.provider.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateProviderProfileRequest(
        @NotBlank @Size(max = 200) String businessName,
        @Size(max = 2000) String bio,
        String serviceArea,
        @NotEmpty Set<ServiceCategory> categories
) {
}
