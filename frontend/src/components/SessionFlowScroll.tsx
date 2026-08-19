import { useRef } from "react"
import type { RefObject } from "react"
import { motion, useReducedMotion, useScroll, useSpring, useTransform } from "motion/react"

// Visualiza a ideia central do Astra: uma sessão (o tronco) é a unidade que
// conecta todas as áreas do app — os ramos usam os mesmos nomes do menu.

const WIDTH = 1200
const TRUNK_TOP_Y = 40
const TRUNK_BOTTOM_Y = 420
const BRANCH_END_Y = 760
const LABEL_Y = 800

const branches = [
  { label: "Dashboard", x: 80 },
  { label: "Sessões", x: 288 },
  { label: "Metas", x: 496 },
  { label: "Cursos", x: 704 },
  { label: "Roadmaps", x: 912 },
  { label: "Ranking", x: 1120 },
]

const trunkPath = `M ${WIDTH / 2} ${TRUNK_TOP_Y} L ${WIDTH / 2} ${TRUNK_BOTTOM_Y}`

function branchPath(endX: number) {
  const startX = WIDTH / 2
  const midY = (TRUNK_BOTTOM_Y + BRANCH_END_Y) / 2
  return `M ${startX} ${TRUNK_BOTTOM_Y} C ${startX} ${midY} ${endX} ${midY} ${endX} ${BRANCH_END_Y}`
}

type Props = {
  scrollContainerRef: RefObject<HTMLElement | null>
}

export function SessionFlowScroll({ scrollContainerRef }: Props) {
  const sectionRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    container: scrollContainerRef,
    offset: ["start start", "end end"],
  })

  const trunkLength = useSpring(useTransform(scrollYProgress, [0, 0.45], [0, 1]), {
    stiffness: 300,
    damping: 40,
  })
  const branchLength = useSpring(useTransform(scrollYProgress, [0.4, 0.9], [0, 1]), {
    stiffness: 300,
    damping: 40,
  })
  const labelOpacity = useTransform(scrollYProgress, [0.75, 1], [0, 1])
  const trunkDotOpacity = useTransform(trunkLength, (v) => (v > 0 ? 1 : 0))
  const barProgress = useSpring(scrollYProgress, { stiffness: 280, damping: 18, mass: 0.3 })

  if (reducedMotion) {
    return (
      <section className="mx-auto flex max-w-4xl flex-col items-center gap-10 px-4 py-24 text-center">
        <p className="max-w-md text-balance text-muted-foreground">
          Toda hora de foco vira uma sessão. Dashboard, sessões, metas, cursos,
          roadmaps e ranking — tudo se conecta a partir dela.
        </p>
        <div className="flex flex-wrap justify-center gap-3">
          {branches.map((b) => (
            <span key={b.label} className="rounded-full border px-4 py-1.5 text-sm">
              {b.label}
            </span>
          ))}
        </div>
      </section>
    )
  }

  return (
    <section ref={sectionRef} className="relative h-[220vh] w-full">
      <div className="sticky top-0 flex h-svh w-full items-center justify-center overflow-hidden">
        <div
          aria-hidden
          className="absolute right-6 top-1/2 h-40 w-0.5 -translate-y-1/2 overflow-hidden rounded-full bg-muted lg:right-10"
        >
          <motion.div
            className="w-full origin-top rounded-full bg-primary"
            style={{ scaleY: barProgress, height: "100%" }}
          />
        </div>
        <svg
          viewBox={`0 0 ${WIDTH} ${LABEL_Y + 60}`}
          className="h-[85vh] w-auto max-w-[95vw]"
          fill="none"
        >
          <motion.path
            d={trunkPath}
            stroke="currentColor"
            className="text-primary"
            strokeWidth={3}
            strokeLinecap="round"
            style={{ pathLength: trunkLength }}
          />
          <motion.circle
            cx={WIDTH / 2}
            cy={TRUNK_TOP_Y}
            r={7}
            className="fill-primary"
            style={{ opacity: trunkDotOpacity }}
          />
          <text
            x={WIDTH / 2}
            y={TRUNK_TOP_Y - 20}
            textAnchor="middle"
            className="fill-foreground text-[22px] font-medium"
          >
            Sessão
          </text>

          {branches.map((b) => (
            <motion.path
              key={b.label}
              d={branchPath(b.x)}
              stroke="currentColor"
              className="text-border"
              strokeWidth={2}
              strokeLinecap="round"
              style={{ pathLength: branchLength }}
            />
          ))}

          {branches.map((b) => (
            <motion.g key={b.label} style={{ opacity: labelOpacity }}>
              <circle cx={b.x} cy={BRANCH_END_Y} r={5} className="fill-foreground" />
              <text
                x={b.x}
                y={LABEL_Y}
                textAnchor="middle"
                className="fill-muted-foreground text-[18px]"
              >
                {b.label}
              </text>
            </motion.g>
          ))}
        </svg>
      </div>
    </section>
  )
}
