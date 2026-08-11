import { useState } from "react"
import { CheckCircle2 } from "lucide-react"
import { api } from "@/lib/api"
import type { CourseSummary, Pin, RoadmapStep } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
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

function RoadmapNode({
  step,
  index,
  pins,
  courses,
  onPinned,
  variant,
}: {
  step: RoadmapStep
  index?: number
  pins: Pin[]
  courses: CourseSummary[]
  onPinned: () => Promise<void>
  variant: "main" | "sub"
}) {
  const [open, setOpen] = useState(false)
  const done = pins.length > 0
  const isMain = variant === "main"

  return (
    <div
      className={`flex flex-col gap-2 rounded-xl border-2 transition-colors ${
        isMain ? "min-w-56 px-5 py-3" : "min-w-36 px-3 py-2"
      } ${
        done
          ? "border-emerald-500 bg-emerald-500/10"
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
            <Badge key={pin.id} variant="secondary" className="text-[10px]">
              {courseTitle(courses, pin.courseId)}
              {pin.rating != null ? ` · ${pin.rating}/5` : ""}
            </Badge>
          ))}
        </div>
      )}
      {open && <PinPanel stepId={step.id} courses={courses} onPinned={onPinned} />}
    </div>
  )
}

export function RoadmapDiagram({ steps, pinsByStep, courses, onPinned }: Props) {
  const mains = steps
    .filter((s) => !s.parentStepId)
    .sort((a, b) => a.position - b.position)
  const childrenOf = (id: string) =>
    steps.filter((s) => s.parentStepId === id).sort((a, b) => a.position - b.position)

  if (mains.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhuma etapa ainda.</p>
  }

  return (
    <div className="flex flex-col items-center overflow-x-auto py-2">
      {mains.map((main, index) => {
        const children = childrenOf(main.id)
        return (
          <div key={main.id} className="flex w-full flex-col items-center">
            {index > 0 && <div className="h-8 w-px bg-border" />}
            <RoadmapNode
              step={main}
              index={index + 1}
              pins={pinsByStep[main.id] ?? []}
              courses={courses}
              onPinned={onPinned}
              variant="main"
            />
            {children.length > 0 && (
              <>
                <div className="h-6 w-px bg-border" />
                <div className="flex max-w-3xl flex-wrap justify-center gap-3 pb-2">
                  {children.map((child) => (
                    <RoadmapNode
                      key={child.id}
                      step={child}
                      pins={pinsByStep[child.id] ?? []}
                      courses={courses}
                      onPinned={onPinned}
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
