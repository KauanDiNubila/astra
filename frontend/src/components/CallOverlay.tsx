import { useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import {
  HeadphoneOff,
  Headphones,
  Maximize2,
  Mic,
  MicOff,
  Minimize2,
  MonitorOff,
  MonitorUp,
  PhoneOff,
  Settings,
  Video,
  VideoOff,
} from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useCall } from "@/context/CallContext"
import type { CallContextValue } from "@/context/CallContext"
import { useDelayedUnmount, useFrozen } from "@/hooks/useDelayedUnmount"
import { cn } from "@/lib/utils"
import { deviceLabel, supportsSpeakerSelection } from "@/lib/callDevices"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Switch } from "@/components/ui/switch"

type Tile = {
  key: string
  kind: "person" | "screen"
  userId: string
  name: string
  stream: MediaStream | null
  isSelf: boolean
  speaking: boolean
  connection: RTCPeerConnectionState | null
}

function formatElapsed(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  const h = Math.floor(m / 60)
  const pad = (n: number) => n.toString().padStart(2, "0")
  return h > 0 ? `${h}:${pad(m % 60)}:${pad(s)}` : `${pad(m)}:${pad(s)}`
}

function CallTimer({ startedAt, className }: { startedAt: number | null; className?: string }) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const interval = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(interval)
  }, [])
  if (!startedAt) return null
  return <span className={cn("tabular-nums", className)}>{formatElapsed(Math.max(0, Math.floor((now - startedAt) / 1000)))}</span>
}

function hasLiveVideo(stream: MediaStream | null) {
  return !!stream && stream.getVideoTracks().some((t) => t.readyState === "live" && !t.muted)
}

function MediaAudio({ stream, deafened, speakerId }: { stream: MediaStream; deafened: boolean; speakerId: string }) {
  const ref = useRef<HTMLAudioElement>(null)
  useEffect(() => {
    if (ref.current && ref.current.srcObject !== stream) ref.current.srcObject = stream
  }, [stream])
  useEffect(() => {
    const element = ref.current as (HTMLAudioElement & { setSinkId?: (id: string) => Promise<void> }) | null
    if (element?.setSinkId) element.setSinkId(speakerId).catch(() => {})
  }, [speakerId])
  useEffect(() => {
    if (ref.current) ref.current.muted = deafened
  }, [deafened])
  return <audio ref={ref} autoPlay />
}

function TileVideo({ stream, mirror, dark }: { stream: MediaStream; mirror?: boolean; dark?: boolean }) {
  const ref = useRef<HTMLVideoElement>(null)
  useEffect(() => {
    if (ref.current && ref.current.srcObject !== stream) ref.current.srcObject = stream
  }, [stream])
  return (
    <video
      ref={ref}
      autoPlay
      playsInline
      muted
      className={cn(
        "absolute inset-0 size-full object-contain",
        dark ? "bg-black" : "bg-muted",
        mirror && "-scale-x-100",
      )}
    />
  )
}

function CallTile({
  tile,
  onClick,
  compact,
}: {
  tile: Tile
  onClick: () => void
  compact?: boolean
}) {
  const showVideo = tile.kind === "screen" ? !!tile.stream : tile.stream !== null && hasLiveVideo(tile.stream)
  const connecting = tile.connection !== null && tile.connection !== "connected"

  const reducedMotion = useReducedMotion()

  return (
    <motion.button
      type="button"
      onClick={onClick}
      initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
      animate={reducedMotion ? { opacity: 1 } : { opacity: 1, scale: 1 }}
      transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
      className={cn(
        "relative flex min-h-0 items-center justify-center overflow-hidden rounded-xl bg-muted outline-none ring-2 ring-transparent transition-shadow duration-150 focus-visible:ring-ring",
        tile.speaking && tile.kind === "person" && "ring-emerald-500",
        compact ? "aspect-video h-full shrink-0" : "size-full",
      )}
    >
      {showVideo && tile.stream ? (
        <TileVideo stream={tile.stream} mirror={tile.isSelf && tile.kind === "person"} dark={tile.kind === "screen"} />
      ) : (
        <UserAvatar userId={tile.userId} name={tile.name} size={compact ? "default" : "xl"} />
      )}
      <span className="absolute bottom-2 left-2 flex max-w-[85%] items-center gap-1.5 rounded-md bg-black/60 px-2 py-0.5 text-xs text-white">
        <span className="truncate">
          {tile.kind === "screen" ? (tile.isSelf ? "Sua tela" : `Tela de ${tile.name}`) : tile.name}
        </span>
      </span>
      {connecting && (
        <span className="absolute right-2 top-2 rounded-md bg-black/60 px-2 py-0.5 text-[11px] text-white">
          Conectando…
        </span>
      )}
    </motion.button>
  )
}

