import axios from "axios";

export function getApiError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return "Unable to connect to the server. Please check your connection.";
    }

    const response = error.response.data;

    if (response && typeof response === "object") {
      // Check inside data wrapper first
      if (
        "data" in response &&
        response.data &&
        typeof response.data === "object" &&
        "message" in response.data &&
        typeof response.data.message === "string"
      ) {
        return response.data.message;
      }
      // Check top-level message next
      if ("message" in response && typeof response.message === "string") {
        return response.message;
      }
    }

    switch (error.response.status) {
      case 400:
        return "Invalid request.";

      case 401:
        return "Unauthorized request.";

      case 403:
        return "Access denied.";

      case 404:
        return "Requested resource was not found.";

      case 409:
        return "A conflict occurred while processing the request.";

      case 500:
        return "Internal server error.";

      default:
        return "Something went wrong. Please try again.";
    }
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}