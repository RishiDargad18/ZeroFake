import { productApi } from "@/api/productApi";
import type { DashboardStatistics } from "@/types/dashboard";

class DashboardService {
  async getStatistics(): Promise<DashboardStatistics> {
    const response = await productApi.getAllProducts();

    const products = response.data ?? [];

    return {
      productsRegistered: products.length,

      // Temporary mock values until the
      // Blockchain Service is integrated.
      blockchainTransactions: 4875,

      // Temporary mock values until the
      // Verification Service is integrated.
      productVerifications: 932,

      // Temporary mock values until the
      // Fraud Detection Service is integrated.
      fraudReports: 17,
    };
  }
}

export const dashboardService =
  new DashboardService();