import { Fragment, useEffect, useRef, useState } from "react"
import type { ChangeEvent, ClipboardEvent, FormEvent, KeyboardEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { ImagePlus, Plus, Reply, Send, Users, X } from "lucide-react"
import { motion, useAnimate, useReducedMotion } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useChat, useAttachmentUrl } from "@/context/ChatContext"
import { formatRelativeTime } from "@/lib/format"
import type { Message } from "@/lib/types"
import { cn } from "@/lib/utils"
import { AdminBadge } from "@/components/AdminBadge"
import { CreateGroupModal } from "@/components/CreateGroupModal"
import { FriendProfileModal } from "@/components/FriendProfileModal"
import { GroupInfoModal } from "@/components/GroupInfoModal"
import { ImageLightbox } from "@/components/ImageLightbox"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
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

function AttachmentImage({
  messageId,
  onOpen,
  onLoaded,
}: {
  messageId: string
  onOpen: () => void
  onLoaded: () => void
}) {
  const url = useAttachmentUrl(messageId)
  if (!url) {
    return <div className="h-40 w-52 animate-pulse rounded-md bg-foreground/10" />
  }
  return (
    <button type="button" onClick={onOpen} className="block cursor-zoom-in">
      {/* onLoad avisa a conversa pra reancorar: é o único momento em que dá
          pra ter certeza de que a imagem já tem altura final, sem depender
          do ResizeObserver nem de acertar uma janela de tempo. */}
      <img
        src={url}
        alt="Imagem enviada"
        onLoad={onLoaded}
        className="max-h-64 max-w-full rounded-md object-cover"
      />
    </button>
  )
}

type PendingImage = {
  file: File
  previewUrl: string
}

