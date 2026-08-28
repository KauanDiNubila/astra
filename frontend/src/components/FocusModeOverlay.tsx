import { useEffect, useRef, useState } from "react"
import type { ChangeEvent, ReactNode } from "react"
import { createPortal } from "react-dom"
import { CheckCircle2, Clock, ImagePlus, Maximize, Minimize, Palette, Sparkles, Target, X, Zap } from "lucide-react"
import { AnimatePresence, motion, useReducedMotion } from "motion/react"
import { useBatteryStatus } from "@/hooks/useBatteryStatus"
import { useCountUp } from "@/hooks/use-count-up"
import { useTheme } from "@/context/ThemeContext"
import { formatMinutes } from "@/lib/format"
import { cn } from "@/lib/utils"
import type { GoalProgress } from "@/lib/types"
import {
  clearCustomFocusImage,
  getCustomFocusImage,
  resizeImageForFocusBackground,
  setCustomFocusImage,
} from "@/lib/customFocusImage"
import { FOCUS_BACKGROUND_LIBRARY, findLibraryBackground } from "@/lib/focusBackgroundLibrary"
import { Particles } from "@/components/magicui/particles"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"

type FocusTheme = "default" | "custom" | `library:${string}`

const LIBRARY_ID_PREFIX = "library:"

// Sem sintonia fina por foto (seriam dezenas com a biblioteca crescendo) —
// um overlay escuro neutro cobre texto legível na maioria das imagens.
const PHOTO_OVERLAY = "linear-gradient(rgba(10, 10, 12, 0.5), rgba(10, 10, 12, 0.5))"
const PHOTO_PARTICLE_COLOR = "#ffffff"

const FOCUS_THEME_KEY = "astra:focus-theme"

function loadFocusTheme(): FocusTheme {
  if (typeof window === "undefined") return "default"
  const raw = localStorage.getItem(FOCUS_THEME_KEY)
  if (raw === "default" || raw === "custom") return raw
  if (raw?.startsWith(LIBRARY_ID_PREFIX) && findLibraryBackground(raw.slice(LIBRARY_ID_PREFIX.length))) {
    return raw as FocusTheme
  }
  return "default"
}

function ThemePicker({
  theme,
  customImageUrl,
  onChange,
  onUploadFile,
  onRemoveCustom,
}: {
  theme: FocusTheme
  customImageUrl: string | null
  onChange: (theme: FocusTheme) => void
  onUploadFile: (file: File) => void
  onRemoveCustom: () => void
}) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  function handleCustomClick() {
    if (customImageUrl) {
      onChange("custom")
    } else {
      fileInputRef.current?.click()
    }
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) onUploadFile(file)
    event.target.value = ""
  }

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          title="Atmosfera de fundo"
        >
          <Palette className={cn("size-4", theme === "default" && "text-muted-foreground/50")} />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="z-[110] max-h-96 w-64 overflow-y-auto" align="end">
        <div className="flex flex-col gap-3">
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              title="Padrão"
              onClick={() => onChange("default")}
              className={cn(
                "size-8 shrink-0 rounded-full border-2 bg-muted transition-transform hover:scale-110",
                theme === "default" ? "border-primary" : "border-transparent",
              )}
            />

            <div className="relative shrink-0">
              <button
                type="button"
                title={customImageUrl ? "Sua foto" : "Enviar sua foto"}
                onClick={handleCustomClick}
                className={cn(
                  "flex size-8 items-center justify-center rounded-full border-2 bg-cover bg-center transition-transform hover:scale-110",
                  theme === "custom" ? "border-primary" : "border-transparent",
                  !customImageUrl && "bg-muted",
                )}
                style={customImageUrl ? { backgroundImage: `url(${customImageUrl})` } : undefined}
              >
                {!customImageUrl && <ImagePlus className="size-4 text-muted-foreground" />}
              </button>
              {customImageUrl && (
                <button
                  type="button"
                  title="Remover sua foto"
                  onClick={onRemoveCustom}
                  className="absolute -right-1 -top-1 flex size-4 items-center justify-center rounded-full border border-border bg-background text-muted-foreground hover:text-foreground"
                >
                  <X className="size-2.5" />
                </button>
              )}
            </div>
          </div>

          {FOCUS_BACKGROUND_LIBRARY.map((category) => (
            <div key={category.id} className="flex flex-col gap-1.5">
              <p className="text-xs font-medium text-muted-foreground">{category.label}</p>
              <div className="flex flex-wrap gap-1.5">
                {category.backgrounds.map((bg) => {
                  const value: FocusTheme = `${LIBRARY_ID_PREFIX}${bg.id}`
                  return (
                    <button
                      key={bg.id}
                      type="button"
                      title={bg.label}
                      onClick={() => onChange(value)}
                      className={cn(
                        "size-8 shrink-0 rounded-full border-2 bg-cover bg-center transition-transform hover:scale-110",
                        theme === value ? "border-primary" : "border-transparent",
                      )}
                      style={{ backgroundImage: `url(${bg.image})` }}
                    />
                  )
                })}
              </div>
            </div>
          ))}
        </div>
        <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={handleFileChange} />
      </PopoverContent>
    </Popover>
  )
}

