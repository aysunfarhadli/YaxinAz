package com.yaxinaz.event.dto;

import com.yaxinaz.event.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
        @NotNull AttendanceStatus status
) {
}
