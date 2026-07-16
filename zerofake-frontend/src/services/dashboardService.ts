import { productApi } from "@/api/productApi";
import { blockchainApi } from "@/api/blockchainApi";
import { fraudApi } from "@/api/fraudApi";
import type { DashboardStatistics } from "@/types/dashboard";
import type { UserResponse } from "@/types/auth";

class DashboardService {
  async getStatistics(user: UserResponse): Promise<DashboardStatistics> {
    let productsRegistered = 0;
    let blockchainTransactions = 0;
    let productVerifications = 0;
    let fraudReports = 0;

    const isAdmin = user?.role === "ROLE_ADMIN";

    try {
      const prodRes = await productApi.getAllProducts();
      let products = prodRes.data || [];

      const txRes = await blockchainApi.getAllTransactions();
      let txs = txRes || [];

      if (!isAdmin && user) {
        const userTxs = txs.filter(tx => tx.performedBy === user.id);
        const relatedProductIds = new Set(userTxs.map(tx => tx.productId));

        products.forEach(p => {
          if (p.manufacturerId === user.id) {
            relatedProductIds.add(p.id);
          }
        });

        products = products.filter(p => relatedProductIds.has(p.id));
        txs = txs.filter(tx => relatedProductIds.has(tx.productId));
      }

      productsRegistered = products.length;
      blockchainTransactions = txs.length;

      const logsRes = await fraudApi.getVerificationLogs();
      let logs = logsRes.data || [];
      if (!isAdmin && user) {
        const productIds = new Set(products.map(p => p.id));
        logs = logs.filter(log => productIds.has(log.productId));
      }
      productVerifications = logs.length;

      const fraudRes = await fraudApi.getFraudReports();
      let reports = fraudRes.data || [];
      if (!isAdmin && user) {
        const productIds = new Set(products.map(p => p.id));
        reports = reports.filter(report => productIds.has(report.productId));
      }
      fraudReports = reports.length;
    } catch (err) {
      console.error("Failed to calculate dashboard statistics:", err);
    }

    return {
      productsRegistered,
      blockchainTransactions,
      productVerifications,
      fraudReports,
    };
  }
}

export const dashboardService =
  new DashboardService();