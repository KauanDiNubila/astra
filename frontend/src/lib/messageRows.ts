import type { Message } from "@/lib/types"

export const GROUP_WINDOW_MS = 5 * 60 * 1000

export type MessageRowData = {
  message: Message
  newDay: boolean
  groupedWithPrev: boolean
  groupedWithNext: boolean
}

export function buildMessageRows(messages: Message[]): MessageRowData[] {
  return messages.map((message, i) => {
    const prev = messages[i - 1]
    const next = messages[i + 1]
    const time = new Date(message.createdAt).getTime()
    return {
      message,
      newDay: !prev || new Date(prev.createdAt).toDateString() !== new Date(message.createdAt).toDateString(),
      groupedWithPrev:
        !!prev && prev.senderId === message.senderId && time - new Date(prev.createdAt).getTime() < GROUP_WINDOW_MS,
      groupedWithNext:
        !!next && next.senderId === message.senderId && new Date(next.createdAt).getTime() - time < GROUP_WINDOW_MS,
    }
  })
}
