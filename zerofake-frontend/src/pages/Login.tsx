import { useState } from "react";
import { useForm } from "react-hook-form";
import { Eye, EyeOff, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";

import { useAuth } from "@/hooks/useAuth";

import { getApiError } from "@/utils/apiError";

import {
  GlassButton,
  GlassCard,
  GlassInput,
} from "@/components/ui";

import type { LoginRequest } from "@/types/auth";

export default function Login() {
  const navigate = useNavigate();

  const {
    login,
    isLoading,
  } = useAuth();

  const [showPassword, setShowPassword] =
    useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>({
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (
    data: LoginRequest
  ) => {
    try {
      await login(data);

      toast.success(
        "Login successful."
      );

      navigate("/dashboard", {
        replace: true,
      });
    } catch (error) {
      toast.error(
        getApiError(error)
      );
    }
  };

  return (
    <div
      className="
        flex
        min-h-screen
        items-center
        justify-center
        bg-gradient-to-br
        from-slate-950
        via-slate-900
        to-slate-950
        p-6
      "
    >
      <GlassCard className="w-full max-w-md p-8">
        <div className="mb-8 text-center">
          <ShieldCheck
            size={56}
            className="mx-auto mb-4 text-blue-400"
          />

          <h1 className="text-3xl font-bold">
            ZeroFake
          </h1>

          <p className="mt-2 text-gray-400">
            Sign in to continue
          </p>
        </div>

        <form
          onSubmit={handleSubmit(
            onSubmit
          )}
          className="space-y-5"
        >
          <GlassInput
            label="Email"
            type="email"
            placeholder="Enter email"
            error={errors.email?.message}
            disabled={isLoading}
            {...register("email", {
              required:
                "Email is required",
              pattern: {
                value:
                  /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message:
                  "Enter a valid email.",
              },
            })}
          />

          <div className="relative">
            <GlassInput
              label="Password"
              type={
                showPassword
                  ? "text"
                  : "password"
              }
              placeholder="Enter password"
              error={
                errors.password
                  ?.message
              }
              disabled={isLoading}
              {...register(
                "password",
                {
                  required:
                    "Password is required",
                }
              )}
            />

            <button
              type="button"
              onClick={() =>
                setShowPassword(
                  (previous) =>
                    !previous
                )
              }
              className="
                absolute
                right-4
                top-[46px]
                text-gray-400
              "
            >
              {showPassword ? (
                <EyeOff size={18} />
              ) : (
                <Eye size={18} />
              )}
            </button>
          </div>

          <GlassButton
            type="submit"
            className="w-full"
            loading={isLoading}
            disabled={isLoading}
          >
            Sign In
          </GlassButton>
        </form>
      </GlassCard>
    </div>
  );
}