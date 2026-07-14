import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { authService } from "@/services/authService";

import type {
  AuthResponse,
  LoginRequest,
  UserResponse,
} from "@/types/auth";

interface AuthContextValue {
  user: UserResponse | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  login: (
    request: LoginRequest
  ) => Promise<void>;

  logout: () => Promise<void>;
}

export const AuthContext =
  createContext<AuthContextValue | null>(
    null
  );

const ACCESS_TOKEN_KEY =
  "zerofake_access_token";

const REFRESH_TOKEN_KEY =
  "zerofake_refresh_token";

const USER_KEY = "zerofake_user";

interface AuthProviderProps {
  children: React.ReactNode;
}

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [user, setUser] =
    useState<UserResponse | null>(null);

  const [token, setToken] =
    useState<string | null>(null);

  const [isLoading, setIsLoading] =
    useState(true);

  useEffect(() => {
    const storedToken =
      localStorage.getItem(
        ACCESS_TOKEN_KEY
      );

    const storedUser =
      localStorage.getItem(USER_KEY);

    if (storedToken) {
      setToken(storedToken);
    }

    if (storedUser) {
      setUser(
        JSON.parse(storedUser)
      );
    }

    setIsLoading(false);
  }, []);

  const login = useCallback(
    async (
      request: LoginRequest
    ) => {
      const response: AuthResponse =
        await authService.login(
          request
        );

      setToken(
        response.token.accessToken
      );

      setUser(response.user);

      localStorage.setItem(
        ACCESS_TOKEN_KEY,
        response.token.accessToken
      );

      localStorage.setItem(
        REFRESH_TOKEN_KEY,
        response.token.refreshToken
      );

      localStorage.setItem(
        USER_KEY,
        JSON.stringify(response.user)
      );
    },
    []
  );

  const logout = useCallback(
  async () => {
    try {
      await authService.logout();
    } finally {
      setToken(null);

      setUser(null);

      localStorage.removeItem(
        ACCESS_TOKEN_KEY
      );

      localStorage.removeItem(
        REFRESH_TOKEN_KEY
      );

      localStorage.removeItem(
        USER_KEY
      );

      window.location.href = "/login";
    }
  },
  []
);

  const value = useMemo(
    () => ({
      user,
      token,
      isLoading,
      isAuthenticated:
        token !== null,
      login,
      logout,
    }),
    [
      user,
      token,
      isLoading,
      login,
      logout,
    ]
  );

  return (
    <AuthContext.Provider
      value={value}
    >
      {children}
    </AuthContext.Provider>
  );
}