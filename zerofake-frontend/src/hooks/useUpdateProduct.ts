import { useCallback, useState } from "react";

import { productService } from "@/services/productService";

import type {
  ProductResponse,
  UpdateProductRequest,
} from "@/types/product";

interface UseUpdateProductReturn {
  isUpdating: boolean;

  updateProduct: (
    productId: string,
    request: UpdateProductRequest
  ) => Promise<ProductResponse>;
}

export function useUpdateProduct(): UseUpdateProductReturn {
  const [isUpdating, setIsUpdating] =
    useState(false);

  const updateProduct = useCallback(
    async (
      productId: string,
      request: UpdateProductRequest
    ) => {
      setIsUpdating(true);

      try {
        return await productService.updateProduct(
          productId,
          request
        );
      } catch (error) {
        throw error;
      } finally {
        setIsUpdating(false);
      }
    },
    []
  );

  return {
    updateProduct,
    isUpdating,
  };
}