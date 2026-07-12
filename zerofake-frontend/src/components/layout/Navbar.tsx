import { motion } from "framer-motion";
import {
  Bell,
  Menu,
  Search,
  UserCircle2,
} from "lucide-react";
import ThemeToggle from "../ui/ThemeToggle";

interface NavbarProps {
  onMenuClick: () => void;
}

export default function Navbar({
  onMenuClick,
}: NavbarProps) {
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

          <div
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
              py-3
              md:flex
            "
          >
            <Search size={18} className="text-blue-400" />

            <span className="text-sm text-gray-400">
              Search...
            </span>
          </div>
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

          <button
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
                Guest User
              </p>

              <p className="text-xs text-gray-400">
                Frontend Preview
              </p>
            </div>
          </button>
        </div>
      </div>
    </motion.header>
  );
}