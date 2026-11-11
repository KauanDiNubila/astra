import { Link } from "react-router-dom"
import { ChevronLeft, Phone } from "lucide-react"
import { AdminBadge } from "@/components/AdminBadge"
import { GroupAvatar } from "@/components/GroupAvatar"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import type { ConversationRef } from "@/lib/conversation"
import type { ConversationSummary, GroupConversationSummary } from "@/lib/types"

type ConversationHeaderProps = {
  conversation: ConversationRef
  friend: ConversationSummary | undefined
  group: GroupConversationSummary | undefined
  memberCount: number
  callDisabled: boolean
  onOpenProfile: () => void
  onCall: () => void
}

const profileButtonClass =
  "flex flex-1 items-center gap-2 px-4 py-3 text-left transition-colors hover:bg-muted/50 disabled:cursor-default disabled:hover:bg-transparent sm:pl-4"

export function ConversationHeader({
  conversation,
  friend,
  group,
  memberCount,
  callDisabled,
  onOpenProfile,
  onCall,
}: ConversationHeaderProps) {
  return (
    <div className="flex items-center border-b">
      <Link to="/chat" aria-label="Voltar" className="p-3 text-muted-foreground hover:text-foreground sm:hidden">
        <ChevronLeft className="size-5" />
      </Link>
      {conversation.kind === "group" ? (
        <button type="button" onClick={onOpenProfile} disabled={!group} className={profileButtonClass}>
          <GroupAvatar groupId={conversation.id} />
          <span className="flex flex-col">
            <span className="font-medium">{group?.groupName}</span>
            <span className="text-xs text-muted-foreground">
              {memberCount > 0 ? `${memberCount} membros` : group?.memberNames.join(", ")}
            </span>
          </span>
        </button>
      ) : (
        <button type="button" onClick={onOpenProfile} disabled={!friend} className={profileButtonClass}>
          {friend && <UserAvatar userId={friend.friendUserId} name={friend.friendName} size="sm" />}
          <span className="flex flex-col">
            <span className="flex items-center gap-1 font-medium">
              {friend?.friendName}
              {friend?.friendAdmin && <AdminBadge />}
            </span>
            {friend?.friendBio && <span className="text-xs text-muted-foreground">{friend.friendBio}</span>}
          </span>
        </button>
      )}
      <Button
        type="button"
        variant="ghost"
        size="icon"
        title="Ligar"
        aria-label="Ligar"
        disabled={callDisabled}
        className="mr-2 max-sm:size-10"
        onClick={onCall}
      >
        <Phone className="size-4" />
      </Button>
    </div>
  )
}
