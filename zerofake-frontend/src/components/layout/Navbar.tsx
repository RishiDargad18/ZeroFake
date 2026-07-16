import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  Bell,
  Menu,
  Search,
  UserCircle2,
  LogOut,
} from "lucide-react";
import ThemeToggle from "../ui/ThemeToggle";
import { useAuth } from "@/hooks/useAuth";

interface NavbarProps {
  onMenuClick: () => void;
}

export default function Navbar({
  onMenuClick,
}: NavbarProps) {
  const { user, logout } = useAuth();
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [navbarSearch, setNavbarSearch] = useState("");

  return (
    <motion.header
      initial={{ y: -24, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.35 }}
      className="
        sticky
        top-0
        z-30
        border-b
        border-white/10
        bg-white/10
        backdrop-blur-2xl
      "
    >
      <div className="flex h-20 items-center justify-between px-6">
        {/* Left */}

        <div className="flex items-center gap-4">
          <button
            onClick={onMenuClick}
            className="
              rounded-xl
              p-2
              transition
              hover:bg-white/10
              lg:hidden
            "
          >
            <Menu size={22} />
          </button>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              const q = navbarSearch.trim();
              if (!q) return;
              const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(q);
              if (isUuid) {
                window.location.href = `/timeline?productId=${encodeURIComponent(q)}`;
              } else {
                window.location.href = `/products?search=${encodeURIComponent(q)}`;
              }
            }}
            className="
              hidden
              w-80
              items-center
              gap-3
              rounded-2xl
              border
              border-white/10
              bg-white/5
              px-4
              py-2
              md:flex
              focus-within:border-blue-500
              focus-within:ring-2
              focus-within:ring-blue-500/30
            "
          >
            <Search size={18} className="text-blue-400" />
            <input
              type="text"
              placeholder="Search product name, code or UUID..."
              value={navbarSearch}
              onChange={(e) => setNavbarSearch(e.target.value)}
              className="
                flex-1
                bg-transparent
                text-sm
                text-white
                outline-none
                placeholder:text-gray-400
              "
            />
          </form>
        </div>

        {/* Right */}

        <div className="flex items-center gap-3">
          <button
            className="
              rounded-2xl
              border
              border-white/10
              bg-white/5
              p-3
              transition
              hover:bg-white/10
            "
          >
            <Bell
              size={20}
              className="text-blue-400"
            />
          </button>

          <ThemeToggle />

          <div className="relative">
            <button
              onClick={() => setShowProfileMenu(!showProfileMenu)}
              className="
                flex
                items-center
                gap-3
                rounded-2xl
                border
                border-white/10
                bg-white/5
                px-4
                py-2
                transition
                hover:bg-white/10
              "
            >
              <UserCircle2
                size={32}
                className="text-blue-400"
              />

              <div className="hidden text-left md:block">
                <p className="text-sm font-semibold">
                  {user ? `${user.firstName} ${user.lastName}` : "Guest User"}
                </p>

                <p className="text-xs text-gray-400">
                  {user ? user.role.replace("ROLE_", "") : "Frontend Preview"}
                </p>
              </div>
            </button>

            <AnimatePresence>
              {showProfileMenu && (
                <motion.div
                  initial={{ opacity: 0, y: 10, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 10, scale: 0.95 }}
                  transition={{ duration: 0.15 }}
                  className="
                    absolute
                    right-0
                    mt-2
                    w-48
                    origin-top-right
                    rounded-2xl
                    border
                    border-white/10
                    bg-black/90
                    p-2
                    shadow-xl
                    backdrop-blur-md
                  "
                >
                  <button
                    onClick={() => {
                      setShowProfileMenu(false);
                      void logout();
                    }}
                    className="
                      flex
                      w-full
                      items-center
                      gap-3
                      rounded-xl
                      px-4
                      py-3
                      text-sm
                      text-red-400
                      transition
                      hover:bg-red-500/10
                    "
                  >
                    <LogOut size={16} />
                    <span>Log Out</span>
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </motion.header>
  );
}