import { fraudApi } from "@/api/fraudApi";

import type {
  VerificationResponse,
  VerifyProductRequest,
} from "@/types/verification";

class VerificationService {
  async verifyProduct(
    request: VerifyProductRequest
  ): Promise<VerificationResponse> {
    const response =
      await fraudApi.verifyProduct(
        request
      );

    return response.data;
  }
}

export const verificationService =
  new VerificationService();