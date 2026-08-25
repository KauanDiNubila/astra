import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Stepper } from "@/components/ui/stepper"

type Props = {
  open: boolean
  onClose: () => void
  dailyTarget: number
  weeklyTarget: number
  onSaved: () => void | Promise<void>
}

export function EditGoalsModal({ open, onClose, dailyTarget, weeklyTarget, onSaved }: Props) {
  const reducedMotion = useReducedMotion()
  const [daily, setDaily] = useState(dailyTarget)
  const [weekly, setWeekly] = useState(weeklyTarget)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [rendered, setRendered] = useState(open)

  useEffect(() => {
    if (open) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 300)
    return () => clearTimeout(timeout)
  }, [open])

  useEffect(() => {
    if (!open) return
    setDaily(dailyTarget)
    setWeekly(weeklyTarget)
    setError(null)
  }, [open, dailyTarget, weeklyTarget])

  useEffect(() => {
    if (!rendered) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [rendered])

  useEffect(() => {
    if (!rendered) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [rendered, onClose])

  if (!rendered) return null

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSaving(true)
    try {
      if (daily > 0) {
        await api.put("/goals", { type: "DAILY", targetHours: daily })
      }
      if (weekly > 0) {
        await api.put("/goals", { type: "WEEKLY", targetHours: weekly })
      }
      await onSaved()
      toast.success("Metas salvas.")
      onClose()
    } catch {
      setError("Não foi possível salvar as metas.")
    } finally {
      setSaving(false)
    }
  }

  return createPortal(
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: open ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className="fixed inset-0 z-100 flex items-center justify-center overflow-y-auto p-4"
    >
      <div onClick={onClose} className="fixed inset-0 bg-black/20 backdrop-blur-[1px] dark:bg-black/60" />

      <div className="pointer-events-none relative z-101 my-auto w-full max-w-sm">
        <motion.div
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94, y: 16 }}
          animate={
            reducedMotion
              ? { opacity: open ? 1 : 0 }
              : { opacity: open ? 1 : 0, scale: open ? 1 : 0.96, y: open ? 0 : 16 }
          }
          transition={{ type: "spring", damping: 22, stiffness: 320, mass: 0.8 }}
          className="pointer-events-auto w-full overflow-hidden rounded-2xl border border-border bg-popover shadow-lg"
        >
          <form onSubmit={onSubmit}>
            <div className="flex items-center justify-between px-6 py-4">
              <h2 className="text-lg font-semibold text-popover-foreground">Editar metas</h2>
              <button
                type="button"
                title="Fechar"
                onClick={onClose}
                className="p-1 text-muted-foreground transition-colors hover:text-foreground"
              >
                <X size={20} />
              </button>
            </div>

            <div className="flex flex-col gap-4 px-6 pb-2">
              <div className="flex flex-col gap-2">
                <Label htmlFor="goal-daily">Meta diária (horas)</Label>
                <Stepper
                  id="goal-daily"
                  min={0}
                  max={24}
                  value={daily}
                  onChange={setDaily}
                  aria-label="Meta diária em horas"
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="goal-weekly">Meta semanal (horas)</Label>
                <Stepper
                  id="goal-weekly"
                  min={0}
                  max={168}
                  value={weekly}
                  onChange={setWeekly}
                  aria-label="Meta semanal em horas"
                />
              </div>
            </div>

            <div className="flex flex-col-reverse items-center justify-end gap-3 px-6 py-5 sm:flex-row">
              {error && <p className="mr-auto text-sm text-destructive">{error}</p>}
              <Button type="button" variant="outline" onClick={onClose} className="w-full sm:w-auto">
                Cancelar
              </Button>
              <Button type="submit" disabled={saving} className="w-full sm:w-auto">
                {saving ? "Salvando..." : "Salvar"}
              </Button>
            </div>
          </form>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
