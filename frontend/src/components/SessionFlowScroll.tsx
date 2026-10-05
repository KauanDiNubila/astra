import { useRef } from "react"
import type { RefObject } from "react"
import type { MotionValue } from "motion/react"
import { motion, useReducedMotion, useScroll, useSpring, useTransform } from "motion/react"

// Visualiza o ecossistema do Astra em duas camadas: Astra se divide em 4
// pilares (Foco, Aprendizado, Social, GitHub) e cada pilar abre nas telas
// reais do app — só o pilar Foco é de fato calculado por agregação sobre a
// sessão; os outros três existem lado a lado, não "nascem" dela.

const WIDTH = 1480
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
  {
    label: "GitHub",
    x: 1310,
    leaves: [
      { label: "Atividade", x: 1240 },
      { label: "Insights", x: 1380 },
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

function StaticFlow() {
  return (
    <section className="mx-auto flex max-w-4xl flex-col items-center gap-10 px-4 py-24 text-center">
      <p className="max-w-md text-balance text-muted-foreground">
        Cada sessão de foco alimenta seu dashboard, metas e ranking. O Astra
        vai além dela também, com aprendizado, conexão com outras pessoas e
        integração com o GitHub.
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

const V_WIDTH = 200
const V_X_TRUNK = 16
const V_X_GROUP = 52
const V_X_LEAF = 88
const V_ROOT_Y = 18
const V_GROUP_ROW = 38
const V_LEAF_ROW = 30
const V_GROUP_GAP = 18
const V_TRUNK_SPAN = 0.6
const V_START = 0.02

const verticalLayout = (() => {
  let y = 66
  const items = groups.map((g) => {
    const groupY = y
    y += V_GROUP_ROW
    const leaves = g.leaves.map((l) => {
      const leafY = y
      y += V_LEAF_ROW
      return { label: l.label, y: leafY }
    })
    y += V_GROUP_GAP
    return { label: g.label, y: groupY, leaves }
  })
  const trunkEndY = items[items.length - 1].y
  const trunkSpan = trunkEndY - V_ROOT_Y
  return {
    items: items.map((item) => ({
      ...item,
      start: V_START + V_TRUNK_SPAN * ((item.y - V_ROOT_Y) / trunkSpan),
    })),
    trunkEndY,
    height: y - V_GROUP_GAP,
  }
})()

const springConfig = { stiffness: 300, damping: 40 }

function useDrawn(length: MotionValue<number>) {
  return useTransform(length, (v) => (v > 0.005 ? 1 : 0))
}

function VerticalLeaf({
  label,
  y,
  groupY,
  start,
  progress,
}: {
  label: string
  y: number
  groupY: number
  start: number
  progress: MotionValue<number>
}) {
  const branch = useSpring(useTransform(progress, [start, start + 0.06], [0, 1]), springConfig)
  const opacity = useTransform(progress, [start + 0.03, start + 0.08], [0, 1])
  const drawn = useDrawn(branch)
  const from = Math.max(groupY + 8, y - 14)
  return (
    <>
      <motion.path
        d={`M ${V_X_GROUP} ${from} C ${V_X_GROUP} ${y} ${V_X_GROUP} ${y} ${V_X_LEAF} ${y}`}
        stroke="currentColor"
        className="text-border"
        strokeWidth={1.75}
        strokeLinecap="round"
        style={{ pathLength: branch, opacity: drawn }}
      />
      <motion.g style={{ opacity }}>
        <circle cx={V_X_LEAF} cy={y} r={3.5} className="fill-foreground" />
        <text x={V_X_LEAF + 12} y={y + 4.5} className="fill-muted-foreground text-[14px]">
          {label}
        </text>
      </motion.g>
    </>
  )
}

function VerticalGroup({
  group,
  progress,
}: {
  group: (typeof verticalLayout.items)[number]
  progress: MotionValue<number>
}) {
  const { start, y } = group
  const branch = useSpring(useTransform(progress, [start, start + 0.05], [0, 1]), springConfig)
  const opacity = useTransform(progress, [start + 0.02, start + 0.07], [0, 1])
  const sub = useSpring(useTransform(progress, [start + 0.05, start + 0.17], [0, 1]), springConfig)
  const branchDrawn = useDrawn(branch)
  const subDrawn = useDrawn(sub)
  const lastLeafY = group.leaves[group.leaves.length - 1].y

  return (
    <>
      <motion.path
        d={`M ${V_X_TRUNK} ${y - 16} C ${V_X_TRUNK} ${y} ${V_X_TRUNK} ${y} ${V_X_GROUP} ${y}`}
        stroke="currentColor"
        className="text-border"
        strokeWidth={2}
        strokeLinecap="round"
        style={{ pathLength: branch, opacity: branchDrawn }}
      />
      <motion.path
        d={`M ${V_X_GROUP} ${y} L ${V_X_GROUP} ${lastLeafY - 14}`}
        stroke="currentColor"
        className="text-border"
        strokeWidth={1.75}
        strokeLinecap="round"
        style={{ pathLength: sub, opacity: subDrawn }}
      />
      <motion.g style={{ opacity }}>
        <circle cx={V_X_GROUP} cy={y} r={5.5} className="fill-primary" />
        <text x={V_X_GROUP + 14} y={y + 5} className="fill-foreground text-[15px] font-medium">
          {group.label}
        </text>
      </motion.g>
      {group.leaves.map((leaf, i) => (
        <VerticalLeaf
          key={leaf.label}
          label={leaf.label}
          y={leaf.y}
          groupY={y}
          start={start + 0.07 + i * 0.02}
          progress={progress}
        />
      ))}
    </>
  )
}

export function SessionFlowVertical({ scrollContainerRef }: Props) {
  const sectionRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    container: scrollContainerRef,
    offset: ["start start", "end end"],
  })
  const trunk = useSpring(useTransform(scrollYProgress, [V_START, V_START + V_TRUNK_SPAN], [0, 1]), springConfig)
  const rootDotOpacity = useTransform(trunk, (v) => (v > 0 ? 1 : 0))
  const trunkDrawn = useDrawn(trunk)

  if (reducedMotion) return <StaticFlow />

  return (
    <div>
      <p className="mx-auto max-w-xs text-balance px-4 pt-24 text-center text-muted-foreground">
        Cada sessão de foco alimenta seu dashboard, metas e ranking. O Astra vai além dela também, com
        aprendizado, conexão com outras pessoas e integração com o GitHub.
      </p>
      <section ref={sectionRef} className="relative h-[240vh] w-full">
        <div className="sticky top-0 flex h-app w-full items-center justify-center overflow-hidden">
          <svg
            viewBox={`0 0 ${V_WIDTH} ${verticalLayout.height + 16}`}
            className="h-[78svh] w-auto max-w-[92vw]"
            fill="none"
          >
            <motion.path
              d={`M ${V_X_TRUNK} ${V_ROOT_Y} L ${V_X_TRUNK} ${verticalLayout.trunkEndY}`}
              stroke="currentColor"
              className="text-primary"
              strokeWidth={2.5}
              strokeLinecap="round"
              style={{ pathLength: trunk, opacity: trunkDrawn }}
            />
            <motion.circle
              cx={V_X_TRUNK}
              cy={V_ROOT_Y}
              r={6}
              className="fill-primary"
              style={{ opacity: rootDotOpacity }}
            />
            <text x={V_X_TRUNK + 16} y={V_ROOT_Y + 6} className="fill-foreground text-[20px] font-semibold">
              Astra
            </text>
            {verticalLayout.items.map((group) => (
              <VerticalGroup key={group.label} group={group} progress={scrollYProgress} />
            ))}
          </svg>
        </div>
      </section>
    </div>
  )
}

export function SessionFlowScroll({ scrollContainerRef }: Props) {
  const sectionRef = useRef<HTMLDivElement>(null)
  const reducedMotion = useReducedMotion()
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    container: scrollContainerRef,
    offset: ["start start", "end end"],
  })

  const rootTrunkLength = useSpring(useTransform(scrollYProgress, [0, 0.2], [0, 1]), springConfig)
  const midBranchLength = useSpring(useTransform(scrollYProgress, [0.15, 0.45], [0, 1]), springConfig)
  const leafBranchLength = useSpring(useTransform(scrollYProgress, [0.4, 0.7], [0, 1]), springConfig)
  const midLabelOpacity = useTransform(scrollYProgress, [0.35, 0.5], [0, 1])
  const leafLabelOpacity = useTransform(scrollYProgress, [0.65, 0.9], [0, 1])
  const rootDotOpacity = useTransform(rootTrunkLength, (v) => (v > 0 ? 1 : 0))
  const barProgress = useSpring(scrollYProgress, { stiffness: 280, damping: 18, mass: 0.3 })

  if (reducedMotion) return <StaticFlow />

  return (
    <section ref={sectionRef} className="relative h-[260vh] w-full">
      <div className="sticky top-0 flex h-app w-full items-center justify-center overflow-hidden">
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
