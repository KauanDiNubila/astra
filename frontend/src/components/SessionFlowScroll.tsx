import { useRef } from "react"
import type { RefObject } from "react"
import { motion, useReducedMotion, useScroll, useSpring, useTransform } from "motion/react"

// Visualiza o ecossistema do Astra em duas camadas: Astra se divide em 3
// pilares (Foco, Aprendizado, Social) e cada pilar abre nas telas reais do
// app — só o pilar Foco é de fato calculado por agregação sobre a sessão;
// os outros dois existem lado a lado, não "nascem" dela.

const WIDTH = 1200
const ROOT_Y = 40
const ROOT_LABEL_Y = 20
const ROOT_TRUNK_BOTTOM_Y = 180
const MID_Y = 320
const MID_LABEL_Y = 296
const LEAF_Y = 560
const LEAF_LABEL_Y = 600

const groups = [
  {
    label: "Foco",
    x: 300,
    leaves: [
      { label: "Sessões", x: 90 },
      { label: "Dashboard", x: 230 },
      { label: "Metas", x: 370 },
      { label: "Ranking", x: 510 },
    ],
  },
  {
    label: "Aprendizado",
    x: 730,
    leaves: [
      { label: "Cursos", x: 660 },
      { label: "Roadmaps", x: 800 },
    ],
  },
  {
    label: "Social",
    x: 1020,
    leaves: [
      { label: "Amigos", x: 950 },
      { label: "Chat", x: 1090 },
    ],
  },
]

const rootTrunkPath = `M ${WIDTH / 2} ${ROOT_Y} L ${WIDTH / 2} ${ROOT_TRUNK_BOTTOM_Y}`

function curvePath(startX: number, startY: number, endX: number, endY: number) {
  const midY = (startY + endY) / 2
  return `M ${startX} ${startY} C ${startX} ${midY} ${endX} ${midY} ${endX} ${endY}`
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

  const springConfig = { stiffness: 300, damping: 40 }
  const rootTrunkLength = useSpring(useTransform(scrollYProgress, [0, 0.2], [0, 1]), springConfig)
  const midBranchLength = useSpring(useTransform(scrollYProgress, [0.15, 0.45], [0, 1]), springConfig)
  const leafBranchLength = useSpring(useTransform(scrollYProgress, [0.4, 0.7], [0, 1]), springConfig)
  const midLabelOpacity = useTransform(scrollYProgress, [0.35, 0.5], [0, 1])
  const leafLabelOpacity = useTransform(scrollYProgress, [0.65, 0.9], [0, 1])
  const rootDotOpacity = useTransform(rootTrunkLength, (v) => (v > 0 ? 1 : 0))
  const barProgress = useSpring(scrollYProgress, { stiffness: 280, damping: 18, mass: 0.3 })

  if (reducedMotion) {
    return (
      <section className="mx-auto flex max-w-4xl flex-col items-center gap-10 px-4 py-24 text-center">
        <p className="max-w-md text-balance text-muted-foreground">
          Cada sessão de foco alimenta seu dashboard, metas e ranking. O Astra
          vai além dela também, com aprendizado e conexão com outras pessoas.
        </p>
        <div className="flex flex-col gap-6">
          {groups.map((g) => (
            <div key={g.label} className="flex flex-col items-center gap-2">
              <span className="text-sm font-medium text-foreground">{g.label}</span>
              <div className="flex flex-wrap justify-center gap-3">
                {g.leaves.map((l) => (
                  <span
                    key={l.label}
                    className="rounded-full border px-4 py-1.5 text-sm text-muted-foreground"
                  >
                    {l.label}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </section>
    )
  }

  return (
    <section ref={sectionRef} className="relative h-[260vh] w-full">
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
          viewBox={`0 0 ${WIDTH} ${LEAF_LABEL_Y + 60}`}
          className="h-[85vh] w-auto max-w-[95vw]"
          fill="none"
        >
          <motion.path
            d={rootTrunkPath}
            stroke="currentColor"
            className="text-primary"
            strokeWidth={3}
            strokeLinecap="round"
            style={{ pathLength: rootTrunkLength }}
          />
          <motion.circle
            cx={WIDTH / 2}
            cy={ROOT_Y}
            r={7}
            className="fill-primary"
            style={{ opacity: rootDotOpacity }}
          />
          <text
            x={WIDTH / 2}
            y={ROOT_LABEL_Y}
            textAnchor="middle"
            className="fill-foreground text-[22px] font-medium"
          >
            Astra
          </text>

          {groups.map((g) => (
            <motion.path
              key={g.label}
              d={curvePath(WIDTH / 2, ROOT_TRUNK_BOTTOM_Y, g.x, MID_Y)}
              stroke="currentColor"
              className="text-border"
              strokeWidth={2.5}
              strokeLinecap="round"
              style={{ pathLength: midBranchLength }}
            />
          ))}

          {groups.map((g) => (
            <motion.g key={g.label} style={{ opacity: midLabelOpacity }}>
              <circle cx={g.x} cy={MID_Y} r={6} className="fill-primary" />
              <text
                x={g.x}
                y={MID_LABEL_Y}
                textAnchor="middle"
                className="fill-foreground text-[13px] font-medium"
              >
                {g.label}
              </text>
            </motion.g>
          ))}

          {groups.flatMap((g) =>
            g.leaves.map((l) => (
              <motion.path
                key={l.label}
                d={curvePath(g.x, MID_Y, l.x, LEAF_Y)}
                stroke="currentColor"
                className="text-border"
                strokeWidth={2}
                strokeLinecap="round"
                style={{ pathLength: leafBranchLength }}
              />
            )),
          )}

          {groups.flatMap((g) =>
            g.leaves.map((l) => (
              <motion.g key={l.label} style={{ opacity: leafLabelOpacity }}>
                <circle cx={l.x} cy={LEAF_Y} r={5} className="fill-foreground" />
                <text
                  x={l.x}
                  y={LEAF_LABEL_Y}
                  textAnchor="middle"
                  className="fill-muted-foreground text-[13px]"
                >
                  {l.label}
                </text>
              </motion.g>
            )),
          )}
        </svg>
      </div>
    </section>
  )
}
