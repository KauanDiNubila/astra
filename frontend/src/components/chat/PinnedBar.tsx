import { Pin, PinOff } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { Message } from "@/lib/types"
import { cn } from "@/lib/utils"

type PinnedBarProps = {
  pins: Message[]
  current: Message
  authorLabel: string
  onShow: () => void
  onUnpin: () => void
}

export function PinnedBar({ pins, current, authorLabel, onShow, onUnpin }: PinnedBarProps) {
  return (
    <div className="flex items-center gap-1 border-b bg-muted/30 pr-2">
      <button
        type="button"
        onClick={onShow}
        title={pins.length > 1 ? "Ir para a mensagem e mostrar a próxima fixada" : "Ir para a mensagem"}
        className="flex min-w-0 flex-1 items-center gap-3 px-4 py-2 text-left transition-colors hover:bg-muted/50"
      >
        {pins.length > 1 ? (
          <span className="flex w-0.5 shrink-0 flex-col gap-0.5 self-stretch py-0.5" aria-hidden>
            {pins.map((p) => (
              <span
                key={p.id}
                className={cn(
                  "min-h-1 flex-1 rounded-full transition-colors",
                  p.id === current.id ? "bg-primary" : "bg-muted-foreground/30",
                )}
              />
            ))}
          </span>
        ) : null}
        <Pin className="size-4 shrink-0 text-muted-foreground" />
        <span className="min-w-0 flex-1">
          <span className="block text-xs font-medium text-muted-foreground">
            Mensagem fixada
            {pins.length > 1 && ` · ${pins.findIndex((p) => p.id === current.id) + 1} de ${pins.length}`}
          </span>
          <span className="block truncate text-sm">
            <span className="font-medium">{authorLabel}:</span> {current.content ?? "📷 Foto"}
          </span>
        </span>
      </button>
      <Button
        type="button"
        variant="ghost"
        size="icon"
        title="Desafixar"
        aria-label="Desafixar mensagem"
        className="shrink-0 max-sm:size-10"
        onClick={onUnpin}
      >
        <PinOff className="size-4" />
      </Button>
    </div>
  )
}
