import { Pause, Play } from "lucide-react"
import { AnimatePresence, motion, useReducedMotion } from "motion/react"
import { useLocation, useNavigate } from "react-router-dom"
import { usePomodoro } from "@/context/PomodoroContext"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

export function PomodoroMiniWidget() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const reducedMotion = useReducedMotion()
  const { running, focusedSeconds, mode, isLongBreak, timeLeft, handlePrimaryClick } = usePomodoro()

  const visible = (running || focusedSeconds > 0) && pathname !== "/sessions"
  const modeLabel = mode === "focus" ? "Foco" : isLongBreak ? "Pausa longa" : "Pausa"

  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, y: -16, scale: 0.96 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={reducedMotion ? { opacity: 0 } : { opacity: 0, y: -16, scale: 0.96 }}
          transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
          className="fixed right-4 top-20 z-40 flex items-center gap-3 rounded-full border bg-card py-2 pl-4 pr-2 shadow-lg"
        >
          <button
            type="button"
            onClick={() => navigate("/sessions")}
            className="flex items-center gap-2 text-left"
            title="Abrir sessões"
          >
            <Badge variant={mode === "focus" ? "default" : "secondary"}>{modeLabel}</Badge>
            <span className="font-mono text-sm tabular-nums">{formatClock(timeLeft)}</span>
          </button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="size-8 shrink-0 rounded-full"
            onClick={handlePrimaryClick}
            title={running ? "Pausar" : "Continuar"}
          >
            {running ? <Pause className="size-4" /> : <Play className="size-4" />}
          </Button>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
