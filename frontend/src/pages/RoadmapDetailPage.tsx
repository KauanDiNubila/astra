import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { motion } from "motion/react"
import { Plus } from "lucide-react"
import { api } from "@/lib/api"
import { invalidateRoadmaps } from "@/lib/queryClient"
import { formatStructure, roadmapProgress } from "@/lib/roadmapProgress"
import { cn } from "@/lib/utils"
import { usePomodoro } from "@/context/PomodoroContext"
import type { Pin, RoadmapDetail, RoadmapStep } from "@/lib/types"
import { FormModal } from "@/components/FormModal"
import { PageSkeleton } from "@/components/PageSkeleton"
import { RoadmapDiagram } from "@/components/RoadmapDiagram"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
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
  const { courses } = usePomodoro()
  const [roadmap, setRoadmap] = useState<RoadmapDetail | null>(null)
  const [pinsByStep, setPinsByStep] = useState<Record<string, Pin[]>>({})
  const [loading, setLoading] = useState(true)
  const [stepTitle, setStepTitle] = useState("")
  const [stepDescription, setStepDescription] = useState("")
  const [parentStepId, setParentStepId] = useState(NO_PARENT)
  const [stepModalOpen, setStepModalOpen] = useState(false)
  const [savingStep, setSavingStep] = useState(false)
  const [stepError, setStepError] = useState<string | null>(null)

  async function loadRoadmap(signal?: AbortSignal) {
    const [roadmapRes, pinsRes] = await Promise.all([
      api.get<RoadmapDetail>(`/roadmaps/${id}`, { signal }),
      api.get<Record<string, Pin[]>>(`/roadmaps/${id}/pins`, { signal }),
    ])
    setRoadmap(roadmapRes.data)
    setPinsByStep(pinsRes.data)
  }

  useEffect(() => {
    if (!id) return
    const controller = new AbortController()
    // Cursos vêm do PomodoroContext — evita refazer a mesma busca aqui.
    loadRoadmap(controller.signal)
      .catch(() => {})
      .finally(() => setLoading(false))
    return () => controller.abort()
  }, [id])

  // Mudança de status manda o step atualizado — só substitui aquele item,
  // sem recarregar o roadmap inteiro. Pin/recurso continuam recarregando
  // tudo (chamam onChanged sem argumento).
  async function handleStepChanged(updatedStep?: RoadmapStep) {
    if (updatedStep) {
      setRoadmap((prev) =>
        prev ? { ...prev, steps: prev.steps.map((s) => (s.id === updatedStep.id ? updatedStep : s)) } : prev,
      )
      void invalidateRoadmaps()
      return
    }
    await loadRoadmap()
    void invalidateRoadmaps()
  }

  async function addStep(event: FormEvent) {
    event.preventDefault()
    if (!roadmap) return
    if (!stepTitle.trim()) {
      setStepError("Dê um título à etapa.")
      return
    }
    setStepError(null)
    setSavingStep(true)
    try {
      const parent = parentStepId === NO_PARENT ? null : parentStepId
      const siblings = roadmap.steps.filter((s) => (s.parentStepId ?? null) === parent)
      await api.post(`/roadmaps/${id}/steps`, {
        title: stepTitle.trim(),
        position: siblings.length + 1,
        parentStepId: parent,
        description: stepDescription.trim() || null,
      })
      await loadRoadmap()
      void invalidateRoadmaps()
      setStepModalOpen(false)
    } catch {
      setStepError("Não foi possível adicionar a etapa.")
    } finally {
      setSavingStep(false)
    }
  }

  function openStepModal() {
    setStepTitle("")
    setStepDescription("")
    setParentStepId(NO_PARENT)
    setStepError(null)
    setStepModalOpen(true)
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
  const progress = roadmapProgress(roadmap.steps)

  return (
    <div className="flex flex-col gap-8">
      <Link to="/roadmaps" className="w-fit text-sm text-muted-foreground transition-colors hover:text-foreground">
        &larr; Roadmaps
      </Link>

      <div className="flex flex-wrap items-end justify-between gap-6">
        <div className="min-w-0">
          <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
            {roadmap.predefined ? "Trilha pronta" : "Seu roadmap"}
            {roadmap.source && ` · ${roadmap.source}`}
          </p>
          <h1 className="mt-1.5 text-2xl font-semibold tracking-tight sm:text-3xl">{roadmap.title}</h1>
          <p className="mt-1 text-sm text-muted-foreground tabular-nums">
            {formatStructure(progress.milestones, progress.topics)}
          </p>
        </div>
        <div className="flex w-full max-w-xs flex-col gap-2">
          <div className="flex items-end justify-between gap-3">
            <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">
              {progress.pct}
              <span className="ml-0.5 text-lg font-medium text-muted-foreground">%</span>
            </span>
            <span className="text-xs text-muted-foreground tabular-nums">
              {progress.done} de {progress.total} etapas
            </span>
          </div>
          <div className="h-1.5 overflow-hidden rounded-full bg-muted">
            <motion.div
              className={cn("h-full rounded-full", progress.pct === 100 ? "bg-emerald-500" : "bg-foreground/80")}
              initial={false}
              animate={{ width: `${progress.pct}%` }}
              transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
            />
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-emerald-500" />
            Concluído
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-amber-500" />
            Aprendendo
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-muted-foreground/40" />
            Pulado
          </span>
          <span className="max-sm:hidden">Clique numa etapa para ver detalhes e marcar o status.</span>
        </div>
        {!roadmap.predefined && (
          <Button type="button" variant="outline" size="sm" className="rounded-full" onClick={openStepModal}>
            <Plus data-icon="inline-start" />
            Etapa
          </Button>
        )}
      </div>

      <RoadmapDiagram
        roadmapId={id!}
        steps={roadmap.steps}
        pinsByStep={pinsByStep}
        courses={courses}
        predefined={roadmap.predefined}
        onChanged={handleStepChanged}
      />

      <FormModal
        open={stepModalOpen}
        title="Nova etapa"
        submitLabel="Adicionar etapa"
        savingLabel="Adicionando..."
        saving={savingStep}
        error={stepError}
        onClose={() => setStepModalOpen(false)}
        onSubmit={addStep}
      >
        <div className="flex flex-col gap-2">
          <Label htmlFor="step-title">Título</Label>
          <Input
            id="step-title"
            autoFocus
            value={stepTitle}
            onChange={(e) => setStepTitle(e.target.value)}
            placeholder="Spring Security"
          />
        </div>
        <div className="flex flex-col gap-2">
          <Label>Onde encaixar</Label>
          <Select value={parentStepId} onValueChange={setParentStepId}>
            <SelectTrigger className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={NO_PARENT}>Novo marco principal</SelectItem>
              {mainSteps.map((step) => (
                <SelectItem key={step.id} value={step.id}>
                  Tópico de: {step.title}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="step-description">Descrição (opcional)</Label>
          <Textarea
            id="step-description"
            value={stepDescription}
            onChange={(e) => setStepDescription(e.target.value)}
            rows={3}
            className="resize-none"
          />
        </div>
      </FormModal>
    </div>
  )
}
