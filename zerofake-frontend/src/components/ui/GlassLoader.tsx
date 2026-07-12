import { motion } from "framer-motion";
import { cn } from "../../utils/cn";

interface GlassLoaderProps {
  size?: "sm" | "md" | "lg";
  message?: string;
  className?: string;
}

const sizes = {
  sm: "h-10 w-10",
  md: "h-16 w-16",
  lg: "h-24 w-24",
};

export default function GlassLoader({
  size = "md",
  message,
  className,
}: GlassLoaderProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-4",
        className
      )}
    >
      <motion.div
        animate={{ rotate: 360 }}
        transition={{
          repeat: Infinity,
          ease: "linear",
          duration: 1.2,
        }}
        className={cn(
          "relative rounded-full",
          sizes[size]
        )}
      >
        <div
          className="
            absolute
            inset-0
            rounded-full
            border-4
            border-blue-500/20
          "
        />

        <div
          className="
            absolute
            inset-0
            rounded-full
            border-4
            border-transparent
            border-t-blue-500
            shadow-[0_0_24px_rgba(59,130,246,0.6)]
          "
        />
      </motion.div>

      {message && (
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="
            text-center
            text-sm
            text-gray-400
          "
        >
          {message}
        </motion.p>
      )}
    </div>
  );
}