import { motion, useReducedMotion } from "motion/react"

const BEAM_BACKGROUND = "linear-gradient(to bottom, var(--foreground) 0%, transparent 72%)"
const GLOW_BACKGROUND = "radial-gradient(ellipse at 50% 100%, var(--foreground) 0%, transparent 70%)"
const LIGHT_WASH_BACKGROUND = "linear-gradient(to bottom, var(--muted) 0%, transparent 100%)"

export function Spotlight() {
  const reducedMotion = useReducedMotion()

  return (
    <motion.div
      aria-hidden
      initial={reducedMotion ? false : { opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1.8, ease: [0.22, 1, 0.36, 1] }}
      className="pointer-events-none absolute inset-x-0 top-0 h-[52rem] overflow-hidden"
    >
      <div
        className="absolute inset-x-0 top-0 h-[34rem] dark:hidden"
        style={{ background: LIGHT_WASH_BACKGROUND }}
      />
      <div
        className="absolute left-1/2 top-[-24rem] hidden h-[46rem] w-[64rem] -translate-x-1/2 opacity-[0.26] blur-[100px] dark:block"
        style={{ background: GLOW_BACKGROUND }}
      />
      <div
        className="absolute -left-24 top-[-16rem] hidden h-[46rem] w-[22rem] rotate-[32deg] opacity-[0.14] blur-[80px] dark:block"
        style={{ background: BEAM_BACKGROUND }}
      />
      <div
        className="absolute -right-24 top-[-16rem] hidden h-[46rem] w-[22rem] -rotate-[32deg] opacity-[0.14] blur-[80px] dark:block"
        style={{ background: BEAM_BACKGROUND }}
      />
    </motion.div>
  )
}
