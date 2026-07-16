import { useState } from "react";
import { useForm } from "react-hook-form";
import { Eye, EyeOff, ShieldCheck, ArrowLeft, AlertCircle } from "lucide-react";
import { useNavigate, Link } from "react-router-dom";
import { toast } from "react-hot-toast";
import logoImg from "@/assets/logo.png";
import { authService } from "@/services/authService";
import { getApiError } from "@/utils/apiError";
import {
  GlassButton,
  GlassCard,
  GlassInput,
  GlassSelect,
} from "@/components/ui";
import type { RegisterRequest } from "@/types/auth";

export default function Register() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isRegistered, setIsRegistered] = useState(false);
  const [registerError, setRegisterError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterRequest>({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      role: "ROLE_CUSTOMER",
    },
  });

  const onSubmit = async (data: RegisterRequest) => {
    setIsLoading(true);
    setRegisterError(null);
    try {
      await authService.register(data);
      toast.success("Account registered successfully!");
      setIsRegistered(true);
    } catch (error) {
      const errMsg = getApiError(error);
      setRegisterError(errMsg);
      toast.error(errMsg);
    } finally {
      setIsLoading(false);
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
      {isRegistered ? (
        <GlassCard className="w-full max-w-lg p-8 text-center space-y-6">
          <div className="flex h-20 w-20 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-400 mx-auto border border-emerald-500/20 shadow-lg shadow-emerald-500/5 animate-pulse">
            <ShieldCheck size={48} />
          </div>

          <div className="space-y-2">
            <h1 className="text-3xl font-bold text-white">Registration Successful!</h1>
            <p className="text-gray-400">
              Your platform account has been successfully created and seeded on the registry.
            </p>
          </div>

          <div className="bg-white/5 border border-white/10 rounded-2xl p-5 text-sm text-gray-300">
            Please log in using the email and password credentials you registered.
          </div>

          <div className="pt-2">
            <GlassButton
              onClick={() => navigate("/login")}
              className="w-full font-semibold"
            >
              Proceed to Login
            </GlassButton>
          </div>
        </GlassCard>
      ) : (
        <GlassCard className="w-full max-w-lg p-8">
          <div className="mb-6 text-center">
            <img
              src={logoImg}
              alt="ZeroFake Logo"
              className="mx-auto mb-4 h-16 w-16 object-contain"
            />

            <h1 className="text-3xl font-bold">
              ZeroFake
            </h1>

            <p className="mt-2 text-gray-400">
              Create a new platform account
            </p>
          </div>

          <form
            onSubmit={handleSubmit(onSubmit)}
            className="space-y-5"
          >
            {registerError && (
              <div className="flex items-center gap-3 p-4 rounded-2xl bg-red-500/10 border border-red-500/25 text-red-400 text-sm">
                <AlertCircle size={20} className="shrink-0" />
                <span>{registerError}</span>
              </div>
            )}

            <div className="grid gap-4 sm:grid-cols-2">
              <GlassInput
                label="First Name"
                placeholder="e.g. John"
                error={errors.firstName?.message}
                disabled={isLoading}
                {...register("firstName", {
                  required: "First name is required",
                })}
              />

              <GlassInput
                label="Last Name"
                placeholder="e.g. Doe"
                error={errors.lastName?.message}
                disabled={isLoading}
                {...register("lastName", {
                  required: "Last name is required",
                })}
              />
            </div>

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
                  message: "Enter a valid email address.",
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
                  pattern: {
                    value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,64}$/,
                    message: "Must be 8-64 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char.",
                  },
                })}
              />

              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                className="absolute right-4 top-[46px] text-gray-400"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            <GlassSelect
              label="Account Role"
              error={errors.role?.message}
              disabled={isLoading}
              options={[
                { label: "Manufacturer", value: "ROLE_MANUFACTURER" },
                { label: "Warehouse Operator", value: "ROLE_WAREHOUSE" },
                { label: "Distributor", value: "ROLE_DISTRIBUTOR" },
                { label: "Retailer", value: "ROLE_RETAILER" },
                { label: "Customer", value: "ROLE_CUSTOMER" },
              ]}
              {...register("role", {
                required: "Role selection is required",
              })}
            />

            <div className="pt-2">
              <GlassButton
                type="submit"
                className="w-full font-semibold"
                loading={isLoading}
                disabled={isLoading}
              >
                Register Account
              </GlassButton>
            </div>
          </form>

          <div className="mt-6 text-center text-sm text-gray-400 border-t border-white/5 pt-4">
            <Link
              to="/login"
              className="inline-flex items-center gap-2 text-blue-400 hover:text-blue-300 transition"
            >
              <ArrowLeft size={16} />
              Back to Sign In
            </Link>
          </div>
        </GlassCard>
      )}
    </div>
  );
}
