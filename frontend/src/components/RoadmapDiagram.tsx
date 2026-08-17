import { useEffect, useMemo, useRef, useState } from "react"
import { flushSync } from "react-dom"
import { CheckCircle2, GitBranch, List, X } from "lucide-react"
import { motion } from "motion/react"
import { api } from "@/lib/api"
import { useSpotlight } from "@/hooks/useSpotlight"
import { BUTTON_REVEAL_CLASS, cn } from "@/lib/utils"
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
  roadmapId: string
  steps: RoadmapStep[]
  pinsByStep: Record<string, Pin[]>
  courses: CourseSummary[]
  onChanged: () => Promise<void>
}

function toggleStepCompleted(roadmapId: string, step: RoadmapStep, onChanged: () => Promise<void>) {
  return api
    .patch(`/roadmaps/${roadmapId}/steps/${step.id}`, { completed: !step.completed })
    .then(onChanged)
}

function unpinCourse(stepId: string, pinId: string, onChanged: () => Promise<void>) {
  return api.delete(`/steps/${stepId}/pins/${pinId}`).then(onChanged)
}

type ViewMode = "graph" | "list"

const VIEW_MODE_KEY = "astra:roadmap-view-mode"

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
    return <p className="text-xs text-muted-foreground">Crie um curso para poder pinar aqui.</p>
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
    <div className="flex flex-wrap items-center gap-2" onClick={(e) => e.stopPropagation()}>
      <Select value={courseId} onValueChange={setCourseId}>
        <SelectTrigger className="h-8 w-40 text-xs">
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
        <SelectTrigger className="h-8 w-20 text-xs">
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
      <Button type="button" size="sm" className="h-8" onClick={pin} disabled={saving}>
        Pinar
      </Button>
    </div>
  )
}

// ---------- Visao "lista": marcos empilhados com subtopicos em leque ----------

function ListNode({
  step,
  index,
  pins,
  courses,
  onPinned,
  onUnpinned,
  onToggleCompleted,
  variant,
}: {
  step: RoadmapStep
  index?: number
  pins: Pin[]
  courses: CourseSummary[]
  onPinned: () => Promise<void>
  onUnpinned: (pinId: string) => Promise<void>
  onToggleCompleted: () => Promise<void>
  variant: "main" | "sub"
}) {
  const [open, setOpen] = useState(false)
  const done = step.completed
  const isMain = variant === "main"
  const { onMouseMove } = useSpotlight()

  return (
    <div
      className={`flex flex-col gap-2 rounded-xl border-2 transition-colors ${
        isMain ? "min-w-56 px-5 py-3" : "min-w-36 px-3 py-2"
      } ${
        done
          ? "border-emerald-500 bg-emerald-950"
          : isMain
            ? "border-primary bg-primary/5"
            : "border-border bg-card hover:border-foreground/30"
      }`}
    >
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={`flex items-center gap-2 text-left font-medium ${isMain ? "text-base" : "text-sm"}`}
      >
        {isMain && (
          <span
            className={`flex size-6 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${
              done ? "bg-emerald-500 text-white" : "bg-primary text-primary-foreground"
            }`}
          >
            {done ? <CheckCircle2 className="size-4" /> : index}
          </span>
        )}
        {!isMain && done && <CheckCircle2 className="size-4 shrink-0 text-emerald-500" />}
        <span>{step.title}</span>
      </button>
      {pins.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {pins.map((pin) => (
            <Badge key={pin.id} variant="secondary" className="gap-1 pr-1 text-[10px]">
              {courseTitle(courses, pin.courseId)}
              {pin.rating != null ? ` · ${pin.rating}/5` : ""}
              <button
                type="button"
                title="Despinar curso"
                onClick={(e) => {
                  e.stopPropagation()
                  onUnpinned(pin.id)
                }}
                className="rounded-full p-0.5 hover:bg-foreground/10"
              >
                <X className="size-2.5" />
              </button>
            </Badge>
          ))}
        </div>
      )}
      {open && (
        <div className="flex flex-col gap-2" onClick={(e) => e.stopPropagation()}>
          <Button
            type="button"
            size="sm"
            variant={done ? "secondary" : "outline"}
            className={cn("h-8 w-fit gap-1.5 text-xs", BUTTON_REVEAL_CLASS)}
            onMouseMove={onMouseMove}
            onClick={onToggleCompleted}
          >
            {done ? <CheckCircle2 className="size-3.5" /> : null}
            {done ? "Concluído" : "Marcar como concluído"}
          </Button>
          <PinPanel stepId={step.id} courses={courses} onPinned={onPinned} />
        </div>
      )}
    </div>
  )
}

