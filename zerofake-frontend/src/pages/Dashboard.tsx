import { useEffect } from "react";

import { motion } from "framer-motion";
import {
  Activity,
  BarChart3,
  PackageSearch,
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
  EmptyState,
  GlassBadge,
  GlassCard,
  GlassLoader,
  GlassStatCard,
} from "@/components/ui";

import {
  DASHBOARD_STATISTICS,
  RECENT_ACTIVITY,
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
  const {
    statistics,
    loading,
    error,
  } = useDashboard();

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

  if (
    statistics &&
    statistics.productsRegistered === 0
  ) {
    return (
      <EmptyState
        title="No Products Found"
        description="Register your first product to begin tracking it on the blockchain."
        icon={<PackageSearch size={56} />}
      />
    );
  }

  const statCards =
    DASHBOARD_STATISTICS.map((item) => {
      if (
        item.title ===
        "Products Registered"
      ) {
        return {
          ...item,
          value:
            statistics?.productsRegistered ??
            item.value,
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
                  data={verificationTrendData}
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
                  data={fraudTrendData}
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
            {RECENT_ACTIVITY.map((activity) => (
              <motion.div
                key={activity.id}
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
                  <h3 className="font-medium">
                    {activity.title}
                  </h3>

                  <p className="mt-1 text-sm text-gray-400">
                    {activity.timestamp}
                  </p>
                </div>

                <GlassBadge
                  variant={activity.status}
                >
                  {activity.status.toUpperCase()}
                </GlassBadge>
              </motion.div>
            ))}
          </div>
        </GlassCard>
      </motion.section>
    </motion.div>
  );
}