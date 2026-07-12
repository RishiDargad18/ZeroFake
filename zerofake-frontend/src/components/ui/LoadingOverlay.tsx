import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";

import { cn } from "../../utils/cn";
import GlassCard from "./GlassCard";
import GlassLoader from "./GlassLoader";

const DEFAULT_MESSAGES = [
  "Loading Dashboard...",
  "Connecting to Blockchain...",
  "Verifying Product...",
  "Scanning QR...",
  "Detecting Fraud...",
];

export interface LoadingOverlayProps {
  open: boolean;
  message?: string;
}

export default function LoadingOverlay({
  open,
  message,
}: LoadingOverlayProps) {
  const [messageIndex, setMessageIndex] = useState(0);

  const currentMessage = useMemo(() => {
    if (message) {
      return message;
    }

    return DEFAULT_MESSAGES[messageIndex];
  }, [message, messageIndex]);

  useEffect(() => {
    if (!open || message) {
      return;
    }

    const interval = window.setInterval(() => {
      setMessageIndex((previous) =>
        (previous + 1) % DEFAULT_MESSAGES.length
      );
    }, 2200);

    return () => clearInterval(interval);
  }, [open, message]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.25 }}
          className="
            fixed
            inset-0
            z-[999]
            flex
            items-center
            justify-center
            bg-black/40
            backdrop-blur-md
          "
        >
          <GlassCard
            className={cn(
              "w-full",
              "max-w-md",
              "text-center"
            )}
          >
            <GlassLoader
              size="lg"
              message={currentMessage}
            />
          </GlassCard>
        </motion.div>
      )}
    </AnimatePresence>
  );
}