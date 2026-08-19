import type { CSSProperties } from "react"
import { AnimatePresence, motion, useReducedMotion } from "motion/react"

function FlipChar({ char, reducedMotion }: { char: string; reducedMotion: boolean }) {
  return (
    <span className="relative inline-block w-[1ch] text-center" style={{ perspective: 240 }}>
      <span className="invisible">{char}</span>
      <AnimatePresence initial={false}>
        <motion.span
          key={char}
          initial={reducedMotion ? { opacity: 0 } : { rotateX: -90, opacity: 0 }}
          animate={{ rotateX: 0, opacity: 1 }}
          exit={reducedMotion ? { opacity: 0 } : { rotateX: 90, opacity: 0 }}
          transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
          className="absolute inset-0"
          style={{ transformStyle: "preserve-3d" }}
        >
          {char}
        </motion.span>
      </AnimatePresence>
    </span>
  )
}

type Props = {
  value: string
  className?: string
  style?: CSSProperties
}

export function FlipDigits({ value, className, style }: Props) {
  const reducedMotion = useReducedMotion()
  return (
    <span className={className} style={style}>
      {value.split("").map((char, i) => (
        <FlipChar key={i} char={char} reducedMotion={!!reducedMotion} />
      ))}
    </span>
  )
}
