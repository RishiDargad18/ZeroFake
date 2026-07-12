import { motion } from "framer-motion";
import { cn } from "../../utils/cn";

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  padding?: boolean;
}

export default function GlassCard({
  children,
  className,
  hover = false,
  padding = true,
}: GlassCardProps) {
  return (
    <motion.div
      whileHover={hover ? { y: -4, scale: 1.01 } : undefined}
      transition={{ duration: 0.2 }}
      className={cn(
        "rounded-3xl",
        "border border-white/10",
        "bg-white/10 dark:bg-white/5",
        "backdrop-blur-2xl",
        "shadow-[8px_8px_24px_rgba(0,0,0,0.15),-8px_-8px_24px_rgba(255,255,255,0.04)]",
        padding && "p-6",
        className
      )}
    >
      {children}
    </motion.div>
  );
}