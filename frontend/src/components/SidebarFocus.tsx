import { Pause, Play } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { usePomodoro } from "@/context/PomodoroContext"
import { cn } from "@/lib/utils"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useSidebar } from "@/components/ui/sidebar"

function formatClock(totalSeconds: number) {
  const m = Math.floor(totalSeconds / 60).toString().padStart(2, "0")
  const s = Math.floor(totalSeconds % 60).toString().padStart(2, "0")
  return `${m}:${s}`
}

export function SidebarFocus() {
  const navigate = useNavigate()
  const { state, isMobile } = useSidebar()
  const { mode, isLongBreak, running, focusedSeconds, timeLeft, totalSeconds, handlePrimaryClick } = usePomodoro()
  const collapsed = state === "collapsed" && !isMobile
  const active = running || focusedSeconds > 0 || mode === "break"
  const progress = totalSeconds > 0 ? 1 - timeLeft / totalSeconds : 0
  const phase = mode === "break" ? (isLongBreak ? "Pausa longa" : "Pausa") : "Foco"
  const status = running ? phase : "Pausado"

  if (collapsed) {
    return (
      <Tooltip>
        <TooltipTrigger asChild>
          <button
            type="button"
            onClick={handlePrimaryClick}
            aria-label={running ? "Pausar" : active ? "Continuar" : "Iniciar foco"}
            className={cn(
              "flex size-8 items-center justify-center rounded-md transition-colors",
              active ? "bg-sidebar-accent text-sidebar-foreground" : "bg-foreground text-background",
            )}
          >
            {running ? <Pause className="size-4" /> : <Play className="size-4" />}
          </button>
        </TooltipTrigger>
        <TooltipContent side="right">{active ? `${status} · ${formatClock(timeLeft)}` : "Iniciar foco"}</TooltipContent>
      </Tooltip>
    )
  }

  if (!active) {
    return (
      <button
        type="button"
        onClick={handlePrimaryClick}
        className="flex h-9 w-full items-center justify-center gap-2 rounded-md bg-foreground text-sm font-medium text-background transition-opacity hover:opacity-90"
      >
        <Play className="size-3.5 fill-current" />
        Iniciar foco
      </button>
    )
  }

  return (
    <div className="relative flex h-9 items-center gap-1 overflow-hidden rounded-md border border-sidebar-border bg-sidebar-accent/60 pl-3 pr-1">
      <button
        type="button"
        onClick={() => navigate("/sessions")}
        title="Abrir sessões"
        className="flex min-w-0 flex-1 items-center justify-between gap-2 text-left"
      >
        <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <span
            className={cn(
              "size-1.5 rounded-full",
              running ? (mode === "break" ? "bg-amber-400" : "bg-emerald-500") : "bg-muted-foreground/50",
              running && "animate-pulse motion-reduce:animate-none",
            )}
          />
          {status}
        </span>
        <span className="font-mono text-sm font-medium tabular-nums">{formatClock(timeLeft)}</span>
      </button>
      <button
        type="button"
        onClick={handlePrimaryClick}
        aria-label={running ? "Pausar" : "Continuar"}
        className="flex size-7 shrink-0 items-center justify-center rounded-md transition-colors hover:bg-sidebar-accent"
      >
        {running ? <Pause className="size-4" /> : <Play className="size-4" />}
      </button>
      <span className="absolute inset-x-0 bottom-0 h-0.5 bg-sidebar-border">
        <span
          className="block h-full origin-left bg-sidebar-foreground/70 transition-transform duration-1000 ease-linear"
          style={{ transform: `scaleX(${progress})` }}
        />
      </span>
    </div>
  )
}
