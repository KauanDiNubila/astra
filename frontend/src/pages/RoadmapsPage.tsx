import { useEffect, useState } from "react"
import type { FormEvent, ReactNode } from "react"
import { useQueries, useQuery } from "@tanstack/react-query"
import { motion, useReducedMotion } from "motion/react"
import { ArrowUpRight, Map as MapIcon, Plus } from "lucide-react"
import { Link } from "react-router-dom"
import { api } from "@/lib/api"
import { fetchJson, invalidateRoadmaps, queryKeys } from "@/lib/queryClient"
import { milestoneStates, roadmapProgress } from "@/lib/roadmapProgress"
import { cn, gridItem, gridStagger } from "@/lib/utils"
import type { Roadmap, RoadmapDetail } from "@/lib/types"
import { CardGridSkeleton } from "@/components/CardGridSkeleton"
import { FormModal } from "@/components/FormModal"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

function RoadmapCard({ roadmap, detail }: { roadmap: Roadmap; detail: RoadmapDetail | undefined }) {
  const reducedMotion = useReducedMotion()
  const progress = detail ? roadmapProgress(detail.steps) : null
  const states = detail ? milestoneStates(detail.steps) : []

  return (
    <Link
      to={`/roadmaps/${roadmap.id}`}
      className="group flex h-full flex-col gap-6 rounded-2xl border p-5 transition-colors duration-200 hover:border-foreground/20 hover:bg-muted/20 sm:p-6"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-xs font-medium tracking-wide text-muted-foreground uppercase">
            {progress ? `${progress.milestones} marcos · ${progress.topics} tópicos` : (roadmap.source ?? "Roadmap")}
          </p>
          <h2 className="mt-1.5 line-clamp-2 font-semibold tracking-tight break-words">{roadmap.title}</h2>
        </div>
        <ArrowUpRight className="size-4 shrink-0 text-muted-foreground opacity-0 transition-all duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100" />
      </div>

      <div className="mt-auto flex flex-col gap-4">
        <div className="flex h-3 items-center" aria-hidden="true">
          {states.map((state, i) => (
            <div key={i} className="flex flex-1 items-center last:flex-none">
              <span
                className={cn(
                  "size-2.5 shrink-0 rounded-full border-2 transition-colors",
                  state === "done" && "border-emerald-500 bg-emerald-500",
                  state === "active" && "border-amber-400 bg-amber-400",
                  state === "todo" && "border-muted-foreground/30",
                )}
              />
              {i < states.length - 1 && (
                <span className={cn("h-px flex-1", state === "done" ? "bg-emerald-500/60" : "bg-border")} />
              )}
            </div>
          ))}
        </div>
        <div className="flex items-end justify-between gap-3">
          <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">
            {progress?.pct ?? 0}
            <span className="ml-0.5 text-lg font-medium text-muted-foreground">%</span>
          </span>
          <span className="text-xs text-muted-foreground tabular-nums">
            {!progress
              ? "Carregando…"
              : progress.total === 0
                ? "Sem etapas ainda"
                : progress.started
                  ? `${progress.done} de ${progress.total} etapas concluídas`
                  : "Ainda não começou"}
          </span>
        </div>
        <div className="h-1.5 overflow-hidden rounded-full bg-muted">
          <motion.div
            className={cn("h-full rounded-full", progress?.pct === 100 ? "bg-emerald-500" : "bg-foreground/80")}
            initial={{ width: reducedMotion ? `${progress?.pct ?? 0}%` : 0 }}
            animate={{ width: `${progress?.pct ?? 0}%` }}
            transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1], delay: 0.1 }}
          />
        </div>
      </div>
    </Link>
  )
}

function NewRoadmapModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [title, setTitle] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setTitle("")
    setError(null)
  }, [open])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) {
      setError("Dê um título ao roadmap.")
      return
    }
    setError(null)
    setSaving(true)
    try {
      await api.post("/roadmaps", { title: title.trim(), source: null })
      await invalidateRoadmaps()
      onClose()
    } catch {
      setError("Não foi possível criar o roadmap.")
    } finally {
      setSaving(false)
    }
  }

  return (
    <FormModal
      open={open}
      title="Novo roadmap"
      submitLabel="Criar roadmap"
      savingLabel="Criando..."
      saving={saving}
      error={error}
      onClose={onClose}
      onSubmit={onSubmit}
    >
      <div className="flex flex-col gap-2">
        <Label htmlFor="roadmap-title">Título</Label>
        <Input
          id="roadmap-title"
          autoFocus
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Minha trilha de backend"
        />
        <p className="text-xs text-muted-foreground">Depois você adiciona os marcos e os tópicos de cada um.</p>
      </div>
    </FormModal>
  )
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-sm font-medium">{title}</h2>
      {children}
    </section>
  )
}

export function RoadmapsPage() {
  const [modalOpen, setModalOpen] = useState(false)
  const roadmapsQuery = useQuery({ queryKey: queryKeys.roadmaps, queryFn: fetchJson<Roadmap[]>("/roadmaps") })
  const roadmaps = roadmapsQuery.data ?? []
  const details = useQueries({
    queries: roadmaps.map((roadmap) => ({
      queryKey: queryKeys.roadmap(roadmap.id),
      queryFn: fetchJson<RoadmapDetail>(`/roadmaps/${roadmap.id}`),
    })),
  })
  const detailById = new Map(roadmaps.map((roadmap, i) => [roadmap.id, details[i]?.data]))

  if (roadmapsQuery.isPending) {
    return <CardGridSkeleton />
  }

  const pctOf = (r: Roadmap) => {
    const detail = detailById.get(r.id)
    return detail ? roadmapProgress(detail.steps).pct : 0
  }
  const byProgress = (a: Roadmap, b: Roadmap) => pctOf(b) - pctOf(a)
  const own = roadmaps.filter((r) => !r.predefined).sort(byProgress)
  const predefined = roadmaps.filter((r) => r.predefined).sort(byProgress)
  const inProgress = roadmaps.filter((r) => {
    const detail = detailById.get(r.id)
    return detail ? roadmapProgress(detail.steps).started : false
  }).length

  const grid = (items: Roadmap[]) => (
    <motion.div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3" variants={gridStagger} initial="hidden" animate="show">
      {items.map((roadmap) => (
        <motion.div key={roadmap.id} variants={gridItem}>
          <RoadmapCard roadmap={roadmap} detail={detailById.get(roadmap.id)} />
        </motion.div>
      ))}
    </motion.div>
  )

  return (
    <div className="flex flex-col gap-10">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Roadmaps</h1>
          <p className="mt-1 text-sm text-muted-foreground tabular-nums">
            {roadmaps.length} {roadmaps.length === 1 ? "trilha" : "trilhas"} · {inProgress} em andamento
          </p>
        </div>
        <Button type="button" className="rounded-full" onClick={() => setModalOpen(true)}>
          <Plus data-icon="inline-start" />
          Novo roadmap
        </Button>
      </div>

      <Section title="Seus roadmaps">
        {own.length > 0 ? (
          grid(own)
        ) : (
          <button
            type="button"
            onClick={() => setModalOpen(true)}
            className="flex items-center gap-4 rounded-2xl border border-dashed px-5 py-5 text-left transition-colors hover:bg-muted/20"
          >
            <MapIcon className="size-5 shrink-0 text-muted-foreground" />
            <span className="flex flex-col">
              <span className="text-sm font-medium">Monte seu próprio roadmap</span>
              <span className="text-sm text-muted-foreground">Organize o que quer aprender em marcos e tópicos.</span>
            </span>
          </button>
        )}
      </Section>

      {predefined.length > 0 && <Section title="Trilhas prontas">{grid(predefined)}</Section>}

      <NewRoadmapModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  )
}
