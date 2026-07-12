import { AnimatePresence, motion } from "framer-motion";
import { AlertTriangle } from "lucide-react";
import { useEffect, useRef } from "react";

import { cn } from "../../utils/cn";
import GlassButton from "./GlassButton";
import GlassCard from "./GlassCard";

export interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDialog({
  open,
  title,
  description,
  confirmText = "Confirm",
  cancelText = "Cancel",
  loading = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const confirmButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    confirmButtonRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !loading) {
        onCancel();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      window.removeEventListener(
        "keydown",
        handleKeyDown
      );
    };
  }, [open, loading, onCancel]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="
            fixed
            inset-0
            z-[1000]
            flex
            items-center
            justify-center
            bg-black/50
            p-4
            backdrop-blur-md
          "
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onClick={!loading ? onCancel : undefined}
        >
          <motion.div
            initial={{
              scale: 0.9,
              opacity: 0,
              y: 16,
            }}
            animate={{
              scale: 1,
              opacity: 1,
              y: 0,
            }}
            exit={{
              scale: 0.9,
              opacity: 0,
              y: 16,
            }}
            transition={{
              duration: 0.2,
            }}
            onClick={(event) =>
              event.stopPropagation()
            }
            className="w-full max-w-md"
          >
            <GlassCard>
              <div className="flex flex-col items-center text-center">
                <div
                  className="
                    mb-6
                    flex
                    h-16
                    w-16
                    items-center
                    justify-center
                    rounded-full
                    bg-red-500/10
                    text-red-400
                  "
                >
                  <AlertTriangle size={32} />
                </div>

                <h2 className="text-2xl font-bold">
                  {title}
                </h2>

                <p
                  className="
                    mt-3
                    text-sm
                    text-gray-400
                  "
                >
                  {description}
                </p>

                <div
                  className="
                    mt-8
                    flex
                    w-full
                    gap-3
                  "
                >
                  <GlassButton
                    variant="secondary"
                    className="flex-1"
                    onClick={onCancel}
                    disabled={loading}
                  >
                    {cancelText}
                  </GlassButton>

                  <GlassButton
                    ref={confirmButtonRef}
                    variant="danger"
                    className="flex-1"
                    loading={loading}
                    onClick={onConfirm}
                  >
                    {confirmText}
                  </GlassButton>
                </div>
              </div>
            </GlassCard>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}