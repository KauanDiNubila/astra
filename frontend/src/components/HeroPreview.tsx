import { useRef } from "react"
import type { RefObject } from "react"
import { motion, useReducedMotion, useScroll, useSpring, useTransform } from "motion/react"
import { useTheme } from "@/context/ThemeContext"

const ALT =
  "Dashboard do Astra mostrando tempo de foco de hoje, da semana e total, streak, atividade do GitHub e heatmap do último ano"

function Frame() {
  const { theme } = useTheme()
  const src = theme === "dark" ? "/preview-dashboard-dark.png" : "/preview-dashboard-light.png"

  return (
    <div className="overflow-hidden rounded-xl border border-border shadow-2xl ring-1 ring-black/5 dark:ring-white/10">
      <img
        src={src}
        alt={ALT}
        width={2880}
        height={1800}
        fetchPriority="high"
        className="aspect-square w-full object-cover object-top sm:aspect-[2.12/1]"
      />
    </div>
  )
}

export function HeroPreview({ scrollContainerRef }: { scrollContainerRef: RefObject<HTMLElement | null> }) {
  const sectionRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    container: scrollContainerRef,
    offset: ["start end", "center center"],
  })

  const progress = useSpring(scrollYProgress, { stiffness: 220, damping: 40, mass: 0.4 })
  const rotateX = useTransform(progress, [0, 1], [18, 0])
  const scale = useTransform(progress, [0, 1], [0.94, 1])
  const y = useTransform(progress, [0, 1], [32, 0])

  if (reducedMotion) {
    return (
      <div className="mx-auto w-full max-w-5xl px-4 pb-24">
        <Frame />
      </div>
    )
  }

  return (
    <div ref={sectionRef} className="mx-auto w-full max-w-5xl px-4 pb-24" style={{ perspective: "1400px" }}>
      <motion.div style={{ rotateX, scale, y, transformStyle: "preserve-3d" }}>
        <Frame />
      </motion.div>
    </div>
  )
}
