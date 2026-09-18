package com.yaxinaz.community.dto;

import com.yaxinaz.community.MembershipStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMembershipStatusRequest(
        @NotNull MembershipStatus status
) {
}
