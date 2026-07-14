import {
  useMemo,
  useState,
} from "react";

import { AnimatePresence, motion } from "framer-motion";

import QrScanner from "@/components/verification/QrScanner";

import { toast } from "react-hot-toast";

import { useVerification } from "@/hooks/useVerification";

import { getApiError } from "@/utils/apiError";

import type {
  VerificationResponse,
  VerifyProductRequest,
} from "@/types/verification";

import {
  GlassBadge,
  GlassButton,
  GlassCard,
  GlassInput,
  GlassLoader,
  GlassSelect,
} from "@/components/ui";

import type { SelectOption } from "@/types/common";

export default function VerifyProduct() {
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

  const [result, setResult] =
    useState<VerificationResponse | null>(
      null
    );
const [showScanner, setShowScanner] =
  useState(false);

  const roleOptions =
    useMemo<SelectOption[]>(
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

  const updateField = (
    field: keyof VerifyProductRequest,
    value: string
  ) => {
    setForm((previous) => ({
      ...previous,
      [field]: value,
    }));
  };

  const handleSubmit = async () => {
    if (form.productId.trim() === "") {
  toast.error(
    "Please enter or scan a Product ID."
  );
  return;
}
setResult(null);
    try {
      const response =
        await verifyProduct(form);

      setResult(response);

      toast.success(
        "Product verified successfully."
      );
    } catch (error) {
      toast.error(
        getApiError(error)
      );
    }
  };

  const openScanner = () => {
  setShowScanner(true);
};

const closeScanner = () => {
  setShowScanner(false);
};

const handleScan = (
  value: string
) => {
  setForm((previous) => ({
    ...previous,
    productId: value,
  }));

  closeScanner();

  toast.success(
    "QR code scanned successfully."
  );
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

          <GlassInput
            label="User ID"
            value={form.userId}
            onChange={(e) =>
              updateField(
                "userId",
                e.target.value
              )
            }
          />

          <GlassSelect
            label="User Role"
            placeholder="Select user role"
            options={roleOptions}
            value={form.userRole}
            onChange={(e) =>
              updateField(
                "userRole",
                e.target.value
              )
            }
          />

          <GlassInput
            label="IP Address"
            value={form.ipAddress}
            onChange={(e) =>
              updateField(
                "ipAddress",
                e.target.value
              )
            }
          />

          <GlassInput
            label="Device Info"
            value={form.deviceInfo}
            onChange={(e) =>
              updateField(
                "deviceInfo",
                e.target.value
              )
            }
          />

          <GlassInput
            label="Location"
            value={form.location}
            onChange={(e) =>
              updateField(
                "location",
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

            <p>
              <strong>Product ID:</strong>{" "}
              {result.productId}
            </p>

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