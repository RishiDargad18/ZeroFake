import {
  AlertTriangle,
  Boxes,
  Package,
  ShieldCheck,
  type LucideIcon,
} from "lucide-react";

import type { BadgeVariant } from "@/components/ui/GlassBadge";

export interface DashboardStatistic {
  id: number;
  title: string;
  value: number;
  icon: LucideIcon;
  trend: "up" | "down" | "neutral";
  percentage: number;
}

export interface DashboardActivity {
  id: number;
  title: string;
  timestamp: string;
  status: BadgeVariant;
}

export const DASHBOARD_STATISTICS: DashboardStatistic[] = [
  {
    id: 1,
    title: "Products Registered",
    value: 1250,
    icon: Package,
    trend: "up",
    percentage: 12.4,
  },
  {
    id: 2,
    title: "Blockchain Transactions",
    value: 4875,
    icon: Boxes,
    trend: "up",
    percentage: 18.7,
  },
  {
    id: 3,
    title: "Product Verifications",
    value: 932,
    icon: ShieldCheck,
    trend: "up",
    percentage: 9.3,
  },
  {
    id: 4,
    title: "Fraud Reports",
    value: 17,
    icon: AlertTriangle,
    trend: "down",
    percentage: 4.1,
  },
];

export const RECENT_ACTIVITY: DashboardActivity[] = [
  {
    id: 1,
    title: "Product ZX-102 registered",
    timestamp: "2 minutes ago",
    status: "success",
  },
  {
    id: 2,
    title: "Ownership transferred",
    timestamp: "15 minutes ago",
    status: "info",
  },
  {
    id: 3,
    title: "Verification completed",
    timestamp: "1 hour ago",
    status: "success",
  },
  {
    id: 4,
    title: "Fraud alert generated",
    timestamp: "3 hours ago",
    status: "warning",
  },
];