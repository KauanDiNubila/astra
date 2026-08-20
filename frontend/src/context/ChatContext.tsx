import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { createChatClient } from "@/lib/chatSocket"
import { useAuth } from "@/context/AuthContext"
import type { ConversationSummary, Message } from "@/lib/types"

type ChatContextValue = {
  connected: boolean
  activeFriendId: string | null
  setActiveFriendId: (id: string | null) => void
  conversations: ConversationSummary[]
  loadConversations: () => Promise<void>
  messagesFor: (friendId: string) => Message[]
  loadHistory: (friendId: string) => Promise<void>
  sendMessage: (friendId: string, content: string) => void
  markRead: (friendId: string) => Promise<void>
  totalUnread: number
}

const ChatContext = createContext<ChatContextValue | undefined>(undefined)

export function ChatProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [connected, setConnected] = useState(false)
  const [activeFriendId, setActiveFriendId] = useState<string | null>(null)
  const [conversations, setConversations] = useState<ConversationSummary[]>([])
  const [messagesByFriend, setMessagesByFriend] = useState<Record<string, Message[]>>({})

  const clientRef = useRef<ReturnType<typeof createChatClient> | null>(null)
  const activeFriendIdRef = useRef<string | null>(null)
  const conversationsRef = useRef<ConversationSummary[]>([])

  useEffect(() => {
    activeFriendIdRef.current = activeFriendId
  }, [activeFriendId])

  useEffect(() => {
    conversationsRef.current = conversations
  }, [conversations])

  function loadConversations() {
    return api.get<ConversationSummary[]>("/chat/conversations").then((res) => setConversations(res.data))
  }

  function loadHistory(friendId: string) {
    return api.get<Message[]>(`/chat/${friendId}/messages`).then((res) => {
      setMessagesByFriend((prev) => ({ ...prev, [friendId]: res.data }))
    })
  }

  async function markRead(friendId: string) {
    await api.post(`/chat/${friendId}/read`)
    setConversations((prev) => prev.map((c) => (c.friendUserId === friendId ? { ...c, unreadCount: 0 } : c)))
  }

  function handleIncoming(message: Message) {
    const me = user?.id
    const otherId = message.senderId === me ? message.recipientId : message.senderId

    setMessagesByFriend((prev) => {
      const existing = prev[otherId] ?? []
      if (existing.some((m) => m.id === message.id)) return prev
      return { ...prev, [otherId]: [...existing, message] }
    })

    if (message.senderId === me) return

    if (activeFriendIdRef.current === otherId) {
      markRead(otherId)
      return
    }

    const found = conversationsRef.current.find((c) => c.friendUserId === otherId)
    const updated: ConversationSummary = {
      friendUserId: otherId,
      friendName: found?.friendName ?? "",
      lastMessage: message.content,
      lastMessageAt: message.createdAt,
      unreadCount: (found?.unreadCount ?? 0) + 1,
    }
    setConversations((prev) =>
      found ? prev.map((c) => (c.friendUserId === otherId ? updated : c)) : [...prev, updated],
    )
    toast(updated.friendName ? `${updated.friendName}: ${message.content}` : message.content)
  }

  useEffect(() => {
    if (!user) return

    const client = createChatClient()
    client.onConnect = () => {
      setConnected(true)
      client.subscribe("/user/queue/messages", (frame) => {
        handleIncoming(JSON.parse(frame.body) as Message)
      })
    }
    client.onDisconnect = () => setConnected(false)
    client.activate()
    clientRef.current = client
    loadConversations()

    return () => {
      client.deactivate()
      clientRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  function messagesFor(friendId: string) {
    return messagesByFriend[friendId] ?? []
  }

  function sendMessage(friendId: string, content: string) {
    const client = clientRef.current
    if (!client || !client.connected) return
    client.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({ recipientId: friendId, content }),
    })
  }

  const totalUnread = conversations.reduce((sum, c) => sum + c.unreadCount, 0)

  return (
    <ChatContext.Provider
      value={{
        connected,
        activeFriendId,
        setActiveFriendId,
        conversations,
        loadConversations,
        messagesFor,
        loadHistory,
        sendMessage,
        markRead,
        totalUnread,
      }}
    >
      {children}
    </ChatContext.Provider>
  )
}

export function useChat() {
  const ctx = useContext(ChatContext)
  if (!ctx) {
    throw new Error("useChat precisa estar dentro de um ChatProvider")
  }
  return ctx
}
