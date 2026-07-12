import { AnimatePresence, motion } from "framer-motion";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "../../contexts/ThemeContext";

export default function ThemeToggle() {
  const { isDark, toggleTheme } = useTheme();

  return (
    <button
      type="button"
      aria-label="Toggle theme"
      aria-pressed={isDark}
      onClick={toggleTheme}
      className="
        glass
        relative
        flex
        h-11
        w-11
        items-center
        justify-center
        rounded-full
        transition-all
        duration-300
        hover:scale-105
        hover:shadow-lg
        focus:outline-none
        focus:ring-2
        focus:ring-blue-500
      "
    >
      <AnimatePresence mode="wait">
        <motion.div
          key={isDark ? "moon" : "sun"}
          initial={{ opacity: 0, rotate: -90, scale: 0.6 }}
          animate={{ opacity: 1, rotate: 0, scale: 1 }}
          exit={{ opacity: 0, rotate: 90, scale: 0.6 }}
          transition={{ duration: 0.2 }}
        >
          {isDark ? (
            <Moon size={20} className="text-blue-400" />
          ) : (
            <Sun size={20} className="text-amber-500" />
          )}
        </motion.div>
      </AnimatePresence>
    </button>
  );
}