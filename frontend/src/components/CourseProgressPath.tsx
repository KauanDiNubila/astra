import { Check } from "lucide-react"

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

const NODE_SIZE = 40
const SPACING_X = 90
const AMPLITUDE = 26
const PADDING = 30

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

  const centerY = PADDING + AMPLITUDE
  const points = sorted.map((item, i) => ({
    item,
    x: PADDING + i * SPACING_X,
    y: centerY + AMPLITUDE * Math.sin(i * 1.1),
  }))
  const width = PADDING * 2 + (points.length - 1) * SPACING_X
  const height = centerY + AMPLITUDE + PADDING

  return (
    <div className="overflow-x-auto rounded-lg border bg-muted/20 p-4">
      <div className="relative" style={{ width, height }}>
        <svg
          className="absolute inset-0"
          width={width}
          height={height}
          viewBox={`0 0 ${width} ${height}`}
        >
          {points.slice(1).map((p, i) => {
            const prev = points[i]
            const midX = (prev.x + p.x) / 2
            const done = prev.item.completed && p.item.completed
            return (
              <path
                key={p.item.id}
                d={`M ${prev.x} ${prev.y} C ${midX} ${prev.y}, ${midX} ${p.y}, ${p.x} ${p.y}`}
                fill="none"
                strokeWidth={4}
                strokeLinecap="round"
                className={done ? "stroke-emerald-500" : "stroke-border"}
              />
            )
          })}
        </svg>

        {points.map((p) => (
          <button
            key={p.item.id}
            type="button"
            title={p.item.title}
            disabled={saving}
            onClick={() => handleClick(p.item.position)}
            className={`absolute flex -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full border-2 text-xs font-semibold shadow-sm transition-colors ${
              p.item.completed
                ? "border-emerald-500 bg-emerald-500 text-white"
                : "border-border bg-card text-muted-foreground hover:border-foreground/40"
            }`}
            style={{ left: p.x, top: p.y, width: NODE_SIZE, height: NODE_SIZE }}
          >
            {p.item.completed ? <Check className="size-5" /> : p.item.position}
          </button>
        ))}
      </div>
    </div>
  )
}
