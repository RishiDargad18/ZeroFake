import { createApiClient } from "@/api/axios";
import type { ApiResponse } from "@/types/api";
import type { ProductResponse } from "@/types/product";

const api = createApiClient(import.meta.env.VITE_PRODUCT_API);

const BASE_URL = "/api/v1/products";

export const productApi = {
  async getAllProducts() {
    const response =
      await api.get<ApiResponse<ProductResponse[]>>(
        BASE_URL
      );

    return response.data;
  },

  async getProductById(id: string) {
    const response =
      await api.get<ApiResponse<ProductResponse>>(
        `${BASE_URL}/${id}`
      );

    return response.data;
  },

  async getProductsByCategory(
    categoryId: string
  ) {
    const response =
      await api.get<ApiResponse<ProductResponse[]>>(
        `${BASE_URL}/category/${categoryId}`
      );

    return response.data;
  },

  async getProductsByManufacturer(
    manufacturerId: string
  ) {
    const response =
      await api.get<ApiResponse<ProductResponse[]>>(
        `${BASE_URL}/manufacturer/${manufacturerId}`
      );

    return response.data;
  },
};