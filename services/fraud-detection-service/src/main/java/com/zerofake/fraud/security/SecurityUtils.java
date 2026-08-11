package com.zerofake.fraud.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Convenience accessors for the caller resolved from the current access token.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    public static boolean hasRole(String role) {
        return currentUser()
                .map(user -> role.equals(user.role()))
                .orElse(false);
    }
}
