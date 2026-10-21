import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { api } from "@/lib/api"
import type { CourseSummary } from "@/lib/types"
import { useDelayedUnmount } from "@/hooks/useDelayedUnmount"
import { ModalScroller } from "@/components/ModalScroller"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Stepper } from "@/components/ui/stepper"

const MAX_INITIAL_MODULES = 30

export function NewCourseModal({
  open,
  onClose,
  onCreated,
}: {
  open: boolean
  onClose: () => void
  onCreated: () => void | Promise<void>
}) {
  const reducedMotion = useReducedMotion()
  const rendered = useDelayedUnmount(open, 300)
  const [title, setTitle] = useState("")
  const [platform, setPlatform] = useState("")
  const [moduleCount, setModuleCount] = useState(0)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setTitle("")
    setPlatform("")
    setModuleCount(0)
    setError(null)
  }, [open])

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

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) {
      setError("Dê um título ao curso.")
      return
    }
    setError(null)
    setSaving(true)
    try {
      await api.post<CourseSummary>("/courses", {
        title: title.trim(),
        platform: platform.trim() || null,
        moduleCount: moduleCount || null,
      })
      await onCreated()
      onClose()
    } catch {
      setError("Não foi possível criar o curso.")
    } finally {
      setSaving(false)
    }
  }

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
              <h2 className="text-lg font-semibold text-popover-foreground">Novo curso</h2>
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
                <Label htmlFor="course-title">Título</Label>
                <Input
                  id="course-title"
                  autoFocus
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Spring Boot na Prática"
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="course-platform">Plataforma</Label>
                <Input
                  id="course-platform"
                  value={platform}
                  onChange={(e) => setPlatform(e.target.value)}
                  placeholder="Udemy, Alura, YouTube…"
                />
              </div>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <Label>Módulos</Label>
                  <p className="mt-1 text-xs text-muted-foreground">Você pode adicionar mais depois.</p>
                </div>
                <Stepper
                  value={moduleCount}
                  onChange={setModuleCount}
                  min={0}
                  max={MAX_INITIAL_MODULES}
                  aria-label="Quantidade de módulos"
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2 px-6 py-4">
              <Button type="button" variant="outline" onClick={onClose}>
                Cancelar
              </Button>
              <Button type="submit" disabled={saving}>
                {saving ? "Criando..." : "Criar curso"}
              </Button>
            </div>
          </form>
        </motion.div>
      </div>
    </ModalScroller>,
    document.body,
  )
}
