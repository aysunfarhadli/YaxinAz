package com.yaxinaz.poll.dto;

public record PollOptionResponse(
        Long id,
        String text,
        long voteCount,
        double percentage
) {
}
