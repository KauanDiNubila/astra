import { useEffect, useState } from "react"
import { AppWindow, Monitor } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useCall } from "@/context/CallContext"
import { useDelayedUnmount, useFrozen } from "@/hooks/useDelayedUnmount"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"

function SourceCard({
  source,
  selected,
  onSelect,
}: {
  source: AstraScreenSource
  selected: boolean
  onSelect: () => void
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      aria-pressed={selected}
      className={cn(
        "flex flex-col gap-1.5 rounded-lg border p-2 text-left outline-none transition-colors focus-visible:ring-2 focus-visible:ring-ring",
        selected ? "border-primary bg-primary/10" : "hover:bg-muted/60",
      )}
    >
      <img src={source.thumbnail} alt="" className="aspect-video w-full rounded-md bg-black object-contain" />
      <span className="flex items-center gap-1.5 text-xs">
        {source.kind === "screen" ? (
          <Monitor className="size-3.5 shrink-0" />
        ) : (
          <AppWindow className="size-3.5 shrink-0" />
        )}
        <span className="truncate">{source.name}</span>
      </span>
    </button>
  )
}

export function ScreenSourcePicker() {
  const { screenPickerSources, confirmScreenShare, cancelScreenShare } = useCall()
  const reducedMotion = useReducedMotion()
  const visible = screenPickerSources !== null
  const rendered = useDelayedUnmount(visible)
  const sources = useFrozen(screenPickerSources ?? [], !visible)
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [withAudio, setWithAudio] = useState(true)

  useEffect(() => {
    if (!visible) return
    setSelectedId(sources[0]?.id ?? null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible])

  useEffect(() => {
    if (!visible) return
    function onKey(event: KeyboardEvent) {
      if (event.key === "Escape") cancelScreenShare()
    }
    window.addEventListener("keydown", onKey)
    return () => window.removeEventListener("keydown", onKey)
  }, [visible, cancelScreenShare])

  if (!rendered) return null

  const screens = sources.filter((s) => s.kind === "screen")
  const windows = sources.filter((s) => s.kind === "window")
  const hidden = reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.97 }
  const shown = reducedMotion ? { opacity: 1 } : { opacity: 1, scale: 1 }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: visible ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className={cn(
        "fixed inset-0 z-[70] flex items-center justify-center bg-black/60 p-4",
        !visible && "pointer-events-none",
      )}
      onClick={cancelScreenShare}
    >
      <motion.div
        role="dialog"
        aria-modal="true"
        aria-label="Escolher o que compartilhar"
        initial={hidden}
        animate={visible ? shown : hidden}
        transition={{ duration: reducedMotion ? 0.15 : 0.3, ease: [0.22, 1, 0.36, 1] }}
        onClick={(event) => event.stopPropagation()}
        className="flex max-h-full w-full max-w-3xl flex-col gap-4 rounded-2xl border bg-card p-4 shadow-xl"
      >
        <div>
          <h2 className="text-lg font-semibold">O que você quer compartilhar?</h2>
          <p className="text-sm text-muted-foreground">Escolha uma tela inteira ou uma janela.</p>
        </div>

        <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto">
          {screens.length > 0 && (
            <section className="flex flex-col gap-2">
              <h3 className="text-xs font-medium text-muted-foreground">Telas</h3>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {screens.map((s) => (
                  <SourceCard key={s.id} source={s} selected={s.id === selectedId} onSelect={() => setSelectedId(s.id)} />
                ))}
              </div>
            </section>
          )}
          {windows.length > 0 && (
            <section className="flex flex-col gap-2">
              <h3 className="text-xs font-medium text-muted-foreground">Janelas</h3>
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {windows.map((s) => (
                  <SourceCard key={s.id} source={s} selected={s.id === selectedId} onSelect={() => setSelectedId(s.id)} />
                ))}
              </div>
            </section>
          )}
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3 border-t pt-3">
          <div className="flex items-center gap-2">
            <Switch id="share-system-audio" checked={withAudio} onCheckedChange={setWithAudio} />
            <Label htmlFor="share-system-audio" className="text-sm">
              Compartilhar o som do computador
            </Label>
          </div>
          <div className="flex gap-2">
            <Button type="button" variant="ghost" onClick={cancelScreenShare}>
              Cancelar
            </Button>
            <Button
              type="button"
              disabled={!selectedId}
              onClick={() => selectedId && void confirmScreenShare(selectedId, withAudio)}
            >
              Compartilhar
            </Button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  )
}
