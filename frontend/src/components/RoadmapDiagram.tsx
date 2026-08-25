import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react"
import type { FormEvent } from "react"
import { CheckCircle2, X } from "lucide-react"
import { AnimatePresence, motion } from "motion/react"
import { api } from "@/lib/api"
import { useSpotlight } from "@/hooks/useSpotlight"
import { BUTTON_REVEAL_CLASS, cn } from "@/lib/utils"
import type { CourseSummary, Pin, RoadmapStep, StepStatus } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { ScrollProgress } from "@/components/ui/scroll-progress"
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
  predefined: boolean
  onChanged: () => Promise<void>
}

function setStepStatus(
  roadmapId: string,
  step: RoadmapStep,
  status: StepStatus | null,
  onChanged: () => Promise<void>,
) {
  return api.patch(`/roadmaps/${roadmapId}/steps/${step.id}`, { status }).then(onChanged)
}

function unpinCourse(stepId: string, pinId: string, onChanged: () => Promise<void>) {
  return api.delete(`/steps/${stepId}/pins/${pinId}`).then(onChanged)
}

function courseTitle(courses: CourseSummary[], courseId: string) {
  return courses.find((c) => c.id === courseId)?.title ?? "Curso"
}

function addResource(stepId: string, label: string, url: string, onChanged: () => Promise<void>) {
  return api.post(`/steps/${stepId}/resources`, { label, url }).then(onChanged)
}

function removeResource(stepId: string, resourceId: string, onChanged: () => Promise<void>) {
  return api.delete(`/steps/${stepId}/resources/${resourceId}`).then(onChanged)
}

const PANEL_TRANSITION = { duration: 0.45, ease: "easeInOut" as const }

function revealIfHidden(el: HTMLElement | null) {
  if (!el) return
  const hidden = el.getBoundingClientRect().bottom - window.innerHeight
  if (hidden > 0) {
    window.scrollBy({ top: hidden + 24, behavior: "smooth" })
  }
}

function MeasuredPanel({
  onAnimationComplete,
  children,
}: {
  onAnimationComplete?: () => void
  children: React.ReactNode
}) {
  const innerRef = useRef<HTMLDivElement>(null)
  const [contentHeight, setContentHeight] = useState(0)

  useLayoutEffect(() => {
    const el = innerRef.current
    if (!el) return
    setContentHeight(el.scrollHeight)
    const observer = new ResizeObserver((entries) => {
      setContentHeight(entries[0].contentRect.height)
    })
    observer.observe(el)
    return () => observer.disconnect()
  }, [])

  return (
    <motion.div
      initial={{ height: 0, opacity: 0 }}
      animate={{ height: contentHeight, opacity: 1 }}
      exit={{ height: 0, opacity: 0 }}
      transition={PANEL_TRANSITION}
      className="overflow-hidden"
      onAnimationComplete={onAnimationComplete}
    >
      <div ref={innerRef}>{children}</div>
    </motion.div>
  )
}

function useCloseOnClickOutside(
  active: boolean,
  ref: React.RefObject<HTMLElement | null>,
  onClose: () => void,
) {
  useEffect(() => {
    if (!active) return
    function handleClickOutside(event: MouseEvent) {
      const target = event.target as HTMLElement
      if (target.closest('[data-slot="select-content"]')) return
      if (ref.current && !ref.current.contains(target)) {
        onClose()
      }
    }
    document.addEventListener("mousedown", handleClickOutside)
    return () => document.removeEventListener("mousedown", handleClickOutside)
  }, [active, ref, onClose])
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
        rating: null,
        status: null,
      })
      setCourseId("")
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
      <Button type="button" size="sm" className="h-8" onClick={pin} disabled={saving}>
        Pinar
      </Button>
    </div>
  )
}

