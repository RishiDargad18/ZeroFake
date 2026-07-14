import { Navigate } from "react-router-dom";

import {
  GlassButton,
  GlassCard,
} from "@/components/ui";

import { useAuth } from "@/hooks/useAuth";

import type { UserRole } from "@/types/auth";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: UserRole[];
}

export default function ProtectedRoute({
  children,
  allowedRoles,
}: ProtectedRouteProps) {
  const {
    user,
    isAuthenticated,
    isLoading,
  } = useAuth();

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  if (
    allowedRoles &&
    user &&
    !allowedRoles.includes(user.role)
  ) {
    return (
      <div
        className="
          flex
          min-h-screen
          items-center
          justify-center
          p-6
        "
      >
        <GlassCard className="max-w-lg p-10 text-center">
          <h1 className="text-3xl font-bold">
            Access Denied
          </h1>

          <p className="mt-4 text-gray-400">
            You do not have permission to
            access this page.
          </p>

          <GlassButton
            className="mt-8"
            onClick={() => {
              window.location.href =
                "/dashboard";
            }}
          >
            Back to Dashboard
          </GlassButton>
        </GlassCard>
      </div>
    );
  }

  return <>{children}</>;
}