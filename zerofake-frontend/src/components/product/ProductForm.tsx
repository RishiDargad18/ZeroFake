import { useForm } from "react-hook-form";

import {
  GlassButton,
  GlassInput,
  GlassSelect,
  GlassTextarea,
} from "@/components/ui";

import type {
  CreateProductRequest,
  UpdateProductRequest,
} from "@/types/product";

import type {
  SelectOption,
} from "@/types/common";

interface ProductFormProps {
  onSubmit: (
    data: CreateProductRequest
  ) => Promise<void>;

  loading: boolean;

  categories: SelectOption[];

  initialValues?: Partial<UpdateProductRequest>;
}

export default function ProductForm({
  onSubmit,
  loading,
  categories,
  initialValues,
}: ProductFormProps) {
  const {
  register,
  handleSubmit,
  reset,
  formState: { errors },
} = useForm<CreateProductRequest>({
    defaultValues: {
      productCode: "",
      productName: "",
      description: "",
      brand: "",
      categoryId: "",
      manufacturerId: "",
    },
  });

  return (
    <form
  onSubmit={handleSubmit(onSubmit)}
>
      <GlassInput
        label="Product Code"
        placeholder="Enter product code"
        disabled={loading}
        error={errors.productCode?.message}
        {...register("productCode", {
          required: "Product code is required",
        })}
      />

      <GlassInput
        label="Product Name"
        placeholder="Enter product name"
        disabled={loading}
        error={errors.productName?.message}
        {...register("productName", {
          required: "Product name is required",
        })}
      />

      <GlassTextarea
        label="Description"
        placeholder="Enter product description"
        disabled={loading}
        error={errors.description?.message}
        {...register("description", {
          required: "Description is required",
        })}
      />
            <GlassInput
        label="Brand"
        placeholder="Enter brand name"
        disabled={loading}
        error={errors.brand?.message}
        {...register("brand", {
          required: "Brand is required",
        })}
      />

      <GlassSelect
        label="Category"
        placeholder="Select a category"
        options={categories}
        disabled={loading}
        error={errors.categoryId?.message}
        {...register("categoryId", {
          required: "Category is required",
        })}
      />

      <GlassInput
        label="Manufacturer"
        placeholder="Enter manufacturer ID"
        disabled={loading}
        error={errors.manufacturerId?.message}
        helperText="This will be replaced by the authenticated manufacturer in a later milestone."
        {...register("manufacturerId", {
          required: "Manufacturer is required",
        })}
      />

      <div className="flex justify-end pt-2">
        <GlassButton
          type="submit"
          loading={loading}
          disabled={loading}
        >
          Create Product
        </GlassButton>
      </div>
    </form>
  );
}