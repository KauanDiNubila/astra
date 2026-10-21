import { useEffect, useMemo, useRef, useState } from "react"
import type { FormEvent } from "react"
import { CalendarIcon, Minus, Pencil, Plus, Timer, Trash2 } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { invalidateStudyStats } from "@/lib/queryClient"
import { useGitHub } from "@/context/GitHubContext"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatMinutes } from "@/lib/format"
import { dayKey } from "@/lib/studyStats"
import { cn } from "@/lib/utils"
import type { Session } from "@/lib/types"
import { BigDurationInput } from "@/components/BigDurationInput"
import { CategoryPicker } from "@/components/CategoryPicker"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PillToggleButton } from "@/components/PillToggleButton"
import { PomodoroTimer } from "@/components/PomodoroTimer"
import { SessionGithubActivity } from "@/components/SessionGithubActivity"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Textarea } from "@/components/ui/textarea"

const PAGE_SIZE = 30

function combineDateWithNow(date: Date) {
  const now = new Date()
  const combined = new Date(date)
  combined.setHours(now.getHours(), now.getMinutes(), 0, 0)
  return combined
}

type RegisterMode = "pomodoro" | "manual"

const QUICK_MINUTES = [25, 50, 60, 90, 120]

function dayLabel(key: string) {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)
  if (key === dayKey(today)) return "Hoje"
  if (key === dayKey(yesterday)) return "Ontem"
  const [year, month, day] = key.split("-").map(Number)
  const date = new Date(year, month - 1, day)
  const label = date.toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "short",
    ...(year !== today.getFullYear() ? { year: "numeric" } : {}),
  })
  return label.charAt(0).toUpperCase() + label.slice(1).replace(".", "")
}

function timeLabel(iso: string) {
  return new Date(iso).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })
}

function groupByDay(sessions: Session[]) {
  const groups: { day: string; total: number; items: Session[] }[] = []
  for (const session of sessions) {
    const day = dayKey(new Date(session.startedAt))
    const last = groups[groups.length - 1]
    if (last && last.day === day) {
      last.items.push(session)
      last.total += session.focusedMinutes
    } else {
      groups.push({ day, total: session.focusedMinutes, items: [session] })
    }
  }
  return groups
}

