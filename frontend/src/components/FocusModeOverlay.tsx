import { useEffect, useState } from "react"
import type { ReactNode } from "react"
import { createPortal } from "react-dom"
import { Clock, Sparkles, X, Zap } from "lucide-react"
import { AnimatePresence, motion, useReducedMotion } from "motion/react"
import { useBatteryStatus } from "@/hooks/useBatteryStatus"
import { useCountUp } from "@/hooks/use-count-up"
import { useTheme } from "@/context/ThemeContext"
import { cn } from "@/lib/utils"
import { Particles } from "@/components/magicui/particles"
import { Button } from "@/components/ui/button"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"

type Props = {
  open: boolean
  onExit: () => void
  children: ReactNode
}

const PARTICLES_KEY = "astra:focus-particles-enabled"

function loadParticlesEnabled() {
  if (typeof window === "undefined") return true
  const raw = localStorage.getItem(PARTICLES_KEY)
  return raw === null ? true : raw === "true"
}

function LiveClock() {
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>
    const msUntilNextMinute = 60000 - (Date.now() % 60000)
    const timeout = setTimeout(() => {
      setNow(new Date())
      interval = setInterval(() => setNow(new Date()), 60000)
    }, msUntilNextMinute)
    return () => {
      clearTimeout(timeout)
      clearInterval(interval)
    }
  }, [])

  const hours = now.getHours().toString().padStart(2, "0")
  const minutes = now.getMinutes().toString().padStart(2, "0")

  return (
    <span className="flex items-center gap-1.5 rounded-full border px-2 py-0.5 font-mono text-sm text-muted-foreground tabular-nums">
      <Clock className="size-3.5" />
      {hours}:{minutes}
    </span>
  )
}

function BatteryIndicator() {
  const status = useBatteryStatus()
  const reducedMotion = useReducedMotion()
  const rawPercent = status ? Math.round(status.level * 100) : 0
  const animatedPercent = useCountUp(rawPercent)
  if (!status) return null

  const low = rawPercent <= 20 && !status.charging

  return (
    <span
      className={cn(
        "flex items-center gap-1.5 text-sm tabular-nums",
        low ? "text-destructive" : "text-muted-foreground",
      )}
    >
      <span className="relative flex h-3 w-5 items-center rounded-[3px] border border-current p-[1.5px]">
        {low && (
          <span
            aria-hidden
            className="absolute -inset-1 -z-10 rounded-full blur-md"
            style={{
              background: "color-mix(in oklch, var(--destructive) 40%, transparent)",
              animation: reducedMotion ? "none" : "astra-focus-breathe 2s ease-in-out infinite",
            }}
          />
        )}
        <span
          className="block h-full overflow-hidden rounded-[1px] bg-current transition-[width] duration-700 ease-out"
          style={{ width: `${Math.max(rawPercent, 4)}%` }}
        >
          {status.charging && (
            <span
              aria-hidden
              className="block h-full w-full"
              style={{
                background:
                  "linear-gradient(90deg, transparent, color-mix(in oklch, white 70%, transparent), transparent)",
                backgroundSize: "200% 100%",
                animation: reducedMotion ? "none" : "astra-shimmer 1.8s linear infinite",
              }}
            />
          )}
        </span>
        <span className="absolute -right-[3px] top-1/2 h-1.5 w-[2px] -translate-y-1/2 rounded-r-[1px] bg-current" />
      </span>
      {status.charging && <Zap className="size-3.5 fill-current" />}
      {Math.round(animatedPercent)}%
    </span>
  )
}

export function FocusModeOverlay({ open, onExit, children }: Props) {
  const reducedMotion = useReducedMotion()
  const { theme, toggleTheme } = useTheme()
  const [particlesEnabled, setParticlesEnabled] = useState(loadParticlesEnabled)

  function toggleParticles() {
    setParticlesEnabled((enabled) => {
      const next = !enabled
      localStorage.setItem(PARTICLES_KEY, String(next))
      return next
    })
  }

  useEffect(() => {
    if (!open) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [open])

  useEffect(() => {
    if (!open) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onExit()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [open, onExit])

  return createPortal(
    <AnimatePresence>
      {open && (
        <motion.div
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
          transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
          className="fixed inset-0 z-[100] overflow-y-auto bg-background"
        >
          <AnimatePresence>
            {particlesEnabled && !reducedMotion && (
              <motion.div
                key="particles"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
                className="pointer-events-none absolute inset-0 z-0"
              >
                <Particles
                  className="h-full w-full"
                  quantity={100}
                  ease={80}
                  color={theme === "dark" ? "#ffffff" : "#000000"}
                />
              </motion.div>
            )}
          </AnimatePresence>

          <div className="fixed left-4 top-4 z-10 flex items-center gap-3">
            <LiveClock />
            <BatteryIndicator />
          </div>

          <div className="fixed right-4 top-4 z-10 flex items-center gap-1">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              title={particlesEnabled ? "Desligar partículas" : "Ligar partículas"}
              onClick={toggleParticles}
            >
              <Sparkles className={cn("size-4", !particlesEnabled && "text-muted-foreground/50")} />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              title={theme === "dark" ? "Modo claro" : "Modo escuro"}
              onClick={toggleTheme}
            >
              <ThemeToggleIcon isDark={theme === "dark"} className="size-5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              title="Sair do modo foco"
              onClick={onExit}
            >
              <X className="size-4" />
            </Button>
          </div>
          <div className="relative z-10 mx-auto flex min-h-svh w-full max-w-2xl flex-col px-4 py-16 sm:px-8">
            {children}
          </div>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body,
  )
}