type Props = {
  open: boolean
  onExit: () => void
  dailyGoal: GoalProgress | null
  focusedMinutes: number
  pomodoroMinutes: number
  completedPomodoros: number
  pomodorosUntilLongBreak: number
  disableBreaks: boolean
  sessionCaption: string
  children: ReactNode
}

// Sem divisão por pomodoro, ou divisão fina demais pra caber — cai pra uma
// barra contínua em vez de segmentos ilegíveis.
const MAX_SEGMENTS = 24

function GoalProgressBar({
  achievedMinutes,
  targetMinutes,
  segmentMinutes,
}: {
  achievedMinutes: number
  targetMinutes: number
  segmentMinutes: number
}) {
  const segmentCount = segmentMinutes > 0 ? Math.max(1, Math.ceil(targetMinutes / segmentMinutes)) : 1

  if (segmentCount <= 1 || segmentCount > MAX_SEGMENTS) {
    const percent = targetMinutes > 0 ? Math.min(100, (achievedMinutes / targetMinutes) * 100) : 0
    return (
      <div className="h-2 w-full overflow-hidden rounded-full bg-muted">
        <div className="h-full bg-emerald-500 transition-all" style={{ width: `${percent}%` }} />
      </div>
    )
  }

  return (
    <div className="flex h-2 w-full gap-1">
      {Array.from({ length: segmentCount }, (_, i) => {
        const segStart = i * segmentMinutes
        const segSize = Math.min(targetMinutes, segStart + segmentMinutes) - segStart
        const segAchieved = Math.min(segSize, Math.max(0, achievedMinutes - segStart))
        const segPercent = segSize > 0 ? (segAchieved / segSize) * 100 : 0
        return (
          <div key={i} className="h-full flex-1 overflow-hidden rounded-full bg-muted">
            <div className="h-full bg-emerald-500 transition-all" style={{ width: `${segPercent}%` }} />
          </div>
        )
      })}
    </div>
  )
}

