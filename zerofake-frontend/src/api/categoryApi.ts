import { createApiClient } from "@/api/axios";

import type { ApiResponse } from "@/types/api";
import type { CategoryResponse } from "@/types/product";

const api = createApiClient(
  import.meta.env.VITE_PRODUCT_API
);

const BASE_URL = "/api/v1/categories";

export const categoryApi = {
  async getAllCategories() {
    const response =
      await api.get<
        ApiResponse<CategoryResponse[]>
      >(BASE_URL);

    return response.data;
  },
};