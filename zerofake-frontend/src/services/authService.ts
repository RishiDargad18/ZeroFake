import { authApi } from "@/api/authApi";

import type {
  AuthResponse,
  LoginRequest,
  RefreshTokenRequest,
  TokenResponse,
  UserResponse,
  RegisterRequest,
  RegisterResponse,
} from "@/types/auth";

class AuthService {
  async login(
    request: LoginRequest
  ): Promise<AuthResponse> {
    return await authApi.login(request);
  }

  async refreshToken(
    request: RefreshTokenRequest
  ): Promise<TokenResponse> {
    return await authApi.refreshToken(
      request
    );
  }

  async logout(): Promise<void> {
    await authApi.logout();
  }

  async getCurrentUser(): Promise<UserResponse> {
    return await authApi.getCurrentUser();
  }

  async register(request: RegisterRequest): Promise<RegisterResponse> {
    const apiResponse = await authApi.register(request);
    return apiResponse.data;
  }
}

export const authService =
  new AuthService();