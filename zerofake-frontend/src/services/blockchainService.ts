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

}

export const blockchainService =
  new BlockchainService();