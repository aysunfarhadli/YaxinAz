package com.yaxinaz.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String content,
        String imageUrl
) {
}
