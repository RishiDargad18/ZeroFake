export type FraudType =
  | "PRODUCT_NOT_FOUND"
  | "BLOCKCHAIN_MISMATCH"
  | "INVALID_OWNER"
  | "MULTIPLE_LOCATION_SCAN"
  | "DUPLICATE_QR"
  | "SUSPICIOUS_ACTIVITY"
  | "EXPIRED_PRODUCT";

export type FraudStatus =
  | "OPEN"
  | "UNDER_INVESTIGATION"
  | "RESOLVED";

export interface FraudReportResponse {
  reportId: string;
  productId: string;
  fraudType: FraudType;
  status: FraudStatus;
  riskScore: number;
  detectedAt: string;
}