import { motion } from "framer-motion";
import {
  ArrowDownRight,
  ArrowUpRight,
  Loader2,
} from "lucide-react";
import {
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import { cn } from "../../utils/cn";
import GlassCard from "./GlassCard";

export interface GlassStatCardProps {
  title: string;
  value: number | string;
  icon: ReactNode;
  trend?: "up" | "down" | "neutral";
  percentage?: number;
  loading?: boolean;
  className?: string;
}

export default function GlassStatCard({
  title,
  value,
  icon,
  trend = "neutral",
  percentage,
  loading = false,
  className,
}: GlassStatCardProps) {
  const numericValue = useMemo(() => {
    if (typeof value === "number") {
      return value;
    }

    const parsed = Number(value);

    return Number.isNaN(parsed) ? null : parsed;
  }, [value]);

  const [displayValue, setDisplayValue] = useState(0);

  useEffect(() => {
    if (loading) {
      return;
    }

    if (numericValue === null) {
      return;
    }

    let start = 0;

    const duration = 700;
    const step = Math.max(
      1,
      Math.ceil(numericValue / (duration / 16))
    );

    const timer = window.setInterval(() => {
      start += step;

      if (start >= numericValue) {
        setDisplayValue(numericValue);
        clearInterval(timer);
      } else {
        setDisplayValue(start);
      }
    }, 16);

    return () => clearInterval(timer);
  }, [numericValue, loading]);

  const trendColor =
    trend === "up"
      ? "text-emerald-400"
      : trend === "down"
      ? "text-red-400"
      : "text-blue-400";

  const TrendIcon =
    trend === "up"
      ? ArrowUpRight
      : trend === "down"
      ? ArrowDownRight
      : null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{
        y: -6,
        scale: 1.02,
      }}
      transition={{
        duration: 0.25,
      }}
    >
      <GlassCard
        hover={false}
        className={cn(
          "flex flex-col gap-6",
          className
        )}
      >
        {/* Header */}

        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm text-gray-400">
              {title}
            </p>

            {loading ? (
              <div className="mt-3 flex items-center gap-2">
                <Loader2
                  size={20}
                  className="animate-spin text-blue-400"
                />

                <span className="text-sm text-gray-400">
                  Loading...
                </span>
              </div>
            ) : (
              <h2 className="mt-2 text-4xl font-bold">
                {numericValue !== null
                  ? displayValue.toLocaleString()
                  : value}
              </h2>
            )}
          </div>

          <div
            className="
              flex
              h-14
              w-14
              items-center
              justify-center
              rounded-2xl
              bg-blue-600/15
              text-blue-400
            "
          >
            {icon}
          </div>
        </div>

        {(TrendIcon || percentage !== undefined) && (
          <div
            className={cn(
              "flex items-center gap-2 text-sm font-medium",
              trendColor
            )}
          >
            {TrendIcon && <TrendIcon size={16} />}

            {percentage !== undefined && (
              <span>
                {trend !== "neutral"
                  ? `${percentage}%`
                  : `${percentage}% change`}
              </span>
            )}
          </div>
        )}
      </GlassCard>
    </motion.div>
  );
}