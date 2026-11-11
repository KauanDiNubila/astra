import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { toast } from "sonner"
import { api, getErrorMessage } from "@/lib/api"
import { createChatClient } from "@/lib/chatSocket"
import { loadChatSoundEnabled, saveChatSoundEnabled } from "@/lib/chatSoundPreference"
import { conversationKey, conversationOf, conversationPath, sameConversation } from "@/lib/conversation"
import type { ConversationRef } from "@/lib/conversation"
import { playMessagePing } from "@/lib/sound"
import { useAuth } from "@/context/AuthContext"
import { useFriends } from "@/context/FriendsContext"
import type {
  CallEvent,
  ConversationSummary,
  GroupConversationSummary,
  GroupMember,
  Message,
} from "@/lib/types"

const NO_MESSAGES: Message[] = []

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
  conversations: ConversationSummary[]
  conversationsLoaded: boolean
  loadConversations: () => Promise<void>
  groupConversations: GroupConversationSummary[]
  groupConversationsLoaded: boolean
  loadGroupConversations: () => Promise<void>
  totalUnread: number
  totalGroupUnread: number

  openConversation: (ref: ConversationRef) => void
  closeConversation: () => void
  messagesFor: (ref: ConversationRef) => Message[]
  pinsFor: (ref: ConversationRef) => Message[]
  sendMessage: (ref: ConversationRef, content: string, replyToMessageId?: string) => boolean
  sendImageMessage: (ref: ConversationRef, file: File, caption?: string, replyToMessageId?: string) => Promise<void>
  setMessagePinned: (message: Message, pinned: boolean) => Promise<void>

  membersOf: (groupId: string) => GroupMember[]
  loadGroupMembers: (groupId: string) => Promise<void>
  createGroup: (name: string, memberIds: string[]) => Promise<string>
  addGroupMember: (groupId: string, userId: string) => Promise<void>

  chatSoundEnabled: boolean
  setChatSoundEnabled: (enabled: boolean) => void

  publishStomp: (destination: string, body: unknown) => boolean
  subscribeCallEvents: (listener: (event: CallEvent) => void) => () => void
}

const ChatContext = createContext<ChatContextValue | undefined>(undefined)

