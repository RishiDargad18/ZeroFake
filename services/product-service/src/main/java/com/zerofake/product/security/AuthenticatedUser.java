package com.zerofake.product.security;

import java.util.UUID;

/**
 * The authenticated caller, resolved from the claims of a validated access token.
 *
 * @param id    the user's unique identifier (userId claim)
 * @param email the user's email address (token subject)
 * @param role  the user's role, including the ROLE_ prefix
 */
public record AuthenticatedUser(UUID id, String email, String role) {
}
