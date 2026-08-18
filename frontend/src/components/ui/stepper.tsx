import * as React from "react"
import { AnimatePresence, motion } from "motion/react"
import { Minus, Plus } from "lucide-react"

import { cn } from "@/lib/utils"

export interface StepperProps {
  id?: string
  value?: number
  defaultValue?: number
  min?: number
  max?: number
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
  onChange,
  className,
  "aria-label": ariaLabel = "Seletor numérico",
}: StepperProps) {
  const isControlled = value !== undefined
  const [internal, setInternal] = React.useState(defaultValue)
  const [direction, setDirection] = React.useState(0)

  const current = isControlled ? value! : internal
  const digits = current.toString().split("")

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

  const step = (dir: number) => {
    const next = Math.min(max, Math.max(min, current + dir))
    if (next === current) return
    setDirection(dir)
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
        "flex w-fit items-center gap-3 rounded-full border border-border bg-transparent p-1",
        className
      )}
    >
      <motion.button
        type="button"
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.92 }}
        transition={{ type: "spring", stiffness: 300, damping: 22 }}
        onClick={() => step(-1)}
        disabled={current <= min}
        className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50"
      >
        <Minus className="size-4" />
      </motion.button>

      <div className="relative flex h-6 shrink-0 items-center justify-center gap-0.5 text-base font-semibold text-foreground tabular-nums">
        {digits.map((digit, index) => (
          <div key={`${index}-${len}`} className="relative w-2.5 h-6">
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
          </div>
        ))}
      </div>

      <motion.button
        type="button"
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.92 }}
        transition={{ type: "spring", stiffness: 300, damping: 22 }}
        onClick={() => step(1)}
        disabled={current >= max}
        className="flex h-8 w-8 shrink-0 cursor-pointer items-center justify-center rounded-full bg-muted text-muted-foreground disabled:pointer-events-none disabled:opacity-50"
      >
        <Plus className="size-4" />
      </motion.button>
    </div>
  )
}

export { Stepper }
