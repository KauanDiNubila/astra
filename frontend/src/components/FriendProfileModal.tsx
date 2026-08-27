import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { AdminBadge } from "@/components/AdminBadge"
import type { ConversationSummary } from "@/lib/types"
import { UserAvatar } from "@/components/UserAvatar"

type Props = {
  friend: ConversationSummary | null
  open: boolean
  onClose: () => void
}

export function FriendProfileModal({ friend, open, onClose }: Props) {
  const reducedMotion = useReducedMotion()
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

  if (!friend || !rendered) return null

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
          <div className="flex items-center justify-between px-6 py-4">
            <h2 className="text-lg font-semibold text-popover-foreground">Perfil</h2>
            <button
              type="button"
              title="Fechar"
              onClick={onClose}
              className="p-1 text-muted-foreground transition-colors hover:text-foreground"
            >
              <X size={20} />
            </button>
          </div>

          <div className="flex flex-col items-center gap-3 rounded-xl border border-border bg-card px-6 py-8">
            <UserAvatar userId={friend.friendUserId} name={friend.friendName} size="xl" />
            <h3 className="flex items-center justify-center gap-1.5 text-center text-lg font-bold text-foreground">
              {friend.friendName}
              {friend.friendAdmin && <AdminBadge />}
            </h3>
            {friend.friendBio && (
              <p className="text-center text-sm text-muted-foreground">{friend.friendBio}</p>
            )}
          </div>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
