export type VerificationResult =
  | "GENUINE"
  | "SUSPICIOUS"
  | "COUNTERFEIT";

export interface VerifyProductRequest {
  productId: string;
  userId: string;
  userRole: string;
  ipAddress: string;
  deviceInfo: string;
  location: string;
}

export interface VerificationResponse {
  productId: string;
  authentic: boolean;
  fraudDetected: boolean;
  riskScore: number;
  verificationResult: VerificationResult;
  triggeredRules: string[];
  message: string;
}

export interface VerificationLogResponse {
  verificationId: string;
  productId: string;
  riskScore: number;
  authentic: boolean;
  fraudDetected: boolean;
  verificationResult: VerificationResult;
  scannedAt: string;
}