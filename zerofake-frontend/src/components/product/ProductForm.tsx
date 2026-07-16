import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useAuth } from "@/hooks/useAuth";
import { categoryService } from "@/services/categoryService";
import { toast } from "react-hot-toast";

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
  const { user } = useAuth();
  const [isCustomCategory, setIsCustomCategory] = useState(false);
  const [isSubmittingCategory, setIsSubmittingCategory] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<CreateProductRequest & { customCategoryName: string }>({
    defaultValues: {
      productCode: initialValues?.productCode || "",
      productName: initialValues?.productName || "",
      description: initialValues?.description || "",
      brand: initialValues?.brand || "",
      categoryId: initialValues?.categoryId || "",
      manufacturerId: initialValues?.manufacturerId || user?.id || "",
      customCategoryName: "",
    },
  });

  useEffect(() => {
    if (user?.id && !initialValues?.manufacturerId) {
      setValue("manufacturerId", user.id);
    }
  }, [user, initialValues, setValue]);

  const onFormSubmit = async (data: any) => {
    let finalCategoryId = data.categoryId;

    if (isCustomCategory && data.customCategoryName) {
      setIsSubmittingCategory(true);
      try {
        const currentCats = await categoryService.getCategories();
        const existingCat = currentCats.find(
          (c) => c.name.toLowerCase() === data.customCategoryName.trim().toLowerCase()
        );

        if (existingCat) {
          finalCategoryId = existingCat.id;
        } else {
          const newCat = await categoryService.createCategory(
            data.customCategoryName.trim(),
            "Created dynamically during product registration."
          );
          finalCategoryId = newCat.id;
        }
      } catch (err) {
        toast.error("Failed to resolve or create category.");
        setIsSubmittingCategory(false);
        return;
      } finally {
        setIsSubmittingCategory(false);
      }
    }

    const requestPayload: CreateProductRequest = {
      productCode: data.productCode,
      productName: data.productName,
      description: data.description,
      brand: data.brand,
      categoryId: finalCategoryId,
      manufacturerId: data.manufacturerId,
    };

    await onSubmit(requestPayload);
  };

  return (
    <form
      onSubmit={handleSubmit(onFormSubmit)}
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

      {isCustomCategory ? (
        <div className="space-y-1">
          <GlassInput
            label="New Category Name"
            placeholder="e.g. Luxury Goods, Cosmetics, Apparel"
            disabled={loading || isSubmittingCategory}
            error={errors.customCategoryName?.message}
            {...register("customCategoryName", {
              required: "New category name is required",
            })}
          />
          <button
            type="button"
            onClick={() => {
              setIsCustomCategory(false);
              setValue("customCategoryName", "");
            }}
            className="text-xs text-blue-400 hover:text-blue-300 transition"
          >
            ← Select from existing categories
          </button>
        </div>
      ) : (
        <div className="space-y-1">
          <GlassSelect
            label="Category"
            placeholder="Select a category"
            options={categories}
            disabled={loading}
            error={errors.categoryId?.message}
            {...register("categoryId", {
              required: !isCustomCategory ? "Category is required" : false,
            })}
          />
          <button
            type="button"
            onClick={() => {
              setIsCustomCategory(true);
              setValue("categoryId", "");
            }}
            className="text-xs text-blue-400 hover:text-blue-300 transition"
          >
            + Category doesn't exist? Create a new one
          </button>
        </div>
      )}

      <GlassInput
        label="Manufacturer"
        value={user ? `${user.firstName} ${user.lastName} (${user.email})` : "Loading manufacturer details..."}
        disabled={true}
        readOnly={true}
        helperText="Automatically detected from your active logged-in manufacturer account session."
      />

      <input
        type="hidden"
        {...register("manufacturerId", {
          required: "Manufacturer ID is missing",
        })}
      />

      <div className="flex justify-end pt-2">
        <GlassButton
          type="submit"
          loading={loading || isSubmittingCategory}
          disabled={loading || isSubmittingCategory}
        >
          Create Product
        </GlassButton>
      </div>
    </form>
  );
}