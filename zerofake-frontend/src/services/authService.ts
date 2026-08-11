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
  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await authApi.login(request);
    return response.data;
  }

  async refreshToken(
    request: RefreshTokenRequest
  ): Promise<TokenResponse> {
    const response = await authApi.refreshToken(request);
    return response.data;
  }

  async logout(): Promise<void> {
    await authApi.logout();
  }

  async getCurrentUser(): Promise<UserResponse> {
    const response = await authApi.getCurrentUser();
    return response.data;
  }

  async register(
    request: RegisterRequest
  ): Promise<RegisterResponse> {
    const response = await authApi.register(request);
    return response.data;
  }
}

export const authService = new AuthService();
