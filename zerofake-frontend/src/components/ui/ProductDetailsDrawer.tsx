import { useEffect } from "react";
import type {
  ProductHistoryResponse,
} from "@/types/blockchain";
import { AnimatePresence, motion } from "framer-motion";
import {
  Calendar,
  Package,
  Tag,
  User,
  Warehouse,
  X,
} from "lucide-react";

import {
  EmptyState,
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassLoader,
} from "@/components/ui";

import type { ProductResponse } from "@/types/product";

interface ProductDetailsDrawerProps {
  product: ProductResponse | null;
  open: boolean;
  onClose: () => void;

  onEdit: (product: ProductResponse) => void;

  onDelete: (product: ProductResponse) => void;

  onRegisterBlockchain: (
    product: ProductResponse
  ) => void;

  onTransferOwnership: (
    product: ProductResponse
  ) => void;

  isRegistering?: boolean;

  isTransferring?: boolean;

  history: ProductHistoryResponse | null;

  historyLoading: boolean;
}
function DetailRow({
  label,
  value,
}: {
  label: string;
  value: React.ReactNode;
}) {
  return (
    <div
      className="
        flex
        items-start
        justify-between
        gap-6
        border-b
        border-white/10
        py-4
      "
    >
      <span
        className="
          text-sm
          font-medium
          text-gray-400
        "
      >
        {label}
      </span>

      <div className="text-right">
        {value}
      </div>
    </div>
  );
}

