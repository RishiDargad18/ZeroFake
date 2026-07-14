import {
  AlertTriangle,
  Boxes,
  GitBranch,
  LayoutDashboard,
  Package,
  Settings,
  ShieldCheck,
} from "lucide-react";

import type { UserRole } from "@/types/auth";

export interface NavigationItem {
  label: string;
  path: string;
  icon: React.ElementType;
  roles: UserRole[];
}

export const navigationItems: NavigationItem[] = [
  {
    label: "Dashboard",
    path: "/dashboard",
    icon: LayoutDashboard,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
      "ROLE_WAREHOUSE",
      "ROLE_DISTRIBUTOR",
      "ROLE_RETAILER",
      "ROLE_CUSTOMER",
    ],
  },
  {
    label: "Products",
    path: "/products",
    icon: Package,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
    ],
  },
  {
    label: "Blockchain",
    path: "/blockchain",
    icon: Boxes,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
    ],
  },
  {
    label: "Verify Product",
    path: "/verify",
    icon: ShieldCheck,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
      "ROLE_WAREHOUSE",
      "ROLE_DISTRIBUTOR",
      "ROLE_RETAILER",
      "ROLE_CUSTOMER",
    ],
  },
  {
    label: "Fraud Reports",
    path: "/fraud",
    icon: AlertTriangle,
    roles: [
      "ROLE_ADMIN",
    ],
  },
  {
    label: "Product Timeline",
    path: "/timeline",
    icon: GitBranch,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
      "ROLE_WAREHOUSE",
      "ROLE_DISTRIBUTOR",
      "ROLE_RETAILER",
      "ROLE_CUSTOMER",
    ],
  },
  {
    label: "Settings",
    path: "/settings",
    icon: Settings,
    roles: [
      "ROLE_ADMIN",
      "ROLE_MANUFACTURER",
      "ROLE_WAREHOUSE",
      "ROLE_DISTRIBUTOR",
      "ROLE_RETAILER",
      "ROLE_CUSTOMER",
    ],
  },
];