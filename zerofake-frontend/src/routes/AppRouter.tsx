import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";
import Products from "@/pages/Products";
import Dashboard from "@/pages/Dashboard";
import PagePlaceholder from "@/components/ui/PagePlaceholder";
import AppLayout from "@/layouts/AppLayout";

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route
            index
            element={<Navigate to="/dashboard" replace />}
          />

          <Route
            path="/dashboard"
            element={<Dashboard />}
          />

          <Route
          path="/products"
          element={<Products />}
        />

          <Route
            path="/blockchain"
            element={
              <PagePlaceholder
                title="Blockchain"
                description="Blockchain operations will be implemented in a future milestone."
              />
            }
          />

          <Route
            path="/verify"
            element={
              <PagePlaceholder
                title="Verify Product"
                description="Product verification UI will be implemented in a future milestone."
              />
            }
          />

          <Route
            path="/fraud"
            element={
              <PagePlaceholder
                title="Fraud Reports"
                description="Fraud reporting will be implemented in a future milestone."
              />
            }
          />

          <Route
            path="/timeline"
            element={
              <PagePlaceholder
                title="Product Timeline"
                description="Animated blockchain ownership timeline will be implemented in a future milestone."
              />
            }
          />

          <Route
            path="/settings"
            element={
              <PagePlaceholder
                title="Settings"
                description="Settings page will be implemented in a future milestone."
              />
            }
          />

          <Route
            path="*"
            element={<Navigate to="/dashboard" replace />}
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}