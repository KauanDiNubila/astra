import { useState } from "react"
import type { RefObject } from "react"
import { Plus, Search, Volume2, VolumeX } from "lucide-react"
import { ConversationListItem } from "@/components/chat/ConversationListItem"
import { GroupAvatar } from "@/components/GroupAvatar"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import type { ConversationRef } from "@/lib/conversation"
import type { ConversationSummary, GroupConversationSummary } from "@/lib/types"
import { cn } from "@/lib/utils"

function normalizeSearch(text: string) {
  return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
}

type ConversationSidebarProps = {
  listRef: RefObject<HTMLDivElement | null>
  conversations: ConversationSummary[]
  groupConversations: GroupConversationSummary[]
  active: ConversationRef | null
  chatSoundEnabled: boolean
  onToggleSound: () => void
  onCreateGroup: () => void
}

export function ConversationSidebar({
  listRef,
  conversations,
  groupConversations,
  active,
  chatSoundEnabled,
  onToggleSound,
  onCreateGroup,
}: ConversationSidebarProps) {
  const [search, setSearch] = useState("")
  const normalizedSearch = normalizeSearch(search.trim())
  const visibleConversations = normalizedSearch
    ? conversations.filter((c) => normalizeSearch(c.friendName).includes(normalizedSearch))
    : conversations
  const visibleGroups = normalizedSearch
    ? groupConversations.filter((c) => normalizeSearch(c.groupName).includes(normalizedSearch))
    : groupConversations

  return (
    <div
      ref={listRef}
      className={cn(
        "overlay-scroll flex w-full shrink-0 flex-col overflow-y-auto border-r sm:w-72",
        active && "hidden sm:flex",
      )}
    >
      <div className="px-3 pt-3">
        <label className="flex h-9 items-center gap-2 rounded-xl border bg-card px-3 text-sm text-muted-foreground focus-within:border-foreground/30">
          <Search className="size-3.5 shrink-0" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar conversa"
            aria-label="Buscar conversa"
            className="min-w-0 flex-1 bg-transparent text-foreground outline-none placeholder:text-muted-foreground"
          />
        </label>
      </div>
      <div className="flex items-center justify-between px-4 pt-3 pb-1">
        <span className="text-xs font-medium text-muted-foreground">Mensagens</span>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          title={chatSoundEnabled ? "Silenciar som de mensagem" : "Ativar som de mensagem"}
          className="size-6 max-sm:size-10"
          onClick={onToggleSound}
        >
          {chatSoundEnabled ? <Volume2 className="size-3.5" /> : <VolumeX className="size-3.5" />}
        </Button>
      </div>
      {normalizedSearch && visibleConversations.length === 0 && visibleGroups.length === 0 && (
        <p className="px-4 py-2 text-sm text-muted-foreground">Nenhuma conversa encontrada.</p>
      )}
      {conversations.length === 0 ? (
        <p className="px-4 pb-3 text-sm text-muted-foreground">Adicione amigos pra começar a conversar.</p>
      ) : (
        <ul className="flex flex-col gap-0.5 p-2">
          {visibleConversations.map((c) => (
            <ConversationListItem
              key={c.friendUserId}
              to={`/chat/${c.friendUserId}`}
              active={active?.kind === "friend" && active.id === c.friendUserId}
              pillId="chat-conversation-pill-friend"
              avatar={<UserAvatar userId={c.friendUserId} name={c.friendName} size="default" />}
              name={c.friendName}
              admin={c.friendAdmin}
              lastMessage={c.lastMessage}
              lastMessageAt={c.lastMessageAt}
              unreadCount={c.unreadCount}
            />
          ))}
        </ul>
      )}

      <div className="mt-2 flex items-center justify-between px-4 pt-2 pb-1">
        <span className="text-xs font-medium text-muted-foreground">Grupos</span>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          title="Criar grupo"
          className="size-6 max-sm:size-10"
          onClick={onCreateGroup}
        >
          <Plus className="size-3.5" />
        </Button>
      </div>
      {groupConversations.length === 0 && (
        <p className="px-4 pb-3 text-sm text-muted-foreground">Nenhum grupo ainda. Use o + para criar um.</p>
      )}
      {groupConversations.length > 0 && (
        <ul className="flex flex-col gap-0.5 p-2">
          {visibleGroups.map((c) => (
            <ConversationListItem
              key={c.groupId}
              to={`/chat/g/${c.groupId}`}
              active={active?.kind === "group" && active.id === c.groupId}
              pillId="chat-conversation-pill-group"
              avatar={<GroupAvatar groupId={c.groupId} className="rounded-lg after:rounded-lg [&>*]:rounded-lg" />}
              name={c.groupName}
              lastMessage={c.lastMessage}
              lastMessageAt={c.lastMessageAt}
              unreadCount={c.unreadCount}
            />
          ))}
        </ul>
      )}
    </div>
  )
}
