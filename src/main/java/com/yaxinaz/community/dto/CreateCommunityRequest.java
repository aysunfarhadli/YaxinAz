package com.yaxinaz.community.dto;

import com.yaxinaz.community.CommunityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommunityRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotNull CommunityType type,
        @NotBlank @Size(max = 100) String city,
        String district,
        String address,
        String coverImageUrl
) {
}
