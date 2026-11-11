import { useEffect, useMemo, useRef, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { MessageCircle } from "lucide-react"
import { useAnimate, useReducedMotion } from "motion/react"
import { toast } from "sonner"
import { useAuth } from "@/context/AuthContext"
import { useCall } from "@/context/CallContext"
import { useChat } from "@/context/ChatContext"
import { useStickToBottom } from "@/hooks/useStickToBottom"
import { conversationKey } from "@/lib/conversation"
import type { ConversationRef } from "@/lib/conversation"
import { buildMessageRows } from "@/lib/messageRows"
import type { Message } from "@/lib/types"
import { cn } from "@/lib/utils"
import { Composer } from "@/components/chat/Composer"
import { ConversationHeader } from "@/components/chat/ConversationHeader"
import { ConversationSidebar } from "@/components/chat/ConversationSidebar"
import { MessageRow } from "@/components/chat/MessageRow"
import { PinnedBar } from "@/components/chat/PinnedBar"
import { ReplyBar } from "@/components/chat/ReplyBar"
import { CreateGroupModal } from "@/components/CreateGroupModal"
import { FriendProfileModal } from "@/components/FriendProfileModal"
import { GroupCallBanner } from "@/components/GroupCallBanner"
import { GroupInfoModal } from "@/components/GroupInfoModal"
import { ImageLightbox } from "@/components/ImageLightbox"
import { OverlayScrollbar } from "@/components/OverlayScrollbar"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"

const NO_MESSAGES: Message[] = []

export function ChatPage() {
  const { friendId, groupId } = useParams<{ friendId?: string; groupId?: string }>()
  const isGroup = !!groupId
  const { user } = useAuth()
  const {
    conversations,
    conversationsLoaded,
    openConversation,
    closeConversation,
    messagesFor,
    pinsFor,
    groupConversations,
    groupConversationsLoaded,
    membersOf,
    chatSoundEnabled,
    setChatSoundEnabled,
    connected,
    setMessagePinned,
  } = useChat()
  const conversation = useMemo<ConversationRef | null>(
    () => (groupId ? { kind: "group", id: groupId } : friendId ? { kind: "friend", id: friendId } : null),
    [friendId, groupId],
  )
  const { phase: callPhase, startDirectCall, startGroupCall } = useCall()
  const ready = conversationsLoaded && groupConversationsLoaded
  const [replyingTo, setReplyingTo] = useState<Message | null>(null)
  const [lightboxMessageId, setLightboxMessageId] = useState<string | null>(null)
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [createGroupModalOpen, setCreateGroupModalOpen] = useState(false)
  const [highlight, setHighlight] = useState<{ id: string; nonce: number } | null>(null)
  const [pinIndex, setPinIndex] = useState(0)
  const scrollContainerRef = useRef<HTMLDivElement>(null)
  const conversationListRef = useRef<HTMLDivElement>(null)
  const messagesContentRef = useRef<HTMLDivElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const [conversationScope, animateConversation] = useAnimate()
  const reducedMotion = useReducedMotion()
  const hadConversationRef = useRef(false)

  useEffect(() => {
    setReplyingTo(null)
    setPinIndex(0)
    if (conversation) {
      openConversation(conversation)
      if (!hadConversationRef.current) {
        animateConversation(
          conversationScope.current,
          reducedMotion ? { opacity: [0, 1] } : { opacity: [0, 1], y: [6, 0] },
          { duration: reducedMotion ? 0 : 0.22, ease: [0.22, 1, 0.36, 1] },
        )
      }
      hadConversationRef.current = true
    } else {
      hadConversationRef.current = false
    }
    return closeConversation
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [conversation])

  const messages = conversation ? messagesFor(conversation) : NO_MESSAGES
  const rows = useMemo(() => buildMessageRows(messages), [messages])
  const pins = conversation ? pinsFor(conversation) : NO_MESSAGES
  const currentPin = pins.length > 0 ? pins[Math.min(pinIndex, pins.length - 1)] : null
  const activeConversationLastMessageAt = isGroup
    ? groupConversations.find((c) => c.groupId === groupId)?.lastMessageAt
    : conversations.find((c) => c.friendUserId === friendId)?.lastMessageAt
  const conversationIsEmpty = messages.length === 0 && ready && !activeConversationLastMessageAt
  const activeGroupMemberCount = groupId ? membersOf(groupId).length : 0

  const { settle } = useStickToBottom(
    scrollContainerRef,
    messagesContentRef,
    `${conversation ? conversationKey(conversation) : ""}|${ready}`,
    `${messages.length}|${ready}|${activeGroupMemberCount}|${pins.length > 0}`,
  )

  function startReply(message: Message) {
    setReplyingTo(message)
    textareaRef.current?.focus()
  }

  function jumpToMessage(id: string) {
    const el = document.getElementById(`message-${id}`)
    if (!el) return false
    el.scrollIntoView({ behavior: "smooth", block: "center" })
    setHighlight({ id, nonce: Date.now() })
    return true
  }

  function showPinnedMessage() {
    if (!currentPin) return
    if (!jumpToMessage(currentPin.id)) toast("Essa mensagem é antiga e não está carregada na conversa.")
    if (pins.length > 1) setPinIndex((i) => (Math.min(i, pins.length - 1) + 1) % pins.length)
  }

  if (!ready) {
    return <PageSkeleton rows={5} />
  }

  const activeFriend = conversations.find((c) => c.friendUserId === friendId)
  const activeGroup = groupConversations.find((c) => c.groupId === groupId)
  const composerPlaceholder = isGroup
    ? activeGroup
      ? `Escreva para ${activeGroup.groupName}…`
      : "Escreva uma mensagem…"
    : activeFriend
      ? `Escreva para ${activeFriend.friendName.trim().split(/\s+/)[0]}…`
      : "Escreva uma mensagem…"
  const activeGroupMembers = groupId ? membersOf(groupId) : []

  function memberName(userId: string) {
    return activeGroupMembers.find((m) => m.userId === userId)?.name ?? ""
  }

  function nameOf(senderId: string) {
    return isGroup ? memberName(senderId) : (activeFriend?.friendName ?? "")
  }

  function authorLabel(senderId: string) {
    return senderId === user?.id ? "Você" : nameOf(senderId)
  }

  function avatarFor(message: Message) {
    if (isGroup) return <UserAvatar userId={message.senderId} name={memberName(message.senderId)} size="sm" />
    return activeFriend && <UserAvatar userId={activeFriend.friendUserId} name={activeFriend.friendName} size="sm" />
  }

  function startCall() {
    if (isGroup && activeGroup) void startGroupCall(activeGroup.groupId, activeGroup.groupName)
    else if (activeFriend) void startDirectCall(activeFriend.friendUserId, activeFriend.friendName)
  }

  return (
    <div
      className={cn(
        "sm:h-[calc(100dvh-9rem-var(--titlebar-h))] md:h-[calc(100dvh-5.5rem-var(--titlebar-h))]",
        conversation
          ? "max-sm:h-[calc(100dvh-3.6rem)]"
          : "max-sm:h-[calc(100dvh-7.2rem-env(safe-area-inset-bottom))]",
      )}
    >
      <h1 className="sr-only">Chat</h1>
      <OverlayScrollbar target={conversationListRef} />
      <OverlayScrollbar target={scrollContainerRef} />
      <div className="flex h-full flex-row overflow-hidden bg-background sm:rounded-2xl sm:border">
        <ConversationSidebar
          listRef={conversationListRef}
          conversations={conversations}
          groupConversations={groupConversations}
          active={conversation}
          chatSoundEnabled={chatSoundEnabled}
          onToggleSound={() => setChatSoundEnabled(!chatSoundEnabled)}
          onCreateGroup={() => setCreateGroupModalOpen(true)}
        />

        <div className={cn("flex flex-1 flex-col overflow-hidden", !conversation && "hidden sm:flex")}>
          {!conversation ? (
            <div className="flex flex-1 flex-col items-center justify-center gap-3 px-6 text-center">
              <span className="flex size-12 items-center justify-center rounded-full border text-muted-foreground">
                <MessageCircle className="size-5" />
              </span>
              <p className="font-medium">Suas conversas</p>
              <p className="max-w-xs text-sm text-muted-foreground">
                {conversations.length > 0
                  ? "Escolha uma conversa ao lado para continuar de onde parou."
                  : "Adicione um amigo para começar a conversar e combinar sessões de estudo."}
              </p>
              {conversations.length === 0 && (
                <Button asChild variant="outline" size="sm" className="rounded-full">
                  <Link to="/friends">Adicionar amigo</Link>
                </Button>
              )}
            </div>
          ) : (
            <div ref={conversationScope} className="flex flex-1 flex-col overflow-hidden">
              <ConversationHeader
                conversation={conversation}
                friend={activeFriend}
                group={activeGroup}
                memberCount={activeGroupMembers.length}
                callDisabled={callPhase !== "idle" || !connected || (isGroup ? !activeGroup : !activeFriend)}
                onOpenProfile={() => setProfileModalOpen(true)}
                onCall={startCall}
              />
              {isGroup && groupId && activeGroup && <GroupCallBanner groupId={groupId} groupName={activeGroup.groupName} />}
              {isGroup ? (
                <GroupInfoModal
                  group={activeGroup ?? null}
                  open={profileModalOpen}
                  onClose={() => setProfileModalOpen(false)}
                />
              ) : (
                <FriendProfileModal
                  friend={activeFriend ?? null}
                  open={profileModalOpen}
                  onClose={() => setProfileModalOpen(false)}
                />
              )}
              {currentPin && (
                <PinnedBar
                  pins={pins}
                  current={currentPin}
                  authorLabel={authorLabel(currentPin.senderId)}
                  onShow={showPinnedMessage}
                  onUnpin={() => void setMessagePinned(currentPin, false)}
                />
              )}
              <div ref={scrollContainerRef} className="overlay-scroll flex-1 overflow-y-auto px-4 py-3">
                <div ref={messagesContentRef} className="flex flex-col">
                  {conversationIsEmpty && (
                    <p className="py-10 text-center text-sm text-muted-foreground">Nenhuma mensagem ainda. Diga olá!</p>
                  )}
                  {rows.map((row) => {
                    const m = row.message
                    const highlightNonce = highlight?.id === m.id ? highlight.nonce : null
                    return (
                      <MessageRow
                        key={m.id}
                        row={row}
                        mine={m.senderId === user?.id}
                        avatar={avatarFor(m)}
                        senderLabel={isGroup ? memberName(m.senderId) : null}
                        replyAuthor={m.replyTo ? authorLabel(m.replyTo.senderId) : ""}
                        highlightNonce={highlightNonce}
                        onReply={() => startReply(m)}
                        onTogglePin={() => void setMessagePinned(m, !m.pinnedAt)}
                        onJumpTo={jumpToMessage}
                        onOpenImage={() => setLightboxMessageId(m.id)}
                        onImageLoaded={() => settle(400)}
                        onHighlightDone={() =>
                          setHighlight((h) => (h?.id === m.id && h.nonce === highlightNonce ? null : h))
                        }
                      />
                    )
                  })}
                </div>
              </div>

              <ReplyBar
                replyingTo={replyingTo}
                targetLabel={(message) => (message.senderId === user?.id ? "você mesmo" : nameOf(message.senderId))}
                onCancel={() => setReplyingTo(null)}
              />
              <Composer
                key={conversationKey(conversation)}
                conversation={conversation}
                placeholder={composerPlaceholder}
                replyToMessageId={replyingTo?.id}
                textareaRef={textareaRef}
                onSent={() => setReplyingTo(null)}
              />
            </div>
          )}
        </div>
      </div>

      <CreateGroupModal open={createGroupModalOpen} onClose={() => setCreateGroupModalOpen(false)} />
      <ImageLightbox messageId={lightboxMessageId} onClose={() => setLightboxMessageId(null)} />
    </div>
  )
}
