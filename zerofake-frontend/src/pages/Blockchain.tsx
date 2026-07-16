import { useState } from "react";
import { 
  Boxes, 
  Cpu, 
  Search, 
  Hash, 
  Activity, 
  ShieldCheck, 
  AlertTriangle,
  GitCommit,
  Clock,
  Layers,
  Copy,
  Check
} from "lucide-react";
import { toast } from "react-hot-toast";
import { blockchainService } from "@/services/blockchainService";
import type { BlockchainTransactionResponse } from "@/types/blockchain";
import { getApiError } from "@/utils/apiError";
import {
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassLoader,
} from "@/components/ui";

export default function Blockchain() {
  const [searchQuery, setSearchQuery] = useState("");
  const [searchType, setSearchType] = useState<"txId" | "productId">("txId");
  const [txResult, setTxResult] = useState<BlockchainTransactionResponse | null>(null);
  const [productListResult, setProductListResult] = useState<BlockchainTransactionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const [copiedText, setCopiedText] = useState<string | null>(null);
  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    toast.success("Copied to clipboard!");
    setTimeout(() => setCopiedText(null), 2000);
  };

  const handleSearch = async () => {
    const query = searchQuery.trim();
    if (!query) {
      toast.error("Please enter a valid Transaction Hash or Product ID.");
      return;
    }

    setIsLoading(true);
    setHasSearched(true);
    setTxResult(null);
    setProductListResult([]);

    try {
      if (searchType === "txId") {
        const tx = await blockchainService.getTransactionByTransactionId(query);
        setTxResult(tx);
        toast.success("Transaction found.");
      } else {
        const list = await blockchainService.getTransactionsByProductId(query);
        setProductListResult(list);
        toast.success(`Found ${list.length} transactions for product.`);
      }
    } catch (error) {
      toast.error(getApiError(error));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold bg-gradient-to-r from-white via-gray-200 to-gray-500 bg-clip-text text-transparent">
          Blockchain Explorer
        </h1>
        <p className="mt-2 text-gray-400">
          Explore blocks, query transaction payloads, and audit the state of Hyperledger Fabric ledger.
        </p>
      </div>

      {/* Network Stats Cards */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <GlassCard className="flex items-center gap-4">
          <div className="rounded-2xl bg-blue-500/10 p-3 text-blue-400">
            <Cpu size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-400">Network Engine</span>
            <h3 className="text-lg font-bold text-white">Fabric v2.5</h3>
          </div>
        </GlassCard>

        <GlassCard className="flex items-center gap-4">
          <div className="rounded-2xl bg-indigo-500/10 p-3 text-indigo-400">
            <Layers size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-400">Active Channel</span>
            <h3 className="text-lg font-bold text-white">mychannel</h3>
          </div>
        </GlassCard>

        <GlassCard className="flex items-center gap-4">
          <div className="rounded-2xl bg-purple-500/10 p-3 text-purple-400">
            <Boxes size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-400">Smart Contract</span>
            <h3 className="text-lg font-bold text-white">zerofake (v2.0)</h3>
          </div>
        </GlassCard>

        <GlassCard className="flex items-center gap-4">
          <div className="rounded-2xl bg-emerald-500/10 p-3 text-emerald-400">
            <ShieldCheck size={24} />
          </div>
          <div>
            <span className="text-xs text-gray-400">MSP Endorsement</span>
            <h3 className="text-lg font-bold text-white">Org1 & Org2</h3>
          </div>
        </GlassCard>
      </div>

      {/* Query Section */}
      <GlassCard>
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <label className="text-sm font-semibold text-gray-300">Query Type:</label>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
                <input 
                  type="radio" 
                  name="searchType" 
                  checked={searchType === "txId"} 
                  onChange={() => setSearchType("txId")}
                  className="text-blue-500 focus:ring-blue-500 bg-white/5 border-white/10"
                />
                Transaction Hash
              </label>
              <label className="flex items-center gap-2 text-sm text-gray-300 cursor-pointer">
                <input 
                  type="radio" 
                  name="searchType" 
                  checked={searchType === "productId"} 
                  onChange={() => setSearchType("productId")}
                  className="text-blue-500 focus:ring-blue-500 bg-white/5 border-white/10"
                />
                Product ID
              </label>
            </div>
          </div>

          <div className="flex flex-col gap-4 md:flex-row md:items-end">
            <div className="flex-1">
              <GlassInput
                label={searchType === "txId" ? "Transaction ID / Hash" : "Product ID (UUID)"}
                placeholder={searchType === "txId" ? "e.g. ebd781a6964ff2679..." : "e.g. 00000000-0000-0000-0000..."}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <GlassButton
              onClick={() => void handleSearch()}
              disabled={isLoading}
              className="flex items-center gap-2"
            >
              <Search size={18} />
              Query Ledger
            </GlassButton>
          </div>
        </div>
      </GlassCard>

      {/* Loading Overlay */}
      {isLoading && (
        <div className="py-12">
          <GlassLoader message="Querying Hyperledger Fabric state database..." />
        </div>
      )}

      {/* Results Rendering */}
      {!isLoading && hasSearched && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold flex items-center gap-2 text-white">
            <Activity className="text-blue-400" size={22} />
            Ledger Query Results
          </h2>

          {/* Single Transaction Result */}
          {searchType === "txId" && txResult && (
            <GlassCard className="relative overflow-hidden">
              <div className="absolute right-0 top-0 -mt-6 -mr-6 h-20 w-20 rounded-full bg-blue-500/10 blur-xl" />
              <div className="flex flex-wrap items-center justify-between gap-4 border-b border-white/5 pb-4 mb-4">
                <div className="flex items-center gap-2.5">
                  <Hash className="text-blue-400" size={20} />
                  <span className="font-semibold text-white">Tx Details</span>
                </div>
                <GlassBadge variant={txResult.status === "SUCCESS" ? "success" : "danger"}>
                  {txResult.status}
                </GlassBadge>
              </div>

              <div className="grid gap-4 sm:grid-cols-2 text-sm text-gray-300">
                <div className="space-y-1">
                  <span className="text-xs text-gray-400 block">Transaction ID / Hash</span>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-blue-300 bg-white/5 px-2.5 py-1 rounded-lg border border-white/5">
                      {txResult.transactionId.slice(0, 8)}...{txResult.transactionId.slice(-8)}
                    </span>
                    <button
                      onClick={() => handleCopy(txResult.transactionId)}
                      className="text-gray-400 hover:text-white transition"
                      title="Copy Full Hash"
                    >
                      {copiedText === txResult.transactionId ? <Check size={14} className="text-emerald-400" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>
                <div className="space-y-1">
                  <span className="text-xs text-gray-400 block">Product ID</span>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-indigo-300 bg-white/5 px-2.5 py-1 rounded-lg border border-white/5">
                      {txResult.productId.slice(0, 8)}...{txResult.productId.slice(-4)}
                    </span>
                    <button
                      onClick={() => handleCopy(txResult.productId)}
                      className="text-gray-400 hover:text-white transition"
                      title="Copy Product UUID"
                    >
                      {copiedText === txResult.productId ? <Check size={14} className="text-emerald-400" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>
                <div className="space-y-1">
                  <span className="text-xs text-gray-400 block">Transaction Type</span>
                  <span className="font-semibold text-white">{txResult.transactionType}</span>
                </div>
                <div className="space-y-1">
                  <span className="text-xs text-gray-400 block">Performed By</span>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-gray-300 bg-white/5 px-2.5 py-1 rounded-lg border border-white/5">
                      {txResult.performedBy.slice(0, 8)}...{txResult.performedBy.slice(-4)}
                    </span>
                    <button
                      onClick={() => handleCopy(txResult.performedBy)}
                      className="text-gray-400 hover:text-white transition"
                      title="Copy User UUID"
                    >
                      {copiedText === txResult.performedBy ? <Check size={14} className="text-emerald-400" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>
                <div className="space-y-1 sm:col-span-2">
                  <span className="text-xs text-gray-400 block">Message</span>
                  <p className="text-white bg-white/5 p-3 rounded-lg border border-white/5">{txResult.message}</p>
                </div>
              </div>
            </GlassCard>
          )}

          {/* Product Transaction List Result */}
          {searchType === "productId" && productListResult.length > 0 && (
            <div className="space-y-4">
              {productListResult.map((tx, idx) => (
                <GlassCard key={idx} className="hover:border-white/20 transition-all duration-200">
                  <div className="flex flex-wrap items-center justify-between gap-4 border-b border-white/5 pb-3 mb-3">
                    <div className="flex items-center gap-2">
                      <GitCommit className="text-indigo-400" size={18} />
                      <span className="font-semibold text-white text-sm">{tx.transactionType}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Clock size={12} className="text-gray-400" />
                      <span className="text-xs text-gray-400">{new Date(tx.createdAt).toLocaleString()}</span>
                      <GlassBadge variant={tx.status === "SUCCESS" ? "success" : "danger"}>
                        {tx.status}
                      </GlassBadge>
                    </div>
                  </div>

                  <div className="space-y-2 text-xs text-gray-300">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-gray-400 w-24 shrink-0">Tx Hash:</span>
                      <span className="font-mono text-blue-300 bg-white/5 px-2 py-0.5 rounded border border-white/5">
                        {tx.transactionId.slice(0, 8)}...{tx.transactionId.slice(-8)}
                      </span>
                      <button
                        onClick={() => handleCopy(tx.transactionId)}
                        className="text-gray-400 hover:text-white transition"
                        title="Copy Tx Hash"
                      >
                        {copiedText === tx.transactionId ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                      </button>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-gray-400 w-24 shrink-0">User ID:</span>
                      <span className="font-mono bg-white/5 px-2 py-0.5 rounded border border-white/5">
                        {tx.performedBy.slice(0, 8)}...{tx.performedBy.slice(-4)}
                      </span>
                      <button
                        onClick={() => handleCopy(tx.performedBy)}
                        className="text-gray-400 hover:text-white transition"
                        title="Copy User ID"
                      >
                        {copiedText === tx.performedBy ? <Check size={12} className="text-emerald-400" /> : <Copy size={12} />}
                      </button>
                    </div>
                    <div className="flex gap-2">
                      <span className="font-semibold text-gray-400 w-24 shrink-0">Details:</span>
                      <span>{tx.message}</span>
                    </div>
                  </div>
                </GlassCard>
              ))}
            </div>
          )}

          {/* Empty / Not Found */}
          {((searchType === "txId" && !txResult) || (searchType === "productId" && productListResult.length === 0)) && (
            <GlassCard className="py-12 text-center">
              <AlertTriangle className="mx-auto mb-3 text-red-500/80" size={48} />
              <h3 className="text-lg font-bold text-white">Record Not Found</h3>
              <p className="mt-2 text-sm text-gray-400 max-w-md mx-auto">
                No ledger records matching the query were found on the current Fabric state database or index log.
              </p>
            </GlassCard>
          )}
        </div>
      )}
    </div>
  );
}
