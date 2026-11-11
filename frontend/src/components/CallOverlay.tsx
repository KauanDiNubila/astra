import { useEffect, useState } from "react"
import type { ReactNode } from "react"
import { Maximize2, Mic, MicOff, Minimize2, PhoneOff } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useCall } from "@/context/CallContext"
import { useCoversApp } from "@/hooks/useCoversApp"
import { useDelayedUnmount, useFrozen } from "@/hooks/useDelayedUnmount"
import { useScreenShareToast } from "@/hooks/useScreenShareToast"
import { buildCallView, MIC_LABELS, micStateOf } from "@/lib/callView"
import type { CallView, Tile } from "@/lib/callView"
import { cn } from "@/lib/utils"
import { CallAudio } from "@/components/call/CallAudio"
import { CallControls } from "@/components/call/CallControls"
import { CallTile } from "@/components/call/CallTile"
import { CallTimer } from "@/components/call/CallTimer"
import { ScreenSourcePicker } from "@/components/ScreenSourcePicker"
import { Button } from "@/components/ui/button"

function gridClass(count: number) {
  if (count <= 1) return "grid-cols-1"
  if (count === 2) return "grid-cols-1 sm:grid-cols-2"
  if (count <= 4) return "grid-cols-2"
  return "grid-cols-2 lg:grid-cols-3"
}

const EASE = [0.22, 1, 0.36, 1] as const

function Presence({
  visible,
  from,
  className,
  children,
  ...rest
}: {
  visible: boolean
  from: { y?: number; scale: number }
  className: string
  children: ReactNode
  role?: string
  "aria-label"?: string
}) {
  const reducedMotion = useReducedMotion()
  const hidden = reducedMotion ? { opacity: 0 } : { opacity: 0, y: from.y ?? 0, scale: from.scale }
  const shown = reducedMotion ? { opacity: 1 } : { opacity: 1, y: 0, scale: 1 }

  return (
    <motion.div
      {...rest}
      initial={hidden}
      animate={visible ? shown : hidden}
      transition={{ duration: reducedMotion ? 0.15 : 0.3, ease: EASE }}
      className={cn(className, !visible && "pointer-events-none")}
    >
      {children}
    </motion.div>
  )
}

function FullCall({ view, visible }: { view: CallView; visible: boolean }) {
  useCoversApp(visible)
  const { setMinimized, setWatchingScreen } = useCall()
  const [pinnedKey, setPinnedKey] = useState<string | null>(null)
  const { tiles } = view

  const autoStageKey = tiles.find((t) => t.kind === "screen" && t.status === "live")?.key ?? null
  const stageKey = pinnedKey && tiles.some((t) => t.key === pinnedKey) ? pinnedKey : autoStageKey
  const stage = tiles.find((t) => t.key === stageKey) ?? null
  const strip = tiles.filter((t) => t.key !== stageKey)

  function togglePin(key: string) {
    setPinnedKey((current) => (current === key ? null : key))
  }

  function handleTileClick(tile: Tile) {
    if (tile.kind === "screen") {
      if (tile.status === "offer" && tile.clientId) {
        setWatchingScreen(tile.clientId, true)
        return
      }
      if (tile.status === "loading") return
    }
    togglePin(tile.key)
  }

  function stopWatching(tile: Tile) {
    if (!tile.clientId) return
    setWatchingScreen(tile.clientId, false)
    setPinnedKey((current) => (current === tile.key ? null : current))
  }

  return (
    <Presence
      visible={visible}
      from={{ scale: 0.985 }}
      role="dialog"
      aria-label={`Chamada com ${view.title}`}
      className="fixed inset-x-0 top-(--titlebar-h) bottom-0 z-50 flex flex-col bg-background pt-[env(safe-area-inset-top)]"
    >
      <div className="flex items-center gap-3 border-b px-4 py-3">
        <div className="min-w-0 flex-1">
          <p className="truncate font-medium">{view.title}</p>
          <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
            {view.ringing ? (
              <span>Chamando…</span>
            ) : view.troubled ? (
              <span className="text-destructive">Conexão instável</span>
            ) : (
              <>
                <span className="size-1.5 rounded-full bg-emerald-500" />
                <CallTimer startedAt={view.startedAt} />
                <span>· {view.count} na call</span>
              </>
            )}
          </p>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          title="Minimizar chamada"
          aria-label="Minimizar chamada"
          className="max-sm:size-10"
          onClick={() => setMinimized(true)}
        >
          <Minimize2 className="size-4" />
        </Button>
      </div>

      <div className="flex min-h-0 flex-1 flex-col gap-2 p-2 sm:p-3">
        {stage ? (
          <>
            <div className="min-h-0 flex-1">
              <CallTile tile={stage} onClick={() => handleTileClick(stage)} onStopWatching={() => stopWatching(stage)} />
            </div>
            {strip.length > 0 && (
              <div className="-m-0.5 flex h-25 shrink-0 gap-2 overflow-x-auto p-0.5 sm:h-33">
                {strip.map((t) => (
                  <CallTile
                    key={t.key}
                    tile={t}
                    compact
                    onClick={() => handleTileClick(t)}
                    onStopWatching={() => stopWatching(t)}
                  />
                ))}
              </div>
            )}
          </>
        ) : (
          <div className={cn("grid min-h-0 flex-1 auto-rows-fr gap-2", gridClass(tiles.length))}>
            {tiles.map((t) => (
              <CallTile key={t.key} tile={t} onClick={() => handleTileClick(t)} onStopWatching={() => stopWatching(t)} />
            ))}
          </div>
        )}
      </div>

      <CallControls />
    </Presence>
  )
}

