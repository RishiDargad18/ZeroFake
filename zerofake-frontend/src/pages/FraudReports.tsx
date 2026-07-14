import { useCallback } from "react";
import type { FraudReportResponse } from "@/types/fraud";
import { toast } from "react-hot-toast";
import { RotateCcw } from "lucide-react";
import { ShieldAlert } from "lucide-react";
import { useFraudReports } from "@/hooks/useFraudReports";
import type { GlassTableColumn } from "@/components/ui/GlassTable";
import { getApiError } from "@/utils/apiError";
import { useState } from "react";

import FraudReportDetailsDrawer from "@/components/ui/FraudReportDetailsDrawer";

import {
  EmptyState,
  GlassBadge,
  GlassButton,
  GlassLoader,
  GlassTable,
} from "@/components/ui";

export default function FraudReports() {
  const {
    reports,
    isLoading,
    refresh,
  } = useFraudReports();
const [
  selectedReport,
  setSelectedReport,
] = useState<FraudReportResponse | null>(
  null
);
  const handleRefresh = useCallback(async () => {
    try {
      await refresh();

      toast.success(
        "Fraud reports refreshed."
      );
    } catch (error) {
      toast.error(
        getApiError(error)
      );
    }
  }, [refresh]);

  const openReport = (
  report: FraudReportResponse
) => {
  setSelectedReport(report);
};

const closeReport = () => {
  setSelectedReport(null);
};


const columns: GlassTableColumn<FraudReportResponse>[] = [
  {
    header: "Product ID",
    accessor: "productId",
  },
  {
    header: "Fraud Type",
    accessor: "fraudType",
  },
  {
    header: "Risk Score",
    cell: (report) => (
      <GlassBadge variant="warning">
        {report.riskScore}
      </GlassBadge>
    ),
  },
  {
    header: "Status",
    cell: (report) => (
      <GlassBadge
        variant={
          report.status === "RESOLVED"
            ? "success"
            : report.status ===
              "UNDER_INVESTIGATION"
            ? "warning"
            : "danger"
        }
      >
        {report.status}
      </GlassBadge>
    ),
  },
  {
    header: "Detected At",
    cell: (report) =>
      new Date(
        report.detectedAt
      ).toLocaleString(),
  },
];
  if (isLoading) {
    return (
      <GlassLoader message="Loading fraud reports..." />
    );
  }

  return (
    <div className="space-y-8">
      <div
        className="
          flex
          flex-col
          gap-4
          md:flex-row
          md:items-center
          md:justify-between
        "
      >
        <div>
          <h1 className="text-4xl font-bold">
            Fraud Reports
          </h1>

          <p className="mt-2 text-gray-400">
            Monitor detected counterfeit
            and suspicious activities.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <GlassBadge variant="danger">
            {reports.length} Reports
          </GlassBadge>

          <GlassButton
            variant="secondary"
            onClick={() =>
              void handleRefresh()
            }
          >
            <RotateCcw
              size={18}
              className="mr-2"
            />
            Refresh
          </GlassButton>
        </div>
      </div>

      {reports.length === 0 ? (
        <EmptyState
  title="No Fraud Reports Found"
  description="No suspicious or counterfeit activity has been detected yet."
  icon={
    <ShieldAlert
      size={56}
      className="text-red-500"
    />
  }
/>
      ) : (
        <GlassTable
  columns={columns}
  data={reports}
  rowKey="reportId"
  emptyMessage="No suspicious or counterfeit activity has been detected yet."
  onRowClick={openReport}
/>
      )}

      <FraudReportDetailsDrawer
  report={selectedReport}
  open={selectedReport !== null}
  onClose={closeReport}
/>

    </div>
  );
}