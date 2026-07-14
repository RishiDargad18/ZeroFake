import { useEffect } from "react";

import { AnimatePresence, motion } from "framer-motion";
import {
  Calendar,
  ShieldAlert,
  X,
} from "lucide-react";

import type { FraudReportResponse } from "@/types/fraud";

import {
  GlassBadge,
  GlassButton,
  GlassCard,
} from "@/components/ui";

interface FraudReportDetailsDrawerProps {
  report: FraudReportResponse | null;
  open: boolean;
  onClose: () => void;
}

function DetailRow({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <div
      className="
        flex
        items-start
        justify-between
        gap-6
        border-b
        border-white/10
        py-4
      "
    >
      <span
        className="
          text-sm
          font-medium
          text-gray-400
        "
      >
        {label}
      </span>

      <div className="max-w-[60%] text-right break-all">
        {value}
      </div>
    </div>
  );
}

export default function FraudReportDetailsDrawer({
  report,
  open,
  onClose,
}: FraudReportDetailsDrawerProps) {
  useEffect(() => {
    if (!open) {
      return;
    }

    const handleEscape = (
      event: KeyboardEvent
    ) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener(
      "keydown",
      handleEscape
    );

    return () =>
      window.removeEventListener(
        "keydown",
        handleEscape
      );
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open && report && (
        <>
          <motion.div
            className="
              fixed
              inset-0
              z-40
              bg-black/40
              backdrop-blur-sm
            "
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />

          <motion.aside
            className="
              fixed
              right-0
              top-0
              z-50
              flex
              h-screen
              w-full
              flex-col
              border-l
              border-white/10
              bg-slate-900/90
              backdrop-blur-2xl
              md:w-[460px]
            "
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ duration: 0.25 }}
          >
            <div
              className="
                flex
                items-center
                justify-between
                border-b
                border-white/10
                p-6
              "
            >
              <div>
                <h2 className="text-2xl font-bold">
                  Fraud Report
                </h2>

                <p className="mt-1 text-sm text-gray-400">
                  Report Details
                </p>
              </div>

              <GlassButton
                variant="outline"
                className="rounded-xl p-2"
                onClick={onClose}
              >
                <X size={18} />
              </GlassButton>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              <GlassCard>
                <DetailRow
                  label="Report ID"
                  value={report.reportId}
                />

                <DetailRow
                  label="Product ID"
                  value={report.productId}
                />

                <DetailRow
                  label="Fraud Type"
                  value={
                    <GlassBadge variant="danger">
                      {report.fraudType}
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Risk Score"
                  value={
                    <GlassBadge variant="warning">
                      {report.riskScore}
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Status"
                  value={
                    <GlassBadge
                      variant={
                        report.status ===
                        "RESOLVED"
                          ? "success"
                          : report.status ===
                            "UNDER_INVESTIGATION"
                          ? "warning"
                          : "danger"
                      }
                    >
                      {report.status}
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Verification Result"
                  value={
                    <GlassBadge>
                      N/A
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Triggered Rules"
                  value={
                    <div className="text-sm text-gray-300">
                      N/A
                    </div>
                  }
                />

                <DetailRow
                  label="Message"
                  value={
                    <div className="text-sm text-gray-300">
                      N/A
                    </div>
                  }
                />

                <DetailRow
                  label="Created Date"
                  value={
                    <div className="flex items-center justify-end gap-2">
                      <Calendar size={16} />
                      {new Date(
                        report.detectedAt
                      ).toLocaleString()}
                    </div>
                  }
                />
              </GlassCard>

              <GlassCard className="mt-6">
                <div
                  className="
                    flex
                    h-48
                    flex-col
                    items-center
                    justify-center
                    text-center
                  "
                >
                  <ShieldAlert
                    size={52}
                    className="mb-4 text-red-400"
                  />

                  <h3 className="text-lg font-semibold">
                    Fraud Analysis
                  </h3>

                  <p className="mt-2 text-sm text-gray-400">
                    Additional fraud analysis
                    will be available in a
                    future milestone.
                  </p>
                </div>
              </GlassCard>
            </div>

            <div
              className="
                border-t
                border-white/10
                p-6
                flex
                justify-end
              "
            >
              <GlassButton
                variant="secondary"
                onClick={onClose}
              >
                Close
              </GlassButton>
            </div>
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
}