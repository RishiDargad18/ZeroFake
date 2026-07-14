import {
  useCallback,
  useState,
} from "react";

import { blockchainService } from "@/services/blockchainService";

import type {
  BlockchainTransactionResponse,
  TransferOwnershipRequest,
} from "@/types/blockchain";

interface UseOwnershipTransferReturn {
  isTransferring: boolean;

  transferOwnership: (
    request: TransferOwnershipRequest
  ) => Promise<BlockchainTransactionResponse>;
}

export function useOwnershipTransfer(): UseOwnershipTransferReturn {
  const [isTransferring, setIsTransferring] =
    useState(false);

  const transferOwnership = useCallback(
    async (
      request: TransferOwnershipRequest
    ) => {
      setIsTransferring(true);

      try {
        return await blockchainService.transferOwnership(
          request
        );
      } catch (error) {
        throw error;
      } finally {
        setIsTransferring(false);
      }
    },
    []
  );

  return {
    transferOwnership,
    isTransferring,
  };
}