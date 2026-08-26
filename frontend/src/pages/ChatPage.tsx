import { useEffect, useRef, useState } from "react"
import type { FormEvent, KeyboardEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { Send } from "lucide-react"
import { motion } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useChat } from "@/context/ChatContext"
import { formatRelativeTime } from "@/lib/format"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"

export function ChatPage() {
  const { friendId } = useParams<{ friendId: string }>()
  const { user } = useAuth()
  const { conversations, conversationsLoaded, messagesFor, loadHistory, sendMessage, markRead, setActiveFriendId } =
    useChat()
  const [draft, setDraft] = useState("")
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    setActiveFriendId(friendId ?? null)
    setDraft("")
    if (friendId) {
      loadHistory(friendId)
      markRead(friendId)
    }
    return () => setActiveFriendId(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId])

  const messages = friendId ? messagesFor(friendId) : []

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: "end" })
  }, [messages.length])

  function submitDraft() {
    if (!friendId || !draft.trim()) return
    sendMessage(friendId, draft.trim())
    setDraft("")
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    submitDraft()
  }

  function onKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      submitDraft()
    }
  }

  if (!conversationsLoaded) {
    return <PageSkeleton rows={5} />
  }

  const activeFriend = conversations.find((c) => c.friendUserId === friendId)

  return (
    <div className="flex h-[calc(100vh-9rem)] gap-4">
      <Card className="w-64 shrink-0 overflow-y-auto p-0">
        {conversations.length === 0 ? (
          <p className="p-4 text-sm text-muted-foreground">Adicione amigos pra começar a conversar.</p>
        ) : (
          <ul className="divide-y">
            {conversations.map((c) => (
              <li key={c.friendUserId} className="relative">
                {c.friendUserId === friendId && (
                  <motion.div
                    layoutId="chat-conversation-pill"
                    className="absolute inset-0 bg-muted"
                    transition={{ type: "spring", stiffness: 500, damping: 35 }}
                  />
                )}
                <Link
                  to={`/chat/${c.friendUserId}`}
                  className="relative z-10 flex flex-col gap-0.5 px-4 py-3 hover:bg-muted/50"
                >
                  <span className="flex items-center gap-2">
                    <UserAvatar userId={c.friendUserId} name={c.friendName} size="sm" />
                    <span className="min-w-0 flex-1">
                      <span className="flex items-center justify-between gap-2">
                        <span className="truncate font-medium">{c.friendName}</span>
                        {c.unreadCount > 0 && (
                          <span className="flex size-5 shrink-0 items-center justify-center rounded-full bg-primary text-[11px] font-medium text-primary-foreground">
                            {c.unreadCount}
                          </span>
                        )}
                      </span>
                      <span className="block truncate text-xs text-muted-foreground">
                        {c.lastMessage ?? "Nenhuma mensagem ainda"}
                      </span>
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <Card className="flex flex-1 flex-col p-0">
        {!friendId ? (
          <div className="flex flex-1 items-center justify-center text-sm text-muted-foreground">
            Selecione uma conversa.
          </div>
        ) : (
          <motion.div
            key={friendId}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
            className="flex flex-1 flex-col overflow-hidden"
          >
            <div className="flex items-center gap-2 border-b px-4 py-3">
              {activeFriend && <UserAvatar userId={activeFriend.friendUserId} name={activeFriend.friendName} size="sm" />}
              <span className="flex flex-col">
                <span className="font-medium">{activeFriend?.friendName}</span>
                {activeFriend?.friendBio && (
                  <span className="text-xs text-muted-foreground">{activeFriend.friendBio}</span>
                )}
              </span>
            </div>
            <div className="flex-1 overflow-y-auto px-4 py-3">
              <div className="flex flex-col gap-2">
                {messages.map((m) => {
                  const mine = m.senderId === user?.id
                  return (
                    <div key={m.id} className={`flex ${mine ? "justify-end" : "justify-start"}`}>
                      <div
                        className={`max-w-[70%] rounded-lg px-3 py-2 text-sm ${
                          mine ? "bg-primary text-primary-foreground" : "bg-muted"
                        }`}
                      >
                        <p className="whitespace-pre-wrap break-words">{m.content}</p>
                        <p
                          className={`mt-1 text-[10px] ${
                            mine ? "text-primary-foreground/70" : "text-muted-foreground"
                          }`}
                        >
                          {formatRelativeTime(m.createdAt)}
                        </p>
                      </div>
                    </div>
                  )
                })}
                <div ref={bottomRef} />
              </div>
            </div>
            <form onSubmit={onSubmit} className="flex items-center gap-2 border-t p-3">
              <div className="flex-1">
                <Textarea
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  onKeyDown={onKeyDown}
                  placeholder="Escreva uma mensagem..."
                  className="min-h-12 w-full resize-none"
                  style={{ fieldSizing: "fixed" }}
                />
              </div>
              <Button type="submit" size="icon" disabled={!draft.trim()}>
                <Send className="size-4" />
              </Button>
            </form>
          </motion.div>
        )}
      </Card>
    </div>
  )
}
