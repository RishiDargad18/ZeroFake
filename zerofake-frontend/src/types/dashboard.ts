export interface DashboardStatistics {
  productsRegistered: number;
  blockchainTransactions: number;
  productVerifications: number;
  fraudReports: number;
}

export interface DashboardActivity {
  id: number;
  title: string;
  timestamp: string;
  status:
    | "success"
    | "warning"
    | "danger"
    | "info"
    | "neutral";
}