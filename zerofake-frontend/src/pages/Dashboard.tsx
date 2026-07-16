import { useEffect, useState } from "react";

import { motion } from "framer-motion";
import {
  BarChart3,
} from "lucide-react";
import { toast } from "react-hot-toast";

import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from "chart.js";

import { Bar, Line } from "react-chartjs-2";

import {
  GlassBadge,
  GlassCard,
  GlassLoader,
  GlassStatCard,
} from "@/components/ui";

import {
  DASHBOARD_STATISTICS,
} from "@/constants/dashboard";

import {
  barChartOptions,
  lineChartOptions,
} from "@/constants/chartOptions";

import {
  fraudTrendData,
  verificationTrendData,
} from "@/constants/dashboardCharts";

import { useDashboard } from "@/hooks/useDashboard";
import { useAuth } from "@/hooks/useAuth";
import { productApi } from "@/api/productApi";
import { blockchainService } from "@/services/blockchainService";
import { fraudApi } from "@/api/fraudApi";
import type { BlockchainTransactionResponse } from "@/types/blockchain";
import { getApiError } from "@/utils/apiError";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Tooltip,
  Legend,
  Filler
);

const containerVariants = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.12,
    },
  },
};

const itemVariants = {
  hidden: {
    opacity: 0,
    y: 20,
  },
  visible: {
    opacity: 1,
    y: 0,
  },
};

