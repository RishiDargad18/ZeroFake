export interface CategoryResponse {
  id: string;
  name: string;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export type BlockchainStatus =
  | "PENDING"
  | "REGISTERED"
  | "FAILED";

export interface ProductResponse {
  id: string;
  productCode: string;
  productName: string;
  description: string;
  brand: string;
  manufacturerId: string;
  category: CategoryResponse;
  qrCodePath: string;
  imageUrl: string;
  imageFileName: string;
  imageContentType: string;
  imageSize: number;
  active: boolean;
  blockchainStatus: BlockchainStatus;
  createdAt: string;
  updatedAt: string;
}