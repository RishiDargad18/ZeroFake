import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import AppLayout from "@/layouts/AppLayout";

import Dashboard from "@/pages/Dashboard";
import FraudReports from "@/pages/FraudReports";
import Login from "@/pages/Login";
import Products from "@/pages/Products";
import VerifyProduct from "@/pages/VerifyProduct";
import Blockchain from "@/pages/Blockchain";
import ProductTimeline from "@/pages/ProductTimeline";
import Settings from "@/pages/Settings";
import Register from "@/pages/Register";

import ProtectedRoute from "@/routes/ProtectedRoute";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        <Route element={<AppLayout />}>
        
          <Route
            index
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />

<Route
  path="/dashboard"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
        "ROLE_WAREHOUSE",
        "ROLE_DISTRIBUTOR",
        "ROLE_RETAILER",
        "ROLE_CUSTOMER",
      ]}
    >
      <Dashboard />
    </ProtectedRoute>
  }
/>

          <Route
  path="/products"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
      ]}
    >
      <Products />
    </ProtectedRoute>
  }
/>

     <Route
  path="/blockchain"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
      ]}
    >
      <Blockchain />
    </ProtectedRoute>
  }
/>

<Route
  path="/verify"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
        "ROLE_WAREHOUSE",
        "ROLE_DISTRIBUTOR",
        "ROLE_RETAILER",
        "ROLE_CUSTOMER",
      ]}
    >
      <VerifyProduct />
    </ProtectedRoute>
  }
/>

<Route
  path="/fraud"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
      ]}
    >
      <FraudReports />
    </ProtectedRoute>
  }
/>
<Route
  path="/timeline"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
        "ROLE_WAREHOUSE",
        "ROLE_DISTRIBUTOR",
        "ROLE_RETAILER",
        "ROLE_CUSTOMER",
      ]}
    >
      <ProductTimeline />
    </ProtectedRoute>
  }
/>

<Route
  path="/settings"
  element={
    <ProtectedRoute
      allowedRoles={[
        "ROLE_ADMIN",
        "ROLE_MANUFACTURER",
        "ROLE_WAREHOUSE",
        "ROLE_DISTRIBUTOR",
        "ROLE_RETAILER",
        "ROLE_CUSTOMER",
      ]}
    >
      <Settings />
    </ProtectedRoute>
  }
/>

          <Route
            path="*"
            element={
              <Navigate
                to="/dashboard"
                replace
              />
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}