import { useEffect, useRef, useState } from "react"
import { AnimatePresence, motion } from "motion/react"
import { X } from "lucide-react"
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
  onDelete?: (id: string) => void
  saving: boolean
}

const NODE_SIZE = 16
const SPACING_X = 22
const PADDING = 8
// Só o suficiente pro leve "pop" de escala no hover/clique — o selo de
// excluir flutua por fora deste container (ver handleHoverChange), então
// não precisa de espaço reservado pra ele aqui.
const POP_MARGIN = 6
const BADGE_SIZE = 14
const BADGE_GAP = 6

function ProgressDot({
  item,
  x,
  y,
  disabled,
  onClick,
  onHoverChange,
}: {
  item: PathItem
  x: number
  y: number
  disabled: boolean
  onClick: () => void
  onHoverChange: (hovering: boolean, el: HTMLButtonElement | null) => void
}) {
  const prevCompleted = useRef(item.completed)
  const [pop, setPop] = useState(false)
  const ref = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (item.completed && !prevCompleted.current) setPop(true)
    prevCompleted.current = item.completed
  }, [item.completed])

  return (
    <motion.button
      ref={ref}
      type="button"
      title={item.title}
      disabled={disabled}
      onClick={onClick}
      onMouseEnter={() => onHoverChange(true, ref.current)}
      onMouseLeave={() => onHoverChange(false, ref.current)}
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

export function CourseProgressPath({ items, onSetProgress, onDelete, saving }: Props) {
  const sorted = [...items].sort((a, b) => a.position - b.position)
  const containerRef = useRef<HTMLDivElement>(null)
  const hideTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const [hover, setHover] = useState<{ id: string; left: number; top: number } | null>(null)

  useEffect(() => () => {
    if (hideTimeoutRef.current) clearTimeout(hideTimeoutRef.current)
  }, [])

  if (sorted.length === 0) return null

  const maxCompleted = sorted.reduce(
    (max, item) => (item.completed && item.position > max ? item.position : max),
    0,
  )

  function handleClick(position: number) {
    onSetProgress(position === maxCompleted ? position - 1 : position)
  }

  function cancelHoverClear() {
    if (hideTimeoutRef.current) {
      clearTimeout(hideTimeoutRef.current)
      hideTimeoutRef.current = null
    }
  }

  // Delay pra dar tempo do mouse atravessar o vão entre o ponto e o selo de
  // excluir, que flutua separado (fora do ponto) — sem isso, o selo some
  // antes do usuário conseguir alcançá-lo.
  function scheduleHoverClear(itemId: string) {
    cancelHoverClear()
    hideTimeoutRef.current = setTimeout(() => {
      setHover((h) => (h?.id === itemId ? null : h))
    }, 250)
  }

  function handleHoverChange(itemId: string, hovering: boolean, el: HTMLButtonElement | null) {
    if (!hovering || !el || !containerRef.current || !onDelete || saving) {
      scheduleHoverClear(itemId)
      return
    }
    cancelHoverClear()
    const dotRect = el.getBoundingClientRect()
    const containerRect = containerRef.current.getBoundingClientRect()
    setHover({
      id: itemId,
      left: dotRect.left - containerRect.left + dotRect.width / 2,
      top: dotRect.top - containerRect.top,
    })
  }

  const centerY = PADDING + NODE_SIZE / 2 + POP_MARGIN
  const points = sorted.map((item, i) => ({
    item,
    x: PADDING + i * SPACING_X + POP_MARGIN,
    y: centerY,
  }))
  const width = PADDING * 2 + (points.length - 1) * SPACING_X + POP_MARGIN * 2
  const height = NODE_SIZE + PADDING * 2 + POP_MARGIN * 2

  return (
    // Não clipa (overflow visible por padrão) — o selo de excluir flutua
    // aqui por cima do container de scroll, sem precisar aumentar sua altura.
    <div ref={containerRef} className="relative">
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
              onHoverChange={(hovering, el) => handleHoverChange(p.item.id, hovering, el)}
            />
          ))}
        </div>
      </div>

      <AnimatePresence>
        {hover && onDelete && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.1 }}
            className="pointer-events-none absolute flex -translate-x-1/2 flex-col items-center"
            style={{ left: hover.left, top: hover.top - BADGE_SIZE - BADGE_GAP, width: BADGE_SIZE }}
          >
            <motion.button
              type="button"
              title="Excluir aula"
              onClick={() => onDelete(hover.id)}
              onMouseEnter={cancelHoverClear}
              onMouseLeave={() => scheduleHoverClear(hover.id)}
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0 }}
              transition={{ type: "spring", damping: 16, stiffness: 500 }}
              className="pointer-events-auto flex items-center justify-center rounded-full bg-destructive text-destructive-foreground shadow-sm"
              style={{ width: BADGE_SIZE, height: BADGE_SIZE }}
            >
              <X className="size-2.5" strokeWidth={3} />
            </motion.button>
            <motion.div
              initial={{ scaleY: 0 }}
              animate={{ scaleY: 1 }}
              exit={{ scaleY: 0 }}
              transition={{ duration: 0.15 }}
              style={{ transformOrigin: "bottom", height: BADGE_GAP }}
              className="w-px bg-destructive"
            />
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
