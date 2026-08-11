import { useMemo, useState } from "react"
import { CheckCircle2 } from "lucide-react"
import { api } from "@/lib/api"
import type { CourseSummary, Pin, RoadmapStep } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

type Props = {
  steps: RoadmapStep[]
  pinsByStep: Record<string, Pin[]>
  courses: CourseSummary[]
  onPinned: () => Promise<void>
}

const MAIN_W = 224
const MAIN_H = 56
const CHILD_W = 180
const CHILD_H = 44
const GAP_X = 14
const GAP_Y = 12
const TRUNK_GAP = 40
const BRANCH_OFFSET = 56
const PADDING = 28
const COLUMNS = 2

type Positioned = {
  step: RoadmapStep
  kind: "main" | "child"
  x: number
  y: number
  w: number
  h: number
}

type Edge = { from: string; to: string; kind: "trunk" | "branch" }

function layout(steps: RoadmapStep[]) {
  const mains = steps.filter((s) => !s.parentStepId).sort((a, b) => a.position - b.position)
  const childrenOf = (id: string) =>
    steps.filter((s) => s.parentStepId === id).sort((a, b) => a.position - b.position)

  const nodes: Positioned[] = []
  const edges: Edge[] = []
  const mainCenterX = PADDING + MAIN_W / 2
  let cursorY = PADDING + MAIN_H / 2
  let prevMainId: string | null = null
  let maxRight = mainCenterX + MAIN_W / 2

  mains.forEach((main) => {
    const children = childrenOf(main.id)
    const rows = Math.ceil(children.length / COLUMNS) || 0
    const childrenBlockH = rows > 0 ? rows * CHILD_H + (rows - 1) * GAP_Y : 0
    const blockH = Math.max(MAIN_H, childrenBlockH)
    const mainY = cursorY + (blockH - MAIN_H) / 2

    nodes.push({ step: main, kind: "main", x: mainCenterX, y: mainY, w: MAIN_W, h: MAIN_H })
    if (prevMainId) edges.push({ from: prevMainId, to: main.id, kind: "trunk" })
    prevMainId = main.id

    const blockTop = cursorY + (blockH - childrenBlockH) / 2
    const childLeft = mainCenterX + MAIN_W / 2 + BRANCH_OFFSET

    children.forEach((child, childIndex) => {
      const col = childIndex % COLUMNS
      const row = Math.floor(childIndex / COLUMNS)
      const cx = childLeft + col * (CHILD_W + GAP_X) + CHILD_W / 2
      const cy = blockTop + row * (CHILD_H + GAP_Y) + CHILD_H / 2
      nodes.push({ step: child, kind: "child", x: cx, y: cy, w: CHILD_W, h: CHILD_H })
      edges.push({ from: main.id, to: child.id, kind: "branch" })
      maxRight = Math.max(maxRight, cx + CHILD_W / 2)
    })

    cursorY += blockH + TRUNK_GAP
  })

  const height = mains.length > 0 ? cursorY - TRUNK_GAP + MAIN_H / 2 + PADDING : PADDING * 2
  const width = maxRight + PADDING
  return { nodes, edges, width, height }
}

function edgePath(from: Positioned, to: Positioned, kind: "trunk" | "branch") {
  if (kind === "trunk") {
    const y1 = from.y + from.h / 2
    const y2 = to.y - to.h / 2
    const midY = (y1 + y2) / 2
    return `M ${from.x} ${y1} C ${from.x} ${midY} ${to.x} ${midY} ${to.x} ${y2}`
  }
  const x1 = from.x + from.w / 2
  const y1 = from.y
  const x2 = to.x - to.w / 2
  const y2 = to.y
  const midX = (x1 + x2) / 2
  return `M ${x1} ${y1} C ${midX} ${y1} ${midX} ${y2} ${x2} ${y2}`
}

function courseTitle(courses: CourseSummary[], courseId: string) {
  return courses.find((c) => c.id === courseId)?.title ?? "Curso"
}

