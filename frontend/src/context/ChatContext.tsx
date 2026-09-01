import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { createChatClient } from "@/lib/chatSocket"
import { useAuth } from "@/context/AuthContext"
import { useFriends } from "@/context/FriendsContext"
import type {
  ConversationSummary,
  Friendship,
  GroupConversationSummary,
  GroupMember,
  Message,
} from "@/lib/types"

const attachmentUrlCache = new Map<string, string>()

export function useAttachmentUrl(messageId: string | null) {
  const [url, setUrl] = useState<string | null>(messageId ? (attachmentUrlCache.get(messageId) ?? null) : null)

  useEffect(() => {
    if (!messageId) return
    const cached = attachmentUrlCache.get(messageId)
    if (cached) {
      setUrl(cached)
      return
    }
    let cancelled = false
    api.get(`/chat/messages/${messageId}/attachment`, { responseType: "blob" }).then((res) => {
      if (cancelled) return
      const objectUrl = URL.createObjectURL(res.data as Blob)
      attachmentUrlCache.set(messageId, objectUrl)
      setUrl(objectUrl)
    })
    return () => {
      cancelled = true
    }
  }, [messageId])

  return url
}

type ChatContextValue = {
  connected: boolean
  activeFriendId: string | null
  setActiveFriendId: (id: string | null) => void
  conversations: ConversationSummary[]
  conversationsLoaded: boolean
  loadConversations: () => Promise<void>
  messagesFor: (friendId: string) => Message[]
  loadHistory: (friendId: string) => Promise<void>
  sendMessage: (friendId: string, content: string, replyToMessageId?: string) => void
  sendImageMessage: (friendId: string, file: File, caption?: string, replyToMessageId?: string) => Promise<void>
  markRead: (friendId: string) => Promise<void>
  totalUnread: number

  activeGroupId: string | null
  setActiveGroupId: (id: string | null) => void
  groupConversations: GroupConversationSummary[]
  groupConversationsLoaded: boolean
  loadGroupConversations: () => Promise<void>
  groupMembersById: Record<string, GroupMember[]>
  loadGroupMembers: (groupId: string) => Promise<void>
  createGroup: (name: string, memberIds: string[]) => Promise<string>
  addGroupMember: (groupId: string, userId: string) => Promise<void>
  groupMessagesFor: (groupId: string) => Message[]
  loadGroupHistory: (groupId: string) => Promise<void>
  sendGroupMessage: (groupId: string, content: string, replyToMessageId?: string) => void
  sendGroupImageMessage: (
    groupId: string,
    file: File,
    caption?: string,
    replyToMessageId?: string,
  ) => Promise<void>
  markGroupRead: (groupId: string) => Promise<void>
  totalGroupUnread: number
}

const ChatContext = createContext<ChatContextValue | undefined>(undefined)

