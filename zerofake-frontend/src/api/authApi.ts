import { createApiClient } from "@/api/axios";

import type { ApiResponse } from "@/types/api";

import type {
  AuthResponse,
  LoginRequest,
  RefreshTokenRequest,
  TokenResponse,
  UserResponse,
  RegisterRequest,
  RegisterResponse,
} from "@/types/auth";

const api = createApiClient(
  import.meta.env.VITE_AUTH_API
);

const BASE_URL = "/api/v1/auth";

export const authApi = {
  async login(
    request: LoginRequest
  ) {
    const response =
      await api.post<AuthResponse>(
        `${BASE_URL}/login`,
        request
      );

    return response.data;
  },

  async refreshToken(
    request: RefreshTokenRequest
  ) {
    const response =
      await api.post<TokenResponse>(
        `${BASE_URL}/refresh`,
        request
      );

    return response.data;
  },

  async logout() {
    const response =
      await api.post<ApiResponse<void>>(
        `${BASE_URL}/logout`
      );

    return response.data;
  },

  async getCurrentUser() {
    const response =
      await api.get<UserResponse>(
        `${BASE_URL}/me`
      );

    return response.data;
  },

  async register(request: RegisterRequest) {
    const response = await api.post<ApiResponse<RegisterResponse>>(
      `${BASE_URL}/register`,
      request
    );
    return response.data;
  },
};