import { useCallback, useState } from "react";

import { productService } from "@/services/productService";

interface UseDeleteProductReturn {
  isDeleting: boolean;

  deleteProduct: (
    productId: string
  ) => Promise<void>;
}

export function useDeleteProduct(): UseDeleteProductReturn {
  const [isDeleting, setIsDeleting] =
    useState(false);

  const deleteProduct = useCallback(
    async (productId: string) => {
      setIsDeleting(true);

      try {
        await productService.deleteProduct(
          productId
        );
      } catch (error) {
        throw error;
      } finally {
        setIsDeleting(false);
      }
    },
    []
  );

  return {
    deleteProduct,
    isDeleting,
  };
}