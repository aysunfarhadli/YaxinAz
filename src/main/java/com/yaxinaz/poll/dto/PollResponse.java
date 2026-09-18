package com.yaxinaz.poll.dto;

import java.time.Instant;
import java.util.List;

public record PollResponse(
        Long id,
        Long communityId,
        String question,
        List<PollOptionResponse> options,
        long totalVotes,
        Instant expiresAt,
        boolean active,
        boolean votedByCurrentUser,
        Long myVoteOptionId,
        Instant createdAt
) {
}
