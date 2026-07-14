import type {
  OwnerRole,
  TransferOwnershipRequest,
} from "@/types/blockchain";
import {
  useEffect,
  useMemo,
  useState,
} from "react";
import ProductDetailsDrawer from "@/components/ui/ProductDetailsDrawer";
import {
  AnimatePresence,
  motion,
} from "framer-motion";
import {
  Package,
  Plus,
  RefreshCw,
  Search,
  X,
} from "lucide-react";
import { useUpdateProduct } from "@/hooks/useUpdateProduct";
import { toast } from "react-hot-toast";
import { useCategories } from "@/hooks/useCategories";
import ConfirmDialog from "@/components/ui/ConfirmDialog";
import type { SelectOption } from "@/types/common";
import { useDeleteProduct } from "@/hooks/useDeleteProduct";
import ProductForm from "@/components/product/ProductForm";
import type { UpdateProductRequest } from "@/types/product";
import { useCreateProduct } from "@/hooks/useCreateProduct";
import { useBlockchain } from "@/hooks/useBlockchain";
import type { CreateProductRequest } from "@/types/product";
import { useBlockchainHistory } from "@/hooks/useBlockchainHistory";
import { useOwnershipTransfer } from "@/hooks/useOwnershipTransfer";
import {
  EmptyState,
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassLoader,
  GlassSelect,
  GlassTable,
} from "@/components/ui";

import type {
  GlassTableColumn,
} from "@/components/ui/GlassTable";

import { useProducts } from "@/hooks/useProducts";

import { getApiError } from "@/utils/apiError";

import type {
  ProductResponse,
} from "@/types/product";

const containerVariants = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.08,
    },
  },
};

const itemVariants = {
  hidden: {
    opacity: 0,
    y: 20,
  },
  visible: {
    opacity: 1,
    y: 0,
  },
};

