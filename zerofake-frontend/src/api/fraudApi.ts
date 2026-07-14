import { createApiClient } from "@/api/axios";
import type { FraudReportResponse } from "@/types/fraud";
import type { ApiResponse } from "@/types/api";

import type {
  VerificationResponse,
  VerifyProductRequest,
} from "@/types/verification";

const api = createApiClient(
  import.meta.env.VITE_FRAUD_API
);

const BASE_URL = "/api/v1/fraud";

export const fraudApi = {
  async verifyProduct(
    request: VerifyProductRequest
  ) {
    const response =
      await api.post<
        ApiResponse<VerificationResponse>
      >(
        `${BASE_URL}/verify`,
        request
      );

    return response.data;
  },
  async getFraudReports() {
  const response =
    await api.get<
      ApiResponse<FraudReportResponse[]>
    >(`${BASE_URL}/reports`);

  return response.data;
},
};