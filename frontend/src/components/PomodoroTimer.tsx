import { useEffect, useRef, useState } from "react"
import type { FormEvent } from "react"
import { ArrowLeft, Settings as SettingsIcon } from "lucide-react"
import { api } from "@/lib/api"
import { loadPomodoroSettings, savePomodoroSettings } from "@/lib/pomodoroSettings"
import type { Category, CourseSummary } from "@/lib/types"
import { CategoryPicker } from "@/components/CategoryPicker"
import { PomodoroSettingsPanel } from "@/components/PomodoroSettingsPanel"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

type Mode = "focus" | "break"
type View = "timer" | "settings"

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

type Props = {
  categories: Category[]
  courses: CourseSummary[]
  onCategoryCreated: () => Promise<void>
  onSessionSaved: () => Promise<void>
}

export function PomodoroTimer({ categories, courses, onCategoryCreated, onSessionSaved }: Props) {
  const [settings, setSettings] = useState(loadPomodoroSettings)
  const [view, setView] = useState<View>("timer")

  useEffect(() => {
    savePomodoroSettings(settings)
  }, [settings])

  const [mode, setMode] = useState<Mode>("focus")
  const [isLongBreak, setIsLongBreak] = useState(false)
  const [timeLeft, setTimeLeft] = useState(settings.focusMinutes * 60)
  const [running, setRunning] = useState(false)
  const [focusedSeconds, setFocusedSeconds] = useState(0)
  const [completedPomodoros, setCompletedPomodoros] = useState(0)
  const completedRef = useRef(0)
  const startedAtRef = useRef<string | null>(null)
  const runningRef = useRef(running)

  useEffect(() => {
    runningRef.current = running
  }, [running])

  const [categoryId, setCategoryId] = useState("")
  const [courseId, setCourseId] = useState("")
  const [note, setNote] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!running) return
    const id = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev > 1) {
          if (mode === "focus") setFocusedSeconds((s) => s + 1)
          return prev - 1
        }

        if (mode === "focus") {
          setFocusedSeconds((s) => s + 1)
          completedRef.current += 1
          setCompletedPomodoros(completedRef.current)

          if (settings.disableBreaks) {
            return settings.focusMinutes * 60
          }

          const longBreak =
            settings.pomodorosUntilLongBreak > 0 &&
            completedRef.current % settings.pomodorosUntilLongBreak === 0
          setIsLongBreak(longBreak)
          setMode("break")
          if (!settings.autoStartBreak) setRunning(false)
          return (longBreak ? settings.longBreakMinutes : settings.shortBreakMinutes) * 60
        }

        setMode("focus")
        if (!settings.autoStartNextPomodoro) setRunning(false)
        return settings.focusMinutes * 60
      })
    }, 1000)
    return () => clearInterval(id)
  }, [running, mode, settings])

  function toggleRunning() {
    if (!running && !startedAtRef.current) {
      startedAtRef.current = new Date().toISOString()
    }
    setRunning((r) => !r)
  }

  function currentModeSeconds() {
    if (mode === "focus") return settings.focusMinutes * 60
    return (isLongBreak ? settings.longBreakMinutes : settings.shortBreakMinutes) * 60
  }

  useEffect(() => {
    if (!runningRef.current) {
      setTimeLeft(currentModeSeconds())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings])

  function resetCycle() {
    setRunning(false)
    setTimeLeft(currentModeSeconds())
  }

  function discard() {
    setRunning(false)
    setMode("focus")
    setIsLongBreak(false)
    setTimeLeft(settings.focusMinutes * 60)
    setFocusedSeconds(0)
    completedRef.current = 0
    setCompletedPomodoros(0)
    startedAtRef.current = null
  }

  const focusedMinutes = Math.floor(focusedSeconds / 60)
  const modeLabel = mode === "focus" ? "Foco" : isLongBreak ? "Pausa longa" : "Pausa"

  async function saveSession(event: FormEvent) {
    event.preventDefault()
    setError(null)
    if (!categoryId) {
      setError("Escolha uma categoria.")
      return
    }
    if (focusedMinutes < 1) {
      setError("Foque por pelo menos 1 minuto antes de salvar.")
      return
    }
    setSaving(true)
    try {
      await api.post("/sessions", {
        categoryId,
        courseId: courseId || null,
        focusedMinutes,
        startedAt: startedAtRef.current ?? new Date().toISOString(),
        note: note.trim() || null,
      })
      discard()
      setNote("")
      await onSessionSaved()
    } catch {
      setError("Não foi possível registrar a sessão.")
    } finally {
      setSaving(false)
    }
  }

  if (view === "settings") {
    return (
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-2">
          <h3 className="font-medium">Configurações do pomodoro</h3>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="gap-1.5"
            onClick={() => setView("timer")}
          >
            <ArrowLeft className="size-3.5" />
            Voltar
          </Button>
        </div>
        <PomodoroSettingsPanel settings={settings} onChange={setSettings} />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="relative flex flex-col items-center gap-3 rounded-lg border bg-muted/20 py-8">
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute right-3 top-3 size-8"
          title="Configurações do pomodoro"
          onClick={() => setView("settings")}
        >
          <SettingsIcon className="size-4" />
        </Button>

        <Badge variant={mode === "focus" ? "default" : "secondary"}>{modeLabel}</Badge>
        <span className="font-mono text-5xl font-semibold tabular-nums">
          {formatClock(timeLeft)}
        </span>
        <div className="flex gap-2">
          <Button type="button" onClick={toggleRunning}>
            {running ? "Pausar" : "Iniciar"}
          </Button>
          <Button type="button" variant="outline" onClick={resetCycle}>
            Reiniciar ciclo
          </Button>
          <Button type="button" variant="ghost" onClick={discard}>
            Descartar
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          {focusedMinutes} min focados
          {completedPomodoros > 0 && !settings.disableBreaks
            ? ` · ${completedPomodoros % settings.pomodorosUntilLongBreak || settings.pomodorosUntilLongBreak}/${settings.pomodorosUntilLongBreak} até pausa longa`
            : completedPomodoros > 0
              ? ` · ${completedPomodoros} pomodoro(s) completo(s)`
              : ""}
        </p>
      </div>

      <form onSubmit={saveSession} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label>Categoria</Label>
          <CategoryPicker
            categories={categories}
            value={categoryId}
            onChange={setCategoryId}
            onRefresh={onCategoryCreated}
          />
        </div>

        {courses.length > 0 && (
          <div className="flex flex-col gap-2">
            <Label>Curso (opcional)</Label>
            <Select
              value={courseId}
              onValueChange={(v) => setCourseId(v === "none" ? "" : v)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Nenhum curso" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">Nenhum curso</SelectItem>
                {courses.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.title}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}

        <div className="flex flex-col gap-2">
          <Label htmlFor="pomodoro-note">Nota (opcional)</Label>
          <Textarea id="pomodoro-note" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button type="submit" disabled={saving}>
          {saving ? "Salvando..." : `Salvar sessão (${focusedMinutes} min)`}
        </Button>
      </form>
    </div>
  )
}