function DeviceSelect({
  label,
  value,
  devices,
  fallback,
  onChange,
}: {
  label: string
  value: string
  devices: MediaDeviceInfo[]
  fallback: string
  onChange: (deviceId: string) => void
}) {
  const known = devices.some((d) => d.deviceId === value)
  return (
    <label className="flex flex-col gap-1 text-xs font-medium text-muted-foreground">
      {label}
      <select
        value={known ? value : ""}
        onChange={(e) => onChange(e.target.value)}
        className="h-9 w-full rounded-md border bg-background px-2 text-sm font-normal text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring max-sm:h-11"
      >
        <option value="">Padrão do sistema</option>
        {devices.map((d, i) => (
          <option key={d.deviceId} value={d.deviceId}>
            {deviceLabel(d, i, fallback)}
          </option>
        ))}
      </select>
    </label>
  )
}

function ProcessingToggle({
  id,
  label,
  description,
  checked,
  onChange,
}: {
  id: string
  label: string
  description: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-start gap-3">
      <div className="min-w-0 flex-1">
        <label htmlFor={id} className="text-sm font-medium">
          {label}
        </label>
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
      <Switch id={id} checked={checked} onCheckedChange={onChange} className="mt-0.5" />
    </div>
  )
}

function DeviceSettings({ buttonClass }: { buttonClass: string }) {
  const { devices, devicePrefs, refreshDevices, selectMic, selectCamera, selectSpeaker, setMicProcessing } = useCall()
  const speakerSupported = supportsSpeakerSelection()

  return (
    <Popover onOpenChange={(open) => open && void refreshDevices()}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          size="icon"
          variant="secondary"
          className={buttonClass}
          title="Configurações de dispositivos"
          aria-label="Configurações de dispositivos"
        >
          <Settings className="size-5" />
        </Button>
      </PopoverTrigger>
      <PopoverContent side="top" align="center" className="w-80 max-w-[calc(100vw-2rem)] gap-3 p-3">
        <p className="font-medium">Dispositivos</p>
        <DeviceSelect
          label="Microfone"
          value={devicePrefs.micId}
          devices={devices.mics}
          fallback="Microfone"
          onChange={(id) => void selectMic(id)}
        />
        {speakerSupported && (
          <DeviceSelect
            label="Saída de áudio"
            value={devicePrefs.speakerId}
            devices={devices.speakers}
            fallback="Alto-falante"
            onChange={selectSpeaker}
          />
        )}
        <DeviceSelect
          label="Câmera"
          value={devicePrefs.cameraId}
          devices={devices.cameras}
          fallback="Câmera"
          onChange={(id) => void selectCamera(id)}
        />
        {devices.cameras.length > 0 && devices.cameras.every((d) => !d.label) && (
          <p className="text-xs text-muted-foreground">Ligue a câmera uma vez para ver os nomes dos dispositivos.</p>
        )}
        <div className="flex flex-col gap-3 border-t pt-3">
          <ProcessingToggle
            id="call-echo-cancellation"
            label="Cancelamento de eco"
            description="Evita que os outros ouçam a própria voz pela sua caixa de som. Com fone de ouvido, pode desligar."
            checked={devicePrefs.echoCancellation}
            onChange={(checked) => void setMicProcessing({ echoCancellation: checked })}
          />
          <ProcessingToggle
            id="call-noise-suppression"
            label="Supressão de ruído"
            description="Reduz ruídos de fundo, como ventilador e teclado."
            checked={devicePrefs.noiseSuppression}
            onChange={(checked) => void setMicProcessing({ noiseSuppression: checked })}
          />
        </div>
      </PopoverContent>
    </Popover>
  )
}

