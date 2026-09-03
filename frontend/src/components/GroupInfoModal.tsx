import { useEffect, useRef, useState } from "react"
import type { ChangeEvent } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { Pencil, UserPlus, X } from "lucide-react"
import { api, getErrorMessage } from "@/lib/api"
import { useFriends } from "@/context/FriendsContext"
import { useChat } from "@/context/ChatContext"
import type { GroupConversationSummary } from "@/lib/types"
import { GroupAvatar, invalidateGroupAvatarCache } from "@/components/GroupAvatar"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"

type Props = {
  group: GroupConversationSummary | null
  open: boolean
  onClose: () => void
}

export function GroupInfoModal({ group, open, onClose }: Props) {
  const reducedMotion = useReducedMotion()
  const { friends } = useFriends()
  const { groupMembersById, loadGroupMembers, addGroupMember } = useChat()
  const [rendered, setRendered] = useState(open)
  const [addingId, setAddingId] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const [avatarVersion, setAvatarVersion] = useState(0)

  useEffect(() => {
    if (open) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 300)
    return () => clearTimeout(timeout)
  }, [open])

  useEffect(() => {
    if (open && group) loadGroupMembers(group.groupId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, group?.groupId])

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

  if (!group || !rendered) return null

  const members = groupMembersById[group.groupId] ?? []
  const memberIds = new Set(members.map((m) => m.userId))
  const addableFriends = friends.filter((f) => f.status === "ACCEPTED" && !memberIds.has(f.friendUserId))

  async function onPickAvatar(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file || !group) return
    setUploadError(null)
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append("file", file)
      await api.post(`/chat/groups/${group.groupId}/avatar`, formData)
      invalidateGroupAvatarCache(group.groupId)
      setAvatarVersion((v) => v + 1)
    } catch (err) {
      setUploadError(getErrorMessage(err, "Não foi possível atualizar a foto do grupo."))
    } finally {
      setUploading(false)
      event.target.value = ""
    }
  }

  async function handleAdd(friendId: string) {
    if (!group) return
    setAddingId(friendId)
    try {
      await addGroupMember(group.groupId, friendId)
    } finally {
      setAddingId(null)
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
            <h2 className="text-lg font-semibold text-popover-foreground">Grupo</h2>
            <button
              type="button"
              title="Fechar"
              onClick={onClose}
              className="p-1 text-muted-foreground transition-colors hover:text-foreground"
            >
              <X size={20} />
            </button>
          </div>

          <div className="flex flex-col items-center gap-3 border-b border-border bg-card px-6 py-8">
            <div className="relative shrink-0">
              <GroupAvatar groupId={group.groupId} size="xl" key={avatarVersion} />
              <button
                type="button"
                title="Trocar foto do grupo"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                className="absolute -right-1 -bottom-1 rounded-full border border-border bg-card p-1.5 text-muted-foreground shadow-md transition-colors hover:text-foreground"
              >
                <Pencil size={14} />
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={onPickAvatar}
                className="hidden"
              />
            </div>
            <h3 className="text-center text-lg font-bold text-foreground">{group.groupName}</h3>
            {uploadError && <p className="text-center text-sm text-destructive">{uploadError}</p>}
          </div>

          <div className="flex flex-col gap-4 px-6 py-6">
            <div className="space-y-1.5">
              <p className="text-xs font-medium text-muted-foreground">
                {members.length} {members.length === 1 ? "membro" : "membros"}
              </p>
              <div className="flex flex-col gap-1">
                {members.map((m) => (
                  <div key={m.userId} className="flex items-center gap-2 rounded-md px-2 py-1.5">
                    <UserAvatar userId={m.userId} name={m.name} size="sm" />
                    <span className="text-sm">{m.name}</span>
                  </div>
                ))}
              </div>
            </div>

            {addableFriends.length > 0 && (
              <div className="space-y-1.5">
                <p className="text-xs font-medium text-muted-foreground">Adicionar amigo</p>
                <div className="flex max-h-40 flex-col gap-1 overflow-y-auto rounded-lg border border-border p-1">
                  {addableFriends.map((f) => (
                    <div key={f.friendUserId} className="flex items-center justify-between gap-2 px-2 py-1.5">
                      <span className="flex items-center gap-2">
                        <UserAvatar userId={f.friendUserId} name={f.friendName} size="sm" />
                        <span className="text-sm">{f.friendName}</span>
                      </span>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        title="Adicionar ao grupo"
                        onClick={() => handleAdd(f.friendUserId)}
                        disabled={addingId === f.friendUserId}
                      >
                        <UserPlus className="size-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </motion.div>,
    document.body,
  )
}