function DailyGoalPanel({
  open,
  goal,
  focusedMinutes,
  pomodoroMinutes,
  completedPomodoros,
  pomodorosUntilLongBreak,
  disableBreaks,
  sessionCaption,
  reducedMotion,
}: {
  open: boolean
  goal: GoalProgress | null
  focusedMinutes: number
  pomodoroMinutes: number
  completedPomodoros: number
  pomodorosUntilLongBreak: number
  disableBreaks: boolean
  sessionCaption: string
  reducedMotion: boolean
}) {
  // achievedHours vem do /dashboard (sessões já salvas antes de abrir o foco);
  // somamos o tempo ao vivo da sessão atual (ainda não salva) por cima, pra o
  // card acompanhar o cronômetro em tempo real sem precisar de outra chamada.
  const achievedMinutesNow = goal ? Math.round(goal.achievedHours * 60) + focusedMinutes : 0
  const targetMinutes = goal ? Math.round(goal.targetHours * 60) : 0
  const reached = goal ? achievedMinutesNow >= targetMinutes : false

  // Sem meta, a única "etapa" que faz sentido é o ciclo de pomodoros até a
  // pausa longa — reaproveita a mesma barra segmentada, só trocando o alvo.
  const showCycleProgress = !goal && !disableBreaks && pomodorosUntilLongBreak > 0
  const cycleTargetMinutes = pomodorosUntilLongBreak * pomodoroMinutes
  const cycleAchievedMinutes = Math.min(cycleTargetMinutes, focusedMinutes - completedPomodoros * pomodoroMinutes) +
    (completedPomodoros % pomodorosUntilLongBreak) * pomodoroMinutes

  return (
    <motion.div
      initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94, y: -8 }}
      animate={
        reducedMotion
          ? { opacity: open ? 1 : 0 }
          : { opacity: open ? 1 : 0, scale: open ? 1 : 0.96, y: open ? 0 : -8 }
      }
      transition={{ type: "spring", damping: 22, stiffness: 320, mass: 0.8 }}
      className="fixed right-4 top-28 z-10 w-80 rounded-2xl border border-border bg-popover p-5 shadow-lg"
    >
      <h3 className="text-sm font-medium text-popover-foreground">{goal ? "Meta diária" : "Tempo de foco"}</h3>
      {goal ? (
        <div className="mt-3 flex flex-col gap-3">
          <div className="flex items-baseline justify-between">
            <p className="text-3xl font-semibold tabular-nums text-foreground">
              {formatMinutes(achievedMinutesNow)}
            </p>
            <p className="text-sm text-muted-foreground">de {formatMinutes(targetMinutes)}</p>
          </div>
          <GoalProgressBar
            achievedMinutes={achievedMinutesNow}
            targetMinutes={targetMinutes}
            segmentMinutes={pomodoroMinutes}
          />
          {reached ? (
            <p className="flex items-center gap-1.5 text-sm text-emerald-600">
              <CheckCircle2 className="size-4" />
              Meta batida
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              Faltam {formatMinutes(Math.max(0, targetMinutes - achievedMinutesNow))}
            </p>
          )}
        </div>
      ) : (
        <div className="mt-3 flex flex-col gap-3">
          <p className="text-3xl font-semibold tabular-nums text-foreground">{formatMinutes(focusedMinutes)}</p>
          {showCycleProgress ? (
            <>
              <GoalProgressBar
                achievedMinutes={cycleAchievedMinutes}
                targetMinutes={cycleTargetMinutes}
                segmentMinutes={pomodoroMinutes}
              />
              <p className="text-sm text-muted-foreground">{sessionCaption}</p>
            </>
          ) : (
            <p className="text-sm text-muted-foreground">Você ainda não definiu uma meta diária.</p>
          )}
        </div>
      )}
    </motion.div>
  )
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

