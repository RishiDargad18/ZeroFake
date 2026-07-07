package com.zerofake.auth.service;

import com.zerofake.auth.dto.common.ApiResponse;
import com.zerofake.auth.dto.request.LoginRequest;
import com.zerofake.auth.dto.request.RefreshTokenRequest;
import com.zerofake.auth.dto.request.RegisterRequest;
import com.zerofake.auth.dto.response.AuthResponse;
import com.zerofake.auth.dto.response.RegisterResponse;
import com.zerofake.auth.dto.response.TokenResponse;
import com.zerofake.auth.dto.response.UserResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    ApiResponse<Void> logout();

    UserResponse getCurrentUser();
}