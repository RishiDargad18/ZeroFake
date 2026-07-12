import type { ChartData } from "chart.js";

export const verificationTrendData: ChartData<"line"> = {
  labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
  datasets: [
    {
      label: "Verifications",
      data: [95, 120, 140, 165, 182, 210, 245],
      borderColor: "#3B82F6",
      backgroundColor: "rgba(59,130,246,0.18)",
      tension: 0.4,
      fill: true,
      pointRadius: 4,
      pointHoverRadius: 6,
    },
  ],
};

export const fraudTrendData: ChartData<"bar"> = {
  labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
  datasets: [
    {
      label: "Fraud Reports",
      data: [4, 3, 5, 2, 6, 3, 2],
      backgroundColor: [
        "#2563EB",
        "#3B82F6",
        "#60A5FA",
        "#2563EB",
        "#3B82F6",
        "#60A5FA",
        "#2563EB",
      ],
      borderRadius: 8,
      maxBarThickness: 40,
    },
  ],
};