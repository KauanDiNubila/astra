import { useEffect, useRef, useState } from "react"
import type { FormEvent } from "react"
import { api } from "@/lib/api"
import type { Category } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

const FOCUS_SECONDS = 25 * 60
const BREAK_SECONDS = 5 * 60

type Mode = "focus" | "break"

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

type Props = {
  categories: Category[]
  onCategoryCreated: () => Promise<void>
  onSessionSaved: () => Promise<void>
}

export function PomodoroTimer({ categories, onCategoryCreated, onSessionSaved }: Props) {
  const [mode, setMode] = useState<Mode>("focus")
  const [timeLeft, setTimeLeft] = useState(FOCUS_SECONDS)
  const [running, setRunning] = useState(false)
  const [focusedSeconds, setFocusedSeconds] = useState(0)
  const [cycles, setCycles] = useState(0)
  const startedAtRef = useRef<string | null>(null)

  const [categoryId, setCategoryId] = useState("")
  const [newCategory, setNewCategory] = useState("")
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
          setMode("break")
          return BREAK_SECONDS
        }
        setMode("focus")
        setCycles((c) => c + 1)
        return FOCUS_SECONDS
      })
    }, 1000)
    return () => clearInterval(id)
  }, [running, mode])

  function toggleRunning() {
    if (!running && !startedAtRef.current) {
      startedAtRef.current = new Date().toISOString()
    }
    setRunning((r) => !r)
  }

  function resetCycle() {
    setRunning(false)
    setTimeLeft(mode === "focus" ? FOCUS_SECONDS : BREAK_SECONDS)
  }

  function discard() {
    setRunning(false)
    setMode("focus")
    setTimeLeft(FOCUS_SECONDS)
    setFocusedSeconds(0)
    setCycles(0)
    startedAtRef.current = null
  }

  async function createCategory() {
    const name = newCategory.trim()
    if (!name) return
    const res = await api.post<Category>("/categories", { name })
    setNewCategory("")
    await onCategoryCreated()
    setCategoryId(res.data.id)
  }

  const focusedMinutes = Math.floor(focusedSeconds / 60)

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
        focusedMinutes,
        startedAt: startedAtRef.current ?? new Date().toISOString(),
        note: note.trim() || null,
      })
      discard()
      setNote("")
      await onSessionSaved()
    } catch {
      setError("Nao foi possivel registrar a sessao.")
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col items-center gap-3 rounded-lg border bg-muted/20 py-8">
        <Badge variant={mode === "focus" ? "default" : "secondary"}>
          {mode === "focus" ? "Foco" : "Pausa"}
        </Badge>
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
          {cycles > 0 ? ` · ${cycles} ciclo(s) completo(s)` : ""}
        </p>
      </div>

      <form onSubmit={saveSession} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label>Categoria</Label>
          {categories.length > 0 && (
            <Select value={categoryId} onValueChange={setCategoryId}>
              <SelectTrigger>
                <SelectValue placeholder="Escolha uma categoria" />
              </SelectTrigger>
              <SelectContent>
                {categories.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
          <div className="flex gap-2">
            <Input
              placeholder="Nova categoria"
              value={newCategory}
              onChange={(e) => setNewCategory(e.target.value)}
            />
            <Button type="button" variant="outline" onClick={createCategory}>
              Criar
            </Button>
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <Label htmlFor="pomodoro-note">Nota (opcional)</Label>
          <Textarea id="pomodoro-note" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button type="submit" disabled={saving || focusedMinutes < 1}>
          {saving ? "Salvando..." : `Salvar sessao (${focusedMinutes} min)`}
        </Button>
      </form>
    </div>
  )
}
