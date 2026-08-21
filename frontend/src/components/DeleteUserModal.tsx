import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import type { AdminUser } from "@/lib/types"
import { Button } from "@/components/ui/button"

type Props = {
  user: AdminUser | null
  onClose: () => void
  onDeleted: () => void
}

export function DeleteUserModal({ user, onClose, onDeleted }: Props) {
  const reducedMotion = useReducedMotion()
  const open = user !== null

  const [rendered, setRendered] = useState(open)
  const [targetUser, setTargetUser] = useState<AdminUser | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (user) setTargetUser(user)
  }, [user])

  useEffect(() => {
    if (open) {
      setRendered(true)
      setError(null)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 300)
    return () => clearTimeout(timeout)
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
    if (!rendered) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [rendered, onClose])

  if (!rendered || !targetUser) return null

  async function onConfirm() {
    if (!targetUser) return
    setError(null)
    setDeleting(true)
    try {
      await api.delete(`/admin/users/${targetUser.id}`)
      onDeleted()
      onClose()
    } catch {
      const message = "Não foi possível excluir esse usuário."
      setError(message)
      toast.error(message)
    } finally {
      setDeleting(false)
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
          <div className="px-6 py-4">
            <h2 className="text-lg font-semibold text-popover-foreground">
              Excluir conta de {targetUser.name}?
            </h2>
          </div>

          <div className="flex flex-col gap-3 rounded-xl border border-border bg-card p-6">
            <p className="text-sm text-muted-foreground">
              Essa ação é <span className="font-medium text-destructive">permanente</span> e apaga tudo o
              que pertence a essa conta: sessões, categorias, cursos, metas e roadmaps próprios. Amizades e
              mensagens trocadas com outras pessoas também somem{" "}
              <span className="font-medium">dos dois lados</span> — não só do usuário excluído.
            </p>
            <p className="text-sm text-muted-foreground">Não tem como desfazer.</p>
          </div>

          <div className="flex flex-col-reverse items-center justify-end gap-3 px-6 py-5 sm:flex-row">
            {error && <p className="mr-auto text-sm text-destructive">{error}</p>}
            <Button type="button" variant="outline" onClick={onClose} className="w-full sm:w-auto">
              Cancelar
            </Button>
            <Button
              type="button"
              variant="destructive"
              disabled={deleting}
              onClick={onConfirm}
              className="w-full sm:w-auto"
            >
              {deleting ? "Excluindo..." : "Excluir conta"}
            </Button>
          </div>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
