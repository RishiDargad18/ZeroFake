import { motion, AnimatePresence } from "framer-motion";
import {
  X,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";

import logoImg from "@/assets/logo.png";
import { useAuth } from "@/hooks/useAuth";
interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function Sidebar({
  isOpen,
  onClose,
}: SidebarProps) {
  const { user } = useAuth();

const visibleNavigationItems =
  navigationItems.filter((item) =>
    user
      ? item.roles.includes(user.role)
      : false
  );

  const sidebarContent = (
    <aside
      className="
        flex
        h-full
        w-72
        flex-col
        border-r
        border-white/10
        bg-white/10
        backdrop-blur-2xl
        supports-[backdrop-filter]:bg-white/5
      "
    >
      {/* Logo */}

      <div
        className="
          flex
          items-center
          justify-between
          border-b
          border-white/10
          px-6
          py-6
        "
      >
        <div className="flex items-center gap-3">
          <img
            src={logoImg}
            alt="ZeroFake Logo"
            className="h-12 w-12 object-contain"
          />

          <div>
            <h1 className="text-xl font-bold bg-gradient-to-r from-blue-400 to-indigo-400 bg-clip-text text-transparent">
              ZeroFake
            </h1>

            <p className="text-[9px] tracking-widest font-semibold text-blue-400">
              VERIFY. TRUST. PROTECT.
            </p>
          </div>
        </div>

        <button
          onClick={onClose}
          className="
            rounded-xl
            p-2
            transition
            hover:bg-white/10
            lg:hidden
          "
        >
          <X size={18} />
        </button>
      </div>

      {/* Navigation */}

      <nav className="flex-1 space-y-2 p-4">
        {visibleNavigationItems.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={onClose}
              className={({ isActive }) =>
                `
                group
                relative
                flex
                items-center
                gap-4
                overflow-hidden
                rounded-2xl
                px-4
                py-3
                transition-all
                duration-300

                ${
                  isActive
                    ? "bg-blue-600 text-white shadow-lg shadow-blue-500/25"
                    : "hover:bg-white/10"
                }
                `
              }
            >
              {({ isActive }) => (
                <motion.div
                  whileHover={{ x: 4 }}
                  whileTap={{ scale: 0.98 }}
                  className="flex w-full items-center gap-4"
                >
                  <Icon
                    size={20}
                    className={
                      isActive
                        ? "text-white"
                        : "text-blue-400"
                    }
                  />

                  <span className="font-medium">
                    {item.label}
                  </span>

                  {isActive && (
                    <motion.div
                      layoutId="active-sidebar-item"
                      className="
                        absolute
                        left-0
                        top-3
                        h-8
                        w-1
                        rounded-full
                        bg-white
                      "
                    />
                  )}
                </motion.div>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Footer Removed */}
    </aside>
  );

  return (
    <>
      {/* Desktop */}

      <div
        className="
          fixed
          inset-y-0
          left-0
          z-40
          hidden
          lg:block
        "
      >
        {sidebarContent}
      </div>

      {/* Mobile */}

      <AnimatePresence>
        {isOpen && (
          <>
            <motion.div
              className="
                fixed
                inset-0
                z-40
                bg-black/50
                backdrop-blur-sm
                lg:hidden
              "
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={onClose}
            />

            <motion.div
              className="
                fixed
                inset-y-0
                left-0
                z-50
                lg:hidden
              "
              initial={{ x: -320 }}
              animate={{ x: 0 }}
              exit={{ x: -320 }}
              transition={{
                type: "spring",
                stiffness: 260,
                damping: 26,
              }}
            >
              {sidebarContent}
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  );
}