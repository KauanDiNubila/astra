import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { ModalScroller } from "@/components/ModalScroller"
import { Check, Copy, ExternalLink, X } from "lucide-react"
import { AdminBadge } from "@/components/AdminBadge"
import { GitHubIcon } from "@/components/icons/GitHubIcon"
import { GithubAvatar } from "@/components/GithubAvatar"
import { fetchGithubProfile } from "@/lib/githubProfile"
import type { GithubProfileSummary } from "@/lib/githubProfile"
import type { ConversationSummary } from "@/lib/types"
import { UserAvatar } from "@/components/UserAvatar"
import { cn } from "@/lib/utils"

function GithubStat({ value, label, plain }: { value?: number; label: string; plain?: boolean }) {
  return (
    <div className="rounded-lg bg-muted/30 px-2 py-2">
      <p className="text-base font-semibold text-foreground">
        {value === undefined ? "–" : plain ? value : value.toLocaleString("pt-BR")}
      </p>
      <p className="text-[11px] text-muted-foreground">{label}</p>
    </div>
  )
}

type Props = {
  friend: ConversationSummary | null
  open: boolean
  onClose: () => void
}

export function FriendProfileModal({ friend, open, onClose }: Props) {
  const reducedMotion = useReducedMotion()
  const [rendered, setRendered] = useState(open)
  const [copied, setCopied] = useState(false)
  const [githubProfile, setGithubProfile] = useState<GithubProfileSummary | null>(null)
  const githubLogin = friend?.friendGithubLogin ?? null

  useEffect(() => {
    setGithubProfile(null)
    if (!open || !githubLogin) return
    let active = true
    void fetchGithubProfile(githubLogin).then((profile) => {
      if (active) setGithubProfile(profile)
    })
    return () => {
      active = false
    }
  }, [open, githubLogin])

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
    <ModalScroller open={open}>
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

          <div className="px-6 pb-6">
          <div className="flex flex-col items-center gap-2 rounded-xl bg-muted/30 px-6 py-8">
            <UserAvatar userId={friend.friendUserId} name={friend.friendName} size="xl" />
            {friend.friendGithubLogin ? (
              <a
                href={`https://github.com/${friend.friendGithubLogin}`}
                target="_blank"
                rel="noopener noreferrer"
                title={`Abrir @${friend.friendGithubLogin} no GitHub`}
                className="group mt-1 flex items-center justify-center gap-1.5 text-center text-2xl font-bold text-foreground transition-colors hover:text-muted-foreground"
              >
                {friend.friendName}
                {friend.friendAdmin && <AdminBadge />}
                <GitHubIcon className="size-4 shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100" />
              </a>
            ) : (
              <h3 className="mt-1 flex items-center justify-center gap-1.5 text-center text-2xl font-bold text-foreground">
                {friend.friendName}
                {friend.friendAdmin && <AdminBadge />}
              </h3>
            )}
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

          </div>

          {friend.friendGithubLogin && (
            <div className="mt-4">
              <a
                href={`https://github.com/${friend.friendGithubLogin}`}
                target="_blank"
                rel="noopener noreferrer"
                className="group -mx-3 flex items-center gap-3 rounded-xl px-3 py-2.5 transition-colors hover:bg-muted/50"
              >
                <span className="relative shrink-0">
                  <GithubAvatar
                    login={friend.friendGithubLogin}
                    src={friend.friendGithubAvatarUrl}
                    className="size-9"
                  />
                  <GitHubIcon className="absolute -bottom-1 -right-1 size-4 rounded-full border-2 border-popover bg-foreground p-0.5 text-popover" />
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-medium text-foreground">@{friend.friendGithubLogin}</span>
                  <span className="block text-xs text-muted-foreground">GitHub conectado</span>
                </span>
                <ExternalLink
                  size={14}
                  className="shrink-0 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100"
                />
              </a>
              <div
                className={cn(
                  "mt-2 grid grid-cols-3 gap-2 text-center transition-opacity duration-300",
                  githubProfile ? "opacity-100" : "opacity-0",
                )}
                aria-hidden={!githubProfile}
              >
                <GithubStat value={githubProfile?.publicRepos} label="Repositórios" />
                <GithubStat value={githubProfile?.followers} label="Seguidores" />
                <GithubStat value={githubProfile?.since} label="No GitHub desde" plain />
              </div>
            </div>
          )}
          </div>
        </motion.div>
      </div>
    </ModalScroller>,
    document.body,
  )
}
