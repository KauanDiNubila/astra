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

  const centerY = PADDING + NODE_SIZE / 2
  const points = sorted.map((item, i) => ({
    item,
    x: PADDING + i * SPACING_X,
    y: centerY,
  }))
  const width = PADDING * 2 + (points.length - 1) * SPACING_X
  const height = NODE_SIZE + PADDING * 2

  return (
    <div className="overflow-x-auto">
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
          <button
            key={p.item.id}
            type="button"
            title={p.item.title}
            disabled={saving}
            onClick={() => handleClick(p.item.position)}
            className={`absolute -translate-x-1/2 -translate-y-1/2 rounded-full border-2 transition-colors ${
              p.item.completed
                ? "border-emerald-500 bg-emerald-500"
                : "border-border bg-card hover:border-foreground/40"
            }`}
            style={{ left: p.x, top: p.y, width: NODE_SIZE, height: NODE_SIZE }}
          />
        ))}
      </div>
    </div>
  )
}
