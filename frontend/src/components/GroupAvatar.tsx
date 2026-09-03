import { useEffect, useState } from "react"
import { Users } from "lucide-react"
import { api } from "@/lib/api"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

// GET /users/*/avatar é público de propósito (funciona direto numa tag
// <img>), mas a foto de grupo passa por requireMember — uma <img> não
// consegue mandar o token de autenticação, então busca autenticada via
// axios (igual useAttachmentUrl faz pros anexos de mensagem) e converte
// pra blob local.
const groupAvatarUrlCache = new Map<string, string>()

export function invalidateGroupAvatarCache(groupId: string) {
  const cached = groupAvatarUrlCache.get(groupId)
  if (cached) URL.revokeObjectURL(cached)
  groupAvatarUrlCache.delete(groupId)
}

type Props = {
  groupId: string
  size?: "default" | "sm" | "lg" | "xl"
  className?: string
}

export function GroupAvatar({ groupId, size = "default", className }: Props) {
  const [url, setUrl] = useState<string | null>(() => groupAvatarUrlCache.get(groupId) ?? null)

  useEffect(() => {
    const cached = groupAvatarUrlCache.get(groupId)
    if (cached) {
      setUrl(cached)
      return
    }
    let cancelled = false
    api
      .get(`/chat/groups/${groupId}/avatar`, { responseType: "blob" })
      .then((res) => {
        if (cancelled) return
        const objectUrl = URL.createObjectURL(res.data as Blob)
        groupAvatarUrlCache.set(groupId, objectUrl)
        setUrl(objectUrl)
      })
      .catch(() => {
        // grupo sem foto (404) — mantém o fallback, sem erro visível
      })
    return () => {
      cancelled = true
    }
  }, [groupId])

  return (
    <Avatar size={size} className={className}>
      {url && <AvatarImage src={url} />}
      <AvatarFallback>
        <Users className="size-4" />
      </AvatarFallback>
    </Avatar>
  )
}