function MiniCall({ view, visible }: { view: CallView; visible: boolean }) {
  const { muted, micMissing, toggleMute, setMinimized, leave } = useCall()
  const mic = micStateOf(muted, micMissing)
  return (
    <Presence
      visible={visible}
      from={{ y: 16, scale: 0.96 }}
      className="fixed bottom-4 right-4 z-50 flex items-center gap-2 rounded-full border bg-card py-2 pl-4 pr-2 shadow-lg max-md:bottom-[calc(4.25rem+env(safe-area-inset-bottom))] max-md:left-4 max-md:right-auto"
    >
      <span className="size-2 shrink-0 rounded-full bg-emerald-500" />
      <button
        type="button"
        onClick={() => setMinimized(false)}
        className="flex min-w-0 flex-col text-left"
        title="Abrir chamada"
      >
        <span className="max-w-32 truncate text-sm font-medium">{view.title}</span>
        <span className="text-[11px] text-muted-foreground">
          {view.ringing ? "Chamando…" : <CallTimer startedAt={view.startedAt} />}
        </span>
      </button>
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="size-8 rounded-full"
        onClick={toggleMute}
        title={MIC_LABELS[mic]}
        aria-label={MIC_LABELS[mic]}
      >
        {mic !== "on" ? <MicOff className="size-4" /> : <Mic className="size-4" />}
      </Button>
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="size-8 rounded-full"
        onClick={() => setMinimized(false)}
        title="Expandir chamada"
        aria-label="Expandir chamada"
      >
        <Maximize2 className="size-4" />
      </Button>
      <Button
        type="button"
        variant="destructive"
        size="icon"
        className="size-8 rounded-full"
        onClick={leave}
        title="Sair da chamada"
        aria-label="Sair da chamada"
      >
        <PhoneOff className="size-4" />
      </Button>
    </Presence>
  )
}

export function CallOverlay() {
  const { user } = useAuth()
  const call = useCall()
  const active = call.phase === "active"
  const view = useFrozen(buildCallView(call, user?.id ?? ""), !active)

  const fullVisible = active && !call.minimized
  const miniVisible = active && call.minimized
  const showFull = useDelayedUnmount(fullVisible)
  const showMini = useDelayedUnmount(miniVisible)

  useEffect(() => {
    if (!miniVisible) return
    document.documentElement.dataset.miniCall = ""
    return () => {
      delete document.documentElement.dataset.miniCall
    }
  }, [miniVisible])

  useScreenShareToast(active)

  return (
    <>
      {active && <CallAudio />}
      {showFull && <FullCall view={view} visible={fullVisible} />}
      {showMini && <MiniCall view={view} visible={miniVisible} />}
      {active && <ScreenSourcePicker />}
    </>
  )
}