export function SessionsPage() {
  const { sessionSavedAt, categories, courses, loadCategories } = usePomodoro()
  const { status: githubStatus } = useGitHub()
  const [sessions, setSessions] = useState<Session[]>([])
  const [loading, setLoading] = useState(true)
  const [registerMode, setRegisterMode] = useState<RegisterMode>("pomodoro")
  const [expandedSessionId, setExpandedSessionId] = useState<string | null>(null)

  const [categoryId, setCategoryId] = useState("")
  const [minutes, setMinutes] = useState(25)
  const [selectedDate, setSelectedDate] = useState<Date>(() => new Date())
  const [datePickerOpen, setDatePickerOpen] = useState(false)
  const [note, setNote] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmSessionId, setConfirmSessionId] = useState<string | null>(null)
  const [deletingSessionId, setDeletingSessionId] = useState<string | null>(null)
  const [hasMore, setHasMore] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const confirmSessionTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  function loadSessions() {
    return api.get<Session[]>("/sessions", { params: { page: 0, size: PAGE_SIZE } }).then((res) => {
      setSessions(res.data)
      setHasMore(res.data.length === PAGE_SIZE)
    })
  }

  async function loadMoreSessions() {
    setLoadingMore(true)
    try {
      const page = Math.floor(sessions.length / PAGE_SIZE)
      const res = await api.get<Session[]>("/sessions", { params: { page, size: PAGE_SIZE } })
      setSessions((prev) => {
        const known = new Set(prev.map((session) => session.id))
        return [...prev, ...res.data.filter((session) => !known.has(session.id))]
      })
      setHasMore(res.data.length === PAGE_SIZE)
    } catch {
      toast.error("Não foi possível carregar mais sessões.")
    } finally {
      setLoadingMore(false)
    }
  }

  useEffect(() => {
    // Categorias vêm do PomodoroContext (já buscadas uma vez pra casca
    // protegida inteira) — não refazer a busca aqui a cada visita à página.
    loadSessions().finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (sessionSavedAt !== null) loadSessions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionSavedAt])

  useEffect(() => {
    return () => {
      if (confirmSessionTimeoutRef.current) clearTimeout(confirmSessionTimeoutRef.current)
    }
  }, [])

  function armSessionDelete(id: string) {
    setConfirmSessionId(id)
    if (confirmSessionTimeoutRef.current) clearTimeout(confirmSessionTimeoutRef.current)
    confirmSessionTimeoutRef.current = setTimeout(() => setConfirmSessionId(null), 3000)
  }

  async function confirmSessionDelete(id: string) {
    if (confirmSessionTimeoutRef.current) clearTimeout(confirmSessionTimeoutRef.current)
    setConfirmSessionId(null)
    setDeletingSessionId(id)
    try {
      await api.delete(`/sessions/${id}`)
      setSessions((prev) => prev.filter((s) => s.id !== id))
      void invalidateStudyStats()
    } catch {
      toast.error("Não foi possível remover a sessão.")
    } finally {
      setDeletingSessionId(null)
    }
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    if (!categoryId) {
      setError("Escolha uma categoria.")
      return
    }
    setSaving(true)
    try {
      await api.post<Session>("/sessions", {
        categoryId,
        focusedMinutes: minutes,
        startedAt: combineDateWithNow(selectedDate).toISOString(),
        note: note.trim() || null,
      })
      await loadSessions()
      void invalidateStudyStats()
      setMinutes(25)
      setNote("")
      setSelectedDate(new Date())
    } catch {
      setError("Não foi possível registrar a sessão.")
    } finally {
      setSaving(false)
    }
  }

  const groups = useMemo(() => groupByDay(sessions), [sessions])

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Sessões</h1>
          <p className="mt-1 text-sm text-muted-foreground">Cronometre um foco ou lance um estudo que já aconteceu.</p>
        </div>
        <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
          <PillToggleButton
            active={registerMode === "pomodoro"}
            layoutId="sessions-mode-pill"
            onClick={() => setRegisterMode("pomodoro")}
            className="h-7 gap-1.5 px-2.5"
          >
            <Timer className="size-3.5" />
            Pomodoro
          </PillToggleButton>
          <PillToggleButton
            active={registerMode === "manual"}
            layoutId="sessions-mode-pill"
            onClick={() => setRegisterMode("manual")}
            className="h-7 gap-1.5 px-2.5"
          >
            <Pencil className="size-3.5" />
            Manual
          </PillToggleButton>
        </div>
      </div>

      <section className="rounded-2xl border p-5 sm:p-8">
        {registerMode === "pomodoro" ? (
          <PomodoroTimer />
        ) : (
          <form onSubmit={onSubmit} className="flex flex-col gap-8">
            <div className="flex flex-col items-center gap-2 py-6">
              <span className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Tempo de foco</span>
              <div className="flex items-center gap-4">
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  className="size-10 rounded-full"
                  aria-label="Diminuir 5 minutos"
                  disabled={minutes <= 5}
                  onClick={() => setMinutes((m) => Math.max(5, m - 5))}
                >
                  <Minus className="size-4" />
                </Button>
                <div className="min-w-60">
                  <BigDurationInput value={minutes} onChange={setMinutes} />
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  className="size-10 rounded-full"
                  aria-label="Aumentar 5 minutos"
                  disabled={minutes >= 1440}
                  onClick={() => setMinutes((m) => Math.min(1440, m + 5))}
                >
                  <Plus className="size-4" />
                </Button>
              </div>
              <div className="mt-2 flex flex-wrap justify-center gap-1.5">
                {QUICK_MINUTES.map((value) => (
                  <button
                    key={value}
                    type="button"
                    onClick={() => setMinutes(value)}
                    className={cn(
                      "h-7 rounded-full border px-3 text-xs transition-colors hover:bg-accent",
                      minutes === value ? "border-foreground/40 bg-accent text-foreground" : "text-muted-foreground",
                    )}
                  >
                    {formatMinutes(value)}
                  </button>
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-4 border-t pt-6">
              <div className="flex flex-wrap items-center gap-2">
                <CategoryPicker chip categories={categories} value={categoryId} onChange={setCategoryId} onRefresh={loadCategories} />
                <Popover open={datePickerOpen} onOpenChange={setDatePickerOpen}>
                  <PopoverTrigger asChild>
                    <button
                      type="button"
                      className="flex h-8 items-center gap-2 rounded-full border px-3 text-sm transition-colors hover:bg-accent data-[state=open]:bg-accent"
                    >
                      <CalendarIcon className="size-3.5 text-muted-foreground" />
                      {dayLabel(dayKey(selectedDate))}
                    </button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={selectedDate}
                      disabled={{ after: new Date() }}
                      onSelect={(date) => {
                        if (!date) return
                        setSelectedDate(date)
                        setDatePickerOpen(false)
                      }}
                    />
                  </PopoverContent>
                </Popover>
                <Button type="submit" size="sm" className="ml-auto h-8 rounded-full px-4" disabled={saving}>
                  {saving ? "Salvando..." : `Registrar ${formatMinutes(minutes)}`}
                </Button>
              </div>
              <Textarea
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="O que você estudou? (opcional)"
                aria-label="Nota"
                className="min-h-20 resize-none"
              />
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
          </form>
        )}
      </section>

      <section className="flex flex-col gap-6">
        <h2 className="text-sm font-medium">Histórico</h2>
        {sessions.length === 0 ? (
          <p className="rounded-2xl border border-dashed px-5 py-10 text-center text-sm text-muted-foreground">
            Nenhuma sessão registrada ainda. Comece um foco acima.
          </p>
        ) : (
          groups.map((group) => (
            <div key={group.day} className="flex flex-col">
              <div className="flex items-baseline justify-between border-b pb-2">
                <span className="text-sm font-medium">{dayLabel(group.day)}</span>
                <span className="text-xs text-muted-foreground tabular-nums">{formatMinutes(group.total)}</span>
              </div>
              <ul className="flex flex-col">
                {group.items.map((s) => {
                  const category = categories.find((c) => c.id === s.categoryId)
                  const course = s.courseId ? courses.find((c) => c.id === s.courseId) : undefined
                  const expandable = !!githubStatus?.connected
                  return (
                    <li key={s.id} className="flex flex-col border-b border-border/50 last:border-b-0">
                      <div
                        className={cn(
                          "group -mx-2 flex items-center gap-4 rounded-md px-2 py-3 transition-colors",
                          expandable && "cursor-pointer hover:bg-muted/40",
                        )}
                        onClick={() => expandable && setExpandedSessionId((prev) => (prev === s.id ? null : s.id))}
                      >
                        <span className="w-20 shrink-0 font-medium tabular-nums">{formatMinutes(s.focusedMinutes)}</span>
                        <div className="flex min-w-0 flex-1 flex-col gap-0.5 sm:flex-row sm:items-center sm:gap-3">
                          <span className="flex shrink-0 items-center gap-2 text-sm">
                            <span
                              className="size-2 rounded-full bg-muted-foreground/60"
                              style={category?.color ? { backgroundColor: category.color } : undefined}
                            />
                            {category?.name ?? "Sem categoria"}
                            {course && <span className="text-muted-foreground">· {course.title}</span>}
                          </span>
                          {s.note && <span className="min-w-0 truncate text-sm text-muted-foreground">{s.note}</span>}
                        </div>
                        <span className="shrink-0 text-xs text-muted-foreground tabular-nums">{timeLabel(s.startedAt)}</span>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className={cn(
                            "h-7 shrink-0 px-2 transition-opacity",
                            confirmSessionId === s.id
                              ? "text-destructive opacity-100"
                              : "text-muted-foreground opacity-0 group-hover:opacity-100 hover:text-destructive focus-visible:opacity-100 max-md:opacity-100",
                          )}
                          disabled={deletingSessionId === s.id}
                          aria-label="Excluir sessão"
                          onClick={(e) => {
                            e.stopPropagation()
                            if (confirmSessionId === s.id) confirmSessionDelete(s.id)
                            else armSessionDelete(s.id)
                          }}
                        >
                          {confirmSessionId === s.id ? "Confirmar?" : <Trash2 className="size-3.5" />}
                        </Button>
                      </div>
                      {expandedSessionId === s.id && (
                        <div className="mb-3 rounded-md bg-muted/30 px-3 py-2">
                          <SessionGithubActivity
                            sessionId={s.id}
                            linkedRepositoryId={s.githubRepositoryId}
                            onLinked={(repositoryId) =>
                              setSessions((prev) =>
                                prev.map((session) =>
                                  session.id === s.id ? { ...session, githubRepositoryId: repositoryId } : session,
                                ),
                              )
                            }
                          />
                        </div>
                      )}
                    </li>
                  )
                })}
              </ul>
            </div>
          ))
        )}
        {hasMore && (
          <Button type="button" variant="outline" className="w-full" disabled={loadingMore} onClick={loadMoreSessions}>
            {loadingMore ? "Carregando..." : "Carregar mais"}
          </Button>
        )}
      </section>
    </div>
  )
}
