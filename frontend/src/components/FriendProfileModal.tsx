import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { Check, Copy, ExternalLink, X } from "lucide-react"
import { AdminBadge } from "@/components/AdminBadge"
import { GitHubIcon } from "@/components/icons/GitHubIcon"
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
  const [copied, setCopied] = useState(false)

  async function copyHandle() {
    if (!friend) return
    try {
      await navigator.clipboard.writeText(`${friend.friendName}#${friend.friendTag}`)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      // permissão de clipboard negada pelo navegador — sem feedback, mas não quebra a tela
    }
  }

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

          <div className="flex flex-col items-center gap-2 rounded-xl border border-border bg-card px-6 py-8">
            <UserAvatar userId={friend.friendUserId} name={friend.friendName} size="xl" />
            <h3 className="mt-1 flex items-center justify-center gap-1.5 text-center text-2xl font-bold text-foreground">
              {friend.friendName}
              {friend.friendAdmin && <AdminBadge />}
            </h3>
            {friend.friendBio && (
              <p className="mt-1 text-center text-sm text-muted-foreground">{friend.friendBio}</p>
            )}
            <button
              type="button"
              onClick={copyHandle}
              title="Copiar identificador"
              className="mt-3 group flex items-center gap-1.5 rounded-full bg-muted px-3 py-1 font-mono text-xs text-muted-foreground transition-colors hover:bg-muted/70 hover:text-foreground"
            >
              <span>
                {friend.friendName}#{friend.friendTag}
              </span>
              {copied ? (
                <Check size={12} className="shrink-0" />
              ) : (
                <Copy size={12} className="shrink-0 opacity-0 transition-opacity group-hover:opacity-100" />
              )}
            </button>

            {friend.friendGithubLogin && (
              <div className="mt-5 w-full border-t border-border pt-4">
                <span className="text-xs font-medium text-muted-foreground">Conexões</span>
                <a
                  href={`https://github.com/${friend.friendGithubLogin}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group mt-2 flex items-center gap-2.5 rounded-lg bg-muted/60 px-3 py-2 transition-colors hover:bg-muted"
                >
                  <GitHubIcon className="size-5 shrink-0 text-foreground" />
                  <span className="min-w-0 flex-1 truncate text-sm font-medium text-foreground">
                    {friend.friendGithubLogin}
                  </span>
                  <ExternalLink
                    size={14}
                    className="shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100"
                  />
                </a>
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
