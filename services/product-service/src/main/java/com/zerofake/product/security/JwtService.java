package com.zerofake.product.security;

import com.zerofake.product.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

/**
 * Validates access tokens issued by the ZeroFake authentication service.
 *
 * <p>Token issuance lives exclusively in the authentication service. This class
 * verifies the signature and issuer, and extracts the identity claims needed to
 * authorize a request without an additional network call.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log =
            LoggerFactory.getLogger(JwtService.class);

    /** Claim names shared with the authentication service. */
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    /**
     * The signing key checked into this repository as a local-development
     * default. It is public by definition, so anyone could forge a token
     * with it -- hence the warning if a deployment ever runs on it.
     */
    private static final String DEVELOPMENT_ONLY_SECRET =
            "ZGV2LW9ubHktc2VjcmV0LWRvLW5vdC11c2UtaW4tcHJvZHVjdGlvbi16ZXJvZmFrZS1obWFjLXNoYS1rZXktMjU2LWJpdHM=";

    @PostConstruct
    private void init() {

        String secret = jwtProperties.getSecret();

        if (DEVELOPMENT_ONLY_SECRET.equals(secret == null ? null : secret.strip())) {
            log.warn(
                    "\n"
                    + "****************************************************************\n"
                    + "Running on the built-in development JWT signing key.\n"
                    + "This key is published in the repository: anyone can forge a\n"
                    + "token with it. Set the JWT_SECRET environment variable before\n"
                    + "exposing this service beyond localhost.\n"
                    + "  openssl rand -base64 64 | tr -d '[:space:]'\n"
                    + "****************************************************************"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(decodeSecret(secret));
    }

    /**
     * Decodes the configured signing key.
     *
     * <p>Surrounding whitespace is stripped before decoding. A secret supplied
     * through an environment file or a shell pipeline very often arrives with a
     * trailing newline or carriage return, and the decoder's own complaint
     * ("Illegal base64 character d") gives no hint that this is the cause.
     */
    private byte[] decodeSecret(String secret) {

        String normalised = secret == null ? "" : secret.strip();

        try {
            return Base64.getDecoder().decode(normalised);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "The configured JWT secret is not valid Base64. Generate one with: "
                            + "openssl rand -base64 64 | tr -d '[:space:]'",
                    ex
            );
        }
    }

    /**
     * Parses and validates the supplied token.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired,
     *                                      or not signed by the expected issuer
     */
    public AuthenticatedUser parse(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.get(CLAIM_USER_ID, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);

        if (userId == null || userId.isBlank() || role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token is missing required identity claims."
            );
        }

        return new AuthenticatedUser(
                UUID.fromString(userId),
                claims.getSubject(),
                role
        );
    }
}
