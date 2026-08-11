import { createApiClient } from "@/api/axios";

import type { ApiResponse } from "@/types/api";

import type {
  BlockchainTransactionResponse,
  BlockchainVerificationResponse,
  ProductHistoryResponse,
  RegisterProductRequest,
  TransferOwnershipRequest,
} from "@/types/blockchain";

const api = createApiClient(
  import.meta.env.VITE_BLOCKCHAIN_API
);

const BASE_URL = "/api/v1/blockchain";

export const blockchainApi = {
  async registerProduct(request: RegisterProductRequest) {
    const response = await api.post<
      ApiResponse<BlockchainTransactionResponse>
    >(`${BASE_URL}/register-product`, request);

    return response.data;
  },

  async verifyProduct(productId: string) {
    const response = await api.post<
      ApiResponse<BlockchainVerificationResponse>
    >(`${BASE_URL}/verify-product`, { productId });

    return response.data;
  },

  async getProductHistory(productId: string) {
    const response = await api.get<ApiResponse<ProductHistoryResponse>>(
      `${BASE_URL}/products/${productId}/history`
    );

    return response.data;
  },

  async transferOwnership(request: TransferOwnershipRequest) {
    const response = await api.post<
      ApiResponse<BlockchainTransactionResponse>
    >(`${BASE_URL}/transfer-ownership`, request);

    return response.data;
  },

  async getTransactionByTransactionId(transactionId: string) {
    const response = await api.get<
      ApiResponse<BlockchainTransactionResponse>
    >(`${BASE_URL}/transactions/${transactionId}`);

    return response.data;
  },

  async getTransactionsByProductId(productId: string) {
    const response = await api.get<
      ApiResponse<BlockchainTransactionResponse[]>
    >(`${BASE_URL}/transactions/product/${productId}`);

    return response.data;
  },

  async getAllTransactions() {
    const response = await api.get<
      ApiResponse<BlockchainTransactionResponse[]>
    >(`${BASE_URL}/transactions`);

    return response.data;
  },
};
