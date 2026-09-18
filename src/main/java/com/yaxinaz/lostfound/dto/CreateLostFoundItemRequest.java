package com.yaxinaz.lostfound.dto;

import com.yaxinaz.lostfound.LostFoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLostFoundItemRequest(
        @NotNull LostFoundType type,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        String imageUrl,
        String location
) {
}
