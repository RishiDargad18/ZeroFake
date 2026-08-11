import { productApi } from "@/api/productApi";
import { blockchainApi } from "@/api/blockchainApi";
import { fraudApi } from "@/api/fraudApi";
import type { DashboardStatistics } from "@/types/dashboard";
import type { UserResponse } from "@/types/auth";

/**
 * Builds the dashboard counters.
 *
 * Each source is fetched independently so that one unavailable — or one the
 * signed-in role is not permitted to read — degrades that single tile rather
 * than blanking the whole dashboard. Aggregated fraud intelligence is
 * restricted to administrators and manufacturers, so a customer receives 403
 * on those endpoints by design.
 */
async function safely<T>(
  load: () => Promise<T>,
  fallback: T,
  label: string
): Promise<T> {
  try {
    return await load();
  } catch (error) {
    console.warn(`Dashboard: could not load ${label}.`, error);
    return fallback;
  }
}

class DashboardService {
  async getStatistics(
    user: UserResponse
  ): Promise<DashboardStatistics> {
    const isAdmin = user?.role === "ROLE_ADMIN";

    const products = await safely(
      async () => (await productApi.getAllProducts()).data ?? [],
      [],
      "products"
    );

    const transactions = await safely(
      async () => (await blockchainApi.getAllTransactions()).data ?? [],
      [],
      "blockchain transactions"
    );

    // Non-administrators only see what they are involved in: products they
    // manufacture, and transactions they performed.
    const visibleProducts = isAdmin
      ? products
      : products.filter((product) => {
          const ownsProduct = product.manufacturerId === user.id;

          const actedOnProduct = transactions.some(
            (tx) => tx.productId === product.id && tx.performedBy === user.id
          );

          return ownsProduct || actedOnProduct;
        });

    const visibleProductIds = new Set(
      visibleProducts.map((product) => product.id)
    );

    const visibleTransactions = isAdmin
      ? transactions
      : transactions.filter((tx) => visibleProductIds.has(tx.productId));

    const logs = await safely(
      async () => (await fraudApi.getVerificationLogs()).data ?? [],
      [],
      "verification logs"
    );

    const reports = await safely(
      async () => (await fraudApi.getFraudReports()).data ?? [],
      [],
      "fraud reports"
    );

    const visibleLogs = isAdmin
      ? logs
      : logs.filter((log) => visibleProductIds.has(log.productId));

    const visibleReports = isAdmin
      ? reports
      : reports.filter((report) =>
          visibleProductIds.has(report.productId)
        );

    return {
      productsRegistered: visibleProducts.length,
      blockchainTransactions: visibleTransactions.length,
      productVerifications: visibleLogs.length,
      fraudReports: visibleReports.length,
    };
  }
}

export const dashboardService = new DashboardService();
