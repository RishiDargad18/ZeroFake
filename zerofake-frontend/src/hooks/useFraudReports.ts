import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { fraudReportService } from "@/services/fraudReportService";

import type { FraudReportResponse } from "@/types/fraud";

interface UseFraudReportsReturn {
  reports: FraudReportResponse[];

  isLoading: boolean;

  refresh: () => Promise<void>;
}

export function useFraudReports(): UseFraudReportsReturn {
  const [reports, setReports] =
    useState<FraudReportResponse[]>([]);

  const [isLoading, setIsLoading] =
    useState(true);

  const refresh = useCallback(
    async () => {
      setIsLoading(true);

      try {
        const response =
          await fraudReportService.getFraudReports();

        setReports(response);
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return {
    reports,
    isLoading,
    refresh,
  };
}