package com.yaxinaz.security;

import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.user.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedResourceAccessException("No authenticated user in context");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentPrincipal().getId();
    }

    public static Role currentRole() {
        return currentPrincipal().getRole();
    }

    public static boolean hasRole(Role role) {
        return currentRole() == role;
    }

    public static void requireOwnerOrRole(Long resourceOwnerId, Role... allowedRoles) {
        Long userId = currentUserId();
        if (userId.equals(resourceOwnerId)) {
            return;
        }
        Role current = currentRole();
        for (Role allowed : allowedRoles) {
            if (current == allowed) {
                return;
            }
        }
        throw new UnauthorizedResourceAccessException("You do not have permission to access this resource");
    }
}
