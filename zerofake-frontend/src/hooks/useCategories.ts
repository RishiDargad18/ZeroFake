import {
  useCallback,
  useEffect,
  useState,
} from "react";

import { categoryService } from "@/services/categoryService";

import type { CategoryResponse } from "@/types/product";

interface UseCategoriesReturn {
  categories: CategoryResponse[];
  isLoading: boolean;
  refresh: () => Promise<void>;
}

export function useCategories(): UseCategoriesReturn {
  const [categories, setCategories] =
    useState<CategoryResponse[]>([]);

  const [isLoading, setIsLoading] =
    useState(false);

  const refresh = useCallback(async () => {
    setIsLoading(true);

    try {
      const response =
        await categoryService.getCategories();

      setCategories(response);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return {
    categories,
    isLoading,
    refresh,
  };
}