export function ChatPage() {
  const { friendId, groupId } = useParams<{ friendId?: string; groupId?: string }>()
  const isGroup = !!groupId
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
    groupConversations,
    groupConversationsLoaded,
    groupMessagesFor,
    loadGroupHistory,
    loadGroupMembers,
    sendGroupMessage,
    sendGroupImageMessage,
    markGroupRead,
    setActiveGroupId,
    groupMembersById,
  } = useChat()
  const ready = conversationsLoaded && groupConversationsLoaded
  const [draft, setDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<Message | null>(null)
  const [displayedReplyingTo, setDisplayedReplyingTo] = useState<Message | null>(null)
  useEffect(() => {
    if (replyingTo) setDisplayedReplyingTo(replyingTo)
  }, [replyingTo])
  const [pendingImage, setPendingImage] = useState<PendingImage | null>(null)
  const [sendingImage, setSendingImage] = useState(false)
  const [lightboxMessageId, setLightboxMessageId] = useState<string | null>(null)
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [createGroupModalOpen, setCreateGroupModalOpen] = useState(false)
  const [highlight, setHighlight] = useState<{ id: string; nonce: number } | null>(null)
  const scrollContainerRef = useRef<HTMLDivElement>(null)
  const messagesContentRef = useRef<HTMLDivElement>(null)
  const pinnedRef = useRef(true)
  const programmaticScrollRef = useRef(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [conversationScope, animateConversation] = useAnimate()
  const reducedMotion = useReducedMotion()

  // formatRelativeTime só recalcula quando o componente re-renderiza — sem
  // isso, uma mensagem mandada "agora" continua mostrando "agora" pra
  // sempre se a conversa ficar aberta sem novas mensagens chegando. Esse
  // tick força um re-render periódico só pra atualizar os textos de tempo.
  const [, forceTimeTick] = useState(0)
  useEffect(() => {
    const interval = setInterval(() => forceTimeTick((t) => t + 1), 30000)
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    setActiveFriendId(isGroup ? null : (friendId ?? null))
    setActiveGroupId(isGroup ? (groupId ?? null) : null)
    setDraft("")
    setReplyingTo(null)
    setPendingImage(null)
    if (isGroup && groupId) {
      loadGroupHistory(groupId)
      loadGroupMembers(groupId)
      markGroupRead(groupId)
    } else if (friendId) {
      loadHistory(friendId)
      markRead(friendId)
    }
    if (friendId || groupId) {
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
    return () => {
      setActiveFriendId(null)
      setActiveGroupId(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId, groupId])

  const messages = isGroup ? (groupId ? groupMessagesFor(groupId) : []) : friendId ? messagesFor(friendId) : []

  // Em grupo, cada bolha de outra pessoa ganha o nome de quem mandou acima do
  // conteúdo — e esse nome só existe depois que loadGroupMembers responde, ou
  // seja, TODAS as bolhas crescem um pouco depois que o histórico já
  // renderizou. É por isso que o grupo continuava abrindo fora do fim mesmo
  // depois de consertar o caso da conversa 1:1: nada reancorava nesse
  // segundo crescimento.
  const activeGroupMemberCount = groupId ? (groupMembersById[groupId]?.length ?? 0) : 0

  // "Grudado no fim" é uma intenção (o usuário rolou pra cima ou não), não
  // uma distância fixa — a versão antiga desistia de acompanhar se um único
  // evento de resize (várias imagens decodificando juntas, por exemplo)
  // empurrasse o conteúdo mais de 300px de uma vez, e depois disso nada
  // mais reancorava, deixando a conversa parada bem em cima de uma imagem.
  // Aqui, enquanto pinnedRef for true, TODA mudança de altura reancora
  // incondicionalmente; só um scroll de verdade que pousa longe do fim
  // desarma, e voltar a rolar até o fim rearma.
  //
  // programmaticScrollRef existe porque a própria correção (stickToBottom
  // setando scrollTop) dispara um evento "scroll" nativo — sem filtrar
  // isso, o handler abaixo lia esse eco como se fosse o usuário rolando
  // pra longe (a nova mensagem ainda "assentando" no layout faz o
  // scrollHeight mudar de novo bem depois do assign, sobrando uma
  // distância de dezenas de pixels no instante do evento) e desgrudava
  // sozinho bem depois de cada mensagem nova — o bug ficava só visível
  // com uma mensagem chegando enquanto a conversa já estava aberta.
  useEffect(() => {
    const container = scrollContainerRef.current
    if (!container) return
    pinnedRef.current = true
    stickToBottom()
    settleScroll(2000)

    function handleScroll() {
      if (programmaticScrollRef.current) {
        programmaticScrollRef.current = false
        return
      }
      if (!container) return
      const distance = container.scrollHeight - container.scrollTop - container.clientHeight
      pinnedRef.current = distance < 40
    }
    container.addEventListener("scroll", handleScroll, { passive: true })
    return () => container.removeEventListener("scroll", handleScroll)
    // ready entra nas deps por causa da tela de carregamento (ver comentário
    // do ResizeObserver logo abaixo) — reanexa assim que o container de
    // verdade existe, não só quando troca de conversa.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId, groupId, ready])

  useEffect(() => {
    settleScroll(2000)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [messages.length, ready, activeGroupMemberCount])

  function stickToBottom() {
    const container = scrollContainerRef.current
    if (!container || !pinnedRef.current) return
    programmaticScrollRef.current = true
    container.scrollTop = container.scrollHeight
  }

  const settleFrameRef = useRef<number | null>(null)

  // Reforça por ~2s via requestAnimationFrame em vez de confiar só no
  // ResizeObserver abaixo: em teste, o ResizeObserver simplesmente não
  // disparou nem uma vez pra um crescimento manual e óbvio do conteúdo
  // (confirmado isolando o teste fora do React) — pode ser algo específico
  // deste navegador automatizado, mas como não dá pra garantir 100% em
  // todo navegador/aba em segundo plano, esse polling curto garante a
  // ancoragem de qualquer jeito, sem depender de nenhum sinal de resize.
  function settleScroll(durationMs: number) {
    if (settleFrameRef.current !== null) cancelAnimationFrame(settleFrameRef.current)
    const deadline = performance.now() + durationMs
    function frame() {
      stickToBottom()
      settleFrameRef.current = performance.now() < deadline ? requestAnimationFrame(frame) : null
    }
    settleFrameRef.current = requestAnimationFrame(frame)
  }

  useEffect(() => {
    return () => {
      if (settleFrameRef.current !== null) cancelAnimationFrame(settleFrameRef.current)
    }
  }, [])

  useEffect(() => {
    const content = messagesContentRef.current
    if (!content) return
    // ResizeObserver como reação imediata em navegadores onde funciona (não
    // depende de rAF, que não roda com a aba em segundo plano) — o polling
    // de settleScroll acima é quem garante a ancoragem de fato.
    // Precisa reanexar a cada troca de conversa E a cada mudança de "ready"
    // (deps friendId/groupId/ready, não []): messagesContentRef só existe
    // depois que a tela de carregamento (conversationsLoaded/
    // groupConversationsLoaded) libera o conteúdo de verdade. Se o
    // histórico da conversa chegasse ANTES disso — uma corrida bem real,
    // já que são duas requisições independentes — o efeito rodava com
    // content nulo, desistia, e como friendId/groupId não mudam quando a
    // tela de carregamento sai do ar, nada tentava de novo depois: a
    // conversa carregava parada no topo pro resto da sessão, sem nenhum
    // erro visível.
    const observer = new ResizeObserver(() => stickToBottom())
    observer.observe(content)
    return () => observer.disconnect()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [friendId, groupId, ready])

  function jumpToMessage(id: string) {
    const el = document.getElementById(`message-${id}`)
    if (!el) return
    el.scrollIntoView({ behavior: "smooth", block: "center" })
    setHighlight({ id, nonce: Date.now() })
  }

  function submitDraft() {
    if (pendingImage) {
      if (!sendingImage) submitImage()
      return
    }
    if (!draft.trim()) return
    if (isGroup && groupId) {
      sendGroupMessage(groupId, draft.trim(), replyingTo?.id)
    } else if (friendId) {
      sendMessage(friendId, draft.trim(), replyingTo?.id)
    } else {
      return
    }
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
      setPendingImage({ file, previewUrl: URL.createObjectURL(file) })
    }
    event.target.value = ""
  }

  function handlePaste(event: ClipboardEvent<HTMLTextAreaElement>) {
    const item = Array.from(event.clipboardData.items).find((i) => i.type.startsWith("image/"))
    if (!item) return
    const file = item.getAsFile()
    if (!file) return
    event.preventDefault()
    setPendingImage({ file, previewUrl: URL.createObjectURL(file) })
  }

  function cancelImage() {
    if (pendingImage) URL.revokeObjectURL(pendingImage.previewUrl)
    setPendingImage(null)
  }

  async function submitImage() {
    if (!pendingImage) return
    setSendingImage(true)
    try {
      const caption = draft.trim() || undefined
      if (isGroup && groupId) {
        await sendGroupImageMessage(groupId, pendingImage.file, caption, replyingTo?.id)
      } else if (friendId) {
        await sendImageMessage(friendId, pendingImage.file, caption, replyingTo?.id)
      }
      URL.revokeObjectURL(pendingImage.previewUrl)
      setPendingImage(null)
      setDraft("")
      setReplyingTo(null)
    } finally {
      setSendingImage(false)
    }
  }

  if (!ready) {
    return <PageSkeleton rows={5} />
  }

  const activeFriend = conversations.find((c) => c.friendUserId === friendId)
  const activeGroup = groupConversations.find((c) => c.groupId === groupId)
  const activeGroupMembers = groupId ? (groupMembersById[groupId] ?? []) : []

  function memberName(userId: string) {
    return activeGroupMembers.find((m) => m.userId === userId)?.name ?? ""
  }

  function replyAuthorLabel(m: Message) {
    if (m.replyTo?.senderId === user?.id) return "Você"
    return isGroup ? memberName(m.replyTo?.senderId ?? "") : (activeFriend?.friendName ?? "")
  }

  return (
    <div className="flex h-[calc(100vh-9rem)] gap-4">
      <Card className="w-64 shrink-0 overflow-y-auto p-0">
        <div className="px-4 py-2">
          <span className="text-xs font-medium text-muted-foreground">Conversas</span>
        </div>
        {conversations.length === 0 ? (
          <p className="px-4 pb-3 text-sm text-muted-foreground">Adicione amigos pra começar a conversar.</p>
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
                        <span className="flex min-w-0 items-center gap-1">
                          <span className="min-w-0 truncate font-medium">{c.friendName}</span>
                          {c.friendAdmin && <AdminBadge />}
                        </span>
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

        <div className="flex items-center justify-between border-t px-4 py-2">
          <span className="text-xs font-medium text-muted-foreground">Grupos</span>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            title="Criar grupo"
            className="size-6"
            onClick={() => setCreateGroupModalOpen(true)}
          >
            <Plus className="size-3.5" />
          </Button>
        </div>
        {groupConversations.length > 0 && (
          <ul className="divide-y">
            {groupConversations.map((c) => (
              <li key={c.groupId} className="relative">
                {c.groupId === groupId && (
                  <motion.div
                    layoutId="chat-conversation-pill"
                    className="absolute inset-0 bg-muted"
                    transition={{ type: "spring", stiffness: 500, damping: 35 }}
                  />
                )}
                <Link
                  to={`/chat/g/${c.groupId}`}
                  className="relative z-10 flex flex-col gap-0.5 px-4 py-3 hover:bg-muted/50"
                >
                  <span className="flex items-center gap-2">
                    <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                      <Users className="size-4" />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="flex items-center justify-between gap-2">
                        <span className="min-w-0 truncate font-medium">{c.groupName}</span>
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

      <CreateGroupModal open={createGroupModalOpen} onClose={() => setCreateGroupModalOpen(false)} />

      <Card className="flex flex-1 flex-col p-0">
        {!friendId && !groupId ? (
          <div className="flex flex-1 items-center justify-center text-sm text-muted-foreground">
            Selecione uma conversa.
          </div>
        ) : (
          <div ref={conversationScope} className="flex flex-1 flex-col overflow-hidden">
            {isGroup ? (
              <button
                type="button"
                onClick={() => setProfileModalOpen(true)}
                disabled={!activeGroup}
                className="flex items-center gap-2 border-b px-4 py-3 text-left transition-colors hover:bg-muted/50 disabled:cursor-default disabled:hover:bg-transparent"
              >
                <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                  <Users className="size-4" />
                </span>
                <span className="flex flex-col">
                  <span className="font-medium">{activeGroup?.groupName}</span>
                  <span className="text-xs text-muted-foreground">
                    {activeGroupMembers.length > 0
                      ? `${activeGroupMembers.length} membros`
                      : activeGroup?.memberNames.join(", ")}
                  </span>
                </span>
              </button>
            ) : (
              <button
                type="button"
                onClick={() => setProfileModalOpen(true)}
                disabled={!activeFriend}
                className="flex items-center gap-2 border-b px-4 py-3 text-left transition-colors hover:bg-muted/50 disabled:cursor-default disabled:hover:bg-transparent"
              >
                {activeFriend && <UserAvatar userId={activeFriend.friendUserId} name={activeFriend.friendName} size="sm" />}
                <span className="flex flex-col">
                  <span className="flex items-center gap-1 font-medium">
                    {activeFriend?.friendName}
                    {activeFriend?.friendAdmin && <AdminBadge />}
                  </span>
                  {activeFriend?.friendBio && (
                    <span className="text-xs text-muted-foreground">{activeFriend.friendBio}</span>
                  )}
                </span>
              </button>
            )}
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
            <div ref={scrollContainerRef} className="flex-1 overflow-y-auto px-4 py-3">
              <div ref={messagesContentRef} className="flex flex-col gap-2">
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
                      {mine && replyButton}
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
                          {isGroup && !mine && (
                            <p className="mb-0.5 text-xs font-medium text-muted-foreground">
                              {memberName(m.senderId)}
                            </p>
                          )}
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
                              <AttachmentImage
                                messageId={m.id}
                                onOpen={() => setLightboxMessageId(m.id)}
                                onLoaded={() => settleScroll(400)}
                              />
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
                      {!mine && replyButton}
                    </motion.div>
                  )
                })}
              </div>
            </div>

            <div
              className={cn(
                "grid ease-out",
                replyingTo ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
                reducedMotion ? "duration-0" : "duration-200",
              )}
              style={{ transitionProperty: "grid-template-rows" }}
            >
              <div className="overflow-hidden">
                <div className="flex items-center justify-between gap-2 border-t bg-muted/40 px-4 py-2">
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-medium text-muted-foreground">
                      Respondendo a{" "}
                      {displayedReplyingTo?.senderId === user?.id
                        ? "você mesmo"
                        : isGroup
                          ? memberName(displayedReplyingTo?.senderId ?? "")
                          : activeFriend?.friendName}
                    </p>
                    <p className="truncate text-sm">{displayedReplyingTo?.content ?? "📷 Foto"}</p>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    tabIndex={replyingTo ? 0 : -1}
                    onClick={() => setReplyingTo(null)}
                  >
                    <X className="size-4" />
                  </Button>
                </div>
              </div>
            </div>

            {pendingImage && (
              <div className="border-t bg-muted/40 px-4 py-2">
                <div className="relative inline-block">
                  <img
                    src={pendingImage.previewUrl}
                    alt="Pré-visualização"
                    className="size-16 rounded-md object-cover"
                  />
                  <button
                    type="button"
                    onClick={cancelImage}
                    disabled={sendingImage}
                    title="Remover anexo"
                    className="absolute -top-1.5 -right-1.5 flex size-5 items-center justify-center rounded-full bg-background text-muted-foreground shadow ring-1 ring-border hover:text-foreground disabled:pointer-events-none disabled:opacity-50"
                  >
                    <X className="size-3" />
                  </button>
                </div>
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
              <Button type="submit" size="icon" disabled={pendingImage ? sendingImage : !draft.trim()}>
                <Send className="size-4" />
              </Button>
            </form>
          </div>
        )}
      </Card>

      <ImageLightbox messageId={lightboxMessageId} onClose={() => setLightboxMessageId(null)} />
    </div>
  )
}
