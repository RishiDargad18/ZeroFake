import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import { productService } from "@/services/productService";
import type { ProductResponse } from "@/types/product";

interface UseProductsReturn {
  products: ProductResponse[];
  filteredProducts: ProductResponse[];
  search: string;
  setSearch: React.Dispatch<React.SetStateAction<string>>;
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  selectedProduct: ProductResponse | null;
  openProduct: (product: ProductResponse) => void;
  closeProduct: () => void;
}

export function useProducts(): UseProductsReturn {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [search, setSearch] = useState("");

  const [isLoading, setIsLoading] = useState(true);

  const [error, setError] = useState<string | null>(
    null
  );
  const [selectedProduct, setSelectedProduct] =
  useState<ProductResponse | null>(null);

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data =
        await productService.getProducts();

      setProducts(data);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError(
          "Unable to load products."
        );
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);
  const openProduct = (
  product: ProductResponse
) => {
  setSelectedProduct(product);
};


const closeProduct = () => {
  setSelectedProduct(null);
};
  const filteredProducts = useMemo(() => {
    const keyword = search.trim().toLowerCase();

    if (!keyword) {
      return products;
    }

    return products.filter((product) => {
      return (
        product.productName
          .toLowerCase()
          .includes(keyword) ||
        product.productCode
          .toLowerCase()
          .includes(keyword) ||
        product.brand
          .toLowerCase()
          .includes(keyword)
      );
    });
  }, [products, search]);

  return {
  products,
  filteredProducts,
  search,
  setSearch,
  isLoading,
  error,
  refresh,
  selectedProduct,
  openProduct,
  closeProduct,
};
}