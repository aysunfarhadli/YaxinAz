package com.yaxinaz.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostCommentRequest(
        @NotBlank @Size(max = 2000) String content
) {
}
