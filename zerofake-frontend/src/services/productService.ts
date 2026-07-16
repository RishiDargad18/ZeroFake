import { productApi } from "@/api/productApi";

import type {
  ProductResponse,
  CreateProductRequest,
  UpdateProductRequest,
} from "@/types/product";

class ProductService {
  async getProducts(): Promise<ProductResponse[]> {
    const response =
      await productApi.getAllProducts();

    return response.data ?? [];
  }

  async refreshProducts(): Promise<ProductResponse[]> {
    return this.getProducts();
  }

  async createProduct(
    request: CreateProductRequest
  ): Promise<ProductResponse> {
    const response =
      await productApi.createProduct(request);

    return response.data;
  }
  async updateProduct(
  productId: string,
  request: UpdateProductRequest
): Promise<ProductResponse> {
  const response =
    await productApi.updateProduct(
      productId,
      request
    );

  return response.data;
}
  async deleteProduct(
    productId: string
  ): Promise<void> {
    await productApi.deleteProduct(productId);
  }

  async getProduct(productId: string): Promise<ProductResponse> {
    const response = await productApi.getProductById(productId);
    return response.data;
  }

  async updateBlockchainStatus(productId: string, status: string): Promise<ProductResponse> {
    const response = await productApi.updateBlockchainStatus(productId, status);
    return response.data;
  }
}

export const productService =
  new ProductService();