export default function ProductDetailsDrawer({
  product,
  open,
  onClose,
  onEdit,
  onDelete,
  onRegisterBlockchain,
  onTransferOwnership,
  isRegistering = false,
  isTransferring = false,
  history,
  historyLoading,
}: ProductDetailsDrawerProps) {
  useEffect(() => {
    const handleEscape = (
      event: KeyboardEvent
    ) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    if (open) {
      window.addEventListener(
        "keydown",
        handleEscape
      );
    }

    return () =>
      window.removeEventListener(
        "keydown",
        handleEscape
      );
  }, [open, onClose]);

  return (
    <AnimatePresence>
      {open && product && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="
              fixed
              inset-0
              z-40
              bg-black/40
              backdrop-blur-sm
            "
          />

          <motion.aside
            initial={{
              x: "100%",
            }}
            animate={{
              x: 0,
            }}
            exit={{
              x: "100%",
            }}
            transition={{
              duration: 0.25,
            }}
            className="
              fixed
              right-0
              top-0
              z-50
              flex
              h-screen
              w-full
              flex-col
              border-l
              border-white/10
              bg-slate-900/90
              backdrop-blur-2xl
              md:w-[450px]
            "
          >            {/* Header */}

            <div
              className="
                flex
                items-center
                justify-between
                border-b
                border-white/10
                p-6
              "
            >
              <div>
                <h2 className="text-2xl font-bold">
                  Product Details
                </h2>

                <p className="mt-1 text-sm text-gray-400">
                  {product.productCode}
                </p>
              </div>

              <GlassButton
                variant="outline"
                onClick={onClose}
                className="rounded-xl p-2"
              >
                <X size={18} />
              </GlassButton>
            </div>

            {/* Body */}

            <div className="flex-1 overflow-y-auto p-6">
              <GlassCard>
                <DetailRow
                  label="Product Code"
                  value={
                    <div className="flex items-center gap-2">
                      <Package size={16} />
                      <span>{product.productCode}</span>
                    </div>
                  }
                />

                <DetailRow
                  label="Product Name"
                  value={product.productName}
                />

                <DetailRow
                  label="Brand"
                  value={
                    <div className="flex items-center gap-2">
                      <Tag size={16} />
                      <span>{product.brand}</span>
                    </div>
                  }
                />

                <DetailRow
                  label="Category"
                  value={product.category.name}
                />

                <DetailRow
                  label="Manufacturer"
                  value={
                    <div className="flex items-center gap-2">
                      <User size={16} />
                      <span>
                        {product.manufacturerId}
                      </span>
                    </div>
                  }
                />

                <DetailRow
                  label="Batch"
                  value={
                    <div className="flex items-center gap-2">
                      <Warehouse size={16} />
                      <span>
                        {"N/A"}
                      </span>
                    </div>
                  }
                />

                <DetailRow
                  label="Blockchain Status"
                  value={
                    <GlassBadge
                      variant={
                        product.blockchainStatus ===
                        "SUCCESS"
                          ? "success"
                          : product.blockchainStatus ===
                            "FAILED"
                          ? "danger"
                          : "warning"
                      }
                    >
                      {product.blockchainStatus}
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Active Status"
                  value={
                    <GlassBadge
                      variant={
                        product.active
                          ? "success"
                          : "danger"
                      }
                    >
                      {product.active
                        ? "ACTIVE"
                        : "INACTIVE"}
                    </GlassBadge>
                  }
                />

                <DetailRow
                  label="Created Date"
                  value={
                    <div className="flex items-center gap-2">
                      <Calendar size={16} />
                      <span>
                        {new Date(
                          product.createdAt
                        ).toLocaleDateString()}
                      </span>
                    </div>
                  }
                />
              </GlassCard>

              <GlassCard className="mt-6">
                <div
                  className="
                    flex
                    h-56
                    flex-col
                    items-center
                    justify-center
                    rounded-2xl
                    border
                    border-dashed
                    border-white/10
                    text-center
                  "
                >
                  <Package
                    size={56}
                    className="mb-4 text-blue-400"
                  />

                  <h3 className="text-lg font-semibold">
                    QR Code Preview
                  </h3>

                  <p className="mt-2 text-sm text-gray-400">
                    (Coming in Blockchain Module)
                  </p>
                </div>
              </GlassCard>
              <GlassCard className="mt-6">
  <div className="mb-6">
    <h3 className="text-lg font-semibold">
      Blockchain History
    </h3>

    <p className="mt-1 text-sm text-gray-400">
      Product transaction history
    </p>
  </div>

  {historyLoading ? (
    <GlassLoader message="Loading blockchain history..." />
  ) : !history ||
    history.history.length === 0 ? (
    <EmptyState
      title="No History"
      description="No blockchain transactions found."
      icon={<Package size={48} />}
    />
  ) : (
    <div className="space-y-4">
      {history.history.map(
        (transaction, index) => (
          <div
            key={index}
            className="
              rounded-xl
              border
              border-white/10
              bg-white/5
              p-4
            "
          >
            <div className="flex items-center justify-between">
              <h4 className="font-semibold">
                {transaction.productStatus}
              </h4>

              <GlassBadge
                variant={
                  transaction.verified
                    ? "success"
                    : "warning"
                }
              >
                {transaction.verified
                  ? "VERIFIED"
                  : "PENDING"}
              </GlassBadge>
            </div>

            <div className="mt-3 space-y-2 text-sm text-gray-300">
              <p>
                <span className="font-medium">
                  Manufacturer:
                </span>{" "}
                {transaction.manufacturerId}
              </p>

              <p>
                <span className="font-medium">
                  Owner:
                </span>{" "}
                {transaction.currentOwnerId}
              </p>

              <p>
                <span className="font-medium">
                  Role:
                </span>{" "}
                {transaction.currentOwnerRole}
              </p>

              <p>
                <span className="font-medium">
                  Updated:
                </span>{" "}
                {new Date(
                  transaction.updatedAt
                ).toLocaleString()}
              </p>
            </div>
          </div>
        )
      )}
    </div>
  )}
</GlassCard>
                  </div>

  <div
  className="
    flex
    flex-wrap
    items-center
    justify-end
    gap-3
    border-t
    border-white/10
    p-6
  "
>
  {product.blockchainStatus === "SUCCESS" && (
    <GlassButton
      loading={isTransferring}
      disabled={isTransferring}
      onClick={() => onTransferOwnership(product)}
    >
      Transfer Ownership
    </GlassButton>
  )}

  {product.blockchainStatus === "PENDING" && (
    <GlassButton
      loading={isRegistering}
      disabled={isRegistering}
      onClick={() => onRegisterBlockchain(product)}
    >
      Register on Blockchain
    </GlassButton>
  )}

  <GlassButton
    onClick={() => onEdit(product)}
  >
    Edit Product
  </GlassButton>

  <GlassButton
    variant="danger"
    onClick={() => onDelete(product)}
  >
    Delete Product
  </GlassButton>

  <GlassButton
    variant="secondary"
    onClick={onClose}
  >
    Close
  </GlassButton>
</div>
    </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
}