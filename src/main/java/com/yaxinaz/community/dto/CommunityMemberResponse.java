package com.yaxinaz.community.dto;

import com.yaxinaz.community.MembershipStatus;

import java.time.Instant;

public record CommunityMemberResponse(
        Long membershipId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String avatarUrl,
        MembershipStatus status,
        Instant joinedAt
) {
}
