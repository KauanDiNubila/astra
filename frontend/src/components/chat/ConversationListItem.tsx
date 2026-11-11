import type { ReactNode } from "react"
import { Link } from "react-router-dom"
import { motion } from "motion/react"
import { AdminBadge } from "@/components/AdminBadge"
import { RelativeTime } from "@/components/chat/RelativeTime"
import { cn } from "@/lib/utils"

type ConversationListItemProps = {
  to: string
  active: boolean
  pillId: string
  avatar: ReactNode
  name: string
  admin?: boolean
  lastMessage: string | null
  lastMessageAt: string | null
  unreadCount: number
}

export function ConversationListItem({
  to,
  active,
  pillId,
  avatar,
  name,
  admin,
  lastMessage,
  lastMessageAt,
  unreadCount,
}: ConversationListItemProps) {
  const unread = unreadCount > 0
  return (
    <li className="relative">
      {active && (
        <motion.div
          layoutId={pillId}
          className="absolute inset-0 rounded-xl bg-muted/70"
          transition={{ type: "spring", stiffness: 500, damping: 35 }}
        />
      )}
      <Link
        to={to}
        className="relative z-10 flex flex-col gap-1 rounded-xl px-3 py-2.5 transition-colors hover:bg-muted/40"
      >
        <span className="flex items-center gap-2.5">
          {avatar}
          <span className="min-w-0 flex-1">
            <span className="flex items-center justify-between gap-2">
              <span className="flex min-w-0 items-center gap-1">
                <span className={cn("min-w-0 truncate", unread ? "font-semibold" : "font-medium")}>{name}</span>
                {admin && <AdminBadge />}
              </span>
              {lastMessageAt && (
                <RelativeTime iso={lastMessageAt} className="shrink-0 text-[11px] text-muted-foreground/70" />
              )}
            </span>
            <span className="flex items-center justify-between gap-2">
              <span className={cn("min-w-0 truncate text-xs", unread ? "font-medium text-foreground" : "text-muted-foreground")}>
                {lastMessage ?? "Nenhuma mensagem ainda"}
              </span>
              {unread && (
                <span
                  aria-label={unreadCount === 1 ? "1 não lida" : `${unreadCount} não lidas`}
                  className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary text-[11px] font-medium text-primary-foreground"
                >
                  {unreadCount}
                </span>
              )}
            </span>
          </span>
        </span>
      </Link>
    </li>
  )
}
