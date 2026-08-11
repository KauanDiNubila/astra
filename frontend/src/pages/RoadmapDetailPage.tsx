import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseSummary, Pin, RoadmapDetail } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

function PinForm({
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

  if (courses.length === 0) {
    return <p className="text-xs text-muted-foreground">Crie um curso para poder pinar.</p>
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <Select value={courseId} onValueChange={setCourseId}>
        <SelectTrigger className="w-48">
          <SelectValue placeholder="Pinar um curso" />
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
        <SelectTrigger className="w-28">
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
      <Button type="button" variant="outline" size="sm" onClick={pin} disabled={saving}>
        Pinar
      </Button>
    </div>
  )
}

export function RoadmapDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [roadmap, setRoadmap] = useState<RoadmapDetail | null>(null)
  const [courses, setCourses] = useState<CourseSummary[]>([])
  const [pinsByStep, setPinsByStep] = useState<Record<string, Pin[]>>({})
  const [loading, setLoading] = useState(true)
  const [stepTitle, setStepTitle] = useState("")

  async function loadRoadmap() {
    const res = await api.get<RoadmapDetail>(`/roadmaps/${id}`)
    setRoadmap(res.data)
    const entries = await Promise.all(
      res.data.steps.map((step) =>
        api.get<Pin[]>(`/steps/${step.id}/pins`).then((r) => [step.id, r.data] as const),
      ),
    )
    setPinsByStep(Object.fromEntries(entries))
  }

  useEffect(() => {
    Promise.all([
      loadRoadmap(),
      api.get<CourseSummary[]>("/courses").then((res) => setCourses(res.data)),
    ]).finally(() => setLoading(false))
  }, [id])

  async function addStep(event: FormEvent) {
    event.preventDefault()
    if (!stepTitle.trim() || !roadmap) return
    await api.post(`/roadmaps/${id}/steps`, {
      title: stepTitle.trim(),
      position: roadmap.steps.length + 1,
    })
    setStepTitle("")
    await loadRoadmap()
  }

  function courseName(courseId: string) {
    return courses.find((c) => c.id === courseId)?.title ?? "Curso"
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  if (!roadmap) {
    return <p className="text-destructive">Roadmap nao encontrado.</p>
  }

  return (
    <div className="flex flex-col gap-6">
      <Link to="/roadmaps" className="text-sm text-muted-foreground hover:text-foreground">
        &larr; Roadmaps
      </Link>
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-semibold">{roadmap.title}</h1>
        {roadmap.predefined && <Badge variant="secondary">Pre-definido</Badge>}
      </div>

      <div className="flex flex-col gap-4">
        {roadmap.steps.map((step) => (
          <Card key={step.id}>
            <CardHeader>
              <CardTitle className="text-base">
                {step.position}. {step.title}
              </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              {(pinsByStep[step.id] ?? []).map((pin) => (
                <div key={pin.id} className="flex items-center gap-2 text-sm">
                  <Badge>{courseName(pin.courseId)}</Badge>
                  {pin.rating != null && (
                    <span className="text-muted-foreground">Nota {pin.rating}/5</span>
                  )}
                </div>
              ))}
              <PinForm stepId={step.id} courses={courses} onPinned={loadRoadmap} />
            </CardContent>
          </Card>
        ))}
      </div>

      {!roadmap.predefined && (
        <form onSubmit={addStep} className="flex gap-2">
          <Input
            placeholder="Nova etapa"
            value={stepTitle}
            onChange={(e) => setStepTitle(e.target.value)}
          />
          <Button type="submit" variant="outline">
            Adicionar etapa
          </Button>
        </form>
      )}
    </div>
  )
}