function ListView({ roadmapId, steps, pinsByStep, courses, onChanged }: Props) {
  const mains = steps.filter((s) => !s.parentStepId).sort((a, b) => a.position - b.position)
  const childrenOf = (id: string) =>
    steps.filter((s) => s.parentStepId === id).sort((a, b) => a.position - b.position)

  return (
    <div className="flex flex-col items-center overflow-x-auto py-2">
      {mains.map((main, index) => {
        const children = childrenOf(main.id)
        return (
          <div key={main.id} className="flex w-full flex-col items-center">
            {index > 0 && <div className="h-8 w-px bg-border" />}
            <ListNode
              step={main}
              index={index + 1}
              pins={pinsByStep[main.id] ?? []}
              courses={courses}
              onPinned={onChanged}
              onUnpinned={(pinId) => unpinCourse(main.id, pinId, onChanged)}
              onToggleCompleted={() => toggleStepCompleted(roadmapId, main, onChanged)}
              variant="main"
            />
            {children.length > 0 && (
              <>
                <div className="h-6 w-px bg-border" />
                <div className="flex max-w-3xl flex-wrap justify-center gap-3 pb-2">
                  {children.map((child) => (
                    <ListNode
                      key={child.id}
                      step={child}
                      pins={pinsByStep[child.id] ?? []}
                      courses={courses}
                      onPinned={onChanged}
                      onUnpinned={(pinId) => unpinCourse(child.id, pinId, onChanged)}
                      onToggleCompleted={() => toggleStepCompleted(roadmapId, child, onChanged)}
                      variant="sub"
                    />
                  ))}
                </div>
              </>
            )}
          </div>
        )
      })}
    </div>
  )
}

// ---------- Visao "grafo": layout calculado com conectores em curva ----------

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

