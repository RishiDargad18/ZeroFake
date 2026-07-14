import { categoryApi } from "@/api/categoryApi";

import type { CategoryResponse } from "@/types/product";

class CategoryService {
  async getCategories(): Promise<
    CategoryResponse[]
  > {
    const response =
      await categoryApi.getAllCategories();

    return response.data ?? [];
  }
}

export const categoryService =
  new CategoryService();