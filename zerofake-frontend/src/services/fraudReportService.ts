import { fraudApi } from "@/api/fraudApi";

import type { FraudReportResponse } from "@/types/fraud";

class FraudReportService {
  async getFraudReports(): Promise<
    FraudReportResponse[]
  > {
    const response =
      await fraudApi.getFraudReports();

    return response.data;
  }
}

export const fraudReportService =
  new FraudReportService();