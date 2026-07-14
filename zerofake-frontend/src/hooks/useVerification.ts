import {
  useCallback,
  useState,
} from "react";

import { verificationService } from "@/services/verificationService";

import type {
  VerificationResponse,
  VerifyProductRequest,
} from "@/types/verification";

interface UseVerificationReturn {
  verifyProduct: (
    request: VerifyProductRequest
  ) => Promise<VerificationResponse>;

  isVerifying: boolean;
}

export function useVerification(): UseVerificationReturn {
  const [isVerifying, setIsVerifying] =
    useState(false);

  const verifyProduct =
    useCallback(
      async (
        request: VerifyProductRequest
      ) => {
        setIsVerifying(true);

        try {
          return await verificationService.verifyProduct(
            request
          );
        } catch (error) {
          throw error;
        } finally {
          setIsVerifying(false);
        }
      },
      []
    );

  return {
    verifyProduct,
    isVerifying,
  };
}