function CallControls() {
  const {
    muted,
    deafened,
    cameraOn,
    screenSharing,
    screenShareSupported,
    toggleMute,
    toggleDeafen,
    toggleCamera,
    toggleScreenShare,
    leave,
  } = useCall()

  const base = "size-12 rounded-full max-sm:size-12"

  return (
    <div className="flex flex-wrap items-center justify-center gap-2 px-3 pt-3 pb-[max(0.75rem,env(safe-area-inset-bottom))]">
      <Button
        type="button"
        size="icon"
        variant={muted ? "destructive" : "secondary"}
        className={base}
        onClick={toggleMute}
        title={muted ? "Ativar microfone" : "Silenciar microfone"}
        aria-label={muted ? "Ativar microfone" : "Silenciar microfone"}
        aria-pressed={muted}
      >
        {muted ? <MicOff className="size-5" /> : <Mic className="size-5" />}
      </Button>
      <Button
        type="button"
        size="icon"
        variant={deafened ? "destructive" : "secondary"}
        className={base}
        onClick={toggleDeafen}
        title={deafened ? "Ativar áudio" : "Desativar áudio da call"}
        aria-label={deafened ? "Ativar áudio" : "Desativar áudio da call"}
        aria-pressed={deafened}
      >
        {deafened ? <HeadphoneOff className="size-5" /> : <Headphones className="size-5" />}
      </Button>
      <Button
        type="button"
        size="icon"
        variant={cameraOn ? "default" : "secondary"}
        className={base}
        onClick={() => void toggleCamera()}
        title={cameraOn ? "Desligar câmera" : "Ligar câmera"}
        aria-label={cameraOn ? "Desligar câmera" : "Ligar câmera"}
        aria-pressed={cameraOn}
      >
        {cameraOn ? <Video className="size-5" /> : <VideoOff className="size-5" />}
      </Button>
      {screenShareSupported && (
        <Button
          type="button"
          size="icon"
          variant={screenSharing ? "default" : "secondary"}
          className={base}
          onClick={() => void toggleScreenShare()}
          title={screenSharing ? "Parar de compartilhar a tela" : "Compartilhar tela"}
          aria-label={screenSharing ? "Parar de compartilhar a tela" : "Compartilhar tela"}
          aria-pressed={screenSharing}
        >
          {screenSharing ? <MonitorOff className="size-5" /> : <MonitorUp className="size-5" />}
        </Button>
      )}
      <DeviceSettings buttonClass={base} />
      <Button
        type="button"
        size="icon"
        variant="destructive"
        className={cn(base, "ml-2")}
        onClick={leave}
        title="Sair da chamada"
        aria-label="Sair da chamada"
      >
        <PhoneOff className="size-5" />
      </Button>
    </div>
  )
}

function gridClass(count: number) {
  if (count <= 1) return "grid-cols-1"
  if (count === 2) return "grid-cols-1 sm:grid-cols-2"
  if (count <= 4) return "grid-cols-2"
  return "grid-cols-2 lg:grid-cols-3"
}

type CallView = {
  title: string
  ringing: boolean
  startedAt: number | null
  count: number
  troubled: boolean
  tiles: Tile[]
}

