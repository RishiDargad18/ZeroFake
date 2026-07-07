package com.zerofake.auth.service.impl;
import com.zerofake.auth.security.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import com.zerofake.auth.dto.common.ApiResponse;
import com.zerofake.auth.dto.request.LoginRequest;
import com.zerofake.auth.dto.request.RefreshTokenRequest;
import com.zerofake.auth.dto.request.RegisterRequest;
import com.zerofake.auth.dto.response.AuthResponse;
import com.zerofake.auth.dto.response.RegisterResponse;
import com.zerofake.auth.dto.response.TokenResponse;
import com.zerofake.auth.dto.response.UserResponse;
import com.zerofake.auth.entity.User;
import com.zerofake.auth.mapper.UserMapper;
import com.zerofake.auth.repository.RefreshTokenRepository;
import com.zerofake.auth.repository.UserRepository;
import com.zerofake.auth.security.jwt.JwtService;
import com.zerofake.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zerofake.auth.config.JwtProperties;
import com.zerofake.auth.entity.RefreshToken;
import com.zerofake.auth.security.user.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return userMapper.toRegisterResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + request.getEmail()
                ));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(refreshToken);
        token.setExpiryDate(
                LocalDateTime.now().plusSeconds(
                        jwtProperties.getRefreshTokenExpiration() / 1000
                )
        );
        token.setRevoked(false);

        refreshTokenRepository.save(token);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setExpiresIn(jwtProperties.getAccessTokenExpiration());

        AuthResponse response = new AuthResponse();
        response.setToken(tokenResponse);
        response.setUser(userMapper.toUserResponse(user));

        return response;
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token.")
                );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new IllegalArgumentException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();

        UserDetails userDetails = new CustomUserDetails(user);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);

        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken.getToken());
        response.setExpiresIn(jwtProperties.getAccessTokenExpiration());

        return response;
    }

    @Override
    public ApiResponse<Void> logout() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            refreshTokenRepository.deleteByUser(customUserDetails.getUser());
        }

        SecurityContextHolder.clearContext();

        return ApiResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Logged out successfully.")
                .data(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            throw new IllegalStateException("No authenticated user found.");
        }

        return userMapper.toUserResponse(customUserDetails.getUser());
    }
}