import { useId } from "react"
import { motion, useReducedMotion } from "motion/react"
import { cn } from "@/lib/utils"

type Props = {
  isDark: boolean
  className?: string
}

export function ThemeToggleIcon({ isDark, className }: Props) {
  const rawId = useId().replace(/:/g, "")
  const clipId = `theme-toggle-clip-${rawId}`
  const reducedMotion = useReducedMotion()
  const transition = { ease: "easeInOut" as const, duration: reducedMotion ? 0 : 0.35 }

  return (
    <span
      aria-hidden="true"
      className={cn(
        "inline-flex shrink-0 items-center justify-center rounded-full transition-colors duration-300",
        isDark ? "bg-black text-white" : "bg-white text-black",
        className,
      )}
    >
      <svg viewBox="0 0 32 32" fill="currentColor" strokeLinecap="round" className="size-full">
        <clipPath id={clipId}>
          <motion.path
            initial={false}
            animate={{ y: isDark ? 10 : 0, x: isDark ? -12 : 0 }}
            transition={transition}
            d="M0-5h30a1 1 0 0 0 9 13v24H0Z"
          />
        </clipPath>
        <g clipPath={`url(#${clipId})`}>
          <motion.circle
            initial={false}
            r={isDark ? 10 : 8}
            animate={{ r: isDark ? 10 : 8 }}
            transition={transition}
            cx="16"
            cy="16"
          />
          <motion.g
            initial={false}
            animate={{
              rotate: isDark ? -100 : 0,
              scale: isDark ? 0.5 : 1,
              opacity: isDark ? 0 : 1,
            }}
            transition={transition}
            stroke="currentColor"
            strokeWidth="1.5"
          >
            <path d="M16 5.5v-4" />
            <path d="M16 30.5v-4" />
            <path d="M1.5 16h4" />
            <path d="M26.5 16h4" />
            <path d="m23.4 8.6 2.8-2.8" />
            <path d="m5.7 26.3 2.9-2.9" />
            <path d="m5.8 5.8 2.8 2.8" />
            <path d="m23.4 23.4 2.9 2.9" />
          </motion.g>
        </g>
      </svg>
    </span>
  )
}
