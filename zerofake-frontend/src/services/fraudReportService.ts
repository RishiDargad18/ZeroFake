import { fraudApi } from "@/api/fraudApi";

import type {
  FraudReportRequest,
  FraudReportResponse,
} from "@/types/fraud";

class FraudReportService {
  async getFraudReports(): Promise<FraudReportResponse[]> {
    const response = await fraudApi.getFraudReports();
    return response.data ?? [];
  }

  async createFraudReport(
    request: FraudReportRequest
  ): Promise<FraudReportResponse> {
    const response = await fraudApi.createFraudReport(request);
    return response.data;
  }
}

export const fraudReportService = new FraudReportService();
