import { motion } from "framer-motion";
import type { ReactNode } from "react";

import { cn } from "@/utils/cn";
import { Database } from "lucide-react";
import EmptyState from "./EmptyState";
import GlassCard from "./GlassCard";
import GlassLoader from "./GlassLoader";

export interface GlassTableColumn<T> {
  header: string;
  accessor?: keyof T | ((row: T) => ReactNode);
  cell?: (row: T) => ReactNode;
  className?: string;
}

interface GlassTableProps<T> {
  columns: GlassTableColumn<T>[];
  data: T[];
  rowKey: keyof T | ((row: T) => React.Key);
  loading?: boolean;
  emptyMessage?: string;
  onRowClick?: (row: T) => void;
  className?: string;
}

export default function GlassTable<T>({
  columns,
  data,
  rowKey,
  loading = false,
  emptyMessage = "No data available.",
  onRowClick,
  className,
}: GlassTableProps<T>) {
  
  if (loading) {
    return (
      <GlassCard className="flex min-h-[300px] items-center justify-center">
        <GlassLoader message="Loading..." />
      </GlassCard>
    );
  }

  if (data.length === 0) {
    return (
      <GlassCard>
        <EmptyState
  title="Nothing Found"
  description={emptyMessage}
  icon={<Database size={56} className="text-blue-500" />}
/>
      </GlassCard>
    );
  }

  return (
    <GlassCard className={cn("overflow-hidden", className)}>
      <div className="overflow-x-auto">
        <table className="min-w-full border-collapse">
          <thead className="sticky top-0 z-10 backdrop-blur-xl">
            <tr className="border-b border-white/10">
              {columns.map((column) => (
                <th
                  key={column.header}
                  className={cn(
                    "bg-white/5 px-6 py-4 text-left text-sm font-semibold text-slate-300",
                    column.className
                  )}
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {data.map((row) => {
              const key =
                typeof rowKey === "function"
                  ? rowKey(row)
                  : (row[rowKey] as React.Key);

              return (
                <motion.tr
                  key={key}
                  whileHover={{
                    scale: 1.005,
                  }}
                  transition={{
                    duration: 0.15,
                  }}
                  onClick={() => onRowClick?.(row)}
                  className={cn(
                    "border-b border-white/5 transition-colors",
                    onRowClick &&
                      "cursor-pointer hover:bg-white/5"
                  )}
                >
                  {columns.map((column) => {
                    let content: ReactNode;

                    if (column.cell) {
                    content = column.cell(row);
                    } else if (
                    typeof column.accessor === "function"
                    ) {
                    content = column.accessor(row);
                    } else if (column.accessor) {
                    content = row[
                        column.accessor
                    ] as ReactNode;
                    } else {
                    content = null;
                    }

                    return (
                      <td
                        key={column.header}
                        className={cn(
                          "px-6 py-4 text-sm text-slate-300",
                          column.className
                        )}
                      >
                        {content}
                      </td>
                    );
                  })}
                </motion.tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </GlassCard>
  );
}