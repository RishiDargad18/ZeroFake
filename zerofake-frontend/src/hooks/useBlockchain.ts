import {
  useCallback,
  useState,
} from "react";

import { blockchainService } from "@/services/blockchainService";

import type {
  BlockchainTransactionResponse,
  RegisterProductRequest,
} from "@/types/blockchain";

interface UseBlockchainReturn {
  isRegistering: boolean;

  registerProduct: (
    request: RegisterProductRequest
  ) => Promise<BlockchainTransactionResponse>;
}

export function useBlockchain(): UseBlockchainReturn {
  const [isRegistering, setIsRegistering] =
    useState(false);

  const registerProduct = useCallback(
    async (
      request: RegisterProductRequest
    ) => {
      setIsRegistering(true);

      try {
        return await blockchainService.registerProduct(
          request
        );
      } catch (error) {
        throw error;
      } finally {
        setIsRegistering(false);
      }
    },
    []
  );

  return {
    registerProduct,
    isRegistering,
  };
}