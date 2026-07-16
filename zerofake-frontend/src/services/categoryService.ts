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

  async createCategory(name: string, description?: string): Promise<CategoryResponse> {
    const response = await categoryApi.createCategory(name, description);
    if (!response.data) {
      throw new Error(response.message || "Failed to create category");
    }
    return response.data;
  }
}

export const categoryService =
  new CategoryService();