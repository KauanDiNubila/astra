import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { baseURL } from "@/lib/api"

function initials(name: string) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ""
  const last = parts.length > 1 ? parts[parts.length - 1][0] : ""
  return (first + last).toUpperCase()
}

type Props = {
  userId: string
  name: string
  size?: "default" | "sm" | "lg" | "xl"
  className?: string
}

export function UserAvatar({ userId, name, size = "default", className }: Props) {
  return (
    <Avatar size={size} className={className}>
      <AvatarImage src={`${baseURL}/users/${userId}/avatar`} />
      <AvatarFallback>{initials(name)}</AvatarFallback>
    </Avatar>
  )
}
