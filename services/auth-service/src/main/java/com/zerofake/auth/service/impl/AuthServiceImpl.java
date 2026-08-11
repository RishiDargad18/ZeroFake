package com.zerofake.auth.service.impl;

import com.zerofake.auth.config.JwtProperties;
import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.dto.request.LoginRequest;
import com.zerofake.auth.dto.request.RefreshTokenRequest;
import com.zerofake.auth.dto.request.RegisterRequest;
import com.zerofake.auth.dto.response.AuthResponse;
import com.zerofake.auth.dto.response.RegisterResponse;
import com.zerofake.auth.dto.response.TokenResponse;
import com.zerofake.auth.dto.response.UserResponse;
import com.zerofake.auth.entity.RefreshToken;
import com.zerofake.auth.entity.User;
import com.zerofake.auth.exception.BadRequestException;
import com.zerofake.auth.exception.ConflictException;
import com.zerofake.auth.exception.UnauthorizedException;
import com.zerofake.auth.mapper.UserMapper;
import com.zerofake.auth.repository.RefreshTokenRepository;
import com.zerofake.auth.repository.UserRepository;
import com.zerofake.auth.security.jwt.JwtService;
import com.zerofake.auth.security.user.CustomUserDetails;
import com.zerofake.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers a new user.
     *
     * <p>Self-service registration is restricted to {@link RoleType#ROLE_CUSTOMER}.
     * Supply chain roles and administrator accounts represent privileged positions
     * in the chain of custody and may only be created by an existing administrator.
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "A user already exists with email: " + request.getEmail()
            );
        }

        if (request.getRole() != RoleType.ROLE_CUSTOMER && !isCurrentUserAdmin()) {
            throw new BadRequestException(
                    "Self-registration is only permitted for ROLE_CUSTOMER. "
                            + "Privileged roles must be created by an administrator."
            );
        }

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return userMapper.toRegisterResponse(savedUser);
    }

    private boolean isCurrentUserAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        RoleType.ROLE_ADMIN.name().equals(authority.getAuthority()));
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // Throws an AuthenticationException on bad credentials or a disabled
        // account; both are translated to 401 by the global exception handler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken token = refreshTokenRepository
                .findByUser(user)
                .orElseGet(RefreshToken::new);

        token.setUser(user);
        token.setToken(refreshToken);
        token.setExpiryDate(
                LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiration() / 1000
                )
        );
        token.setRevoked(false);

        refreshTokenRepository.save(token);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();

        return AuthResponse.builder()
                .token(tokenResponse)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new BadRequestException("Invalid refresh token.")
                );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new BadRequestException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();

        UserDetails userDetails = new CustomUserDetails(user);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new BadRequestException("Refresh token is no longer valid.");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }

    @Override
    public void logout() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            refreshTokenRepository.deleteByUser(customUserDetails.getUser());
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("No authenticated user found.");
        }

        return userMapper.toUserResponse(customUserDetails.getUser());
    }
}
