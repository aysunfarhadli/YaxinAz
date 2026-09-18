package com.yaxinaz.provider.dto;

import jakarta.validation.constraints.NotNull;

public record VerifyProviderRequest(
        @NotNull Boolean verified
) {
}