function computeGraphLayout(steps: RoadmapStep[]) {
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

function GraphView({ roadmapId, steps, pinsByStep, courses, onChanged }: Props) {
  const { nodes, edges, width, height } = useMemo(() => computeGraphLayout(steps), [steps])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [visibleId, setVisibleId] = useState<string | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const panelRef = useRef<HTMLDivElement>(null)
  const emptyHeightRef = useRef(0)
  const collapsingRef = useRef(false)

  const byId = Object.fromEntries(nodes.map((n) => [n.step.id, n]))
  const visibleSelected = visibleId ? byId[visibleId] : null
  const selectedPins = visibleSelected ? (pinsByStep[visibleSelected.step.id] ?? []) : []
  const { onMouseMove } = useSpotlight()

  useEffect(() => {
    if (!selectedId) return
    function handleClickOutside(event: MouseEvent) {
      const target = event.target as HTMLElement
      if (target.closest('[data-slot="select-content"]')) return
      if (containerRef.current && !containerRef.current.contains(target)) {
        setSelectedId(null)
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [selectedId])

  useEffect(() => {
    const el = panelRef.current
    if (el && !visibleId && !collapsingRef.current) {
      emptyHeightRef.current = el.getBoundingClientRect().height
    }
  })

  useEffect(() => {
    if (selectedId) {
      setVisibleId(selectedId)
      return
    }
    const el = panelRef.current
    if (!el || !visibleId) return

    const startHeight = el.getBoundingClientRect().height
    const endHeight = emptyHeightRef.current
    collapsingRef.current = true
    el.style.overflow = "hidden"
    el.style.height = `${startHeight}px`
    void el.offsetHeight
    el.style.transition = "height 250ms ease-out"
    el.style.height = `${endHeight}px`

    let fallback = 0
    function finish() {
      flushSync(() => setVisibleId(null))
      el!.style.transition = ""
      el!.style.height = ""
      el!.style.overflow = ""
      collapsingRef.current = false
    }
    function onTransitionEnd(event: TransitionEvent) {
      if (event.propertyName !== "height") return
      clearTimeout(fallback)
      finish()
    }
    el.addEventListener("transitionend", onTransitionEnd)
    fallback = window.setTimeout(finish, 300)
    return () => {
      clearTimeout(fallback)
      el.removeEventListener("transitionend", onTransitionEnd)
    }
  }, [selectedId])

  return (
    <div ref={containerRef} className="flex flex-col gap-4">
      <div className="overflow-x-auto">
        <div className="relative" style={{ width, height }}>
          <svg
            className="absolute inset-0"
            width={width}
            height={height}
            viewBox={`0 0 ${width} ${height}`}
          >
            {edges.map((edge, index) => {
              const from = byId[edge.from]
              const to = byId[edge.to]
              if (!from || !to) return null
              return (
                <motion.path
                  key={`${edge.from}-${edge.to}`}
                  d={edgePath(from, to, edge.kind)}
                  fill="none"
                  strokeWidth={edge.kind === "trunk" ? 2.5 : 2}
                  strokeDasharray={edge.kind === "branch" ? "1 7" : undefined}
                  strokeLinecap="round"
                  className={edge.kind === "trunk" ? "stroke-primary/50" : "stroke-border"}
                  initial={{ pathLength: 0, opacity: 0 }}
                  animate={{ pathLength: 1, opacity: 1 }}
                  transition={{ duration: 0.5, delay: index * 0.04, ease: "easeOut" }}
                />
              )
            })}
          </svg>

          {nodes.map((node, index) => {
            const done = node.step.completed
            const isMain = node.kind === "main"
            const isSelected = node.step.id === selectedId
            return (
              <motion.button
                key={node.step.id}
                type="button"
                onClick={() => setSelectedId(node.step.id)}
                initial={{ opacity: 0, scale: 0.85 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3, delay: index * 0.03, ease: "easeOut" }}
                whileHover={{ scale: 1.04 }}
                whileTap={{ scale: 0.97 }}
                className={`absolute flex items-start gap-1.5 overflow-hidden rounded-lg border-2 px-3 py-2 text-left shadow-sm transition-colors ${
                  done
                    ? "border-emerald-500 bg-emerald-950"
                    : isMain
                      ? "border-primary bg-primary text-primary-foreground"
                      : "border-border bg-card hover:border-foreground/40"
                } ${isSelected ? (done ? "ring-2 ring-emerald-400" : "ring-2 ring-primary") : ""}`}
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
              </motion.button>
            )
          })}
        </div>
      </div>

      <div ref={panelRef}>
        {visibleSelected ? (
          <div key={visibleSelected.step.id} className="animate-in fade-in duration-200 ease-out">
            <Card>
              <CardContent className="flex flex-col gap-3 pt-6">
                <div className="flex items-center justify-between gap-2">
                  <h3 className="font-medium">{visibleSelected.step.title}</h3>
                  {visibleSelected.step.completed && (
                    <Badge variant="secondary" className="gap-1">
                      <CheckCircle2 className="size-3 text-emerald-500" />
                      Concluído
                    </Badge>
                  )}
                </div>
                {selectedPins.length > 0 && (
                  <div className="flex flex-wrap gap-1.5">
                    {selectedPins.map((pin) => (
                      <Badge key={pin.id} variant="outline" className="gap-1 pr-1">
                        {courseTitle(courses, pin.courseId)}
                        {pin.rating != null ? ` · ${pin.rating}/5` : ""}
                        <button
                          type="button"
                          title="Despinar curso"
                          onClick={() => unpinCourse(visibleSelected.step.id, pin.id, onChanged)}
                          className="rounded-full p-0.5 hover:bg-foreground/10"
                        >
                          <X className="size-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                )}
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  className={cn("w-fit gap-1.5", BUTTON_REVEAL_CLASS)}
                  onMouseMove={onMouseMove}
                  onClick={() => toggleStepCompleted(roadmapId, visibleSelected.step, onChanged)}
                >
                  {visibleSelected.step.completed ? "Desmarcar conclusão" : "Marcar como concluído"}
                </Button>
                <PinPanel stepId={visibleSelected.step.id} courses={courses} onPinned={onChanged} />
              </CardContent>
            </Card>
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">
            Clique numa etapa do diagrama para pinar um curso.
          </p>
        )}
      </div>
    </div>
  )
}

// ---------- Container: alterna entre as duas visoes ----------

export function RoadmapDiagram(props: Props) {
  const [viewMode, setViewMode] = useState<ViewMode>(() => {
    if (typeof window === "undefined") return "graph"
    return (localStorage.getItem(VIEW_MODE_KEY) as ViewMode | null) ?? "graph"
  })

  useEffect(() => {
    localStorage.setItem(VIEW_MODE_KEY, viewMode)
  }, [viewMode])

  const hasSteps = props.steps.some((s) => !s.parentStepId)
  if (!hasSteps) {
    return <p className="text-sm text-muted-foreground">Nenhuma etapa ainda.</p>
  }

  return (
    <div className="relative rounded-lg border bg-muted/20 p-3 pt-14">
      <div className="absolute right-3 top-3 z-20 flex gap-1 rounded-md border bg-background/95 p-1 shadow-sm">
        <Button
          type="button"
          size="icon"
          variant={viewMode === "graph" ? "default" : "ghost"}
          className="size-7"
          title="Diagrama"
          onClick={() => setViewMode("graph")}
        >
          <GitBranch className="size-4" />
        </Button>
        <Button
          type="button"
          size="icon"
          variant={viewMode === "list" ? "default" : "ghost"}
          className="size-7"
          title="Lista"
          onClick={() => setViewMode("list")}
        >
          <List className="size-4" />
        </Button>
      </div>

      {viewMode === "graph" ? <GraphView {...props} /> : <ListView {...props} />}
    </div>
  )
}
