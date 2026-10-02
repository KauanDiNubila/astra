import { useEffect, useState } from "react"
import { Pause, Play } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useLocation, useNavigate } from "react-router-dom"
import { usePomodoro } from "@/context/PomodoroContext"
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
  const { running, focusedSeconds, timeLeft, handlePrimaryClick } = usePomodoro()

  const visible = (running || focusedSeconds > 0) && pathname !== "/sessions"

  // Timer manual em vez de AnimatePresence.exit: essa versão do motion trava
  // sem desmontar quando `visible` alterna rápido (ex.: navegação entre
  // páginas) — mesmo contorno já usado no DailyGoalPanel do FocusModeOverlay.
  const [rendered, setRendered] = useState(visible)
  useEffect(() => {
    if (visible) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 300)
    return () => clearTimeout(timeout)
  }, [visible])

  if (!rendered) return null

  return (
    <motion.div
      initial={reducedMotion ? { opacity: 0 } : { opacity: 0, y: -16, scale: 0.96 }}
      animate={
        reducedMotion
          ? { opacity: visible ? 1 : 0 }
          : { opacity: visible ? 1 : 0, y: visible ? 0 : -16, scale: visible ? 1 : 0.96 }
      }
      transition={{ duration: 0.3, ease: [0.22, 1, 0.36, 1] }}
      className="fixed right-4 top-20 z-40 flex max-md:bottom-[calc(4.25rem+env(safe-area-inset-bottom))] max-md:top-auto items-center gap-3 rounded-full border bg-card py-2 pl-4 pr-2 shadow-lg"
    >
      <button
        type="button"
        onClick={() => navigate("/sessions")}
        className="flex items-center gap-2 text-left"
        title="Abrir sessões"
      >
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
  )
}
