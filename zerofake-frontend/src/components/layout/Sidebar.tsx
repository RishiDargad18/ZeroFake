import { motion, AnimatePresence } from "framer-motion";
import {
  ShieldCheck,
  X,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";

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
          <div
            className="
              flex
              h-12
              w-12
              items-center
              justify-center
              rounded-2xl
              bg-blue-600
              text-white
              shadow-lg
              shadow-blue-600/30
            "
          >
            <ShieldCheck size={24} />
          </div>

          <div>
            <h1 className="text-xl font-bold">
              ZeroFake
            </h1>

            <p className="text-xs text-gray-400">
              Supply Chain Platform
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

      {/* Footer */}

      <div
        className="
          border-t
          border-white/10
          p-5
        "
      >
        <div
          className="
            rounded-2xl
            border
            border-white/10
            bg-white/5
            p-4
            text-center
            shadow-[6px_6px_18px_rgba(0,0,0,0.15),-6px_-6px_18px_rgba(255,255,255,0.03)]
          "
        >
          <p className="text-sm font-semibold">
            ZeroFake
          </p>

          <p className="mt-1 text-xs text-gray-400">
            Blockchain Verification Platform
          </p>
        </div>
      </div>
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