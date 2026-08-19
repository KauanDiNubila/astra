import { useEffect, useRef, useState } from "react"
import type { FormEvent } from "react"
import { CalendarIcon, Pencil, Timer, Trash2 } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatDateTime, formatMinutes, formatMinutesCompact, parseMinutesCompact } from "@/lib/format"
import type { Category, Session } from "@/lib/types"
import { CategoryPicker } from "@/components/CategoryPicker"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PomodoroTimer } from "@/components/PomodoroTimer"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Stepper } from "@/components/ui/stepper"
import { Textarea } from "@/components/ui/textarea"

function combineDateWithNow(date: Date) {
  const now = new Date()
  const combined = new Date(date)
  combined.setHours(now.getHours(), now.getMinutes(), 0, 0)
  return combined
}

type RegisterMode = "pomodoro" | "manual"

export function SessionsPage() {
  const { sessionSavedAt } = usePomodoro()
  const [sessions, setSessions] = useState<Session[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [registerMode, setRegisterMode] = useState<RegisterMode>("pomodoro")

  const [categoryId, setCategoryId] = useState("")
  const [minutes, setMinutes] = useState(25)
  const [selectedDate, setSelectedDate] = useState<Date>(() => new Date())
  const [datePickerOpen, setDatePickerOpen] = useState(false)
  const [note, setNote] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmSessionId, setConfirmSessionId] = useState<string | null>(null)
  const [deletingSessionId, setDeletingSessionId] = useState<string | null>(null)
  const confirmSessionTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  function loadSessions() {
    return api.get<Session[]>("/sessions").then((res) => setSessions(res.data))
  }

  function loadCategories() {
    return api.get<Category[]>("/categories").then((res) => setCategories(res.data))
  }

  useEffect(() => {
    Promise.all([loadSessions(), loadCategories()]).finally(() => setLoading(false))
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
      await loadSessions()
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
      await api.post("/sessions", {
        categoryId,
        focusedMinutes: minutes,
        startedAt: combineDateWithNow(selectedDate).toISOString(),
        note: note.trim() || null,
      })
      setMinutes(25)
      setNote("")
      setSelectedDate(new Date())
      await loadSessions()
    } catch {
      setError("Não foi possível registrar a sessão.")
    } finally {
      setSaving(false)
    }
  }

  function categoryName(id: string) {
    return categories.find((c) => c.id === id)?.name ?? "?"
  }

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Sessões</h1>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between gap-2">
          <CardTitle>Registrar sessão</CardTitle>
          <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
            <Button
              type="button"
              size="sm"
              variant={registerMode === "pomodoro" ? "default" : "ghost"}
              className="h-7 gap-1.5 px-2"
              onClick={() => setRegisterMode("pomodoro")}
            >
              <Timer className="size-3.5" />
              Pomodoro
            </Button>
            <Button
              type="button"
              size="sm"
              variant={registerMode === "manual" ? "default" : "ghost"}
              className="h-7 gap-1.5 px-2"
              onClick={() => setRegisterMode("manual")}
            >
              <Pencil className="size-3.5" />
              Manual
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {registerMode === "pomodoro" ? (
            <PomodoroTimer />
          ) : (
            <form onSubmit={onSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <Label>Categoria</Label>
                <CategoryPicker
                  categories={categories}
                  value={categoryId}
                  onChange={setCategoryId}
                  onRefresh={loadCategories}
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="flex flex-col gap-2">
                  <Label htmlFor="minutes">Tempo de foco</Label>
                  <Stepper
                    id="minutes"
                    editable
                    size="sm"
                    step={5}
                    min={5}
                    max={1440}
                    value={minutes}
                    onChange={setMinutes}
                    formatDisplay={formatMinutesCompact}
                    parseDisplay={parseMinutesCompact}
                    aria-label="Tempo de foco"
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <Label>Quando</Label>
                  <Popover open={datePickerOpen} onOpenChange={setDatePickerOpen}>
                    <PopoverTrigger asChild>
                      <Button type="button" variant="outline" className="w-full justify-between font-normal">
                        {selectedDate.toLocaleDateString("pt-BR")}
                        <CalendarIcon className="size-4 text-muted-foreground" />
                      </Button>
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
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="note">Nota (opcional)</Label>
                <Textarea id="note" value={note} onChange={(e) => setNote(e.target.value)} />
              </div>

              {error && <p className="text-sm text-destructive">{error}</p>}
              <Button type="submit" disabled={saving}>
                {saving ? "Salvando..." : "Registrar"}
              </Button>
            </form>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Minhas sessões</CardTitle>
        </CardHeader>
        <CardContent>
          {sessions.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma sessão registrada ainda.</p>
          ) : (
            <ul className="flex flex-col divide-y">
              {sessions.map((s) => (
                <li key={s.id} className="flex items-center justify-between gap-4 py-3">
                  <div className="flex flex-col">
                    <span className="font-medium">{formatMinutes(s.focusedMinutes)}</span>
                    <span className="text-sm text-muted-foreground">
                      {categoryName(s.categoryId)} &middot; {formatDateTime(s.startedAt)}
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    {s.note && (
                      <span className="max-w-[50%] truncate text-sm text-muted-foreground">
                        {s.note}
                      </span>
                    )}
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className={
                        confirmSessionId === s.id
                          ? "text-destructive"
                          : "text-muted-foreground hover:text-destructive"
                      }
                      disabled={deletingSessionId === s.id}
                      onClick={() =>
                        confirmSessionId === s.id ? confirmSessionDelete(s.id) : armSessionDelete(s.id)
                      }
                    >
                      {confirmSessionId === s.id ? (
                        "Confirmar?"
                      ) : (
                        <Trash2 className="size-4" />
                      )}
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
