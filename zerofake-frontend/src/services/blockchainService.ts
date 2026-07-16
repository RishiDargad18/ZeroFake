import { blockchainApi } from "@/api/blockchainApi";

import type {
  BlockchainTransactionResponse,
  ProductHistoryResponse,
  RegisterProductRequest,
  TransferOwnershipRequest,
} from "@/types/blockchain";

class BlockchainService {
  async registerProduct(
    request: RegisterProductRequest
  ): Promise<BlockchainTransactionResponse> {
    return blockchainApi.registerProduct(
      request
    );
  }
  async getProductHistory(
  productId: string
): Promise<ProductHistoryResponse> {
  return blockchainApi.getProductHistory(
    productId
  );
}

  async transferOwnership(
    request: TransferOwnershipRequest
  ): Promise<BlockchainTransactionResponse> {
    return blockchainApi.transferOwnership(
      request
    );
  }

  async getTransactionByTransactionId(transactionId: string): Promise<BlockchainTransactionResponse> {
    return blockchainApi.getTransactionByTransactionId(transactionId);
  }

  async getTransactionsByProductId(productId: string): Promise<BlockchainTransactionResponse[]> {
    return blockchainApi.getTransactionsByProductId(productId);
  }

  async getAllTransactions(): Promise<BlockchainTransactionResponse[]> {
    return blockchainApi.getAllTransactions();
  }
}

export const blockchainService =
  new BlockchainService();