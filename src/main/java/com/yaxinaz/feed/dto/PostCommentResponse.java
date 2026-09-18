package com.yaxinaz.feed.dto;

import java.time.Instant;

public record PostCommentResponse(
        Long id,
        Long postId,
        Long authorId,
        String authorName,
        String content,
        Instant createdAt
) {
}
