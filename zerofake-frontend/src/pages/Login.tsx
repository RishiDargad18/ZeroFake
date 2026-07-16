import { useState } from "react";
import { useForm } from "react-hook-form";
import { Eye, EyeOff, AlertCircle } from "lucide-react";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "react-hot-toast";

import logoImg from "@/assets/logo.png";
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

  const [showPassword, setShowPassword] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

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

  const onSubmit = async (data: LoginRequest) => {
    setLoginError(null);
    try {
      await login(data);
      toast.success("Login successful.");
      navigate("/dashboard", { replace: true });
    } catch (error) {
      const errMsg = getApiError(error);
      setLoginError(errMsg);
      toast.error(errMsg);
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
          <img
            src={logoImg}
            alt="ZeroFake Logo"
            className="mx-auto mb-4 h-16 w-16 object-contain"
          />

          <h1 className="text-3xl font-bold">
            ZeroFake
          </h1>

          <p className="mt-2 text-gray-400">
            Sign in to continue
          </p>
        </div>

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="space-y-5"
        >
          {loginError && (
            <div className="flex items-center gap-3 p-4 rounded-2xl bg-red-500/10 border border-red-500/25 text-red-400 text-sm">
              <AlertCircle size={20} className="shrink-0" />
              <span>{loginError}</span>
            </div>
          )}

          <GlassInput
            label="Email"
            type="email"
            placeholder="Enter email"
            error={errors.email?.message}
            disabled={isLoading}
            {...register("email", {
              required: "Email is required",
              pattern: {
                value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                message: "Enter a valid email.",
              },
            })}
          />

          <div className="relative">
            <GlassInput
              label="Password"
              type={showPassword ? "text" : "password"}
              placeholder="Enter password"
              error={errors.password?.message}
              disabled={isLoading}
              {...register("password", {
                required: "Password is required",
              })}
            />

            <button
              type="button"
              onClick={() => setShowPassword((prev) => !prev)}
              className="absolute right-4 top-[46px] text-gray-400"
            >
              {showPassword ? (
                <EyeOff size={18} />
              ) : (
                <Eye size={18} />
              )}
            </button>
          </div>

          <div className="pt-2">
            <GlassButton
              type="submit"
              className="w-full font-semibold"
              loading={isLoading}
              disabled={isLoading}
            >
              Sign In
            </GlassButton>
          </div>
        </form>

        <div className="mt-8 text-center text-sm text-gray-400 border-t border-white/5 pt-4">
          Don't have an account?{" "}
          <Link
            to="/register"
            className="text-blue-400 hover:underline transition"
          >
            Create one
          </Link>
        </div>
      </GlassCard>
    </div>
  );
}