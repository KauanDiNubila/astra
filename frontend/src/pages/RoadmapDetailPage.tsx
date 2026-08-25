import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseSummary, Pin, RoadmapDetail } from "@/lib/types"
import { PageSkeleton } from "@/components/PageSkeleton"
import { RoadmapDiagram } from "@/components/RoadmapDiagram"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"

const NO_PARENT = "none"

export function RoadmapDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [roadmap, setRoadmap] = useState<RoadmapDetail | null>(null)
  const [courses, setCourses] = useState<CourseSummary[]>([])
  const [pinsByStep, setPinsByStep] = useState<Record<string, Pin[]>>({})
  const [loading, setLoading] = useState(true)
  const [stepTitle, setStepTitle] = useState("")
  const [stepDescription, setStepDescription] = useState("")
  const [parentStepId, setParentStepId] = useState(NO_PARENT)

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
    if (!id) return
    Promise.all([
      loadRoadmap(),
      api.get<CourseSummary[]>("/courses").then((res) => setCourses(res.data)),
    ]).finally(() => setLoading(false))
  }, [id])

  async function addStep(event: FormEvent) {
    event.preventDefault()
    if (!stepTitle.trim() || !roadmap) return
    const parent = parentStepId === NO_PARENT ? null : parentStepId
    const siblings = roadmap.steps.filter((s) => (s.parentStepId ?? null) === parent)
    await api.post(`/roadmaps/${id}/steps`, {
      title: stepTitle.trim(),
      position: siblings.length + 1,
      parentStepId: parent,
      description: stepDescription.trim() || null,
    })
    setStepTitle("")
    setStepDescription("")
    await loadRoadmap()
  }

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  if (!roadmap) {
    return <p className="text-destructive">Roadmap não encontrado.</p>
  }

  const mainSteps = roadmap.steps
    .filter((s) => !s.parentStepId)
    .sort((a, b) => a.position - b.position)

  return (
    <div className="flex flex-col gap-6">
      <Link to="/roadmaps" className="text-sm text-muted-foreground hover:text-foreground">
        &larr; Roadmaps
      </Link>
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-semibold">{roadmap.title}</h1>
        {roadmap.predefined && <Badge variant="secondary">Pré-definido</Badge>}
      </div>

      <RoadmapDiagram
        roadmapId={id!}
        steps={roadmap.steps}
        pinsByStep={pinsByStep}
        courses={courses}
        predefined={roadmap.predefined}
        onChanged={loadRoadmap}
      />

      {!roadmap.predefined && (
        <form onSubmit={addStep} className="flex flex-col gap-2">
          <div className="flex flex-wrap gap-2">
            <Input
              placeholder="Título da etapa"
              value={stepTitle}
              onChange={(e) => setStepTitle(e.target.value)}
              className="max-w-56"
            />
            <Select value={parentStepId} onValueChange={setParentStepId}>
              <SelectTrigger className="w-52">
                <SelectValue placeholder="Onde encaixar?" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={NO_PARENT}>Novo marco principal</SelectItem>
                {mainSteps.map((step) => (
                  <SelectItem key={step.id} value={step.id}>
                    Subtópico de: {step.title}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button type="submit" variant="outline">
              Adicionar
            </Button>
          </div>
          <Textarea
            placeholder="Descrição (opcional)"
            value={stepDescription}
            onChange={(e) => setStepDescription(e.target.value)}
            className="max-w-md"
            rows={2}
          />
        </form>
      )}
    </div>
  )
}
