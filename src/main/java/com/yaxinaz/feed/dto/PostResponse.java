package com.yaxinaz.feed.dto;

import com.yaxinaz.feed.PostType;

import java.time.Instant;

public record PostResponse(
        Long id,
        Long communityId,
        Long authorId,
        String authorName,
        PostType postType,
        String title,
        String content,
        String imageUrl,
        boolean pinned,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser,
        Instant createdAt,
        Instant updatedAt
) {
}