function PinPanel({
  stepId,
  courses,
  onPinned,
}: {
  stepId: string
  courses: CourseSummary[]
  onPinned: () => Promise<void>
}) {
  const [courseId, setCourseId] = useState("")
  const [rating, setRating] = useState("")
  const [saving, setSaving] = useState(false)

  if (courses.length === 0) {
    return <p className="text-sm text-muted-foreground">Crie um curso para poder pinar aqui.</p>
  }

  async function pin() {
    if (!courseId) return
    setSaving(true)
    try {
      await api.post(`/steps/${stepId}/pins`, {
        courseId,
        rating: rating ? Number(rating) : null,
        status: null,
      })
      setCourseId("")
      setRating("")
      await onPinned()
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <Select value={courseId} onValueChange={setCourseId}>
        <SelectTrigger className="h-9 w-48">
          <SelectValue placeholder="Pinar curso" />
        </SelectTrigger>
        <SelectContent>
          {courses.map((course) => (
            <SelectItem key={course.id} value={course.id}>
              {course.title}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select value={rating} onValueChange={setRating}>
        <SelectTrigger className="h-9 w-24">
          <SelectValue placeholder="Nota" />
        </SelectTrigger>
        <SelectContent>
          {[1, 2, 3, 4, 5].map((n) => (
            <SelectItem key={n} value={String(n)}>
              {n}/5
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Button type="button" size="sm" onClick={pin} disabled={saving}>
        Pinar
      </Button>
    </div>
  )
}

export function RoadmapDiagram({ steps, pinsByStep, courses, onPinned }: Props) {
  const { nodes, edges, width, height } = useMemo(() => layout(steps), [steps])
  const [selectedId, setSelectedId] = useState<string | null>(null)

  if (nodes.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma etapa ainda.</p>
  }

  const byId = Object.fromEntries(nodes.map((n) => [n.step.id, n]))
  const selected = selectedId ? byId[selectedId] : null
  const selectedPins = selected ? (pinsByStep[selected.step.id] ?? []) : []

  return (
    <div className="flex flex-col gap-4">
      <div className="overflow-x-auto rounded-lg border bg-muted/20 p-3">
        <div className="relative" style={{ width, height }}>
          <svg
            className="absolute inset-0"
            width={width}
            height={height}
            viewBox={`0 0 ${width} ${height}`}
          >
            {edges.map((edge) => {
              const from = byId[edge.from]
              const to = byId[edge.to]
              if (!from || !to) return null
              return (
                <path
                  key={`${edge.from}-${edge.to}`}
                  d={edgePath(from, to, edge.kind)}
                  fill="none"
                  strokeWidth={edge.kind === "trunk" ? 2.5 : 2}
                  strokeDasharray={edge.kind === "branch" ? "1 7" : undefined}
                  strokeLinecap="round"
                  className={edge.kind === "trunk" ? "stroke-primary/50" : "stroke-border"}
                />
              )
            })}
          </svg>

          {nodes.map((node) => {
            const pins = pinsByStep[node.step.id] ?? []
            const done = pins.length > 0
            const isMain = node.kind === "main"
            const isSelected = node.step.id === selectedId
            return (
              <button
                key={node.step.id}
                type="button"
                onClick={() => setSelectedId(node.step.id)}
                className={`absolute flex items-start gap-1.5 overflow-hidden rounded-lg border-2 px-3 py-2 text-left shadow-sm transition-colors ${
                  done
                    ? "border-emerald-500 bg-emerald-500/10"
                    : isMain
                      ? "border-primary bg-primary text-primary-foreground"
                      : "border-border bg-card hover:border-foreground/40"
                } ${isSelected ? "ring-2 ring-primary ring-offset-2 ring-offset-background" : ""}`}
                style={{
                  left: node.x - node.w / 2,
                  top: node.y - node.h / 2,
                  width: node.w,
                  height: node.h,
                }}
              >
                {done && <CheckCircle2 className="size-4 shrink-0 text-emerald-500" />}
                <span
                  className={`line-clamp-2 min-w-0 leading-snug font-medium ${isMain ? "text-sm" : "text-xs"}`}
                >
                  {node.step.title}
                </span>
              </button>
            )
          })}
        </div>
      </div>

      {selected ? (
        <Card>
          <CardContent className="flex flex-col gap-3 pt-6">
            <div className="flex items-center justify-between gap-2">
              <h3 className="font-medium">{selected.step.title}</h3>
              {selectedPins.length > 0 && (
                <Badge variant="secondary" className="gap-1">
                  <CheckCircle2 className="size-3 text-emerald-500" />
                  Concluido
                </Badge>
              )}
            </div>
            {selectedPins.length > 0 && (
              <div className="flex flex-wrap gap-1.5">
                {selectedPins.map((pin) => (
                  <Badge key={pin.id} variant="outline">
                    {courseTitle(courses, pin.courseId)}
                    {pin.rating != null ? ` · ${pin.rating}/5` : ""}
                  </Badge>
                ))}
              </div>
            )}
            <PinPanel stepId={selected.step.id} courses={courses} onPinned={onPinned} />
          </CardContent>
        </Card>
      ) : (
        <p className="text-sm text-muted-foreground">
          Clique numa etapa do diagrama para pinar um curso.
        </p>
      )}
    </div>
  )
}
