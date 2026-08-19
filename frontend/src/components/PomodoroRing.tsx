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
}

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

export function PomodoroRing({ size, mode, isLongBreak, timeLeft, totalSeconds, caption, emphasize }: Props) {
  const reducedMotion = useReducedMotion()
  const stroke = Math.round(size * 0.045)
  const radius = (size - stroke) / 2
  const circumference = 2 * Math.PI * radius
  const progress = totalSeconds > 0 ? 1 - timeLeft / totalSeconds : 0
  const dashOffset = circumference * (1 - progress)
  const modeLabel = mode === "focus" ? "Foco" : isLongBreak ? "Pausa longa" : "Pausa"

  return (
    <motion.div
      initial={
        !emphasize || reducedMotion ? false : { opacity: 0, scale: 0.82, filter: "blur(6px)" }
      }
      animate={{ opacity: 1, scale: 1, filter: "blur(0px)" }}
      transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
      className="relative flex items-center justify-center"
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" strokeWidth={stroke} className="stroke-foreground/15" />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={dashOffset}
          className={`transition-[stroke-dashoffset] duration-1000 ease-linear ${
            mode === "focus" ? "stroke-primary" : "stroke-emerald-500"
          }`}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <AnimatePresence mode="wait" initial={false}>
          <motion.div
            key={`${mode}-${isLongBreak}`}
            initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 1.04 }}
            transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
            className="flex flex-col items-center gap-2"
          >
            <Badge variant={mode === "focus" ? "default" : "secondary"}>{modeLabel}</Badge>
            <FlipDigits
              value={formatClock(timeLeft)}
              className="font-mono font-semibold tabular-nums"
              style={{ fontSize: size * 0.16 }}
            />
            {caption && <span className="text-xs text-muted-foreground">{caption}</span>}
          </motion.div>
        </AnimatePresence>
      </div>
    </motion.div>
  )
}
