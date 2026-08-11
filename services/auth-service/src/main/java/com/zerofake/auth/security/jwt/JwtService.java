package com.zerofake.auth.security.jwt;

import com.zerofake.auth.config.JwtProperties;
import com.zerofake.auth.constant.JwtClaims;
import com.zerofake.auth.security.user.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log =
            LoggerFactory.getLogger(JwtService.class);

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

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getAccessTokenExpiration()
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    /**
     * Builds a signed JWT.
     *
     * <p>The token carries the user's identifier and role as claims so that the
     * downstream services (product, blockchain, fraud detection) can authenticate
     * and authorize a request without calling back into this service.
     */
    private String buildToken(
            UserDetails userDetails,
            long expiration
    ) {

        Date now = new Date();

        Map<String, Object> claims = Map.of(
                JwtClaims.USER_ID, resolveUserId(userDetails),
                JwtClaims.ROLE, resolveRole(userDetails)
        );

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    private String resolveUserId(UserDetails userDetails) {

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser().getId().toString();
        }

        return "";
    }

    private String resolveRole(UserDetails userDetails) {

        return userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
}
