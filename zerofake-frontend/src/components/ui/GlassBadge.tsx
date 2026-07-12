import { cn } from "../../utils/cn";

export type BadgeVariant =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "neutral";

interface GlassBadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  className?: string;
}

const variants: Record<BadgeVariant, string> = {
  success:
    "bg-emerald-500/15 border-emerald-500/30 text-emerald-400",

  warning:
    "bg-amber-500/15 border-amber-500/30 text-amber-400",

  danger:
    "bg-red-500/15 border-red-500/30 text-red-400",

  info:
    "bg-blue-500/15 border-blue-500/30 text-blue-400",

  neutral:
    "bg-white/10 border-white/15 text-gray-300",
};

export default function GlassBadge({
  children,
  variant = "neutral",
  className,
}: GlassBadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center",
        "rounded-full",
        "border",
        "px-3 py-1",
        "text-xs font-semibold",
        "backdrop-blur-xl",
        "transition-all duration-200",
        variants[variant],
        className
      )}
    >
      {children}
    </span>
  );
}