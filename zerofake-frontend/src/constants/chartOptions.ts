import type { ChartOptions } from "chart.js";

export const lineChartOptions: ChartOptions<"line"> = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: false,
    },
    tooltip: {
      intersect: false,
      mode: "index",
    },
  },

  interaction: {
    intersect: false,
    mode: "index",
  },

  scales: {
    x: {
      grid: {
        display: false,
      },
      ticks: {
        color: "#94A3B8",
      },
    },
    y: {
      beginAtZero: true,
      ticks: {
        color: "#94A3B8",
      },
      grid: {
        color: "rgba(148,163,184,0.12)",
      },
    },
  },
};

export const barChartOptions: ChartOptions<"bar"> = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: false,
    },
  },

  scales: {
    x: {
      grid: {
        display: false,
      },
      ticks: {
        color: "#94A3B8",
      },
    },

    y: {
      beginAtZero: true,
      ticks: {
        color: "#94A3B8",
      },
      grid: {
        color: "rgba(148,163,184,0.12)",
      },
    },
  },
};