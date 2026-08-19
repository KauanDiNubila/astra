import * as React from "react"
import { AnimatePresence, motion } from "motion/react"
import { Minus, Plus } from "lucide-react"

import { cn } from "@/lib/utils"
import { Input } from "@/components/ui/input"

export interface StepperProps {
  id?: string
  value?: number
  defaultValue?: number
  min?: number
  max?: number
  step?: number
  editable?: boolean
  size?: "default" | "sm"
  formatDisplay?: (value: number) => string
  parseDisplay?: (text: string) => number | null
  onChange?: (val: number) => void
  className?: string
  "aria-label"?: string
}

const digitVariants = {
  initial: (dir: number) => ({
    y: dir > 0 ? 20 : -20,
    opacity: 0,
    scale: 0.5,
    filter: "blur(2px)",
  }),
  animate: {
    y: 0,
    opacity: 1,
    scale: 1,
    filter: "blur(0px)",
  },
  exit: (dir: number) => ({
    y: dir > 0 ? -20 : 20,
    opacity: 0,
    scale: 0.5,
    filter: "blur(2px)",
  }),
}

function Stepper({
  id,
  value,
  defaultValue = 0,
  min = 0,
  max = 999,
  step = 1,
  editable = false,
  size = "default",
  formatDisplay,
  parseDisplay,
  onChange,
  className,
  "aria-label": ariaLabel = "Seletor numérico",
}: StepperProps) {
  const isControlled = value !== undefined
  const [internal, setInternal] = React.useState(defaultValue)
  const [direction, setDirection] = React.useState(0)
  const [isEditing, setIsEditing] = React.useState(false)
  const [draft, setDraft] = React.useState("")

  const current = isControlled ? value! : internal
  const sm = size === "sm"

  const display = (n: number) => formatDisplay?.(n) ?? n.toString()

  // Reserva a largura do maior valor possível no intervalo, pra "2h" e
  // "2h05" ocuparem o mesmo espaço — sem isso os botões +/- mudam de
  // posição a cada clique e cliques rápidos em sequência erram o alvo.
  const maxLength = React.useMemo(() => {
    let longest = display(min).length
    for (let v = min; v <= max; v += step) {
      longest = Math.max(longest, display(v).length)
    }
    longest = Math.max(longest, display(max).length)
    return longest
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [min, max, step, formatDisplay])

  const rawDigits = display(current).split("")
  const totalPad = Math.max(0, maxLength - rawDigits.length)
  const leadPad = Math.floor(totalPad / 2)
  const trailPad = totalPad - leadPad
  const digits = [...Array(leadPad).fill(""), ...rawDigits, ...Array(trailPad).fill("")]

  const [prevDigits, setPrevDigits] = React.useState<string[]>([])
  const [prevTicks, setPrevTicks] = React.useState<number[]>([])

  const len = digits.length
  const lenDiff = len - prevDigits.length

  const nextTicks = digits.map((digit, i) => {
    const prevI = i - lenDiff
    const prevDigit = prevI >= 0 ? prevDigits[prevI] : undefined
    const prevTick = prevI >= 0 ? prevTicks[prevI] : 0

    return digit !== prevDigit ? (prevTick ?? 0) + 1 : (prevTick ?? 0)
  })

  if (prevDigits.join("") !== digits.join("")) {
    setPrevTicks(nextTicks)
    setPrevDigits(digits)
  }

  const applyStep = (dir: number) => {
    const next = Math.min(max, Math.max(min, current + dir * step))
    if (next === current) return
    setDirection(dir)
    if (!isControlled) setInternal(next)
    onChange?.(next)
  }

  const commitDraft = () => {
    setIsEditing(false)
    const parsed = parseDisplay ? parseDisplay(draft) : Number.parseInt(draft, 10)
    const next =
      parsed === null || parsed === undefined || Number.isNaN(parsed)
        ? current
        : Math.min(max, Math.max(min, parsed))
    if (next === current) return
    setDirection(next > current ? 1 : -1)
    if (!isControlled) setInternal(next)
    onChange?.(next)
  }

  return (
    <div
      id={id}
      data-slot="stepper"
      role="group"
      aria-label={ariaLabel}
      className={cn(
        "flex w-fit items-center rounded-full border border-border bg-transparent",
        sm ? "gap-1.5 p-1" : "gap-3 p-1",
        className
      )}
    >
      <motion.button
        type="button"
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.92 }}
        transition={{ type: "spring", stiffness: 300, damping: 22 }}
        onClick={() => applyStep(-1)}
        disabled={current <= min}
        className={cn(
          "flex shrink-0 cursor-pointer items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50",
          sm ? "h-6 w-6" : "h-8 w-8",
        )}
      >
        <Minus className={sm ? "size-3" : "size-4"} />
      </motion.button>

      {isEditing ? (
        <Input
          type="text"
          inputMode={parseDisplay ? "text" : "numeric"}
          autoFocus
          value={draft}
          onChange={(e) =>
            setDraft(e.target.value.replace(parseDisplay ? /[^0-9a-zA-Z:]/g : /[^0-9]/g, ""))
          }
          onBlur={commitDraft}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.preventDefault()
              commitDraft()
            }
            if (e.key === "Escape") setIsEditing(false)
          }}
          style={{ width: `${maxLength + 1.5}ch` }}
          className={cn(
            "rounded-md border-none bg-transparent font-semibold shadow-none focus-visible:ring-1",
            sm ? "h-5 px-0.5 text-sm" : "h-6 px-1 text-base",
          )}
        />
      ) : (
        <button
          type="button"
          disabled={!editable}
          onClick={() => {
            setDraft("")
            setIsEditing(true)
          }}
          className={cn(
            "relative flex shrink-0 items-center justify-center gap-0.5 rounded-sm font-semibold text-foreground tabular-nums",
            sm ? "h-5 text-sm" : "h-6 text-base",
            editable && "cursor-text hover:bg-muted/50",
          )}
        >
          {digits.map((digit, index) => (
            <div key={index} className={cn("relative", sm ? "h-5 w-2.5" : "h-6 w-3")}>
              {digit !== "" && (
                <AnimatePresence mode="popLayout" initial={false} custom={direction}>
                  <motion.span
                    key={nextTicks[index]}
                    custom={direction}
                    variants={digitVariants}
                    initial="initial"
                    animate="animate"
                    exit="exit"
                    transition={{ type: "spring", stiffness: 200, damping: 16, mass: 1.2 }}
                    className="absolute inset-0 flex items-center justify-center"
                  >
                    {digit}
                  </motion.span>
                </AnimatePresence>
              )}
            </div>
          ))}
        </button>
      )}

      <motion.button
        type="button"
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.92 }}
        transition={{ type: "spring", stiffness: 300, damping: 22 }}
        onClick={() => applyStep(1)}
        disabled={current >= max}
        className={cn(
          "flex shrink-0 cursor-pointer items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50",
          sm ? "h-6 w-6" : "h-8 w-8",
        )}
      >
        <Plus className={sm ? "size-3" : "size-4"} />
      </motion.button>
    </div>
  )
}

export { Stepper }
