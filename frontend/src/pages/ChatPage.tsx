import { Fragment, useEffect, useRef, useState } from "react"
import type { ChangeEvent, ClipboardEvent, FormEvent, KeyboardEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { ImagePlus, Reply, Send, X } from "lucide-react"
import { motion, useAnimate } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useChat, useAttachmentUrl } from "@/context/ChatContext"
import { formatRelativeTime } from "@/lib/format"
import type { Message } from "@/lib/types"
import { FriendProfileModal } from "@/components/FriendProfileModal"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"

const URL_PATTERN = /(https?:\/\/[^\s]+|www\.[^\s]+)/g
const URL_PREFIX = /^(https?:\/\/|www\.)/i
const TRAILING_PUNCTUATION = /[.,!?;:'")\]}]+$/

function linkifyText(text: string) {
  return text.split(URL_PATTERN).map((part, i) => {
    if (!URL_PREFIX.test(part)) return part
    const trailingMatch = part.match(TRAILING_PUNCTUATION)
    const trailing = trailingMatch ? trailingMatch[0] : ""
    const url = trailing ? part.slice(0, -trailing.length) : part
    const href = url.startsWith("www.") ? `https://${url}` : url
    return (
      <Fragment key={i}>
        <a href={href} target="_blank" rel="noopener noreferrer" className="underline underline-offset-2 hover:opacity-80">
          {url}
        </a>
        {trailing}
      </Fragment>
    )
  })
}

function AttachmentImage({ messageId, onLoad }: { messageId: string; onLoad?: () => void }) {
  const url = useAttachmentUrl(messageId)
  if (!url) {
    return <div className="h-40 w-52 animate-pulse rounded-md bg-foreground/10" />
  }
  return (
    <img
      src={url}
      alt="Imagem enviada"
      onLoad={onLoad}
      className="max-h-64 max-w-full rounded-md object-cover"
    />
  )
}

type PendingImage = {
  file: File
  previewUrl: string
  caption: string
}

export function ChatPage() {
  const { friendId } = useParams<{ friendId: string }>()
  const { user } = useAuth()
  const {
    conversations,
    conversationsLoaded,
    messagesFor,
    loadHistory,
    sendMessage,
    sendImageMessage,
    markRead,
    setActiveFriendId,
  } = useChat()
  const [draft, setDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<Message | null>(null)
  const [pendingImage, setPendingImage] = useState<PendingImage | null>(null)
  const [sendingImage, setSendingImage] = useState(false)
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [highlight, setHighlight] = useState<{ id: string; nonce: number } | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)
  const scrollContainerRef = useRef<HTMLDivElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [conversationScope, animateConversation] = useAnimate()

  useEffect(() => {
    setActiveFriendId(friendId ?? null)
    setDraft("")
    setReplyingTo(null)
    setPendingImage(null)
    if (friendId) {
      loadHistory(friendId)
      markRead(friendId)
      // Disparado via useAnimate em vez de key={friendId} + initial/animate:
      // um motion.div remontado a cada troca de conversa reanima direitinho,
      // mas em dev o StrictMode monta/desmonta/remonta de propósito uma vez
      // a mais, e a animação de entrada tocava duas vezes seguidas (efeito
      // "fantasma"). Sem remount, só um efeito disparando animate(), o
      // pior caso do StrictMode é reiniciar a mesma transição no meio —
      // imperceptível, não repete do zero.
      animateConversation(
        conversationScope.current,
        { opacity: [0, 1], y: [6, 0] },
        { duration: 0.22, ease: [0.22, 1, 0.36, 1] },
      )
    }
    return () => setActiveFriendId(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId])

  const messages = friendId ? messagesFor(friendId) : []

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: "end" })
    // friendId entra na dependência de propósito: sem ele, trocar de conversa
    // pra um amigo cujo histórico em cache já tem a mesma quantidade de
    // mensagens da conversa anterior não disparava a rolagem (o length não
    // mudava), deixando a tela parada no scroll de onde a conversa anterior
    // ficou em vez de abrir no fim da nova.
  }, [friendId, messages.length])

  function scrollToBottomIfNear() {
    const container = scrollContainerRef.current
    if (!container) return
    // Imagem de anexo carrega depois do primeiro scroll (placeholder tem
    // altura diferente da imagem real) e empurra o conteúdo, deixando a
    // tela "quase" no fim em vez de no fim de verdade. Só reajusta se já
    // estava perto — não puxa quem rolou pra cima pra ler o histórico.
    if (container.scrollHeight - container.scrollTop - container.clientHeight < 300) {
      bottomRef.current?.scrollIntoView({ block: "end" })
    }
  }

  function jumpToMessage(id: string) {
    const el = document.getElementById(`message-${id}`)
    if (!el) return
    el.scrollIntoView({ behavior: "smooth", block: "center" })
    setHighlight({ id, nonce: Date.now() })
  }

  function submitDraft() {
    if (!friendId || !draft.trim()) return
    sendMessage(friendId, draft.trim(), replyingTo?.id)
    setDraft("")
    setReplyingTo(null)
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

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) {
      setPendingImage({ file, previewUrl: URL.createObjectURL(file), caption: "" })
    }
    event.target.value = ""
  }

  function handlePaste(event: ClipboardEvent<HTMLTextAreaElement>) {
    const item = Array.from(event.clipboardData.items).find((i) => i.type.startsWith("image/"))
    if (!item) return
    const file = item.getAsFile()
    if (!file) return
    event.preventDefault()
    setPendingImage({ file, previewUrl: URL.createObjectURL(file), caption: "" })
  }

  function cancelImage() {
    if (pendingImage) URL.revokeObjectURL(pendingImage.previewUrl)
    setPendingImage(null)
  }

  async function submitImage() {
    if (!friendId || !pendingImage) return
    setSendingImage(true)
    try {
      await sendImageMessage(friendId, pendingImage.file, pendingImage.caption.trim() || undefined, replyingTo?.id)
      URL.revokeObjectURL(pendingImage.previewUrl)
      setPendingImage(null)
      setReplyingTo(null)
    } finally {
      setSendingImage(false)
    }
  }

  if (!conversationsLoaded) {
    return <PageSkeleton rows={5} />
  }

  const activeFriend = conversations.find((c) => c.friendUserId === friendId)

  function replyAuthorLabel(m: Message) {
    return m.replyTo?.senderId === user?.id ? "Você" : (activeFriend?.friendName ?? "")
  }

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
          <div ref={conversationScope} className="flex flex-1 flex-col overflow-hidden">
            <button
              type="button"
              onClick={() => setProfileModalOpen(true)}
              disabled={!activeFriend}
              className="flex items-center gap-2 border-b px-4 py-3 text-left transition-colors hover:bg-muted/50 disabled:cursor-default disabled:hover:bg-transparent"
            >
              {activeFriend && <UserAvatar userId={activeFriend.friendUserId} name={activeFriend.friendName} size="sm" />}
              <span className="flex flex-col">
                <span className="font-medium">{activeFriend?.friendName}</span>
                {activeFriend?.friendBio && (
                  <span className="text-xs text-muted-foreground">{activeFriend.friendBio}</span>
                )}
              </span>
            </button>
            <FriendProfileModal
              friend={activeFriend ?? null}
              open={profileModalOpen}
              onClose={() => setProfileModalOpen(false)}
            />
            <div ref={scrollContainerRef} className="flex-1 overflow-y-auto px-4 py-3">
              <div className="flex flex-col gap-2">
                {messages.map((m) => {
                  const mine = m.senderId === user?.id
                  const replyButton = (
                    <button
                      type="button"
                      title="Responder"
                      onClick={() => setReplyingTo(m)}
                      className="shrink-0 rounded-full p-1 text-muted-foreground opacity-0 transition-opacity hover:text-foreground group-hover:opacity-100"
                    >
                      <Reply className="size-3.5" />
                    </button>
                  )
                  return (
                    <motion.div
                      key={m.id}
                      id={`message-${m.id}`}
                      initial={{ opacity: 0, scale: 0.94, y: 4 }}
                      animate={{ opacity: 1, scale: 1, y: 0 }}
                      transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}
                      className={`group flex items-center gap-1 ${mine ? "justify-end" : "justify-start"}`}
                    >
                      {!mine && replyButton}
                      <div
                        className={`relative max-w-[70%] overflow-hidden rounded-lg px-3 py-2 text-sm ${
                          mine ? "bg-primary text-primary-foreground" : "bg-muted"
                        }`}
                      >
                        {highlight?.id === m.id && (
                          <motion.div
                            key={highlight.nonce}
                            initial={{ opacity: 0.35 }}
                            animate={{ opacity: 0 }}
                            transition={{ duration: 1.1, ease: "easeOut" }}
                            onAnimationComplete={() =>
                              setHighlight((h) => (h?.nonce === highlight.nonce ? null : h))
                            }
                            className="pointer-events-none absolute inset-0 bg-foreground"
                          />
                        )}
                        <div className="relative z-10">
                          {m.replyTo && (
                            <button
                              type="button"
                              onClick={() => jumpToMessage(m.replyTo!.id)}
                              className={`mb-1.5 block w-full rounded border-l-2 px-2 py-1 text-left text-xs transition-colors ${
                                mine
                                  ? "border-primary-foreground/40 bg-primary-foreground/10 hover:bg-primary-foreground/20"
                                  : "border-foreground/20 bg-foreground/5 hover:bg-foreground/10"
                              }`}
                            >
                              <p className="font-medium">{replyAuthorLabel(m)}</p>
                              <p className="truncate opacity-80">{m.replyTo.contentPreview}</p>
                            </button>
                          )}
                          {m.attachmentId && (
                            <div className={m.content ? "mb-1.5" : ""}>
                              <AttachmentImage messageId={m.id} onLoad={scrollToBottomIfNear} />
                            </div>
                          )}
                          {m.content && (
                            <p className="whitespace-pre-wrap break-words">{linkifyText(m.content)}</p>
                          )}
                          <p
                            className={`mt-1 text-[10px] ${
                              mine ? "text-primary-foreground/70" : "text-muted-foreground"
                            }`}
                          >
                            {formatRelativeTime(m.createdAt)}
                          </p>
                        </div>
                      </div>
                      {mine && replyButton}
                    </motion.div>
                  )
                })}
                <div ref={bottomRef} />
              </div>
            </div>

            {replyingTo && (
              <div className="flex items-center justify-between gap-2 border-t bg-muted/40 px-4 py-2">
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-medium text-muted-foreground">
                    Respondendo a {replyingTo.senderId === user?.id ? "você mesmo" : activeFriend?.friendName}
                  </p>
                  <p className="truncate text-sm">{replyingTo.content ?? "📷 Foto"}</p>
                </div>
                <Button type="button" variant="ghost" size="icon" onClick={() => setReplyingTo(null)}>
                  <X className="size-4" />
                </Button>
              </div>
            )}

            {pendingImage && (
              <div className="flex items-center gap-3 border-t bg-muted/40 px-4 py-2">
                <img
                  src={pendingImage.previewUrl}
                  alt="Pré-visualização"
                  className="size-14 shrink-0 rounded-md object-cover"
                />
                <Input
                  value={pendingImage.caption}
                  onChange={(e) => setPendingImage((p) => (p ? { ...p, caption: e.target.value } : p))}
                  placeholder="Legenda (opcional)"
                  className="flex-1"
                />
                <Button type="button" size="icon" onClick={submitImage} disabled={sendingImage}>
                  <Send className="size-4" />
                </Button>
                <Button type="button" variant="ghost" size="icon" onClick={cancelImage} disabled={sendingImage}>
                  <X className="size-4" />
                </Button>
              </div>
            )}

            <form onSubmit={onSubmit} className="flex items-center gap-2 border-t p-3">
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleFileChange}
              />
              <Button
                type="button"
                variant="ghost"
                size="icon"
                title="Anexar imagem"
                onClick={() => fileInputRef.current?.click()}
              >
                <ImagePlus className="size-4" />
              </Button>
              <div className="flex-1">
                <Textarea
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  onKeyDown={onKeyDown}
                  onPaste={handlePaste}
                  placeholder="Escreva uma mensagem..."
                  className="max-h-40 min-h-10 w-full resize-none"
                />
              </div>
              <Button type="submit" size="icon" disabled={!draft.trim()}>
                <Send className="size-4" />
              </Button>
            </form>
          </div>
        )}
      </Card>
    </div>
  )
}