function buildView(call: CallContextValue, userId: string): CallView {
  const tiles: Tile[] = [
    {
      key: "self",
      kind: "person",
      userId,
      name: "Você",
      stream: call.cameraOn ? call.localStream : null,
      isSelf: true,
      speaking: !!call.speaking.self,
      connection: null,
    },
  ]
  if (call.screenSharing && call.localScreen) {
    tiles.push({
      key: "self-screen",
      kind: "screen",
      userId,
      name: "Você",
      stream: call.localScreen,
      isSelf: true,
      speaking: false,
      connection: null,
    })
  }
  for (const peer of call.participants) {
    const media = call.remoteMedia(peer.clientId)
    tiles.push({
      key: peer.clientId,
      kind: "person",
      userId: peer.userId,
      name: peer.name || "Participante",
      stream: media.camera,
      isSelf: false,
      speaking: !!call.speaking[peer.clientId],
      connection: call.connectionOf(peer.clientId),
    })
    if (media.screen) {
      tiles.push({
        key: `${peer.clientId}-screen`,
        kind: "screen",
        userId: peer.userId,
        name: peer.name || "participante",
        stream: media.screen,
        isSelf: false,
        speaking: false,
        connection: null,
      })
    }
  }
  return {
    title: call.call?.title ?? "",
    ringing: call.ringing,
    startedAt: call.startedAt,
    count: call.participants.length + 1,
    troubled: call.participants.some((p) => call.connectionOf(p.clientId) === "failed"),
    tiles,
  }
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
  const { setMinimized } = useCall()
  const [pinnedKey, setPinnedKey] = useState<string | null>(null)
  const { tiles } = view

  const autoStageKey = tiles.find((t) => t.kind === "screen" && !t.isSelf)?.key ?? null
  const stageKey = pinnedKey && tiles.some((t) => t.key === pinnedKey) ? pinnedKey : autoStageKey
  const stage = tiles.find((t) => t.key === stageKey) ?? null
  const strip = tiles.filter((t) => t.key !== stageKey)

  function togglePin(key: string) {
    setPinnedKey((current) => (current === key ? null : key))
  }

  return (
    <Presence
      visible={visible}
      from={{ scale: 0.985 }}
      role="dialog"
      aria-label={`Chamada com ${view.title}`}
      className="fixed inset-0 z-50 flex flex-col bg-background pt-[env(safe-area-inset-top)]"
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
              <CallTile tile={stage} onClick={() => togglePin(stage.key)} />
            </div>
            {strip.length > 0 && (
              <div className="flex h-24 shrink-0 gap-2 overflow-x-auto sm:h-32">
                {strip.map((t) => (
                  <CallTile key={t.key} tile={t} compact onClick={() => togglePin(t.key)} />
                ))}
              </div>
            )}
          </>
        ) : (
          <div className={cn("grid min-h-0 flex-1 auto-rows-fr gap-2", gridClass(tiles.length))}>
            {tiles.map((t) => (
              <CallTile key={t.key} tile={t} onClick={() => togglePin(t.key)} />
            ))}
          </div>
        )}
      </div>

      <CallControls />
    </Presence>
  )
}

function MiniCall({ view, visible }: { view: CallView; visible: boolean }) {
  const { muted, toggleMute, setMinimized, leave } = useCall()
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
        title={muted ? "Ativar microfone" : "Silenciar microfone"}
        aria-label={muted ? "Ativar microfone" : "Silenciar microfone"}
      >
        {muted ? <MicOff className="size-4" /> : <Mic className="size-4" />}
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
  const view = useFrozen(buildView(call, user?.id ?? ""), !active)

  const fullVisible = active && !call.minimized
  const miniVisible = active && call.minimized
  const showFull = useDelayedUnmount(fullVisible)
  const showMini = useDelayedUnmount(miniVisible)

  return (
    <>
      {active &&
        call.participants.map((peer) => {
          const media = call.remoteMedia(peer.clientId)
          return (
            <span key={peer.clientId}>
              {media.camera && (
                <MediaAudio
                  key={media.camera.id}
                  stream={media.camera}
                  deafened={call.deafened}
                  speakerId={call.devicePrefs.speakerId}
                />
              )}
              {media.screen && (
                <MediaAudio
                  key={media.screen.id}
                  stream={media.screen}
                  deafened={call.deafened}
                  speakerId={call.devicePrefs.speakerId}
                />
              )}
            </span>
          )
        })}
      {showFull && <FullCall view={view} visible={fullVisible} />}
      {showMini && <MiniCall view={view} visible={miniVisible} />}
    </>
  )
}
