import { useRef, useState } from "react"
import type { KeyboardEvent } from "react"

type Part = "hours" | "minutes"

const INPUT =
  "bg-transparent text-center text-6xl font-semibold tracking-[-0.045em] tabular-nums outline-none rounded-md caret-foreground transition-colors selection:bg-foreground/20 selection:text-foreground hover:bg-muted/40 focus:bg-muted/60"
const UNIT = "text-2xl font-medium text-muted-foreground"

export function BigDurationInput({
  value,
  onChange,
  min = 5,
  max = 1440,
}: {
  value: number
  onChange: (minutes: number) => void
  min?: number
  max?: number
}) {
  const [draft, setDraft] = useState<{ part: Part; text: string } | null>(null)
  const cancelRef = useRef(false)
  const hours = Math.floor(value / 60)
  const minutes = value % 60

  function text(part: Part) {
    if (draft?.part === part) return draft.text
    return part === "hours" ? String(hours) : String(minutes).padStart(2, "0")
  }

  function commit(part: Part, raw: string) {
    if (cancelRef.current) {
      cancelRef.current = false
      setDraft(null)
      return
    }
    const parsed = Number.parseInt(raw, 10)
    const n = Number.isNaN(parsed) ? 0 : parsed
    const total = part === "hours" ? n * 60 + minutes : hours * 60 + Math.min(59, n)
    onChange(Math.min(max, Math.max(min, total)))
    setDraft(null)
  }

  function onKeyDown(part: Part, event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter") {
      event.preventDefault()
      event.currentTarget.blur()
    } else if (event.key === "Escape") {
      cancelRef.current = true
      event.currentTarget.blur()
    } else if (event.key === "ArrowUp" || event.key === "ArrowDown") {
      event.preventDefault()
      const step = (part === "hours" ? 60 : 5) * (event.key === "ArrowUp" ? 1 : -1)
      setDraft(null)
      onChange(Math.min(max, Math.max(min, value + step)))
    }
  }

  function field(part: Part, label: string) {
    const current = text(part)
    return (
      <input
        type="text"
        inputMode="numeric"
        aria-label={label}
        value={current}
        maxLength={2}
        style={{ width: `${Math.max(1, current.length) + 0.6}ch` }}
        className={INPUT}
        onFocus={(e) => {
          setDraft({ part, text: current })
          e.currentTarget.select()
        }}
        onChange={(e) => setDraft({ part, text: e.target.value.replace(/\D/g, "").slice(0, 2) })}
        onBlur={(e) => commit(part, e.target.value)}
        onKeyDown={(e) => onKeyDown(part, e)}
      />
    )
  }

  return (
    <div className="flex items-baseline justify-center gap-1">
      {field("hours", "Horas")}
      <span className={`${UNIT} mr-2`}>h</span>
      {field("minutes", "Minutos")}
      <span className={UNIT}>min</span>
    </div>
  )
}
