import * as React from "react"
import { motion, useScroll, useSpring } from "motion/react"

import { cn } from "@/lib/utils"

type ScrollProgressProps = {
  containerRef: React.RefObject<HTMLElement | null>
  axis?: "x" | "y"
  className?: string
}

function ScrollProgress({ containerRef, axis = "x", className }: ScrollProgressProps) {
  const { scrollXProgress, scrollYProgress } = useScroll({ container: containerRef })
  const progress = useSpring(axis === "x" ? scrollXProgress : scrollYProgress, {
    stiffness: 280,
    damping: 18,
    mass: 0.3,
  })

  return (
    <motion.div
      data-slot="scroll-progress"
      className={cn(
        "pointer-events-none absolute z-10 rounded-full bg-primary",
        axis === "x" ? "bottom-0 left-0 h-0.5 w-full origin-left" : "right-0 top-0 h-full w-0.5 origin-top",
        className
      )}
      style={axis === "x" ? { scaleX: progress } : { scaleY: progress }}
    />
  )
}

export { ScrollProgress }