export function ChatProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const { friends } = useFriends()
  const [connected, setConnected] = useState(false)
  const [activeFriendId, setActiveFriendId] = useState<string | null>(null)
  const [conversations, setConversations] = useState<ConversationSummary[]>([])
  const [conversationsLoaded, setConversationsLoaded] = useState(false)
  const [messagesByFriend, setMessagesByFriend] = useState<Record<string, Message[]>>({})

  const [activeGroupId, setActiveGroupId] = useState<string | null>(null)
  const [groupConversations, setGroupConversations] = useState<GroupConversationSummary[]>([])
  const [groupConversationsLoaded, setGroupConversationsLoaded] = useState(false)
  const [messagesByGroup, setMessagesByGroup] = useState<Record<string, Message[]>>({})
  const [groupMembersById, setGroupMembersById] = useState<Record<string, GroupMember[]>>({})

  const clientRef = useRef<ReturnType<typeof createChatClient> | null>(null)
  const activeFriendIdRef = useRef<string | null>(null)
  const conversationsRef = useRef<ConversationSummary[]>([])
  const friendsRef = useRef<Friendship[]>([])
  const activeGroupIdRef = useRef<string | null>(null)
  const groupConversationsRef = useRef<GroupConversationSummary[]>([])

  useEffect(() => {
    activeFriendIdRef.current = activeFriendId
  }, [activeFriendId])

  useEffect(() => {
    conversationsRef.current = conversations
  }, [conversations])

  useEffect(() => {
    friendsRef.current = friends
  }, [friends])

  useEffect(() => {
    activeGroupIdRef.current = activeGroupId
  }, [activeGroupId])

  useEffect(() => {
    groupConversationsRef.current = groupConversations
  }, [groupConversations])

  function loadConversations() {
    return api.get<ConversationSummary[]>("/chat/conversations").then((res) => setConversations(res.data))
  }

  function loadHistory(friendId: string) {
    return api.get<Message[]>(`/chat/${friendId}/messages`).then((res) => {
      setMessagesByFriend((prev) => ({ ...prev, [friendId]: res.data }))
    })
  }

  async function markRead(friendId: string) {
    // Uma mensagem nova dispara markRead a cada chegada enquanto a conversa
    // está aberta — sem essa guarda, 5 mensagens em sequência viram 5 POSTs
    // pro mesmo "já lido".
    const current = conversationsRef.current.find((c) => c.friendUserId === friendId)
    if (current && current.unreadCount === 0) return
    await api.post(`/chat/${friendId}/read`)
    setConversations((prev) => prev.map((c) => (c.friendUserId === friendId ? { ...c, unreadCount: 0 } : c)))
  }

  function loadGroupConversations() {
    return api.get<GroupConversationSummary[]>("/chat/groups").then((res) => setGroupConversations(res.data))
  }

  function loadGroupHistory(groupId: string) {
    return api.get<Message[]>(`/chat/groups/${groupId}/messages`).then((res) => {
      setMessagesByGroup((prev) => ({ ...prev, [groupId]: res.data }))
    })
  }

  function loadGroupMembers(groupId: string) {
    return api.get<GroupMember[]>(`/chat/groups/${groupId}/members`).then((res) => {
      setGroupMembersById((prev) => ({ ...prev, [groupId]: res.data }))
    })
  }

  async function createGroup(name: string, memberIds: string[]) {
    const res = await api.post<GroupConversationSummary>("/chat/groups", { name, memberIds })
    await loadGroupConversations()
    return res.data.groupId
  }

  async function addGroupMember(groupId: string, userId: string) {
    await api.post(`/chat/groups/${groupId}/members`, { userId })
    await loadGroupMembers(groupId)
  }

  async function markGroupRead(groupId: string) {
    const current = groupConversationsRef.current.find((c) => c.groupId === groupId)
    if (current && current.unreadCount === 0) return
    await api.post(`/chat/groups/${groupId}/read`)
    setGroupConversations((prev) => prev.map((c) => (c.groupId === groupId ? { ...c, unreadCount: 0 } : c)))
  }

  function handleIncomingGroup(message: Message) {
    const groupId = message.groupId!
    const me = user?.id

    setMessagesByGroup((prev) => {
      const existing = prev[groupId] ?? []
      if (existing.some((m) => m.id === message.id)) return prev
      return { ...prev, [groupId]: [...existing, message] }
    })

    if (message.senderId === me) return

    if (activeGroupIdRef.current === groupId) {
      markGroupRead(groupId)
      return
    }

    const found = groupConversationsRef.current.find((c) => c.groupId === groupId)
    const groupName = found?.groupName ?? "Grupo"
    const preview = message.content ?? "📷 Foto"
    const updated: GroupConversationSummary = {
      groupId,
      groupName,
      memberNames: found?.memberNames ?? [],
      lastMessage: preview,
      lastMessageAt: message.createdAt,
      unreadCount: (found?.unreadCount ?? 0) + 1,
    }
    setGroupConversations((prev) => (found ? prev.map((c) => (c.groupId === groupId ? updated : c)) : [...prev, updated]))
    toast(`${groupName}: ${preview}`)
  }

  function handleIncoming(message: Message) {
    if (message.groupId) {
      handleIncomingGroup(message)
      return
    }

    const me = user?.id
    const otherId = message.senderId === me ? message.recipientId! : message.senderId

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
    const friend = friendsRef.current.find((f) => f.friendUserId === otherId)
    const friendName = found?.friendName ?? friend?.friendName ?? "Contato"
    const friendBio = found?.friendBio ?? friend?.friendBio ?? null
    const friendAdmin = found?.friendAdmin ?? friend?.friendAdmin ?? false
    const preview = message.content ?? "📷 Foto"
    const updated: ConversationSummary = {
      friendUserId: otherId,
      friendName,
      friendBio,
      friendAdmin,
      lastMessage: preview,
      lastMessageAt: message.createdAt,
      unreadCount: (found?.unreadCount ?? 0) + 1,
    }
    setConversations((prev) =>
      found ? prev.map((c) => (c.friendUserId === otherId ? updated : c)) : [...prev, updated],
    )
    toast(`${friendName}: ${preview}`)
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
    loadConversations().finally(() => setConversationsLoaded(true))
    loadGroupConversations().finally(() => setGroupConversationsLoaded(true))

    return () => {
      client.deactivate()
      clientRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  function messagesFor(friendId: string) {
    return messagesByFriend[friendId] ?? []
  }

  function sendMessage(friendId: string, content: string, replyToMessageId?: string) {
    const client = clientRef.current
    if (!client || !client.connected) return
    client.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({ recipientId: friendId, content, replyToMessageId: replyToMessageId ?? null }),
    })
  }

  async function sendImageMessage(friendId: string, file: File, caption?: string, replyToMessageId?: string) {
    const form = new FormData()
    form.append("file", file)
    if (caption) form.append("caption", caption)
    if (replyToMessageId) form.append("replyToMessageId", replyToMessageId)
    // Sem atualizar o estado local aqui de propósito — a entrega chega pelo
    // mesmo handleIncoming via STOMP, igual mensagem de texto, evitando duas
    // lógicas de shape de mensagem divergentes.
    await api.post(`/chat/${friendId}/messages/image`, form)
  }

  const totalUnread = conversations.reduce((sum, c) => sum + c.unreadCount, 0)

  function groupMessagesFor(groupId: string) {
    return messagesByGroup[groupId] ?? []
  }

  function sendGroupMessage(groupId: string, content: string, replyToMessageId?: string) {
    const client = clientRef.current
    if (!client || !client.connected) return
    client.publish({
      destination: "/app/chat.sendGroup",
      body: JSON.stringify({ groupId, content, replyToMessageId: replyToMessageId ?? null }),
    })
  }

  async function sendGroupImageMessage(groupId: string, file: File, caption?: string, replyToMessageId?: string) {
    const form = new FormData()
    form.append("file", file)
    if (caption) form.append("caption", caption)
    if (replyToMessageId) form.append("replyToMessageId", replyToMessageId)
    await api.post(`/chat/groups/${groupId}/messages/image`, form)
  }

  const totalGroupUnread = groupConversations.reduce((sum, c) => sum + c.unreadCount, 0)

  return (
    <ChatContext.Provider
      value={{
        connected,
        activeFriendId,
        setActiveFriendId,
        conversations,
        conversationsLoaded,
        loadConversations,
        messagesFor,
        loadHistory,
        sendMessage,
        sendImageMessage,
        markRead,
        totalUnread,

        activeGroupId,
        setActiveGroupId,
        groupConversations,
        groupConversationsLoaded,
        loadGroupConversations,
        groupMembersById,
        loadGroupMembers,
        createGroup,
        addGroupMember,
        groupMessagesFor,
        loadGroupHistory,
        sendGroupMessage,
        sendGroupImageMessage,
        markGroupRead,
        totalGroupUnread,
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
