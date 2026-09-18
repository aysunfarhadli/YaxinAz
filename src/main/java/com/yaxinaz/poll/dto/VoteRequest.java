package com.yaxinaz.poll.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(
        @NotNull Long optionId
) {
}
