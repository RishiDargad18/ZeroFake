import { useState } from "react";
import { Outlet } from "react-router-dom";

import Navbar from "../components/layout/Navbar";
import Sidebar from "../components/layout/Sidebar";

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] =
    useState(false);

  return (
    <div className="min-h-screen">
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      <div className="lg:ml-72">
        <Navbar
          onMenuClick={() =>
            setSidebarOpen(true)
          }
        />

        <main
          className="
            min-h-[calc(100vh-80px)]
            p-6
            md:p-8
            lg:p-10
          "
        >
          <Outlet />
        </main>
      </div>
    </div>
  );
}