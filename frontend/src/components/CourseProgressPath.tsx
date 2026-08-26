import { useEffect, useRef, useState } from "react"
import { motion } from "motion/react"
import { cn, SCROLLBAR_HIDE_CLASS } from "@/lib/utils"

type PathItem = {
  id: string
  title: string
  position: number
  completed: boolean
}

type Props = {
  items: PathItem[]
  onSetProgress: (position: number) => void
  saving: boolean
}

const NODE_SIZE = 16
const SPACING_X = 22
const PADDING = 8
// Margem extra só pra caber o hover/pop (escala até 1.4x) sem cortar nas
// pontas — o container tem overflow-x-auto (pro scroll horizontal quando
// tem muita aula), então qualquer coisa que passe da borda é cortada de
// verdade, não só escondida.
const CLIP_MARGIN = 5

function ProgressDot({
  item,
  x,
  y,
  disabled,
  onClick,
}: {
  item: PathItem
  x: number
  y: number
  disabled: boolean
  onClick: () => void
}) {
  const prevCompleted = useRef(item.completed)
  const [pop, setPop] = useState(false)

  useEffect(() => {
    if (item.completed && !prevCompleted.current) setPop(true)
    prevCompleted.current = item.completed
  }, [item.completed])

  return (
    <motion.button
      type="button"
      title={item.title}
      disabled={disabled}
      onClick={onClick}
      whileHover={{ scale: 1.15 }}
      whileTap={{ scale: 0.85 }}
      animate={pop ? { scale: [1, 1.4, 1] } : { scale: 1 }}
      transition={{ duration: 0.35, ease: "easeOut" }}
      onAnimationComplete={() => setPop(false)}
      className={`absolute -translate-x-1/2 -translate-y-1/2 rounded-full border-2 transition-colors ${
        item.completed
          ? "border-emerald-500 bg-emerald-500"
          : "border-border bg-card hover:border-foreground/40"
      }`}
      style={{ left: x, top: y, width: NODE_SIZE, height: NODE_SIZE }}
    />
  )
}

export function CourseProgressPath({ items, onSetProgress, saving }: Props) {
  const sorted = [...items].sort((a, b) => a.position - b.position)
  if (sorted.length === 0) return null

  const maxCompleted = sorted.reduce(
    (max, item) => (item.completed && item.position > max ? item.position : max),
    0,
  )

  function handleClick(position: number) {
    onSetProgress(position === maxCompleted ? position - 1 : position)
  }

  const centerY = PADDING + NODE_SIZE / 2 + CLIP_MARGIN
  const points = sorted.map((item, i) => ({
    item,
    x: PADDING + i * SPACING_X + CLIP_MARGIN,
    y: centerY,
  }))
  const width = PADDING * 2 + (points.length - 1) * SPACING_X + CLIP_MARGIN * 2
  const height = NODE_SIZE + PADDING * 2 + CLIP_MARGIN * 2

  return (
    <div className={cn("overflow-x-auto", SCROLLBAR_HIDE_CLASS)}>
      <div className="relative" style={{ width, height }}>
        <svg
          className="absolute inset-0"
          width={width}
          height={height}
          viewBox={`0 0 ${width} ${height}`}
        >
          {points.slice(1).map((p, i) => {
            const prev = points[i]
            const done = prev.item.completed && p.item.completed
            return (
              <line
                key={p.item.id}
                x1={prev.x}
                y1={prev.y}
                x2={p.x}
                y2={p.y}
                strokeWidth={2}
                strokeLinecap="round"
                className={done ? "stroke-emerald-500" : "stroke-border"}
              />
            )
          })}
        </svg>

        {points.map((p) => (
          <ProgressDot
            key={p.item.id}
            item={p.item}
            x={p.x}
            y={p.y}
            disabled={saving}
            onClick={() => handleClick(p.item.position)}
          />
        ))}
      </div>
    </div>
  )
}
