package com.yaxinaz.community.event;

import com.yaxinaz.community.MembershipStatus;

public record MembershipStatusChangedEvent(
        Long userId,
        Long communityId,
        String communityName,
        MembershipStatus newStatus
) {
}
