import {
  useCallback,
  useState,
} from "react";

import { blockchainService } from "@/services/blockchainService";

import type {
  ProductHistoryResponse,
} from "@/types/blockchain";

interface UseBlockchainHistoryReturn {
  history: ProductHistoryResponse | null;

  isLoading: boolean;

  loadHistory: (
    productId: string
  ) => Promise<ProductHistoryResponse>;
}

export function useBlockchainHistory(): UseBlockchainHistoryReturn {
  const [history, setHistory] =
    useState<ProductHistoryResponse | null>(
      null
    );

  const [isLoading, setIsLoading] =
    useState(false);

  const loadHistory = useCallback(
    async (productId: string) => {
      setIsLoading(true);

      try {
        const response =
          await blockchainService.getProductHistory(
            productId
          );

        setHistory(response);

        return response;
      } catch (error) {
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  return {
    history,
    isLoading,
    loadHistory,
  };
}