package com.yaxinaz.user.dto;

import com.yaxinaz.user.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        String avatarUrl,
        String preferredLanguage,
        boolean enabled,
        Instant createdAt
) {
}
