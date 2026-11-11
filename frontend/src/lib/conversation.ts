import type { Message } from "@/lib/types"

export type ConversationRef = { kind: "friend"; id: string } | { kind: "group"; id: string }

export function conversationKey(ref: ConversationRef) {
  return `${ref.kind === "group" ? "g" : "f"}:${ref.id}`
}

export function sameConversation(a: ConversationRef | null, b: ConversationRef | null) {
  return a !== null && b !== null && a.kind === b.kind && a.id === b.id
}

export function conversationOf(message: Message, myId: string | undefined): ConversationRef {
  if (message.groupId) return { kind: "group", id: message.groupId }
  return { kind: "friend", id: message.senderId === myId ? message.recipientId! : message.senderId }
}

export function conversationPath(ref: ConversationRef) {
  return ref.kind === "group" ? `/chat/groups/${ref.id}` : `/chat/${ref.id}`
}
