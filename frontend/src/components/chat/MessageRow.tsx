import { Fragment } from "react"
import type { ReactNode } from "react"
import { Pin, PinOff, Reply } from "lucide-react"
import { motion } from "motion/react"
import { AttachmentImage } from "@/components/chat/AttachmentImage"
import { MessageText } from "@/components/chat/MessageText"
import { SwipeableMessage } from "@/components/chat/SwipeableMessage"
import { messageDayLabel, messageTime } from "@/lib/format"
import type { MessageRowData } from "@/lib/messageRows"
import { cn } from "@/lib/utils"

const ACTION_CLASS =
  "shrink-0 rounded-full p-1 text-muted-foreground opacity-0 transition-opacity hover:text-foreground focus-visible:opacity-100 group-hover:opacity-100 [@media(hover:none)]:opacity-60"

type MessageRowProps = {
  row: MessageRowData
  mine: boolean
  avatar: ReactNode
  senderLabel: string | null
  replyAuthor: string
  highlightNonce: number | null
  onReply: () => void
  onTogglePin: () => void
  onJumpTo: (messageId: string) => void
  onOpenImage: () => void
  onImageLoaded: () => void
  onHighlightDone: () => void
}

export function MessageRow({
  row,
  mine,
  avatar,
  senderLabel,
  replyAuthor,
  highlightNonce,
  onReply,
  onTogglePin,
  onJumpTo,
  onOpenImage,
  onImageLoaded,
  onHighlightDone,
}: MessageRowProps) {
  const { message: m, newDay, groupedWithPrev, groupedWithNext } = row
  const showAvatar = !mine && !groupedWithNext

  const replyButton = (
    <button type="button" title="Responder" onClick={onReply} className={ACTION_CLASS}>
      <Reply className="size-3.5" />
    </button>
  )
  const pinButton = (
    <button
      type="button"
      title={m.pinnedAt ? "Desafixar" : "Fixar"}
      aria-label={m.pinnedAt ? "Desafixar mensagem" : "Fixar mensagem"}
      onClick={onTogglePin}
      className={ACTION_CLASS}
    >
      {m.pinnedAt ? <PinOff className="size-3.5" /> : <Pin className="size-3.5" />}
    </button>
  )

  return (
    <Fragment>
      {newDay && (
        <div className="my-4 flex items-center gap-3 first:mt-0" role="separator">
          <span className="h-px flex-1 bg-border/60" />
          <span className="text-[11px] font-medium text-muted-foreground">{messageDayLabel(m.createdAt)}</span>
          <span className="h-px flex-1 bg-border/60" />
        </div>
      )}
      <motion.div
        id={`message-${m.id}`}
        initial={{ opacity: 0, scale: 0.94, y: 4 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}
        onDoubleClick={(e) => {
          if ((e.target as HTMLElement).closest("[data-message-bubble]")) return
          window.getSelection()?.removeAllRanges()
          onReply()
        }}
        className={cn(
          "group flex items-end gap-1",
          mine ? "justify-end" : "justify-start",
          groupedWithPrev && !newDay ? "mt-0.5" : newDay ? "mt-0" : "mt-3",
        )}
      >
        {mine && pinButton}
        {mine && replyButton}
        {!mine && <span className="mr-1 w-6 shrink-0">{showAvatar && avatar}</span>}
        <SwipeableMessage onReply={onReply}>
          <div
            data-message-bubble
            title={new Date(m.createdAt).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" })}
            className={cn(
              "relative max-w-[70%] overflow-hidden rounded-[18px] px-3.5 py-2 text-sm",
              mine ? "bg-primary text-primary-foreground" : "border bg-card",
              groupedWithPrev && !newDay && (mine ? "rounded-tr-md" : "rounded-tl-md"),
              groupedWithNext && (mine ? "rounded-br-md" : "rounded-bl-md"),
            )}
          >
            {highlightNonce !== null && (
              <motion.div
                key={highlightNonce}
                initial={{ opacity: 0.35 }}
                animate={{ opacity: 0 }}
                transition={{ duration: 1.1, ease: "easeOut" }}
                onAnimationComplete={onHighlightDone}
                className="pointer-events-none absolute inset-0 bg-foreground"
              />
            )}
            <div className="relative z-10">
              {senderLabel !== null && !mine && !groupedWithPrev && (
                <p className="mb-0.5 text-xs font-medium text-muted-foreground">{senderLabel}</p>
              )}
              {m.replyTo && (
                <button
                  type="button"
                  onClick={() => onJumpTo(m.replyTo!.id)}
                  className={`mb-1.5 block w-full rounded border-l-2 px-2 py-1 text-left text-xs transition-colors ${
                    mine
                      ? "border-primary-foreground/40 bg-primary-foreground/10 hover:bg-primary-foreground/20"
                      : "border-foreground/20 bg-foreground/5 hover:bg-foreground/10"
                  }`}
                >
                  <p className="font-medium">{replyAuthor}</p>
                  <p className="truncate opacity-80">{m.replyTo.contentPreview}</p>
                </button>
              )}
              {m.attachmentId && (
                <div className={m.content ? "mb-1.5" : ""}>
                  <AttachmentImage messageId={m.id} onOpen={onOpenImage} onLoaded={onImageLoaded} />
                </div>
              )}
              {m.content && <MessageText text={m.content} />}
              {(!groupedWithNext || m.pinnedAt) && (
                <p
                  className={`mt-1 flex items-center gap-1 text-[10px] tabular-nums ${
                    mine ? "text-primary-foreground/70" : "text-muted-foreground"
                  }`}
                >
                  {m.pinnedAt && <Pin className="size-2.5" aria-label="Fixada" role="img" />}
                  {!groupedWithNext && messageTime(m.createdAt)}
                </p>
              )}
            </div>
          </div>
        </SwipeableMessage>
        {!mine && replyButton}
        {!mine && pinButton}
      </motion.div>
    </Fragment>
  )
}
