import { blockchainApi } from "@/api/blockchainApi";

import type {
  BlockchainTransactionResponse,
  ProductHistoryResponse,
  RegisterProductRequest,
  TransferOwnershipRequest,
} from "@/types/blockchain";

class BlockchainService {
  /**
   * Anchors the product's identity on the ledger.
   *
   * The blockchain service also promotes the product to REGISTERED in the
   * product catalogue, so the client does not need to follow up with a status
   * update of its own.
   */
  async registerProduct(
    request: RegisterProductRequest
  ): Promise<BlockchainTransactionResponse> {
    const response = await blockchainApi.registerProduct(request);
    return response.data;
  }

  async getProductHistory(
    productId: string
  ): Promise<ProductHistoryResponse> {
    const response = await blockchainApi.getProductHistory(productId);
    return response.data;
  }

  async transferOwnership(
    request: TransferOwnershipRequest
  ): Promise<BlockchainTransactionResponse> {
    const response = await blockchainApi.transferOwnership(request);
    return response.data;
  }

  async getTransactionByTransactionId(
    transactionId: string
  ): Promise<BlockchainTransactionResponse> {
    const response =
      await blockchainApi.getTransactionByTransactionId(transactionId);
    return response.data;
  }

  async getTransactionsByProductId(
    productId: string
  ): Promise<BlockchainTransactionResponse[]> {
    const response =
      await blockchainApi.getTransactionsByProductId(productId);
    return response.data ?? [];
  }

  async getAllTransactions(): Promise<BlockchainTransactionResponse[]> {
    const response = await blockchainApi.getAllTransactions();
    return response.data ?? [];
  }
}

export const blockchainService = new BlockchainService();
