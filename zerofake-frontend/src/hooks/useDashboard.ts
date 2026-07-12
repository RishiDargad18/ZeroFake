import { useCallback, useEffect, useState } from "react";

import { dashboardService } from "@/services/dashboardService";
import type { DashboardStatistics } from "@/types/dashboard";

interface UseDashboardReturn {
  statistics: DashboardStatistics | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useDashboard(): UseDashboardReturn {
  const [statistics, setStatistics] =
    useState<DashboardStatistics | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response =
        await dashboardService.getStatistics();

      setStatistics(response);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError(
          "Failed to load dashboard statistics."
        );
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return {
    statistics,
    loading,
    error,
    refresh,
  };
}