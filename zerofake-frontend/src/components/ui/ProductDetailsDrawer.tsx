import { useEffect, useState } from "react";
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
  Copy,
  Check
} from "lucide-react";
import { toast } from "react-hot-toast";

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
  const [copiedText, setCopiedText] = useState<string | null>(null);
  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    toast.success("Copied to clipboard!");
    setTimeout(() => setCopiedText(null), 2000);
  };

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
                  label="Product ID"
                  value={
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs text-blue-300 bg-white/5 border border-white/5 px-2 py-0.5 rounded">
                        {product.id.slice(0, 8)}...{product.id.slice(-4)}
                      </span>
                      <button
                        onClick={() => handleCopy(product.id)}
                        className="text-gray-400 hover:text-white transition"
                        title="Copy Product ID"
                      >
                        {copiedText === product.id ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                      </button>
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
                      <span className="font-mono text-xs text-blue-300 bg-white/5 border border-white/5 px-2 py-0.5 rounded">
                        {product.manufacturerId.slice(0, 8)}...{product.manufacturerId.slice(-4)}
                      </span>
                      <button
                        onClick={() => handleCopy(product.manufacturerId)}
                        className="text-gray-400 hover:text-white transition"
                        title="Copy Manufacturer ID"
                      >
                        {copiedText === product.manufacturerId ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                      </button>
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
                {product.blockchainStatus === "SUCCESS" ? (
                  <div className="flex flex-col items-center justify-center p-4 text-center">
                    <div className="bg-white p-4 rounded-2xl shadow-xl shadow-blue-500/5 border border-white/20">
                      <img
                        src={`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${product.id}`}
                        alt="Product Authentication QR Code"
                        className="w-44 h-44 rounded-xl"
                      />
                    </div>
                    <h3 className="text-lg font-semibold mt-4 text-white">
                      Verifiable Blockchain Seal
                    </h3>
                    <p className="mt-1 text-xs text-gray-400 max-w-xs">
                      Scan this QR code with any terminal camera to verify authenticity or auto-transfer custody.
                    </p>
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center p-6 text-center border border-dashed border-white/10 rounded-2xl h-56">
                    <Package size={48} className="text-gray-500 mb-2 animate-pulse" />
                    <h3 className="text-sm font-medium text-gray-400">
                      QR Code Unavailable
                    </h3>
                    <p className="mt-1 text-xs text-gray-500 max-w-xs">
                      Please register this product on the blockchain first to activate its verifiable secure QR seal.
                    </p>
                  </div>
                )}
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
              <div className="flex items-center gap-2">
                <span className="font-medium">
                  Manufacturer:
                </span>{" "}
                <span className="font-mono text-xs text-blue-300">
                  {transaction.manufacturerId.slice(0, 8)}...{transaction.manufacturerId.slice(-4)}
                </span>
                <button
                  onClick={() => handleCopy(transaction.manufacturerId)}
                  className="text-gray-400 hover:text-white transition"
                  title="Copy Manufacturer UUID"
                >
                  {copiedText === transaction.manufacturerId ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                </button>
              </div>

              <div className="flex items-center gap-2">
                <span className="font-medium">
                  Owner:
                </span>{" "}
                <span className="font-mono text-xs text-blue-300">
                  {transaction.currentOwnerId.slice(0, 8)}...{transaction.currentOwnerId.slice(-4)}
                </span>
                <button
                  onClick={() => handleCopy(transaction.currentOwnerId)}
                  className="text-gray-400 hover:text-white transition"
                  title="Copy Owner UUID"
                >
                  {copiedText === transaction.currentOwnerId ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                </button>
              </div>

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