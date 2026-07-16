import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { 
  Search, 
  QrCode, 
  Calendar, 
  CheckCircle2, 
  AlertCircle, 
  ShieldCheck,
  Clock
} from "lucide-react";
import { toast } from "react-hot-toast";
import { useBlockchainHistory } from "@/hooks/useBlockchainHistory";
import { productService } from "@/services/productService";
import type { ProductResponse } from "@/types/product";
import { getApiError } from "@/utils/apiError";
import QrScanner from "@/components/verification/QrScanner";
import {
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassLoader,
  EmptyState
} from "@/components/ui";

export default function ProductTimeline() {
  const [searchParams] = useSearchParams();
  const [productIdInput, setProductIdInput] = useState("");
  const { history, isLoading: historyLoading, loadHistory } = useBlockchainHistory();
  const [productDetails, setProductDetails] = useState<ProductResponse | null>(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [showScanner, setShowScanner] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  useEffect(() => {
    const productId = searchParams.get("productId");
    if (productId) {
      setProductIdInput(productId);
      void handleSearch(productId);
    }
  }, [searchParams]);

  const handleSearch = async (targetId?: string) => {
    const idToSearch = targetId || productIdInput.trim();
    if (!idToSearch) {
      toast.error("Please enter or scan a valid Product ID.");
      return;
    }

    setHasSearched(true);
    setDetailsLoading(true);
    setProductDetails(null);

    try {
      // Load history from Blockchain
      await loadHistory(idToSearch);
      
      // Load details from Product database
      try {
        const details = await productService.getProduct(idToSearch);
        setProductDetails(details);
      } catch (err) {
        // Product might not be in off-chain database (e.g. registered directly or deleted)
        console.warn("Product details not found off-chain:", err);
      }

      toast.success("Product timeline retrieved.");
    } catch (error) {
      toast.error(getApiError(error));
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleScan = (scannedValue: string) => {
    setProductIdInput(scannedValue);
    setShowScanner(false);
    toast.success("QR Code scanned.");
    void handleSearch(scannedValue);
  };

  // Helper to map role to specific Tailwind colors and nice label
  const getRoleLabelAndColor = (role: string) => {
    switch (role.toUpperCase()) {
      case "MANUFACTURER":
        return { label: "Manufacturer", gradient: "from-blue-500 to-indigo-500", text: "text-indigo-400" };
      case "WAREHOUSE":
        return { label: "Warehouse", gradient: "from-purple-500 to-pink-500", text: "text-pink-400" };
      case "DISTRIBUTOR":
        return { label: "Distributor", gradient: "from-orange-500 to-amber-500", text: "text-orange-400" };
      case "RETAILER":
        return { label: "Retailer", gradient: "from-teal-500 to-emerald-500", text: "text-teal-400" };
      case "CUSTOMER":
        return { label: "Customer", gradient: "from-green-500 to-emerald-600", text: "text-green-400" };
      default:
        return { label: role, gradient: "from-gray-500 to-slate-500", text: "text-gray-400" };
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-white via-gray-200 to-gray-500 bg-clip-text text-transparent">
          Product Timeline
        </h1>
        <p className="mt-2 text-gray-400">
          Verify and trace the complete chain of custody and blockchain history of your product.
        </p>
      </div>

      {/* Search Controls */}
      <GlassCard>
        <div className="flex flex-col gap-4 md:flex-row md:items-end">
          <div className="flex-1">
            <GlassInput
              label="Product ID (UUID)"
              placeholder="e.g. 3f4c12ab-9d21-4b89-b7c9-a0d31e789f12"
              value={productIdInput}
              onChange={(e) => setProductIdInput(e.target.value)}
            />
          </div>
          <div className="flex gap-3">
            <GlassButton
              variant="secondary"
              onClick={() => setShowScanner(true)}
              className="flex items-center gap-2"
            >
              <QrCode size={18} />
              Scan QR
            </GlassButton>
            <GlassButton
              onClick={() => void handleSearch()}
              disabled={historyLoading || detailsLoading}
              className="flex items-center gap-2"
            >
              <Search size={18} />
              Trace Product
            </GlassButton>
          </div>
        </div>
      </GlassCard>

      {/* Loading state */}
      {(historyLoading || detailsLoading) && (
        <div className="py-12">
          <GlassLoader message="Querying blockchain history and registry..." />
        </div>
      )}

      {/* Results */}
      {!historyLoading && !detailsLoading && hasSearched && (
        <div className="grid gap-8 lg:grid-cols-3">
          
          {/* Product Registry Details Panel */}
          <div className="lg:col-span-1 space-y-6">
            <h2 className="text-xl font-bold flex items-center gap-2 text-white">
              <ShieldCheck className="text-blue-400" size={22} />
              Registry Info
            </h2>
            
            <GlassCard>
              {productDetails ? (
                <div className="space-y-6">
                  {productDetails.imageUrl && (
                    <div className="aspect-video w-full overflow-hidden rounded-xl border border-white/10 bg-black/20">
                      <img 
                        src={productDetails.imageUrl} 
                        alt={productDetails.productName} 
                        className="h-full w-full object-cover"
                      />
                    </div>
                  )}
                  <div className="space-y-4">
                    <div>
                      <span className="text-xs text-gray-400 uppercase tracking-wider">Product Name</span>
                      <h3 className="text-lg font-bold text-white">{productDetails.productName}</h3>
                    </div>
                    <div>
                      <span className="text-xs text-gray-400 uppercase tracking-wider">Code / SKU</span>
                      <p className="text-sm font-mono text-gray-300">{productDetails.productCode}</p>
                    </div>
                    <div>
                      <span className="text-xs text-gray-400 uppercase tracking-wider">Brand</span>
                      <p className="text-sm text-gray-300">{productDetails.brand}</p>
                    </div>
                    <div>
                      <span className="text-xs text-gray-400 uppercase tracking-wider">Category</span>
                      <p className="text-sm text-gray-300">{productDetails.category.name}</p>
                    </div>
                    <div>
                      <span className="text-xs text-gray-400 uppercase tracking-wider">Blockchain Registry Status</span>
                      <div className="mt-1">
                        <GlassBadge variant={productDetails.blockchainStatus === "SUCCESS" ? "success" : "warning"}>
                          {productDetails.blockchainStatus}
                        </GlassBadge>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="py-8 text-center text-gray-400">
                  <AlertCircle size={40} className="mx-auto mb-3 text-yellow-500/80" />
                  <p className="text-sm">Product metadata not registered in off-chain database, but transaction audit records exist on blockchain.</p>
                </div>
              )}
            </GlassCard>
          </div>

          {/* Supply Chain Timeline Panel */}
          <div className="lg:col-span-2 space-y-6">
            <h2 className="text-xl font-bold flex items-center gap-2 text-white">
              <Clock className="text-indigo-400" size={22} />
              Chain of Custody Timeline
            </h2>

            {history && history.history && history.history.length > 0 ? (
              <div className="relative pl-6 sm:pl-8 space-y-8 before:absolute before:left-[17px] sm:before:left-[21px] before:top-2 before:bottom-2 before:w-[2px] before:bg-gradient-to-b before:from-blue-500 before:via-indigo-500 before:to-emerald-500">
                {history.history.map((tx, idx) => {
                  const roleConfig = getRoleLabelAndColor(tx.currentOwnerRole);
                  return (
                    <motion.div
                      key={idx}
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.4, delay: idx * 0.1 }}
                      className="relative group"
                    >
                      {/* Timeline Dot */}
                      <span className={`absolute -left-[30px] sm:-left-[35px] top-1.5 flex h-6 w-6 sm:h-8 sm:w-8 items-center justify-center rounded-full bg-gradient-to-br ${roleConfig.gradient} shadow-lg ring-4 ring-black/40 group-hover:scale-110 transition-transform duration-200`}>
                        <CheckCircle2 size={14} className="text-white" />
                      </span>

                      {/* Timeline Card */}
                      <GlassCard className="relative overflow-hidden group-hover:border-white/20 transition-all duration-300">
                        {/* Decorative background glow for node */}
                        <div className={`absolute right-0 top-0 -mt-8 -mr-8 h-24 w-24 rounded-full bg-gradient-to-br ${roleConfig.gradient} opacity-5 blur-2xl group-hover:opacity-10 transition-opacity duration-300`} />

                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                          <div>
                            <div className="flex items-center gap-3 flex-wrap">
                              <span className={`text-xs font-bold uppercase tracking-wider ${roleConfig.text}`}>
                                {tx.productStatus}
                              </span>
                              <GlassBadge variant={tx.verified ? "success" : "warning"}>
                                {tx.verified ? "VERIFIED" : "UNVERIFIED"}
                              </GlassBadge>
                            </div>
                            <h3 className="mt-1 text-lg font-bold text-white flex items-center gap-2">
                              {roleConfig.label} Node
                            </h3>
                          </div>
                          
                          <div className="text-xs text-gray-400 flex items-center gap-1.5 bg-white/5 px-3 py-1.5 rounded-lg border border-white/5">
                            <Calendar size={12} className="text-blue-400" />
                            {new Date(tx.updatedAt).toLocaleString()}
                          </div>
                        </div>

                        <div className="mt-4 pt-4 border-t border-white/5 space-y-2 text-sm text-gray-300">
                          <div className="grid grid-cols-1 sm:grid-cols-4 gap-1">
                            <span className="font-semibold text-gray-400">Owner ID:</span>
                            <span className="sm:col-span-3 font-mono text-xs text-blue-300">
                              {tx.currentOwnerId.slice(0, 8)}...{tx.currentOwnerId.slice(-4)}
                            </span>
                          </div>
                          {tx.manufacturerId && (
                            <div className="grid grid-cols-1 sm:grid-cols-4 gap-1">
                              <span className="font-semibold text-gray-400">Manufacturer:</span>
                              <span className="sm:col-span-3 font-mono text-xs text-blue-300">
                                {tx.manufacturerId.slice(0, 8)}...{tx.manufacturerId.slice(-4)}
                              </span>
                            </div>
                          )}

                          <div className="grid grid-cols-1 sm:grid-cols-4 gap-1 pt-1">
                            <span className="font-semibold text-gray-400">Owner Role:</span>
                            <span className="sm:col-span-3 text-xs">
                              <span className={`px-2 py-0.5 rounded-md text-xs font-semibold bg-gradient-to-br ${roleConfig.gradient} text-white`}>
                                {tx.currentOwnerRole}
                              </span>
                            </span>
                          </div>
                        </div>
                      </GlassCard>
                    </motion.div>
                  );
                })}
              </div>
            ) : (
              <GlassCard className="py-12 text-center">
                <EmptyState
                  title="No Timeline Records Found"
                  description="This product ID does not have any recorded blockchain ledger entries."
                  icon={<AlertCircle size={48} className="text-red-400" />}
                />
              </GlassCard>
            )}
          </div>
        </div>
      )}

      {/* QR Scanner Overlay Modal */}
      <AnimatePresence>
        {showScanner && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-md"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowScanner(false)}
          >
            <motion.div
              className="w-full max-w-2xl"
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              transition={{ duration: 0.2 }}
              onClick={(e) => e.stopPropagation()}
            >
              <GlassCard>
                <div className="space-y-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <h2 className="text-2xl font-bold text-white">Scan Product QR Code</h2>
                      <p className="mt-1 text-sm text-gray-400">Place the product QR code in front of your camera.</p>
                    </div>
                    <GlassButton
                      variant="secondary"
                      onClick={() => setShowScanner(false)}
                      className="px-3 py-1.5 text-xs"
                    >
                      Close
                    </GlassButton>
                  </div>

                  <QrScanner
                    onScan={handleScan}
                    onClose={() => setShowScanner(false)}
                  />
                </div>
              </GlassCard>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
