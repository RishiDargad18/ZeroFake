import { motion } from "framer-motion";
import {
  Eye,
  EyeOff,
  AlertCircle,
} from "lucide-react";
import {
  forwardRef,
  useMemo,
  useState,
  type InputHTMLAttributes,
  type ReactNode,
} from "react";

import { cn } from "../../utils/cn";

export interface GlassInputProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, "prefix"> {
  label?: string;
  error?: string;
  helperText?: string;
  prefixIcon?: ReactNode;
  suffixIcon?: ReactNode;
}

const GlassInput = forwardRef<HTMLInputElement, GlassInputProps>(
  (
    {
      label,
      error,
      helperText,
      prefixIcon,
      suffixIcon,
      className,
      disabled,
      type = "text",
      id,
      ...props
    },
    ref
  ) => {
    const [showPassword, setShowPassword] = useState(false);

    const inputType = useMemo(() => {
      if (type !== "password") {
        return type;
      }

      return showPassword ? "text" : "password";
    }, [type, showPassword]);

    const hasPasswordToggle = type === "password";

    return (
      <div className="flex w-full flex-col gap-2">
        {label && (
          <label
            htmlFor={id}
            className="text-sm font-semibold"
          >
            {label}
          </label>
        )}

        <motion.div
          whileFocus={{ scale: 1.01 }}
          className={cn(
            "flex items-center gap-3",
            "rounded-2xl",
            "border",
            "border-white/10",
            "bg-white/10",
            "backdrop-blur-xl",
            "px-4 py-3",
            "transition-all duration-200",
            "focus-within:border-blue-500",
            "focus-within:ring-2",
            "focus-within:ring-blue-500/30",
            disabled && "cursor-not-allowed opacity-60",
            error &&
              "border-red-500 ring-2 ring-red-500/20",
            className
          )}
        >
          {prefixIcon && (
            <span className="text-blue-400">
              {prefixIcon}
            </span>
          )}

          <input
            ref={ref}
            id={id}
            type={inputType}
            disabled={disabled}
            className={cn(
              "flex-1",
              "bg-transparent",
              "outline-none",
              "placeholder:text-gray-400",
              "disabled:cursor-not-allowed"
            )}
            {...props}
          />

          {suffixIcon && !hasPasswordToggle && (
            <span className="text-gray-400">
              {suffixIcon}
            </span>
          )}

          {hasPasswordToggle && (
            <button
              type="button"
              onClick={() =>
                setShowPassword((value) => !value)
              }
              className="
                rounded-lg
                p-1
                transition
                hover:bg-white/10
              "
            >
              {showPassword ? (
                <EyeOff
                  size={18}
                  className="text-gray-400"
                />
              ) : (
                <Eye
                  size={18}
                  className="text-gray-400"
                />
              )}
            </button>
          )}
        </motion.div>

        {error ? (
          <div className="flex items-center gap-2 text-sm text-red-400">
            <AlertCircle size={16} />

            <span>{error}</span>
          </div>
        ) : (
          helperText && (
            <p className="text-sm text-gray-400">
              {helperText}
            </p>
          )
        )}
      </div>
    );
  }
);

GlassInput.displayName = "GlassInput";

export default GlassInput;