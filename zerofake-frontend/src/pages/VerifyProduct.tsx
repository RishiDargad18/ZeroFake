import {
  useState,
  useEffect,
} from "react";
import { useAuth } from "@/hooks/useAuth";

import { AnimatePresence, motion } from "framer-motion";

import QrScanner from "@/components/verification/QrScanner";
import { toast } from "react-hot-toast";
import { Copy, Check } from "lucide-react";
import { blockchainService } from "@/services/blockchainService";

import { useVerification } from "@/hooks/useVerification";

import { getApiError } from "@/utils/apiError";

import type {
  VerificationResponse,
  VerifyProductRequest,
} from "@/types/verification";
import type { OwnerRole } from "@/types/blockchain";

import {
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassLoader,
} from "@/components/ui";

export default function VerifyProduct() {
  const { user } = useAuth();
  const {
    verifyProduct,
    isVerifying,
  } = useVerification();

  const [form, setForm] =
    useState<VerifyProductRequest>({
      productId: "",
      userId: "",
      userRole: "",
      ipAddress: "",
      deviceInfo: "",
      location: "",
    });

  useEffect(() => {
    if (user) {
      setForm((prev) => ({
        ...prev,
        userId: user.id || "",
        userRole: user.role ? user.role.replace("ROLE_", "") : "",
        ipAddress: prev.ipAddress || "127.0.0.1",
        deviceInfo: prev.deviceInfo || navigator.userAgent || "Browser Client",
        location: prev.location || "Local Verification Terminal",
      }));
    }
  }, [user]);

  const [result, setResult] =
    useState<VerificationResponse | null>(
      null
    );
const [showScanner, setShowScanner] =
  useState(false);



  const updateField = (
    field: keyof VerifyProductRequest,
    value: string
  ) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }));
  };

  const [copied, setCopied] = useState(false);
  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    toast.success("Copied to clipboard!");
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSubmit = async (targetProductId?: string) => {
    const pid = targetProductId || form.productId;
    if (pid.trim() === "") {
      toast.error("Please enter or scan a Product ID.");
      return;
    }
    setResult(null);
    try {
      const response = await verifyProduct({
        ...form,
        productId: pid,
      });

      setResult(response);
      toast.success("Product verified successfully.");

      if (response.authentic && response.verificationResult === "GENUINE") {
        toast.loading("Authentic product confirmed! Auto-transferring ownership to you on the ledger...", { id: "transfer" });
        try {
          // 1. Fetch current owner from blockchain history
          const historyRes = await blockchainService.getProductHistory(pid);
          let currentOwner = form.userId; // fallback
          if (historyRes && historyRes.history && historyRes.history.length > 0) {
            currentOwner = historyRes.history[historyRes.history.length - 1].currentOwnerId;
          }

          // 2. Perform transfer
          await blockchainService.transferOwnership({
            productId: pid,
            fromOwnerId: currentOwner,
            toOwnerId: form.userId,
            toOwnerRole: (user?.role?.replace("ROLE_", "") || "CUSTOMER") as OwnerRole,
          });
          toast.success("Ownership auto-transferred successfully on the blockchain!", { id: "transfer" });
        } catch (txErr) {
          console.error("Auto-transfer transaction failed:", txErr);
          toast.error("Verified as authentic, but blockchain ownership transfer failed: " + getApiError(txErr), { id: "transfer" });
        }
      }
    } catch (error) {
      toast.error(getApiError(error));
    }
  };

  const openScanner = () => {
    setShowScanner(true);
  };

  const closeScanner = () => {
    setShowScanner(false);
  };

  const handleScan = (value: string) => {
    setForm((previous) => ({
      ...previous,
      productId: value,
    }));
    closeScanner();
    toast.success("QR code scanned successfully.");
    void handleSubmit(value);
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold">
          Verify Product
        </h1>

        <p className="mt-2 text-gray-400">
          Verify product authenticity.
        </p>
      </div>

      <GlassCard>
        <div className="space-y-5">
          <GlassInput
            label="Product ID"
            value={form.productId}
            onChange={(e) =>
              updateField(
                "productId",
                e.target.value
              )
            }
          />


          <div className="flex justify-end gap-3">
            <GlassButton
            variant="secondary"
            onClick={openScanner}
            disabled={isVerifying}
            >
              Scan QR Code
            </GlassButton>

            <GlassButton
              loading={isVerifying}
              disabled={isVerifying}
              onClick={() => void handleSubmit()}
            >
              Verify Product
            </GlassButton>
          </div>
        </div>
      </GlassCard>

      {isVerifying && (
        <GlassLoader message="Verifying Product..." />
      )}

      {result && (
        <GlassCard>
          <div className="space-y-4">
            <h2 className="text-2xl font-bold">
              Verification Result
            </h2>

            <div className="flex items-center gap-3">
              <strong>Product ID:</strong>
              <span className="font-mono text-xs bg-white/5 border border-white/10 px-2.5 py-1.5 rounded-xl text-indigo-300">
                {result.productId.slice(0, 8)}...{result.productId.slice(-4)}
              </span>
              <button
                type="button"
                onClick={() => handleCopy(result.productId)}
                className="text-gray-400 hover:text-white transition"
                title="Copy Product UUID"
              >
                {copied ? <Check size={14} className="text-emerald-400" /> : <Copy size={14} />}
              </button>
            </div>

            <p>
              <strong>Authentic:</strong>{" "}
              <GlassBadge
                variant={
                  result.authentic
                    ? "success"
                    : "danger"
                }
              >
                {result.authentic
                  ? "YES"
                  : "NO"}
              </GlassBadge>
            </p>

            <p>
              <strong>
                Fraud Detected:
              </strong>{" "}
              <GlassBadge
                variant={
                  result.fraudDetected
                    ? "danger"
                    : "success"
                }
              >
                {result.fraudDetected
                  ? "YES"
                  : "NO"}
              </GlassBadge>
            </p>

            <p>
              <strong>
                Risk Score:
              </strong>{" "}
              {result.riskScore}
            </p>

            <p>
              <strong>
                Verification Result:
              </strong>{" "}
              {result.verificationResult}
            </p>

            <p>
              <strong>
                Triggered Rules:
              </strong>
            </p>

            <ul className="list-disc pl-6">
              {result.triggeredRules.map(
                (rule) => (
                  <li key={rule}>
                    {rule}
                  </li>
                )
              )}
            </ul>

            <p>
              <strong>Message:</strong>{" "}
              {result.message}
            </p>
          </div>
        </GlassCard>
      )}
      <AnimatePresence>
  {showScanner && (
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
      onClick={closeScanner}
    >
      <motion.div
        className="w-full max-w-2xl"
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
                Scan Product QR Code
              </h2>

              <p className="mt-2 text-sm text-gray-400">
                Scan the QR code attached
                to the product.
              </p>
            </div>

            <QrScanner
              onScan={handleScan}
              onClose={closeScanner}
            />

            <div className="flex justify-end">
              <GlassButton
                variant="secondary"
                onClick={closeScanner}
              >
                Close
              </GlassButton>
            </div>
          </div>
        </GlassCard>
      </motion.div>
    </motion.div>
  )}
</AnimatePresence>
    </div>
  );
}