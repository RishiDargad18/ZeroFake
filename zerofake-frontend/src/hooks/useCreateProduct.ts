import { useCallback, useState } from "react";

import { productService } from "@/services/productService";

import type {
  CreateProductRequest,
  ProductResponse,
} from "@/types/product";

interface UseCreateProductReturn {
  isSubmitting: boolean;

  createProduct: (
    data: CreateProductRequest
  ) => Promise<ProductResponse>;
}

export function useCreateProduct(): UseCreateProductReturn {
  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const createProduct = useCallback(
    async (
      data: CreateProductRequest
    ): Promise<ProductResponse> => {
      setIsSubmitting(true);

      try {
        const createdProduct =
          await productService.createProduct(data);

        return createdProduct;
      } catch (error) {
        throw error;
      } finally {
        setIsSubmitting(false);
      }
    },
    []
  );

  return {
    createProduct,
    isSubmitting,
  };
}