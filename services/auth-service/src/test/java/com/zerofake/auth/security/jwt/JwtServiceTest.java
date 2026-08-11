package com.zerofake.auth.security.jwt;

import com.zerofake.auth.config.JwtProperties;
import com.zerofake.auth.constant.JwtClaims;
import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.constant.UserStatus;
import com.zerofake.auth.entity.User;
import com.zerofake.auth.security.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Token issuance.
 *
 * <p>The claims asserted here are a cross-service contract: the product,
 * blockchain and fraud services authorise every request from {@code userId} and
 * {@code role} without calling back to this service. If either claim stops
 * being issued, authorisation silently breaks everywhere downstream.
 */
class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "a-test-signing-key-with-enough-entropy-for-hmac-sha-256!!".getBytes()
    );

    private static final String ISSUER = "zerofake-auth-service";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setAccessTokenExpiration(900_000);
        properties.setRefreshTokenExpiration(604_800_000);

        jwtService = new JwtService(properties);
        ReflectionTestUtils.invokeMethod(jwtService, "init");

        user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("manufacturer@zerofake.com");
        user.setPassword("irrelevant");
        user.setFirstName("Manufacturer");
        user.setLastName("User");
        user.setRole(RoleType.ROLE_MANUFACTURER);
        user.setStatus(UserStatus.ACTIVE);
    }

    private Claims parse(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Nested
    @DisplayName("access token claims")
    class AccessTokenClaims {

        @Test
        @DisplayName("carries the user id, so downstream services need no lookup")
        void carriesUserId() {

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            assertThat(parse(token).get(JwtClaims.USER_ID, String.class))
                    .isEqualTo(user.getId().toString());
        }

        @Test
        @DisplayName("carries the role, including its ROLE_ prefix")
        void carriesRoleWithPrefix() {

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            // The prefix matters: downstream services compare the claim against
            // authorities such as "ROLE_MANUFACTURER" directly.
            assertThat(parse(token).get(JwtClaims.ROLE, String.class))
                    .isEqualTo("ROLE_MANUFACTURER");
        }

        @Test
        @DisplayName("uses the email as the subject")
        void usesEmailAsSubject() {

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            assertThat(parse(token).getSubject()).isEqualTo("manufacturer@zerofake.com");
        }

        @Test
        @DisplayName("is stamped with the configured issuer")
        void isStampedWithIssuer() {

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            assertThat(parse(token).getIssuer()).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("never contains the password hash")
        void neverContainsPasswordHash() {

            user.setPassword("$2a$10$averysecretbcryptedhashvalue");

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            // A JWT payload is only base64, not encryption — anything placed in
            // it is readable by the holder.
            String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));

            assertThat(payload).doesNotContain("averysecretbcryptedhashvalue");
            assertThat(payload).doesNotContain("password");
        }
    }

    @Nested
    @DisplayName("refresh token")
    class RefreshToken {

        @Test
        @DisplayName("outlives the access token")
        void outlivesAccessToken() {

            UserDetails details = new CustomUserDetails(user);

            var access = parse(jwtService.generateAccessToken(details)).getExpiration();
            var refresh = parse(jwtService.generateRefreshToken(details)).getExpiration();

            assertThat(refresh).isAfter(access);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("accepts a token issued for the same user")
        void acceptsTokenForSameUser() {

            UserDetails details = new CustomUserDetails(user);

            assertThat(jwtService.isTokenValid(
                    jwtService.generateAccessToken(details), details)
            ).isTrue();
        }

        @Test
        @DisplayName("rejects a token presented by a different user")
        void rejectsTokenForDifferentUser() {

            String token = jwtService.generateAccessToken(new CustomUserDetails(user));

            User other = new User();
            ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
            other.setEmail("attacker@zerofake.com");
            other.setPassword("irrelevant");
            other.setRole(RoleType.ROLE_CUSTOMER);
            other.setStatus(UserStatus.ACTIVE);

            assertThat(jwtService.isTokenValid(token, new CustomUserDetails(other))).isFalse();
        }

        @Test
        @DisplayName("rejects a token signed with a different key")
        void rejectsTokenSignedWithDifferentKey() {

            String foreignSecret = Base64.getEncoder().encodeToString(
                    "a-completely-different-key-of-sufficient-length-here!!!!!".getBytes()
            );

            String forged = Jwts.builder()
                    .subject(user.getEmail())
                    .issuer(ISSUER)
                    .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(foreignSecret)))
                    .compact();

            assertThatThrownBy(() -> jwtService.extractUsername(forged))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("rejects a token from an unexpected issuer")
        void rejectsTokenFromUnexpectedIssuer() {

            String foreign = Jwts.builder()
                    .subject(user.getEmail())
                    .issuer("some-other-system")
                    .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET)))
                    .compact();

            assertThatThrownBy(() -> jwtService.extractUsername(foreign))
                    .isInstanceOf(io.jsonwebtoken.IncorrectClaimException.class);
        }

        @Test
        @DisplayName("rejects an expired token")
        void rejectsExpiredToken() {

            JwtProperties expired = new JwtProperties();
            expired.setSecret(SECRET);
            expired.setIssuer(ISSUER);
            expired.setAccessTokenExpiration(-1_000);
            expired.setRefreshTokenExpiration(-1_000);

            JwtService expiringService = new JwtService(expired);
            ReflectionTestUtils.invokeMethod(expiringService, "init");

            String token = expiringService.generateAccessToken(new CustomUserDetails(user));

            assertThatThrownBy(() -> expiringService.extractUsername(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("rejects a malformed token")
        void rejectsMalformedToken() {

            assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                    .isInstanceOf(io.jsonwebtoken.JwtException.class);
        }
    }
}
