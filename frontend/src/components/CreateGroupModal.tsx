import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { useNavigate } from "react-router-dom"
import { motion, useReducedMotion } from "motion/react"
import { Check, X } from "lucide-react"
import { useFriends } from "@/context/FriendsContext"
import { useChat } from "@/context/ChatContext"
import { cn } from "@/lib/utils"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

type Props = {
  open: boolean
  onClose: () => void
}

export function CreateGroupModal({ open, onClose }: Props) {
  const reducedMotion = useReducedMotion()
  const navigate = useNavigate()
  const { friends } = useFriends()
  const { createGroup } = useChat()
  const [rendered, setRendered] = useState(open)
  const [name, setName] = useState("")
  const [selected, setSelected] = useState<string[]>([])
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    if (open) {
      setRendered(true)
      setName("")
      setSelected([])
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

  if (!rendered) return null

  const accepted = friends.filter((f) => f.status === "ACCEPTED")

  function toggle(friendId: string) {
    setSelected((prev) => (prev.includes(friendId) ? prev.filter((id) => id !== friendId) : [...prev, friendId]))
  }

  async function submit() {
    if (!name.trim() || selected.length === 0 || creating) return
    setCreating(true)
    try {
      const groupId = await createGroup(name.trim(), selected)
      onClose()
      navigate(`/chat/g/${groupId}`)
    } finally {
      setCreating(false)
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
          <div className="flex items-center justify-between px-6 py-4">
            <h2 className="text-lg font-semibold text-popover-foreground">Criar grupo</h2>
            <button
              type="button"
              title="Fechar"
              onClick={onClose}
              className="p-1 text-muted-foreground transition-colors hover:text-foreground"
            >
              <X size={20} />
            </button>
          </div>

          <div className="flex flex-col gap-4 px-6 pb-6">
            <div className="space-y-1.5">
              <Label htmlFor="group-name">Nome do grupo</Label>
              <Input id="group-name" value={name} onChange={(e) => setName(e.target.value)} maxLength={60} autoFocus />
            </div>

            <div className="space-y-1.5">
              <Label>Adicionar amigos</Label>
              {accepted.length === 0 ? (
                <p className="text-sm text-muted-foreground">Você ainda não tem amigos pra adicionar.</p>
              ) : (
                <div className="flex max-h-56 flex-col gap-1 overflow-y-auto rounded-lg border border-border p-1">
                  {accepted.map((f) => {
                    const isSelected = selected.includes(f.friendUserId)
                    return (
                      <button
                        key={f.friendUserId}
                        type="button"
                        onClick={() => toggle(f.friendUserId)}
                        className={cn(
                          "flex items-center gap-2.5 rounded-md px-2 py-1.5 text-left transition-colors",
                          isSelected ? "bg-primary/10" : "hover:bg-muted/50",
                        )}
                      >
                        <UserAvatar userId={f.friendUserId} name={f.friendName} size="sm" />
                        <span className="min-w-0 flex-1 truncate text-sm">{f.friendName}</span>
                        <span
                          className={cn(
                            "flex size-5 shrink-0 items-center justify-center rounded-full border",
                            isSelected
                              ? "border-primary bg-primary text-primary-foreground"
                              : "border-border",
                          )}
                        >
                          {isSelected && <Check className="size-3" />}
                        </span>
                      </button>
                    )
                  })}
                </div>
              )}
            </div>

            <Button type="button" onClick={submit} disabled={!name.trim() || selected.length === 0 || creating}>
              {creating ? "Criando..." : "Criar grupo"}
            </Button>
          </div>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
