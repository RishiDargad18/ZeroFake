import { motion } from "framer-motion";
import { Loader2 } from "lucide-react";
import type { ReactNode } from "react";
import type { HTMLMotionProps } from "framer-motion";
import { cn } from "../../utils/cn";

type Variant =
  | "primary"
  | "secondary"
  | "outline"
  | "danger";

interface GlassButtonProps
  extends HTMLMotionProps<"button"> {
  variant?: Variant;
  loading?: boolean;
  icon?: ReactNode;
  children: ReactNode;
}

const variants: Record<Variant, string> = {
  primary:
    "bg-blue-600 text-white hover:bg-blue-500 border border-blue-500 shadow-lg shadow-blue-600/25",

  secondary:
    "bg-white/10 text-white border border-white/10 hover:bg-white/20",

  outline:
    "bg-transparent border border-blue-500 text-blue-400 hover:bg-blue-600/10",

  danger:
    "bg-red-600 text-white border border-red-500 hover:bg-red-500",
};

export default function GlassButton({
  variant = "primary",
  loading = false,
  disabled,
  icon,
  children,
  className,
  ...props
}: GlassButtonProps) {
  const isDisabled = disabled || loading;

  return (
    <motion.button
      whileHover={!isDisabled ? { scale: 1.03 } : undefined}
      whileTap={!isDisabled ? { scale: 0.98 } : undefined}
      transition={{ duration: 0.15 }}
      disabled={isDisabled}
      className={cn(
        "inline-flex items-center justify-center gap-2",
        "rounded-2xl px-5 py-3",
        "font-medium",
        "transition-all duration-200",
        "backdrop-blur-xl",
        "disabled:cursor-not-allowed disabled:opacity-60",
        variants[variant],
        className
      )}
      {...props}
    >
      {loading ? (
        <Loader2 size={18} className="animate-spin" />
      ) : (
        icon
      )}

      <span>{children}</span>
    </motion.button>
  );
}