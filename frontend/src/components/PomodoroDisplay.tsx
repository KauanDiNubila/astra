import { AnimatePresence, motion, useReducedMotion } from "motion/react"
import { Badge } from "@/components/ui/badge"
import { FlipDigits } from "@/components/FlipDigits"

type Mode = "focus" | "break"

type Props = {
  size: number
  mode: Mode
  isLongBreak: boolean
  timeLeft: number
  totalSeconds: number
  caption?: string
  emphasize?: boolean
  showBadge?: boolean
}

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

export function pomodoroModeLabel(mode: Mode, isLongBreak: boolean) {
  return mode === "focus" ? "Foco" : isLongBreak ? "Pausa longa" : "Pausa"
}

export function PomodoroDisplay({
  size,
  mode,
  isLongBreak,
  timeLeft,
  totalSeconds,
  caption,
  emphasize,
  showBadge = true,
}: Props) {
  const reducedMotion = useReducedMotion()
  const progress = totalSeconds > 0 ? 1 - timeLeft / totalSeconds : 0
  const modeLabel = pomodoroModeLabel(mode, isLongBreak)

  return (
    <motion.div
      initial={
        !emphasize || reducedMotion ? false : { opacity: 0, scale: 0.94, filter: "blur(6px)" }
      }
      animate={{ opacity: 1, scale: 1, filter: "blur(0px)" }}
      transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
      className="flex flex-col items-center"
      style={{ width: size }}
    >
      {showBadge && (
        <AnimatePresence mode="wait" initial={false}>
          <motion.div
            key={`${mode}-${isLongBreak}-badge`}
            initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 1.04 }}
            transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
            className="mb-2"
          >
            <Badge variant={mode === "focus" ? "default" : "secondary"}>{modeLabel}</Badge>
          </motion.div>
        </AnimatePresence>
      )}
      <div className="h-[3px] w-full overflow-hidden rounded-full bg-foreground/10">
        <div
          className={`h-full rounded-full transition-[width] duration-1000 ease-linear ${
            mode === "focus" ? "bg-primary" : "bg-emerald-500"
          }`}
          style={{ width: `${progress * 100}%` }}
        />
      </div>
      <AnimatePresence mode="wait" initial={false}>
        <motion.div
          key={`${mode}-${isLongBreak}`}
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 1.04 }}
          transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
          className="mt-4 flex flex-col items-center gap-2"
        >
          <FlipDigits
            value={formatClock(timeLeft)}
            className="font-mono font-semibold tabular-nums"
            style={{ fontSize: size * 0.22 }}
          />
          {caption && <span className="text-xs text-muted-foreground">{caption}</span>}
        </motion.div>
      </AnimatePresence>
    </motion.div>
  )
}
