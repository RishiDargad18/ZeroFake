/** Status of a ledger transaction recorded by the blockchain service. */
export type BlockchainStatus =
  | "PENDING"
  | "SUCCESS"
  | "FAILED";

/**
 * Kinds of transaction written to the ledger.
 *
 * Verification is a ledger query, not a transaction, so it does not appear here.
 */
export type TransactionType =
  | "PRODUCT_REGISTERED"
  | "OWNERSHIP_TRANSFERRED";

export interface RegisterProductRequest {
  productId: string;
  manufacturerId: string;
}

export interface BlockchainTransactionResponse {
  id: string;
  productId: string;
  /** Fabric transaction ID. Null when a proposal failed before it was assigned one. */
  transactionId: string | null;
  /** Ledger block the transaction was committed in. Null for failed transactions. */
  blockNumber: number | null;
  /** Not exposed by the Fabric Gateway client; always null for now. */
  blockHash: string | null;
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

/** On-chain state of a product, returned by a ledger query. */
export interface BlockchainVerificationResponse {
  productId: string;
  authentic: boolean;
  message: string;
  manufacturerId: string | null;
  currentOwnerId: string | null;
  currentOwnerRole: string | null;
  productStatus: string | null;
  registeredAt: string | null;
  lastUpdatedAt: string | null;
}
