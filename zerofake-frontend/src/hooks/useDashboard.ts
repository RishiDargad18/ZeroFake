import { useCallback, useEffect, useState } from "react";

import { useAuth } from "@/hooks/useAuth";
import { dashboardService } from "@/services/dashboardService";
import type { DashboardStatistics } from "@/types/dashboard";

interface UseDashboardReturn {
  statistics: DashboardStatistics | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useDashboard(): UseDashboardReturn {
  const { user } = useAuth();
  const [statistics, setStatistics] =
    useState<DashboardStatistics | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError(null);

    try {
      const response =
        await dashboardService.getStatistics(user);

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
  }, [user]);

  useEffect(() => {
    if (user) {
      void refresh();
    }
  }, [refresh, user]);

  return {
    statistics,
    loading,
    error,
    refresh,
  };
}