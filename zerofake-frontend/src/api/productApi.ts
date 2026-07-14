import { createApiClient } from "@/api/axios";
import type { ApiResponse } from "@/types/api";
import type {
  CreateProductRequest,
  ProductResponse,
  UpdateProductRequest,
} from "@/types/product";
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
    async deleteProduct(productId: string) {
    const response =
      await api.delete<ApiResponse<void>>(
        `${BASE_URL}/${productId}`
      );

    return response.data;
  },
  
    async createProduct(
    request: CreateProductRequest
  ) {
    const response =
      await api.post<ApiResponse<ProductResponse>>(
        BASE_URL,
        request
      );

    return response.data;
  },
  async updateProduct(
  productId: string,
  request: UpdateProductRequest
) {
  const response =
    await api.put<ApiResponse<ProductResponse>>(
      `${BASE_URL}/${productId}`,
      request
    );

  return response.data;
},
};