export default function Products() {
const {
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
} = useProducts();

const {
  createProduct,
  isSubmitting,
} = useCreateProduct();

const {
  deleteProduct,
  isDeleting,
} = useDeleteProduct();

const {
  updateProduct,
  isUpdating,
} = useUpdateProduct();

const {
  registerProduct,
  isRegistering,
} = useBlockchain();

const {
  transferOwnership,
  isTransferring,
} = useOwnershipTransfer();

const {
  history,
  isLoading: historyLoading,
  loadHistory,
} = useBlockchainHistory();

const [showEditModal, setShowEditModal] =
  useState(false);

const [productToEdit, setProductToEdit] =
  useState<ProductResponse | null>(null);
const openDeleteDialog = (
  product: ProductResponse
) => {
  closeProduct();
  setProductToDelete(product);
  setShowDeleteDialog(true);
};

const closeDeleteDialog = () => {
  setProductToDelete(null);
  setShowDeleteDialog(false);
};
const openEditModal = (
  product: ProductResponse
) => {
  closeProduct();
  setProductToEdit(product);
  setShowEditModal(true);
};

const closeEditModal = () => {
  setShowEditModal(false);
  setProductToEdit(null);
};

const openTransferModal = (
  product: ProductResponse
) => {
  closeProduct();
  setProductToTransfer(product);
  setShowTransferModal(true);
};

const closeTransferModal = () => {
  setShowTransferModal(false);
  setProductToTransfer(null);

  setNewOwnerId("");
  setNewOwnerRole("");
};

const handleDeleteProduct = async () => {
  if (!productToDelete) {
    return;
  }

  try {
    await deleteProduct(productToDelete.id);

    toast.success(
      "Product deleted successfully."
    );

    await refresh();

    closeDeleteDialog();
  } catch (error) {
    toast.error(getApiError(error));
  }
};
const handleUpdateProduct = async (
  data: CreateProductRequest
) => {
  if (!productToEdit) {
    return;
  }
  const request: UpdateProductRequest = {
    ...data,
    imageUrl: productToEdit.imageUrl,
    imageFileName: productToEdit.imageFileName,
    imageContentType:
      productToEdit.imageContentType,
    imageSize: productToEdit.imageSize,
  };

  try {
    await updateProduct(
      productToEdit.id,
      request
    );

    toast.success(
      "Product updated successfully."
    );

    await refresh();

    closeEditModal();
  } catch (error) {
    toast.error(getApiError(error));
  }
};
const handleRegisterBlockchain = async (
  product: ProductResponse
) => {
  try {
    await registerProduct({
      productId: product.id,
      manufacturerId:
        product.manufacturerId,
    });

    toast.success(
      "Product registered on blockchain successfully."
    );

    await refresh();

    closeProduct();
  } catch (error) {
    toast.error(getApiError(error));
  }
};
const handleTransferOwnership =
  async () => {
    if (!productToTransfer) {
      return;
    }
    if (
  newOwnerId.trim() === "" ||
  newOwnerRole === ""
) {
  toast.error(
    "Please complete all required fields."
  );
  return;
}

    const request: TransferOwnershipRequest =
      {
        productId:
          productToTransfer.id,

        fromOwnerId:
  history?.history.length
    ? history.history[history.history.length - 1].currentOwnerId
    : productToTransfer.manufacturerId,

        toOwnerId: newOwnerId,

        toOwnerRole:
          newOwnerRole,
      };

    try {
      await transferOwnership(request);

      toast.success(
        "Ownership transferred successfully."
      );

      await refresh();

      if (selectedProduct) {
        await loadHistory(
          selectedProduct.id
        );
      }

      closeTransferModal();
    } catch (error) {
      toast.error(
        getApiError(error)
      );
    }
  };
const {
  categories,
  isLoading: categoriesLoading,
} = useCategories();

const [
  showDeleteDialog,
  setShowDeleteDialog,
] = useState(false);

const [
  productToDelete,
  setProductToDelete,
] = useState<ProductResponse | null>(
  null
);

const [showCreateModal, setShowCreateModal] =
  useState(false);

const openCreateModal = () => {
  setShowCreateModal(true);
};

const closeCreateModal = () => {
  setShowCreateModal(false);
};
const [newOwnerId, setNewOwnerId] =
  useState("");

const [newOwnerRole, setNewOwnerRole] =
  useState<OwnerRole | "">("");

  const [showTransferModal, setShowTransferModal] =
  useState(false);

const [productToTransfer, setProductToTransfer] =
  useState<ProductResponse | null>(null);
useEffect(() => {
  if (error) {
    toast.error(getApiError(error));
  }
}, [error]);
useEffect(() => {
  if (!selectedProduct) {
    return;
  }

  void loadHistory(selectedProduct.id);
}, [selectedProduct, loadHistory]);

useEffect(() => {
  if (!categoriesLoading && categories.length === 0) {
    toast.error(
      "Unable to load product categories."
    );
  }
}, [categories, categoriesLoading]);

useEffect(() => {
  if (!showCreateModal) {
    return;
  }
  const handleEscape = (event: KeyboardEvent) => {
    if (event.key === "Escape") {
      closeCreateModal();
    }
  };

  window.addEventListener(
    "keydown",
    handleEscape
  );

  return () =>
    window.removeEventListener(
      "keydown",
      handleEscape
    );
}, [showCreateModal]);

useEffect(() => {
  if (!showEditModal) {
    return;
  }

  const handleEscape = (event: KeyboardEvent) => {
    if (event.key === "Escape") {
      closeEditModal();
    }
  };
  
  window.addEventListener("keydown", handleEscape);

  return () => {
    window.removeEventListener("keydown", handleEscape);
  };
}, [showEditModal]);

const handleCreateProduct = async (
  data: CreateProductRequest
) => {
  try {
    await createProduct(data);

    toast.success(
      "Product created successfully."
    );

    await refresh();

    closeCreateModal();
  } catch (error) {
    toast.error(getApiError(error));
  }
};
  const ownerRoleOptions = useMemo(
  () => [
    {
      label: "Manufacturer",
      value: "MANUFACTURER",
    },
    {
      label: "Warehouse",
      value: "WAREHOUSE",
    },
    {
      label: "Distributor",
      value: "DISTRIBUTOR",
    },
    {
      label: "Retailer",
      value: "RETAILER",
    },
    {
      label: "Customer",
      value: "CUSTOMER",
    },
  ],
  []
);
  const columns = useMemo<
    GlassTableColumn<ProductResponse>[]
  >(
    () => [
              {
        header: "Product Code",
        accessor: "productCode",
      },

      {
        header: "Product Name",
        accessor: "productName",
      },

      {
        header: "Brand",
        accessor: "brand",
      },

      {
        header: "Category",
        accessor: (product) =>
          product.category.name,
      },

      {
        header: "Blockchain Status",
        cell: (product) => (
            <GlassBadge
            variant={
                product.blockchainStatus === "SUCCESS"
                ? "success"
                : product.blockchainStatus === "FAILED"
                ? "danger"
                : "warning"
            }
            >
            {product.blockchainStatus}
            </GlassBadge>
        ),
        },

      {
        header: "Active",
        cell: (product) => (
            <GlassBadge
            variant={product.active ? "success" : "danger"}
            >
            {product.active ? "ACTIVE" : "INACTIVE"}
            </GlassBadge>
        ),
        },
    ],
    []
  );
  const categoryOptions = useMemo<SelectOption[]>(
  () =>
    categories.map((category) => ({
      label: category.name,
      value: category.id,
    })),
  [categories]
);
const editInitialValues =
  useMemo<UpdateProductRequest | undefined>(
    () => {
      if (!productToEdit) {
        return undefined;
      }

      return {
        productCode:
          productToEdit.productCode,
        productName:
          productToEdit.productName,
        description:
          productToEdit.description,
        brand: productToEdit.brand,
        manufacturerId:
          productToEdit.manufacturerId,
        categoryId:
          productToEdit.category.id,
        imageUrl:
          productToEdit.imageUrl,
        imageFileName:
          productToEdit.imageFileName,
        imageContentType:
          productToEdit.imageContentType,
        imageSize:
          productToEdit.imageSize,
      };
    },
    [productToEdit]
  );

  if (isLoading) {
    return (
      <GlassLoader message="Loading Products..." />
    );
  }

  if (products.length === 0) {
    return (
      <EmptyState
        title="No Products Found"
        description="Register your first product to begin using ZeroFake."
        icon={<Package size={60} />}
      />
    );
  }

  return (
    <motion.div
      className="space-y-8"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >      <motion.section variants={itemVariants}>
        <div className="space-y-2">
          <h1 className="text-4xl font-bold">
            Products
          </h1>

          <p className="text-gray-400">
            Manage registered products across the
            ZeroFake platform.
          </p>
        </div>
      </motion.section>

      <motion.section variants={itemVariants}>
        <GlassCard>
          <div
            className="
              flex
              flex-col
              gap-4
              lg:flex-row
              lg:items-center
              lg:justify-between
            "
          >
            <div className="w-full lg:max-w-md">
              <GlassInput
                placeholder="Search by product name, code or brand..."
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                prefixIcon={<Search size={18} />}
              />
            </div>

            <div
  className="
    flex
    flex-wrap
    items-center
    gap-3
  "
>
  <GlassButton
    icon={<Plus size={18} />}
    onClick={openCreateModal}
  >
    New Product
  </GlassButton>
              <GlassButton
                variant="secondary"
                onClick={() => void refresh()}
                icon={<RefreshCw size={18} />}
              >
                Refresh
              </GlassButton>

              <GlassBadge variant="info">
                {filteredProducts.length} Product
                {filteredProducts.length !== 1
                  ? "s"
                  : ""}
              </GlassBadge>
            </div>
          </div>
        </GlassCard>
      </motion.section>

      <motion.section variants={itemVariants}>
        <GlassTable<ProductResponse>
        columns={columns}
        data={filteredProducts}
        rowKey="id"
        loading={isLoading}
        emptyMessage="No products match your search."
        onRowClick={(product) => {
          openProduct(product);
        }}
        />
      </motion.section>
      <AnimatePresence>
  {showEditModal && (
    <motion.div
      className="
        fixed
        inset-0
        z-50
        flex
        items-center
        justify-center
        bg-black/60
        p-4
        backdrop-blur-md
      "
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      onClick={closeEditModal}
    >
      <motion.div
        initial={{
          scale: 0.95,
          opacity: 0,
        }}
        animate={{
          scale: 1,
          opacity: 1,
        }}
        exit={{
          scale: 0.95,
          opacity: 0,
        }}
        transition={{
          duration: 0.2,
        }}
        onClick={(event) =>
          event.stopPropagation()
        }
        className="w-full max-w-3xl"
      >
        <GlassCard>
          <div className="mb-6">
            <h2 className="text-3xl font-bold">
              Edit Product
            </h2>

            <p className="mt-1 text-gray-400">
              Update product information
            </p>
          </div>

          <ProductForm
            initialValues={
              editInitialValues
            }
            onSubmit={
              handleUpdateProduct
            }
            loading={isUpdating}
            categories={categoryOptions}
          />
        </GlassCard>
      </motion.div>
    </motion.div>
  )}
</AnimatePresence>
<ProductDetailsDrawer
  product={selectedProduct}
  open={selectedProduct !== null}
  onClose={closeProduct}
  onEdit={openEditModal}
  onDelete={openDeleteDialog}
  onRegisterBlockchain={handleRegisterBlockchain}
  onTransferOwnership={openTransferModal}
  isRegistering={isRegistering}
  isTransferring={isTransferring}
  history={
    selectedProduct
      ? history
      : null
  }
  historyLoading={historyLoading}
/>
        <ConfirmDialog
  open={showDeleteDialog}
  title="Delete Product"
  description={`Are you sure you want to delete "${productToDelete?.productName}"?`}
  confirmText="Delete"
  cancelText="Cancel"
  loading={isDeleting}
  onConfirm={() => {
  void handleDeleteProduct();
}}
  onCancel={closeDeleteDialog}
/>
<AnimatePresence>
  {showTransferModal &&
    productToTransfer && (
      <motion.div
        className="
          fixed
          inset-0
          z-50
          flex
          items-center
          justify-center
          bg-black/60
          p-4
          backdrop-blur-md
        "
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={closeTransferModal}
      >
        <motion.div
          className="w-full max-w-lg"
          initial={{
            scale: 0.95,
            opacity: 0,
          }}
          animate={{
            scale: 1,
            opacity: 1,
          }}
          exit={{
            scale: 0.95,
            opacity: 0,
          }}
          transition={{
            duration: 0.2,
          }}
          onClick={(event) =>
            event.stopPropagation()
          }
        >
          <GlassCard>
            <div className="space-y-6">
              <div>
                <h2 className="text-2xl font-bold">
                  Transfer Ownership
                </h2>

                <p className="mt-1 text-sm text-gray-400">
                  Transfer this product to the
                  next owner
                </p>
              </div>

              <GlassInput
                label="New Owner ID"
                placeholder="Enter new owner ID"
                value={newOwnerId}
                onChange={(event) =>
                  setNewOwnerId(
                    event.target.value
                  )
                }
                disabled={isTransferring}
              />

              <GlassSelect
                label="Owner Role"
                options={ownerRoleOptions}
                value={newOwnerRole}
                onChange={(event) =>
                  setNewOwnerRole(
                    event.target
                      .value as OwnerRole
                  )
                }
                disabled={isTransferring}
              />

              <div className="flex justify-end gap-3">
                <GlassButton
                  variant="secondary"
                  onClick={
                    closeTransferModal
                  }
                  disabled={isTransferring}
                >
                  Cancel
                </GlassButton>

                <GlassButton
                  loading={isTransferring}
                  disabled={
  isTransferring ||
  newOwnerId.trim() === "" ||
  newOwnerRole === ""
}
                  onClick={() => {
                    void handleTransferOwnership();
                  }}
                >
                  Transfer
                </GlassButton>
              </div>
            </div>
          </GlassCard>
        </motion.div>
      </motion.div>
    )}
</AnimatePresence>
        <AnimatePresence>
  {showCreateModal && (
    <>
      <motion.div
        className="
          fixed
          inset-0
          z-40
          bg-black/50
          backdrop-blur-sm
        "
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={closeCreateModal}
      />

      <motion.div
        className="
          fixed
          inset-0
          z-50
          flex
          items-center
          justify-center
          p-4
        "
        initial={{
          opacity: 0,
          scale: 0.95,
        }}
        animate={{
          opacity: 1,
          scale: 1,
        }}
        exit={{
          opacity: 0,
          scale: 0.95,
        }}
      >
        <div
  onClick={(event: React.MouseEvent<HTMLDivElement>) =>
    event.stopPropagation()
  }
>
  <GlassCard
    className="
      w-full
      max-w-2xl
      max-h-[90vh]
      overflow-y-auto
    "
  >
          {/* Header */}

          <div
            className="
              mb-6
              flex
              items-start
              justify-between
              border-b
              border-white/10
              pb-4
            "
          >
            <div>
              <h2 className="text-2xl font-bold">
                Create Product
              </h2>

              <p className="mt-1 text-sm text-gray-400">
                Register a new product
              </p>
            </div>

            <GlassButton
              variant="outline"
              onClick={closeCreateModal}
              className="rounded-xl p-2"
            >
              <X size={18} />
            </GlassButton>
          </div>

          {/* Form */}

          <ProductForm
            onSubmit={handleCreateProduct}
            loading={isSubmitting}
            categories={categoryOptions}
          />
        </GlassCard>
</div>
      </motion.div>
    </>
  )}
</AnimatePresence>
    </motion.div>
  );
}
