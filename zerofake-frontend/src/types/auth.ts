export interface LoginRequest {
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export type UserRole =
  | "ROLE_ADMIN"
  | "ROLE_MANUFACTURER"
  | "ROLE_WAREHOUSE"
  | "ROLE_DISTRIBUTOR"
  | "ROLE_RETAILER"
  | "ROLE_CUSTOMER";

export type UserStatus =
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED";

export interface UserResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  lastLogin: string | null;
}

export interface AuthResponse {
  token: TokenResponse;
  user: UserResponse;
}