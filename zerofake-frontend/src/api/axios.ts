import axios, {
  AxiosError,
  type AxiosInstance,
} from "axios";

const DEFAULT_TIMEOUT = 15_000;

const ACCESS_TOKEN_KEY =
  "zerofake_access_token";

export function createApiClient(
  baseURL: string
): AxiosInstance {
  const instance = axios.create({
    baseURL,
    timeout: DEFAULT_TIMEOUT,
    headers: {
      "Content-Type":
        "application/json",
      Accept: "application/json",
    },
  });

  instance.interceptors.request.use(
    (config) => {
      const token =
        localStorage.getItem(
          ACCESS_TOKEN_KEY
        );

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      return config;
    },
    (error) =>
      Promise.reject(error)
  );

  instance.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      if (
        error.response?.status === 401
      ) {
        localStorage.removeItem(
          "zerofake_access_token"
        );

        localStorage.removeItem(
          "zerofake_refresh_token"
        );

        localStorage.removeItem(
          "zerofake_user"
        );

        if (
          window.location.pathname !==
          "/login"
        ) {
          window.location.href =
            "/login";
        }
      }

      return Promise.reject(error);
    }
  );

  return instance;
}