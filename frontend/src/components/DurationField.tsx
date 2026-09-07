import { useRef, useState } from "react"
import type { KeyboardEvent } from "react"
import { Minus, Plus } from "lucide-react"

type Part = "hours" | "minutes"

type DurationFieldProps = {
  id?: string
  value: number
  min?: number
  max?: number
  step?: number
  onChange: (minutes: number) => void
  "aria-label"?: string
}

function pad2(n: number) {
  return n.toString().padStart(2, "0")
}

export function DurationField({
  id,
  value,
  min = 0,
  max = 1440,
  step = 5,
  onChange,
  "aria-label": ariaLabel = "Duração",
}: DurationFieldProps) {
  const hours = Math.floor(value / 60)
  const mins = value % 60
  const maxHours = Math.floor(max / 60)

  const [editing, setEditing] = useState<Part | null>(null)
  const [draft, setDraft] = useState("")
  const hoursRef = useRef<HTMLInputElement>(null)
  const minutesRef = useRef<HTMLInputElement>(null)

  function commit(part: Part, raw: string) {
    const parsed = raw === "" ? 0 : Number.parseInt(raw, 10)
    const safe = Number.isNaN(parsed) ? 0 : parsed
    const nextHours = part === "hours" ? Math.min(maxHours, safe) : hours
    const nextMinutes = part === "minutes" ? Math.min(59, safe) : mins
    const total = Math.min(max, Math.max(min, nextHours * 60 + nextMinutes))
    if (total !== value) onChange(total)
  }

  function beginEdit(part: Part) {
    setDraft(part === "hours" ? hours.toString() : pad2(mins))
    setEditing(part)
  }

  // Ao completar 2 dígitos, avança pro campo de minutos sozinho (horas) ou
  // fecha a edição (minutos) - digita os 4 dígitos em sequência, como um
  // relógio, sem precisar clicar de novo pro segundo campo.
  function finishSegment(part: Part, digits: string) {
    commit(part, digits)
    if (part === "hours") {
      beginEdit("minutes")
      requestAnimationFrame(() => minutesRef.current?.focus())
    } else {
      setEditing(null)
    }
  }

  function handleChange(part: Part, raw: string) {
    const digits = raw.replace(/\D/g, "").slice(0, 2)
    setDraft(digits)
    if (digits.length === 2) finishSegment(part, digits)
  }

  function handleKeyDown(part: Part, e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault()
      finishSegment(part, draft)
    } else if (e.key === "Escape") {
      setEditing(null)
    } else if (e.key === "Backspace" && draft === "" && part === "minutes") {
      e.preventDefault()
      beginEdit("hours")
      requestAnimationFrame(() => hoursRef.current?.focus())
    }
  }

  function handleBlur(part: Part) {
    commit(part, draft)
    setEditing(null)
  }

  function stepBy(dir: number) {
    const next = Math.min(max, Math.max(min, value + dir * step))
    if (next !== value) onChange(next)
  }

  function renderSegment(part: Part, label: string) {
    const isEditing = editing === part
    const ref = part === "hours" ? hoursRef : minutesRef

    if (isEditing) {
      return (
        <input
          ref={ref}
          type="text"
          inputMode="numeric"
          aria-label={label}
          autoFocus
          onFocus={(e) => e.currentTarget.select()}
          value={draft}
          onChange={(e) => handleChange(part, e.target.value)}
          onKeyDown={(e) => handleKeyDown(part, e)}
          onBlur={() => handleBlur(part)}
          className="h-5 w-6 rounded-sm bg-transparent text-center text-sm font-semibold text-foreground tabular-nums outline-none focus-visible:ring-1 focus-visible:ring-ring"
        />
      )
    }

    return (
      <button
        type="button"
        aria-label={label}
        onClick={() => beginEdit(part)}
        className="h-5 w-6 rounded-sm text-sm font-semibold text-foreground tabular-nums hover:bg-muted/50"
      >
        {part === "hours" ? pad2(hours) : pad2(mins)}
      </button>
    )
  }

  return (
    <div
      id={id}
      role="group"
      aria-label={ariaLabel}
      className="flex w-fit items-center gap-1.5 rounded-full border border-border bg-transparent p-1"
    >
      <button
        type="button"
        onClick={() => stepBy(-1)}
        disabled={value <= min}
        className="flex size-6 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50"
      >
        <Minus className="size-3" />
      </button>

      <div className="flex items-center">
        {renderSegment("hours", "Horas")}
        <span className="text-sm text-muted-foreground">:</span>
        {renderSegment("minutes", "Minutos")}
      </div>

      <button
        type="button"
        onClick={() => stepBy(1)}
        disabled={value >= max}
        className="flex size-6 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50"
      >
        <Plus className="size-3" />
      </button>
    </div>
  )
}
