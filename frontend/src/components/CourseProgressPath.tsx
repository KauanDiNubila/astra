import { Check } from "lucide-react"
import type { ModuleItem } from "@/lib/types"

type Props = {
  modules: ModuleItem[]
  onSetProgress: (position: number) => void
  saving: boolean
}

const NODE_SIZE = 40
const SPACING_X = 90
const AMPLITUDE = 26
const PADDING = 30

export function CourseProgressPath({ modules, onSetProgress, saving }: Props) {
  const sorted = [...modules].sort((a, b) => a.position - b.position)
  if (sorted.length === 0) return null

  const centerY = PADDING + AMPLITUDE
  const points = sorted.map((m, i) => ({
    module: m,
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
            const done = prev.module.completed && p.module.completed
            return (
              <path
                key={p.module.id}
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
            key={p.module.id}
            type="button"
            title={p.module.title}
            disabled={saving}
            onClick={() => onSetProgress(p.module.position)}
            className={`absolute flex -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full border-2 text-xs font-semibold shadow-sm transition-colors ${
              p.module.completed
                ? "border-emerald-500 bg-emerald-500 text-white"
                : "border-border bg-card text-muted-foreground hover:border-foreground/40"
            }`}
            style={{ left: p.x, top: p.y, width: NODE_SIZE, height: NODE_SIZE }}
          >
            {p.module.completed ? <Check className="size-5" /> : p.module.position}
          </button>
        ))}
      </div>
    </div>
  )
}
