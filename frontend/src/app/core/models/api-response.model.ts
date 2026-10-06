/**
 * Mirrors the backend's standard response envelope. Every REST endpoint returns one of these two
 * shapes (discriminated by `success`).
 */
export interface ApiResponse<T> {
  success: true;
  message: string;
  data: T;
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiError {
  success: false;
  message: string;
  errorCode: string;
  errors: ApiFieldError[];
}

export type ApiEnvelope<T> = ApiResponse<T> | ApiError;

/**
 * Spring Data `Page<T>` shape. Every list endpoint in the contract returns this inside `data`.
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
