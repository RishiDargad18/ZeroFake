import { createApiClient } from "@/api/axios";

import type {
  BlockchainTransactionResponse,
  ProductHistoryResponse,
  RegisterProductRequest,
  TransferOwnershipRequest,
} from "@/types/blockchain";

const api = createApiClient(
  import.meta.env.VITE_BLOCKCHAIN_API
);

const BASE_URL = "/api/v1/blockchain";

export const blockchainApi = {
  async registerProduct(
    request: RegisterProductRequest
  ) {
    const response =
      await api.post<BlockchainTransactionResponse>(
        `${BASE_URL}/register-product`,
        request
      );

    return response.data;
  },
  async getProductHistory(
  productId: string
) {
  const response =
    await api.get<ProductHistoryResponse>(
      `${BASE_URL}/products/${productId}/history`
    );

  return response.data;
},
async transferOwnership(
  request: TransferOwnershipRequest
) {
  const response =
    await api.post<BlockchainTransactionResponse>(
      `${BASE_URL}/transfer-ownership`,
      request
    );

  return response.data;
},

};