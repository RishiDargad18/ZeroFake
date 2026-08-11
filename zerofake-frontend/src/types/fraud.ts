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
  | "UNDER_REVIEW"
  | "CONFIRMED"
  | "FALSE_POSITIVE"
  | "RESOLVED";

export interface FraudReportResponse {
  reportId: string;
  productId: string;
  fraudType: FraudType;
  status: FraudStatus;
  riskScore: number;
  detectedAt: string;
}

export interface FraudReportRequest {
  productId: string;
  /** Optional: defaults to SUSPICIOUS_ACTIVITY when the reporter cannot classify it. */
  fraudType?: FraudType;
  description: string;
}
