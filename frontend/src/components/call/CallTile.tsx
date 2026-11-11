import { useEffect, useRef } from "react"
import type { ReactNode } from "react"
import { EyeOff, MicOff, MonitorUp } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { UserAvatar } from "@/components/UserAvatar"
import { connectionNotice } from "@/lib/callView"
import type { PersonTile as PersonTileData, ScreenTile as ScreenTileData, Tile } from "@/lib/callView"
import { cn } from "@/lib/utils"

function hasLiveVideo(stream: MediaStream | null) {
  return !!stream && stream.getVideoTracks().some((t) => t.readyState === "live" && !t.muted)
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

type TileFrameProps = {
  compact?: boolean
  speaking?: boolean
  label: string
  noMic?: boolean
  notice?: string | null
  disabled?: boolean
  accessibleName?: string
  onClick: () => void
  extra?: ReactNode
  children: ReactNode
}

function TileFrame({
  compact,
  speaking,
  label,
  noMic,
  notice,
  disabled,
  accessibleName,
  onClick,
  extra,
  children,
}: TileFrameProps) {
  const reducedMotion = useReducedMotion()

  return (
    <motion.div
      initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96 }}
      animate={reducedMotion ? { opacity: 1 } : { opacity: 1, scale: 1 }}
      transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
      className={cn("relative min-h-0", compact ? "aspect-video h-full shrink-0" : "size-full")}
    >
      <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        title={accessibleName}
        aria-label={accessibleName}
        className={cn(
          "group/tile relative flex size-full items-center justify-center overflow-hidden rounded-xl bg-muted outline-none ring-2 ring-transparent transition-shadow duration-150 focus-visible:ring-ring",
          speaking && "ring-emerald-500",
        )}
      >
        {children}
        <span className="absolute bottom-2 left-2 flex max-w-[85%] items-center gap-1.5 rounded-md bg-black/60 px-2 py-0.5 text-xs text-white">
          <span className="truncate">{label}</span>
          {noMic && (
            <MicOff className="size-3.5 shrink-0 text-red-400" aria-label="Sem microfone" role="img">
              <title>Sem microfone</title>
            </MicOff>
          )}
        </span>
        {notice && (
          <span className="absolute right-2 top-2 rounded-md bg-black/60 px-2 py-0.5 text-[11px] text-white">
            {notice}
          </span>
        )}
      </button>
      {extra}
    </motion.div>
  )
}

type TileProps<T extends Tile> = {
  tile: T
  compact?: boolean
  onClick: () => void
  onStopWatching?: () => void
}

function PersonTile({ tile, compact, onClick }: TileProps<PersonTileData>) {
  return (
    <TileFrame
      compact={compact}
      speaking={tile.speaking}
      label={tile.name}
      noMic={tile.noMic}
      notice={connectionNotice(tile.connection)}
      onClick={onClick}
    >
      {tile.stream && hasLiveVideo(tile.stream) ? (
        <TileVideo stream={tile.stream} mirror={tile.isSelf} />
      ) : (
        <UserAvatar userId={tile.userId} name={tile.name} size={compact ? "default" : "xl"} />
      )}
    </TileFrame>
  )
}

function ScreenTile({ tile, compact, onClick, onStopWatching }: TileProps<ScreenTileData>) {
  const offer = tile.status === "offer" || tile.status === "loading"
  const label = tile.status === "own" ? "Sua tela" : `Tela de ${tile.name}`

  return (
    <TileFrame
      compact={compact}
      label={label}
      disabled={tile.status === "loading"}
      accessibleName={offer ? `Assistir a tela de ${tile.name}` : undefined}
      onClick={onClick}
      extra={
        tile.status === "live" &&
        onStopWatching && (
          <button
            type="button"
            onClick={onStopWatching}
            title="Parar de assistir"
            aria-label={`Parar de assistir a tela de ${tile.name}`}
            className="absolute right-2 top-2 flex items-center gap-1.5 rounded-md bg-black/60 px-2 py-1 text-[11px] text-white outline-none transition-colors hover:bg-black/80 focus-visible:ring-2 focus-visible:ring-ring"
          >
            <EyeOff className="size-3.5" />
            {!compact && "Parar de assistir"}
          </button>
        )
      }
    >
      {offer ? (
        <span className="flex flex-col items-center gap-2 px-3 text-center">
          <MonitorUp className={cn("text-muted-foreground", compact ? "size-5" : "size-8")} />
          <span
            className={cn(
              "rounded-full bg-primary font-medium text-primary-foreground transition-opacity group-hover/tile:opacity-90",
              compact ? "px-2.5 py-0.5 text-[11px]" : "px-4 py-1.5 text-sm",
            )}
          >
            {tile.status === "loading" ? "Carregando…" : compact ? "Assistir" : "Assistir transmissão"}
          </span>
        </span>
      ) : tile.stream ? (
        <TileVideo stream={tile.stream} dark />
      ) : (
        <UserAvatar userId={tile.userId} name={tile.name} size={compact ? "default" : "xl"} />
      )}
    </TileFrame>
  )
}

export function CallTile(props: TileProps<Tile>) {
  const { tile } = props
  return tile.kind === "person" ? (
    <PersonTile {...props} tile={tile} />
  ) : (
    <ScreenTile {...props} tile={tile} />
  )
}
