package com.yaxinaz.feed.dto;

import com.yaxinaz.feed.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull PostType postType,
        @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String content,
        String imageUrl
) {
}