export function ChatProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const { friends } = useFriends()
  const [connected, setConnected] = useState(false)
  const [conversations, setConversations] = useState<ConversationSummary[]>([])
  const [conversationsLoaded, setConversationsLoaded] = useState(false)
  const [groupConversations, setGroupConversations] = useState<GroupConversationSummary[]>([])
  const [groupConversationsLoaded, setGroupConversationsLoaded] = useState(false)
  const [messagesByConversation, setMessagesByConversation] = useState<Record<string, Message[]>>({})
  const [pinsByConversation, setPinsByConversation] = useState<Record<string, Message[]>>({})
  const [membersByConversation, setMembersByConversation] = useState<Record<string, GroupMember[]>>({})
  const [chatSoundEnabled, setChatSoundEnabledState] = useState(loadChatSoundEnabled)

  const clientRef = useRef<ReturnType<typeof createChatClient> | null>(null)
  const seenMessageIdsRef = useRef(new Set<string>())
  const callListenersRef = useRef(new Set<(event: CallEvent) => void>())
  const activeConversationRef = useRef<ConversationRef | null>(null)
  const chatSoundEnabledRef = useRef(chatSoundEnabled)
  const handleIncomingRef = useRef<(message: Message) => void>(() => {})
  const applyMessageUpdateRef = useRef<(message: Message) => void>(() => {})

  function setChatSoundEnabled(enabled: boolean) {
    chatSoundEnabledRef.current = enabled
    setChatSoundEnabledState(enabled)
    saveChatSoundEnabled(enabled)
  }

  function loadConversations() {
    return api.get<ConversationSummary[]>("/chat/conversations").then((res) => setConversations(res.data))
  }

  function loadGroupConversations() {
    return api.get<GroupConversationSummary[]>("/chat/groups").then((res) => setGroupConversations(res.data))
  }

  function loadHistory(ref: ConversationRef) {
    return api.get<Message[]>(`${conversationPath(ref)}/messages`).then((res) => {
      res.data.forEach((m) => seenMessageIdsRef.current.add(m.id))
      const key = conversationKey(ref)
      setMessagesByConversation((prev) => {
        const loaded = new Set(res.data.map((m) => m.id))
        const arrivedMeanwhile = (prev[key] ?? []).filter((m) => !loaded.has(m.id))
        if (arrivedMeanwhile.length === 0) return { ...prev, [key]: res.data }
        const merged = [...res.data, ...arrivedMeanwhile].sort((a, b) => a.createdAt.localeCompare(b.createdAt))
        return { ...prev, [key]: merged }
      })
    })
  }

  function loadPins(ref: ConversationRef) {
    return api.get<Message[]>(`${conversationPath(ref)}/pins`).then((res) => {
      setPinsByConversation((prev) => ({ ...prev, [conversationKey(ref)]: res.data }))
    })
  }

  function loadGroupMembers(groupId: string) {
    return api.get<GroupMember[]>(`/chat/groups/${groupId}/members`).then((res) => {
      setMembersByConversation((prev) => ({ ...prev, [conversationKey({ kind: "group", id: groupId })]: res.data }))
    })
  }

  async function markRead(ref: ConversationRef) {
    const unread =
      ref.kind === "group"
        ? groupConversations.find((c) => c.groupId === ref.id)?.unreadCount
        : conversations.find((c) => c.friendUserId === ref.id)?.unreadCount
    if (unread === 0) return
    await api.post(`${conversationPath(ref)}/read`)
    if (ref.kind === "group") {
      setGroupConversations((prev) => prev.map((c) => (c.groupId === ref.id ? { ...c, unreadCount: 0 } : c)))
    } else {
      setConversations((prev) => prev.map((c) => (c.friendUserId === ref.id ? { ...c, unreadCount: 0 } : c)))
    }
  }

  function openConversation(ref: ConversationRef) {
    activeConversationRef.current = ref
    const loads = [loadHistory(ref), loadPins(ref), markRead(ref)]
    if (ref.kind === "group") loads.push(loadGroupMembers(ref.id))
    Promise.all(loads).catch((error) => toast.error(getErrorMessage(error, "Não foi possível abrir a conversa.")))
  }

  function closeConversation() {
    activeConversationRef.current = null
  }

  function applyMessageUpdate(message: Message) {
    const key = conversationKey(conversationOf(message, user?.id))
    setMessagesByConversation((prev) =>
      prev[key] ? { ...prev, [key]: prev[key].map((m) => (m.id === message.id ? message : m)) } : prev,
    )
    setPinsByConversation((prev) => {
      const rest = (prev[key] ?? []).filter((m) => m.id !== message.id)
      const next = message.pinnedAt ? [...rest, message] : rest
      next.sort((a, b) => (b.pinnedAt ?? "").localeCompare(a.pinnedAt ?? ""))
      return { ...prev, [key]: next }
    })
  }

  async function setMessagePinned(message: Message, pinned: boolean) {
    try {
      const res = pinned
        ? await api.put<Message>(`/chat/messages/${message.id}/pin`)
        : await api.delete<Message>(`/chat/messages/${message.id}/pin`)
      applyMessageUpdate(res.data)
    } catch (error) {
      toast.error(getErrorMessage(error, pinned ? "Não foi possível fixar a mensagem." : "Não foi possível desafixar."))
    }
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

  function pingIfUnseen(isActiveConversation: boolean) {
    if (!chatSoundEnabledRef.current) return
    if (isActiveConversation && !document.hidden) return
    playMessagePing()
  }

  function handleIncoming(message: Message) {
    if (seenMessageIdsRef.current.has(message.id)) return
    seenMessageIdsRef.current.add(message.id)

    const me = user?.id
    const ref = conversationOf(message, me)
    const key = conversationKey(ref)
    setMessagesByConversation((prev) => ({ ...prev, [key]: [...(prev[key] ?? []), message] }))

    const isOpen = sameConversation(activeConversationRef.current, ref)
    const bumpUnread = message.senderId !== me && !isOpen
    const preview = message.content ?? "📷 Foto"
    const lastMessage = { lastMessage: preview, lastMessageAt: message.createdAt }

    let name: string
    if (ref.kind === "group") {
      name = groupConversations.find((c) => c.groupId === ref.id)?.groupName ?? "Grupo"
      setGroupConversations((prev) => {
        const found = prev.find((c) => c.groupId === ref.id)
        const updated: GroupConversationSummary = {
          groupId: ref.id,
          groupName: found?.groupName ?? name,
          memberNames: found?.memberNames ?? [],
          unreadCount: (found?.unreadCount ?? 0) + (bumpUnread ? 1 : 0),
          ...lastMessage,
        }
        return found ? prev.map((c) => (c.groupId === ref.id ? updated : c)) : [...prev, updated]
      })
    } else {
      const friend = friends.find((f) => f.friendUserId === ref.id)
      name = conversations.find((c) => c.friendUserId === ref.id)?.friendName ?? friend?.friendName ?? "Contato"
      setConversations((prev) => {
        const found = prev.find((c) => c.friendUserId === ref.id)
        const updated: ConversationSummary = {
          friendUserId: ref.id,
          friendName: found?.friendName ?? name,
          friendTag: found?.friendTag ?? "",
          friendBio: found?.friendBio ?? friend?.friendBio ?? null,
          friendAdmin: found?.friendAdmin ?? friend?.friendAdmin ?? false,
          friendGithubLogin: found?.friendGithubLogin ?? null,
          friendGithubAvatarUrl: found?.friendGithubAvatarUrl ?? null,
          unreadCount: (found?.unreadCount ?? 0) + (bumpUnread ? 1 : 0),
          ...lastMessage,
        }
        return found ? prev.map((c) => (c.friendUserId === ref.id ? updated : c)) : [...prev, updated]
      })
    }

    if (message.senderId === me) return

    pingIfUnseen(isOpen)

    if (isOpen) {
      void markRead(ref)
      return
    }

    toast(`${name}: ${preview}`)
  }

  useEffect(() => {
    handleIncomingRef.current = handleIncoming
    applyMessageUpdateRef.current = applyMessageUpdate
  })

  useEffect(() => {
    if (!user) return

    seenMessageIdsRef.current.clear()
    const client = createChatClient()
    client.onConnect = () => {
      setConnected(true)
      client.subscribe("/user/queue/messages", (frame) => {
        handleIncomingRef.current(JSON.parse(frame.body) as Message)
      })
      client.subscribe("/user/queue/message-updates", (frame) => {
        applyMessageUpdateRef.current(JSON.parse(frame.body) as Message)
      })
      client.subscribe("/user/queue/call", (frame) => {
        const event = JSON.parse(frame.body) as CallEvent
        callListenersRef.current.forEach((listener) => listener(event))
      })
    }
    client.onDisconnect = () => setConnected(false)
    client.onWebSocketClose = () => setConnected(false)
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

  function publishStomp(destination: string, body: unknown) {
    const client = clientRef.current
    if (!client || !client.connected) return false
    client.publish({ destination, body: JSON.stringify(body) })
    return true
  }

  function subscribeCallEvents(listener: (event: CallEvent) => void) {
    callListenersRef.current.add(listener)
    return () => {
      callListenersRef.current.delete(listener)
    }
  }

  function sendMessage(ref: ConversationRef, content: string, replyToMessageId?: string) {
    const reply = replyToMessageId ?? null
    return ref.kind === "group"
      ? publishStomp("/app/chat.sendGroup", { groupId: ref.id, content, replyToMessageId: reply })
      : publishStomp("/app/chat.send", { recipientId: ref.id, content, replyToMessageId: reply })
  }

  async function sendImageMessage(ref: ConversationRef, file: File, caption?: string, replyToMessageId?: string) {
    const form = new FormData()
    form.append("file", file)
    if (caption) form.append("caption", caption)
    if (replyToMessageId) form.append("replyToMessageId", replyToMessageId)
    await api.post(`${conversationPath(ref)}/messages/image`, form)
  }

  const totalUnread = conversations.reduce((sum, c) => sum + c.unreadCount, 0)
  const totalGroupUnread = groupConversations.reduce((sum, c) => sum + c.unreadCount, 0)

  return (
    <ChatContext.Provider
      value={{
        connected,
        conversations,
        conversationsLoaded,
        loadConversations,
        groupConversations,
        groupConversationsLoaded,
        loadGroupConversations,
        totalUnread,
        totalGroupUnread,
        openConversation,
        closeConversation,
        messagesFor: (ref) => messagesByConversation[conversationKey(ref)] ?? NO_MESSAGES,
        pinsFor: (ref) => pinsByConversation[conversationKey(ref)] ?? NO_MESSAGES,
        sendMessage,
        sendImageMessage,
        setMessagePinned,
        membersOf: (groupId) => membersByConversation[conversationKey({ kind: "group", id: groupId })] ?? [],
        loadGroupMembers,
        createGroup,
        addGroupMember,
        chatSoundEnabled,
        setChatSoundEnabled,
        publishStomp,
        subscribeCallEvents,
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
