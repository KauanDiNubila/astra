import { useEffect } from "react"
import type { FormEvent, ReactNode } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { useDelayedUnmount } from "@/hooks/useDelayedUnmount"
import { ModalScroller } from "@/components/ModalScroller"
import { Button } from "@/components/ui/button"

export function FormModal({
  open,
  title,
  submitLabel,
  savingLabel,
  saving,
  error,
  onClose,
  onSubmit,
  children,
}: {
  open: boolean
  title: string
  submitLabel: string
  savingLabel: string
  saving: boolean
  error: string | null
  onClose: () => void
  onSubmit: (event: FormEvent) => void
  children: ReactNode
}) {
  const reducedMotion = useReducedMotion()
  const rendered = useDelayedUnmount(open, 300)

  useEffect(() => {
    if (!rendered) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [rendered])

  useEffect(() => {
    if (!open) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [open, onClose])

  if (!rendered) return null

  return createPortal(
    <ModalScroller open={open}>
      <div onClick={onClose} className="fixed inset-0 bg-black/20 backdrop-blur-[1px] dark:bg-black/60" />
      <div className="pointer-events-none relative z-101 my-auto w-full max-w-md">
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
              <h2 className="text-lg font-semibold text-popover-foreground">{title}</h2>
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
              {children}
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2 px-6 py-4">
              <Button type="button" variant="outline" onClick={onClose}>
                Cancelar
              </Button>
              <Button type="submit" disabled={saving}>
                {saving ? savingLabel : submitLabel}
              </Button>
            </div>
          </form>
        </motion.div>
      </div>
    </ModalScroller>,
    document.body,
  )
}
