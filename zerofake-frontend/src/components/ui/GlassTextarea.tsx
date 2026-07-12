import { motion } from "framer-motion";
import { AlertCircle } from "lucide-react";
import {
  forwardRef,
  useEffect,
  useRef,
  type ReactNode,
  type TextareaHTMLAttributes,
} from "react";

import { cn } from "../../utils/cn";

export interface GlassTextareaProps
  extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  autoResize?: boolean;
  startIcon?: ReactNode;
}

const GlassTextarea = forwardRef<
  HTMLTextAreaElement,
  GlassTextareaProps
>(
  (
    {
      id,
      label,
      error,
      helperText,
      autoResize = false,
      startIcon,
      disabled,
      className,
      onChange,
      value,
      ...props
    },
    ref
  ) => {
    const internalRef =
      useRef<HTMLTextAreaElement>(null);

    const textareaRef =
      (ref as React.RefObject<HTMLTextAreaElement>) ??
      internalRef;

    useEffect(() => {
      if (!autoResize || !textareaRef.current) {
        return;
      }

      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }, [value, autoResize, textareaRef]);

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
            "flex items-start gap-3",
            "rounded-2xl",
            "border border-white/10",
            "bg-white/10",
            "backdrop-blur-xl",
            "px-4 py-3",
            "transition-all duration-200",
            "focus-within:border-blue-500",
            "focus-within:ring-2",
            "focus-within:ring-blue-500/30",
            disabled &&
              "cursor-not-allowed opacity-60",
            error &&
              "border-red-500 ring-2 ring-red-500/20"
          )}
        >
          {startIcon && (
            <div className="mt-1 text-blue-400">
              {startIcon}
            </div>
          )}

          <textarea
            {...props}
            id={id}
            ref={textareaRef}
            value={value}
            disabled={disabled}
            onChange={onChange}
            className={cn(
              "min-h-32 w-full",
              "resize-none",
              "bg-transparent",
              "outline-none",
              "placeholder:text-gray-400",
              "disabled:cursor-not-allowed",
              className
            )}
          />
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

GlassTextarea.displayName = "GlassTextarea";

export default GlassTextarea;