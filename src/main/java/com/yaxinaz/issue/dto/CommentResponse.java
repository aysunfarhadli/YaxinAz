package com.yaxinaz.issue.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long issueId,
        Long authorId,
        String authorName,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
