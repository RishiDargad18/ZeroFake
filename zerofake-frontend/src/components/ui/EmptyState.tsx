import { motion } from "framer-motion";
import type { ReactNode } from "react";

import { cn } from "../../utils/cn";
import GlassButton from "./GlassButton";
import GlassCard from "./GlassCard";

export interface EmptyStateProps {
  title: string;
  description: string;
  icon: ReactNode;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
}

export default function EmptyState({
  title,
  description,
  icon,
  actionLabel,
  onAction,
  className,
}: EmptyStateProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className={cn(
        "flex w-full items-center justify-center py-12",
        className
      )}
    >
      <GlassCard
        className="
          w-full
          max-w-xl
          text-center
        "
      >
        <div className="flex flex-col items-center gap-6">
          <div
            className="
              flex
              h-24
              w-24
              items-center
              justify-center
              rounded-full
              bg-blue-500/10
              text-blue-400
            "
          >
            <div className="text-5xl">
              {icon}
            </div>
          </div>

          <div className="space-y-2">
            <h2 className="text-2xl font-bold">
              {title}
            </h2>

            <p className="text-gray-400">
              {description}
            </p>
          </div>

          {actionLabel && onAction && (
            <GlassButton
              variant="primary"
              onClick={onAction}
            >
              {actionLabel}
            </GlassButton>
          )}
        </div>
      </GlassCard>
    </motion.div>
  );
}