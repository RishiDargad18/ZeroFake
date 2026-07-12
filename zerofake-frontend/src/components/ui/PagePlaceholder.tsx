import { motion } from "framer-motion";

interface PagePlaceholderProps {
  title: string;
  description: string;
}

export default function PagePlaceholder({
  title,
  description,
}: PagePlaceholderProps) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35 }}
      className="flex min-h-[70vh] items-center justify-center"
    >
      <div
        className="
          glass
          neumorphism
          w-full
          max-w-3xl
          rounded-3xl
          p-10
          text-center
        "
      >
        <div
          className="
            mx-auto
            mb-6
            h-20
            w-20
            rounded-full
            bg-blue-600/20
            flex
            items-center
            justify-center
          "
        >
          <div className="h-8 w-8 rounded-full bg-blue-500" />
        </div>

        <h1 className="text-4xl font-bold">
          {title}
        </h1>

        <p className="mt-5 text-lg text-gray-400">
          {description}
        </p>
      </div>
    </motion.section>
  );
}