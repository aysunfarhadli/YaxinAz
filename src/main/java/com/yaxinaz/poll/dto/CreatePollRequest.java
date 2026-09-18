package com.yaxinaz.poll.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreatePollRequest(
        @NotBlank @Size(max = 500) String question,
        @Size(min = 2, max = 10) List<@NotBlank String> options,
        @Future Instant expiresAt
) {
}
