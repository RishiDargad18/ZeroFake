export type VerificationResult =
  | "GENUINE"
  | "SUSPICIOUS"
  | "COUNTERFEIT";

/**
 * A verification request.
 *
 * The scanning user is taken from the access token by the fraud detection
 * service, so no user id or role is sent from the client.
 */
export interface VerifyProductRequest {
  productId: string;
  ipAddress?: string;
  deviceInfo?: string;
  location?: string;
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
