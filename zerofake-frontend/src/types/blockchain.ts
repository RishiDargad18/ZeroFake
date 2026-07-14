export type BlockchainStatus =
  | "PENDING"
  | "SUCCESS"
  | "FAILED";

export type TransactionType =
  | "REGISTER_PRODUCT"
  | "TRANSFER_OWNERSHIP";

export interface RegisterProductRequest {
  productId: string;
  manufacturerId: string;
}

export interface BlockchainTransactionResponse {
  id: string;
  productId: string;
  transactionId: string;
  blockNumber: number;
  blockHash: string;
  transactionType: TransactionType;
  performedBy: string;
  status: BlockchainStatus;
  message: string;
  createdAt: string;
}
export interface ProductHistoryItemResponse {
  manufacturerId: string;
  currentOwnerId: string;
  currentOwnerRole: string;
  productStatus: string;
  verified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductHistoryResponse {
  productId: string;
  history: ProductHistoryItemResponse[];
}
export type OwnerRole =
  | "MANUFACTURER"
  | "WAREHOUSE"
  | "DISTRIBUTOR"
  | "RETAILER"
  | "CUSTOMER";

export interface TransferOwnershipRequest {
  productId: string;
  fromOwnerId: string;
  toOwnerId: string;
  toOwnerRole: OwnerRole;
}