function ResourceForm({
  stepId,
  onAdded,
}: {
  stepId: string
  onAdded: () => Promise<void>
}) {
  const [label, setLabel] = useState("")
  const [url, setUrl] = useState("")
  const [saving, setSaving] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!label.trim() || !url.trim()) return
    setSaving(true)
    try {
      await addResource(stepId, label.trim(), url.trim(), onAdded)
      setLabel("")
      setUrl("")
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={onSubmit} className="flex flex-wrap items-center gap-2" onClick={(e) => e.stopPropagation()}>
      <Input
        placeholder="Título do link"
        value={label}
        onChange={(e) => setLabel(e.target.value)}
        className="h-8 w-32 text-xs"
      />
      <Input
        placeholder="URL"
        value={url}
        onChange={(e) => setUrl(e.target.value)}
        className="h-8 w-40 text-xs"
      />
      <Button type="submit" size="sm" className="h-8" disabled={saving}>
        Adicionar link
      </Button>
    </form>
  )
}

// ---------- Visão "grafo": layout calculado com conectores em curva ----------

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

function GraphView({ roadmapId, steps, pinsByStep, courses, predefined, onChanged }: Props) {
  const { nodes, edges, width, height } = useMemo(() => computeGraphLayout(steps), [steps])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const panelOuterRef = useRef<HTMLDivElement>(null)
  const canvasScrollRef = useRef<HTMLDivElement>(null)

  const byId = Object.fromEntries(nodes.map((n) => [n.step.id, n]))
  const selected = selectedId ? byId[selectedId] : null
  const selectedPins = selected ? (pinsByStep[selected.step.id] ?? []) : []
  const { onMouseMove } = useSpotlight()
  const closeSelected = useCallback(() => setSelectedId(null), [])

  useCloseOnClickOutside(selectedId !== null, containerRef, closeSelected)

  const prevSelectedIdRef = useRef<string | null>(null)
  const isFreshOpen = selectedId !== null && prevSelectedIdRef.current === null
  useEffect(() => {
    prevSelectedIdRef.current = selectedId
  }, [selectedId])

  const edgesSvg = useMemo(
    () => (
      <svg className="absolute inset-0" width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
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
    ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [edges, width, height],
  )

  return (
    <div ref={containerRef} className="flex flex-col gap-4">
      <div className="relative">
        <div ref={canvasScrollRef} className="overflow-x-auto">
          <div className="relative" style={{ width, height }}>
            {edgesSvg}

            {nodes.map((node, index) => {
              const status = node.step.status
              const isMain = node.kind === "main"
              const isSelected = node.step.id === selectedId
              const statusClass =
                status === "DONE"
                  ? "border-emerald-500 bg-emerald-100 text-emerald-950 dark:bg-emerald-950 dark:text-emerald-50"
                  : status === "LEARNING"
                    ? "border-amber-500 bg-amber-100 text-amber-950 dark:bg-amber-950 dark:text-amber-50"
                    : status === "SKIPPED"
                      ? "border-muted-foreground/40 bg-muted text-muted-foreground opacity-70"
                      : isMain
                        ? "border-primary bg-primary text-primary-foreground"
                        : "border-border bg-card hover:border-foreground/40"
              const ringClass = isSelected
                ? status === "DONE"
                  ? "ring-2 ring-emerald-400"
                  : status === "LEARNING"
                    ? "ring-2 ring-amber-400"
                    : "ring-2 ring-primary"
                : ""
              return (
                <motion.button
                  key={node.step.id}
                  type="button"
                  onClick={() => setSelectedId(node.step.id)}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ duration: 0.3, delay: index * 0.03, ease: "easeOut" }}
                  className={`absolute flex items-start gap-1.5 overflow-hidden rounded-lg border-2 px-3 py-2 text-left shadow-sm transition-colors hover:brightness-110 ${statusClass} ${ringClass}`}
                  style={{
                    left: node.x - node.w / 2,
                    top: node.y - node.h / 2,
                    width: node.w,
                    height: node.h,
                  }}
                >
                  {status === "DONE" && (
                    <CheckCircle2 className="size-4 shrink-0 text-emerald-600 dark:text-emerald-500" />
                  )}
                  <span
                    className={`line-clamp-2 min-w-0 leading-snug font-medium ${isMain ? "text-sm" : "text-xs"} ${status === "SKIPPED" ? "line-through" : ""}`}
                  >
                    {node.step.title}
                  </span>
                </motion.button>
              )
            })}
          </div>
        </div>
        <ScrollProgress containerRef={canvasScrollRef} axis="x" />
      </div>

      <div ref={panelOuterRef} className="relative">
        <AnimatePresence mode="popLayout" initial={false}>
          {selected ? (
            <MeasuredPanel
              key="content"
              onAnimationComplete={() => isFreshOpen && revealIfHidden(panelOuterRef.current)}
            >
              <Card>
                <CardContent className="flex flex-col gap-3 pt-6">
                  <div className="flex items-center justify-between gap-2">
                    <h3 className="font-medium">{selected.step.title}</h3>
                    {selected.step.status === "DONE" && (
                      <Badge variant="secondary" className="gap-1">
                        <CheckCircle2 className="size-3 text-emerald-500" />
                        Concluído
                      </Badge>
                    )}
                  </div>

                  {(selected.step.description || selected.step.resources.length > 0) && (
                    <div className="flex flex-col gap-2 rounded-md bg-muted/40 p-3">
                      {selected.step.description && (
                        <p className="text-sm leading-relaxed">{selected.step.description}</p>
                      )}
                      {selected.step.resources.length > 0 && (
                        <ul className="flex flex-col gap-1">
                          {selected.step.resources.map((resource) => (
                            <li key={resource.id} className="flex items-center gap-1.5">
                              <a
                                href={resource.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-sm text-primary underline-offset-2 hover:underline"
                              >
                                {resource.label}
                              </a>
                              {!predefined && (
                                <button
                                  type="button"
                                  title="Remover link"
                                  onClick={() => removeResource(selected.step.id, resource.id, onChanged)}
                                  className="rounded-full p-0.5 text-muted-foreground hover:bg-foreground/10"
                                >
                                  <X className="size-3" />
                                </button>
                              )}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  )}

                  {!predefined && <ResourceForm stepId={selected.step.id} onAdded={onChanged} />}

                  {selectedPins.length > 0 && (
                    <div className="flex flex-wrap gap-1.5">
                      {selectedPins.map((pin) => (
                        <Badge key={pin.id} variant="outline" className="gap-1 pr-1">
                          {courseTitle(courses, pin.courseId)}
                          <button
                            type="button"
                            title="Despinar curso"
                            onClick={() => unpinCourse(selected.step.id, pin.id, onChanged)}
                            className="rounded-full p-0.5 hover:bg-foreground/10"
                          >
                            <X className="size-3" />
                          </button>
                        </Badge>
                      ))}
                    </div>
                  )}
                  <Select
                    value={selected.step.status ?? ""}
                    onValueChange={(value) => setStepStatus(roadmapId, selected.step, value as StepStatus, onChanged)}
                  >
                    <SelectTrigger className={cn("h-8 w-fit text-xs", BUTTON_REVEAL_CLASS)} onMouseMove={onMouseMove}>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="LEARNING">Aprendendo</SelectItem>
                      <SelectItem value="DONE">Concluído</SelectItem>
                      <SelectItem value="SKIPPED">Pulado</SelectItem>
                    </SelectContent>
                  </Select>
                  <PinPanel stepId={selected.step.id} courses={courses} onPinned={onChanged} />
                </CardContent>
              </Card>
            </MeasuredPanel>
          ) : (
            <MeasuredPanel key="hint">
              <p className="text-sm text-muted-foreground">
                Clique numa etapa do diagrama para pinar um curso.
              </p>
            </MeasuredPanel>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}

// ---------- Container ----------

export function RoadmapDiagram(props: Props) {
  const hasSteps = props.steps.some((s) => !s.parentStepId)
  if (!hasSteps) {
    return <p className="text-sm text-muted-foreground">Nenhuma etapa ainda.</p>
  }

  return (
    <div className="rounded-lg border bg-muted/20 p-3">
      <GraphView {...props} />
    </div>
  )
}