export function FocusModeOverlay({
  open,
  onExit,
  dailyGoal,
  focusedMinutes,
  pomodoroMinutes,
  completedPomodoros,
  pomodorosUntilLongBreak,
  disableBreaks,
  sessionCaption,
  children,
}: Props) {
  const reducedMotion = useReducedMotion()
  const { theme, toggleTheme } = useTheme()
  const [particlesEnabled, setParticlesEnabled] = useState(loadParticlesEnabled)
  const [focusTheme, setFocusTheme] = useState<FocusTheme>(loadFocusTheme)
  const [customImageUrl, setCustomImageUrl] = useState<string | null>(null)
  const [isFullscreen, setIsFullscreen] = useState(() => !!document.fullscreenElement)
  const [fullscreenTransitioning, setFullscreenTransitioning] = useState(false)
  const [goalPanelOpen, setGoalPanelOpen] = useState(false)
  const [goalPanelRendered, setGoalPanelRendered] = useState(false)
  const goalPanelRef = useRef<HTMLDivElement>(null)

  // Timer manual em vez de AnimatePresence.exit: essa versão do motion trava
  // sem desmontar quando o painel abre/fecha rápido (ex.: clique-fora logo
  // seguido de reabrir) — mesmo contorno já usado no EditProfileModal.
  useEffect(() => {
    if (goalPanelOpen) {
      setGoalPanelRendered(true)
      return
    }
    const timeout = setTimeout(() => setGoalPanelRendered(false), 300)
    return () => clearTimeout(timeout)
  }, [goalPanelOpen])

  useEffect(() => {
    if (!goalPanelOpen) return
    function handlePointerDown(event: PointerEvent) {
      const target = event.target as HTMLElement
      if (goalPanelRef.current?.contains(target)) return
      if (target.closest?.("[data-goal-toggle]")) return
      setGoalPanelOpen(false)
    }
    document.addEventListener("pointerdown", handlePointerDown)
    return () => document.removeEventListener("pointerdown", handlePointerDown)
  }, [goalPanelOpen])

  useEffect(() => {
    function handleFullscreenChange() {
      setIsFullscreen(!!document.fullscreenElement)
      // A troca de tamanho da janela já aconteceu; espera um instante pro
      // layout assentar antes de revelar de novo, senão o fade-in acontece
      // em cima do salto de redimensionamento.
      window.setTimeout(() => setFullscreenTransitioning(false), 60)
    }
    document.addEventListener("fullscreenchange", handleFullscreenChange)
    return () => document.removeEventListener("fullscreenchange", handleFullscreenChange)
  }, [])

  useEffect(() => {
    if (!open && document.fullscreenElement) {
      document.exitFullscreen().catch(() => {})
    }
  }, [open])

  // A API de tela cheia em si não anima (o navegador redimensiona de uma
  // vez). Escondemos o conteúdo com um fade curto antes de pedir a troca, e
  // revelamos de novo só depois que o "fullscreenchange" confirma que o
  // redimensionamento já ocorreu — assim a transição bruta fica encoberta.
  function toggleFullscreen() {
    setFullscreenTransitioning(true)
    window.setTimeout(() => {
      const request = document.fullscreenElement
        ? document.exitFullscreen()
        : document.documentElement.requestFullscreen()
      request.catch(() => setFullscreenTransitioning(false))
    }, 180)
  }

  function toggleParticles() {
    setParticlesEnabled((enabled) => {
      const next = !enabled
      localStorage.setItem(PARTICLES_KEY, String(next))
      return next
    })
  }

  function changeFocusTheme(next: FocusTheme) {
    setFocusTheme(next)
    localStorage.setItem(FOCUS_THEME_KEY, next)
  }

  useEffect(() => {
    let objectUrl: string | null = null
    getCustomFocusImage()
      .then((blob) => {
        if (!blob) {
          if (focusTheme === "custom") changeFocusTheme("default")
          return
        }
        objectUrl = URL.createObjectURL(blob)
        setCustomImageUrl(objectUrl)
      })
      .catch(() => {
        if (focusTheme === "custom") changeFocusTheme("default")
      })
    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function handleUploadFile(file: File) {
    const resized = await resizeImageForFocusBackground(file)
    await setCustomFocusImage(resized)
    setCustomImageUrl((previous) => {
      if (previous) URL.revokeObjectURL(previous)
      return URL.createObjectURL(resized)
    })
    changeFocusTheme("custom")
  }

  async function handleRemoveCustom() {
    await clearCustomFocusImage()
    setCustomImageUrl((previous) => {
      if (previous) URL.revokeObjectURL(previous)
      return null
    })
    if (focusTheme === "custom") changeFocusTheme("default")
  }

  const activeTheme = (() => {
    if (focusTheme === "custom") {
      return customImageUrl ? { image: customImageUrl, overlay: PHOTO_OVERLAY, particleColor: PHOTO_PARTICLE_COLOR } : null
    }
    if (focusTheme.startsWith(LIBRARY_ID_PREFIX)) {
      const bg = findLibraryBackground(focusTheme.slice(LIBRARY_ID_PREFIX.length))
      return bg ? { image: bg.image, overlay: PHOTO_OVERLAY, particleColor: PHOTO_PARTICLE_COLOR } : null
    }
    return null
  })()
  const particleColor = activeTheme?.particleColor ?? (theme === "dark" ? "#ffffff" : "#000000")

  // html tem scrollbar-gutter: stable (index.css) pra evitar salto de layout
  // nas outras páginas. Aqui dentro não existe scroll nenhum (overflow some
  // logo abaixo), então essa reserva de espaço só sobra como uma faixa em
  // branco à direita quando a página entra em tela cheia — desligamos os
  // dois junto com o overflow enquanto o modo foco estiver aberto.
  useEffect(() => {
    if (!open) return
    const previousBodyOverflow = document.body.style.overflow
    const previousHtmlOverflow = document.documentElement.style.overflow
    const previousGutter = document.documentElement.style.scrollbarGutter
    document.body.style.overflow = "hidden"
    document.documentElement.style.overflow = "hidden"
    document.documentElement.style.scrollbarGutter = "auto"
    return () => {
      document.body.style.overflow = previousBodyOverflow
      document.documentElement.style.overflow = previousHtmlOverflow
      document.documentElement.style.scrollbarGutter = previousGutter
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
          <div
            className={cn(
              "transition-opacity duration-200 ease-out",
              fullscreenTransitioning ? "opacity-0" : "opacity-100",
            )}
          >
            {activeTheme && (
              <div
                aria-hidden
                className="pointer-events-none absolute inset-0 z-0 bg-cover bg-center"
                style={{ backgroundImage: `${activeTheme.overlay}, url(${activeTheme.image})` }}
              />
            )}

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
                  <Particles className="h-full w-full" quantity={100} ease={80} color={particleColor} />
                </motion.div>
              )}
            </AnimatePresence>

            <div className="fixed left-4 top-4 z-10 flex items-center gap-3">
              <LiveClock />
              <BatteryIndicator />
            </div>

            <div className="fixed right-4 top-4 z-10 flex flex-col items-end gap-1">
              <div className="flex items-center gap-1">
                <ThemePicker
                  theme={focusTheme}
                  customImageUrl={customImageUrl}
                  onChange={changeFocusTheme}
                  onUploadFile={handleUploadFile}
                  onRemoveCustom={handleRemoveCustom}
                />
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
                  title={isFullscreen ? "Sair da tela cheia" : "Tela cheia"}
                  onClick={toggleFullscreen}
                >
                  {isFullscreen ? <Minimize className="size-4" /> : <Maximize className="size-4" />}
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
              <Button
                type="button"
                variant="ghost"
                size="icon"
                data-goal-toggle
                title={goalPanelOpen ? "Fechar meta diária" : "Ver meta diária"}
                onClick={() => setGoalPanelOpen((o) => !o)}
              >
                <Target className={cn("size-4", !goalPanelOpen && "text-muted-foreground/50")} />
              </Button>
            </div>

            <div ref={goalPanelRef}>
              {goalPanelRendered && (
                <DailyGoalPanel
                  open={goalPanelOpen}
                  goal={dailyGoal}
                  focusedMinutes={focusedMinutes}
                  pomodoroMinutes={pomodoroMinutes}
                  completedPomodoros={completedPomodoros}
                  pomodorosUntilLongBreak={pomodorosUntilLongBreak}
                  disableBreaks={disableBreaks}
                  sessionCaption={sessionCaption}
                  reducedMotion={!!reducedMotion}
                />
              )}
            </div>

            <div className="relative z-10 mx-auto flex min-h-svh w-full max-w-2xl flex-col px-4 py-16 sm:px-8">
              {children}
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body,
  )
}
