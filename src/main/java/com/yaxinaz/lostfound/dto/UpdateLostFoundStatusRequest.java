package com.yaxinaz.lostfound.dto;

import com.yaxinaz.lostfound.LostFoundStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLostFoundStatusRequest(
        @NotNull LostFoundStatus status
) {
}
