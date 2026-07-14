import { AlertCircle, ChevronDown } from "lucide-react";
import {
  forwardRef,
  type SelectHTMLAttributes,
} from "react";

import { cn } from "../../utils/cn";

import type { SelectOption } from "@/types/common";

export interface GlassSelectProps
  extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  helperText?: string;
  placeholder?: string;
  options: SelectOption[];
}

const GlassSelect = forwardRef<
  HTMLSelectElement,
  GlassSelectProps
>(
  (
    {
      id,
      label,
      error,
      helperText,
      placeholder,
      options,
      disabled,
      className,
      ...props
    },
    ref
  ) => {
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

        <div
          className={cn(
            "relative",
            "rounded-2xl",
            "border border-white/10",
            "bg-white/10",
            "backdrop-blur-xl",
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
          <select
            ref={ref}
            id={id}
            disabled={disabled}
            className={cn(
              "h-12 w-full appearance-none",
              "rounded-2xl",
              "bg-transparent",
              "px-4 pr-12",
              "outline-none",
              "disabled:cursor-not-allowed",
              className
            )}
            {...props}
          >
            {placeholder && (
              <option value="" disabled>
              {placeholder}
            </option>
            )}

            {options.map((option) => (
              <option
                key={option.value}
                value={option.value}
                className="bg-slate-900 text-white"
              >
                {option.label}
              </option>
            ))}
          </select>

          <ChevronDown
            size={18}
            className="
              pointer-events-none
              absolute
              right-4
              top-1/2
              -translate-y-1/2
              text-blue-400
            "
          />
        </div>

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

GlassSelect.displayName = "GlassSelect";

export default GlassSelect;