export interface ApiResponse<T> {
  timestamp?: string;
  status: number;
  success: boolean;
  message: string;
  data: T;
}

export interface ApiError {
  status?: number;
  message: string;
  details?: unknown;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}