export default function Dashboard() {
  const { user } = useAuth();
  const {
    statistics,
    loading,
    error,
  } = useDashboard();

  const [recentTx, setRecentTx] = useState<BlockchainTransactionResponse[]>([]);
  const [verificationChart, setVerificationChart] = useState<any>(verificationTrendData);
  const [fraudChart, setFraudChart] = useState<any>(fraudTrendData);

  useEffect(() => {
    const fetchRecentTx = async () => {
      if (!user) return;
      try {
        const txs = await blockchainService.getAllTransactions();
        let filteredTxs = txs;

        if (user.role !== "ROLE_ADMIN") {
          const prodRes = await productApi.getAllProducts();
          const products = prodRes.data || [];

          const userTxs = txs.filter(tx => tx.performedBy === user.id);
          const relatedProductIds = new Set(userTxs.map(tx => tx.productId));

          products.forEach(p => {
            if (p.manufacturerId === user.id) {
              relatedProductIds.add(p.id);
            }
          });

          filteredTxs = txs.filter(tx => relatedProductIds.has(tx.productId));
        }

        const sorted = [...filteredTxs]
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, 5);
        setRecentTx(sorted);
      } catch (err) {
        console.error("Failed to load recent transactions for dashboard:", err);
      }
    };
    if (!loading && user) {
      void fetchRecentTx();
    }
  }, [loading, user]);

  useEffect(() => {
    const fetchChartData = async () => {
      if (!user) return;
      try {
        const [logsRes, reportsRes] = await Promise.all([
          fraudApi.getVerificationLogs(),
          fraudApi.getFraudReports()
        ]);
        
        let logs = logsRes.data || [];
        let reports = reportsRes.data || [];

        if (user.role !== "ROLE_ADMIN") {
          const prodRes = await productApi.getAllProducts();
          const products = prodRes.data || [];

          const txs = await blockchainService.getAllTransactions();
          const userTxs = txs.filter(tx => tx.performedBy === user.id);
          const relatedProductIds = new Set(userTxs.map(tx => tx.productId));

          products.forEach(p => {
            if (p.manufacturerId === user.id) {
              relatedProductIds.add(p.id);
            }
          });

          logs = logs.filter(log => relatedProductIds.has(log.productId));
          reports = reports.filter(report => relatedProductIds.has(report.productId));
        }
        
        const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
        const last7Days = Array.from({ length: 7 }, (_, i) => {
          const d = new Date();
          d.setDate(d.getDate() - (6 - i));
          return d;
        });

        const labels = last7Days.map(d => days[d.getDay()]);

        const verificationsData = last7Days.map(date => {
          return logs.filter(log => {
            const logDate = new Date(log.scannedAt);
            return logDate.toDateString() === date.toDateString();
          }).length;
        });

        const fraudData = last7Days.map(date => {
          return reports.filter(report => {
            const repDate = new Date(report.detectedAt);
            return repDate.toDateString() === date.toDateString();
          }).length;
        });

        setVerificationChart({
          labels,
          datasets: [
            {
              label: "Verifications",
              data: verificationsData,
              borderColor: "#3B82F6",
              backgroundColor: "rgba(59,130,246,0.18)",
              tension: 0.4,
              fill: true,
              pointRadius: 4,
              pointHoverRadius: 6,
            },
          ],
        });

        setFraudChart({
          labels,
          datasets: [
            {
              label: "Fraud Reports",
              data: fraudData,
              backgroundColor: [
                "#2563EB",
                "#3B82F6",
                "#60A5FA",
                "#2563EB",
                "#3B82F6",
                "#60A5FA",
                "#2563EB",
              ],
              borderRadius: 8,
              maxBarThickness: 40,
            },
          ],
        });
      } catch (err) {
        console.error("Failed to load chart trends:", err);
      }
    };
    if (!loading && user) {
      void fetchChartData();
    }
  }, [loading, user]);

  useEffect(() => {
    if (error) {
      toast.error(getApiError(error));
    }
  }, [error]);

  if (loading) {
    return (
      <GlassLoader message="Loading Dashboard..." />
    );
  }

  const statCards =
    DASHBOARD_STATISTICS.map((item) => {
      if (item.title === "Products Registered") {
        return {
          ...item,
          value: statistics?.productsRegistered ?? 0,
        };
      }
      if (item.title === "Blockchain Transactions") {
        return {
          ...item,
          value: statistics?.blockchainTransactions ?? 0,
        };
      }
      if (item.title === "Product Verifications") {
        return {
          ...item,
          value: statistics?.productVerifications ?? 0,
        };
      }
      if (item.title === "Fraud Reports") {
        return {
          ...item,
          value: statistics?.fraudReports ?? 0,
        };
      }

      return item;
    });

  return (
    <motion.div
      className="space-y-8"
      initial="hidden"
      animate="visible"
      variants={containerVariants}
    >
      <motion.section variants={itemVariants}>
        <div className="space-y-2">
          <h1 className="text-4xl font-bold">
            Dashboard
          </h1>

          <p className="text-gray-400">
            Welcome back! Here's what's happening
            across your ZeroFake platform today.
          </p>
        </div>
      </motion.section>

      <motion.section variants={itemVariants}>
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
          {statCards.map((stat) => {
            const Icon = stat.icon;

            return (
              <GlassStatCard
                key={stat.id}
                title={stat.title}
                value={stat.value}
                trend={stat.trend}
                percentage={stat.percentage}
                icon={<Icon size={28} />}
              />
            );
          })}
        </div>
      </motion.section>

      <motion.section variants={itemVariants}>
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
          <GlassCard hover>
            <div className="flex flex-col gap-6">
              <div>
                <h2 className="text-2xl font-semibold">
                  Verification Trend
                </h2>

                <p className="mt-1 text-sm text-gray-400">
                  Product verification activity over
                  the last 7 days
                </p>
              </div>

              <div className="h-72">
                <Line
                  data={verificationChart}
                  options={lineChartOptions}
                />
              </div>
            </div>
          </GlassCard>

          <GlassCard hover>
            <div className="flex flex-col gap-6">
              <div>
                <h2 className="text-2xl font-semibold">
                  Fraud Trend
                </h2>

                <p className="mt-1 text-sm text-gray-400">
                  Fraud detection reports over the
                  last 7 days
                </p>
              </div>

              <div className="h-72">
                <Bar
                  data={fraudChart}
                  options={barChartOptions}
                />
              </div>
            </div>
          </GlassCard>
        </div>
      </motion.section>
            <motion.section variants={itemVariants}>
        <GlassCard>
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-semibold">
                Recent Activity
              </h2>

              <p className="mt-1 text-gray-400">
                Latest actions across the ZeroFake
                platform.
              </p>
            </div>

            <div className="hidden md:flex items-center gap-2 text-sm text-gray-400">
              <BarChart3 size={18} />
              Live Feed
            </div>
          </div>

          <div className="mt-8 space-y-4">
            {recentTx.length === 0 ? (
              <p className="text-gray-400 text-sm text-center py-6">
                No recent activity recorded on the ledger.
              </p>
            ) : (
              recentTx.map((tx) => (
                <motion.div
                  key={tx.id}
                  whileHover={{
                    x: 4,
                  }}
                  transition={{
                    duration: 0.2,
                  }}
                  className="
                    flex
                    flex-col
                    gap-4
                    rounded-2xl
                    border
                    border-white/10
                    bg-white/5
                    p-5
                    backdrop-blur-xl
                    md:flex-row
                    md:items-center
                    md:justify-between
                  "
                >
                  <div>
                    <h3 className="font-medium text-white">
                      {tx.message}
                    </h3>

                    <p className="mt-1 text-xs text-gray-400 font-mono break-all">
                      Tx ID: {tx.transactionId}
                    </p>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="text-xs text-gray-400">
                      {new Date(tx.createdAt).toLocaleTimeString()}
                    </span>
                    <GlassBadge
                      variant={tx.status === "SUCCESS" ? "success" : "danger"}
                    >
                      {tx.status}
                    </GlassBadge>
                  </div>
                </motion.div>
              ))
            )}
          </div>
        </GlassCard>
      </motion.section>
    </motion.div>
  );
}