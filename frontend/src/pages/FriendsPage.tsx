import { useEffect, useRef, useState } from "react"
import type { FormEvent } from "react"
import { motion } from "motion/react"
import { Check, Copy, MessageCircle, MoreHorizontal, UserMinus, UserPlus, Users, X } from "lucide-react"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"
import { useAuth } from "@/context/AuthContext"
import { useFriends } from "@/context/FriendsContext"
import type { Friendship } from "@/lib/types"
import { cn, gridItem, gridStagger } from "@/lib/utils"
import { AdminBadge } from "@/components/AdminBadge"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

function HandleChip({ handle }: { handle: string }) {
  const [copied, setCopied] = useState(false)
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => () => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current)
  }, [])

  async function copy() {
    try {
      await navigator.clipboard.writeText(handle)
      setCopied(true)
      if (timeoutRef.current) clearTimeout(timeoutRef.current)
      timeoutRef.current = setTimeout(() => setCopied(false), 1800)
    } catch {
      toast.error("Não foi possível copiar.")
    }
  }

  return (
    <button
      type="button"
      onClick={copy}
      title="Copiar seu identificador"
      className="group flex h-9 items-center gap-2 rounded-full border pr-3 pl-4 text-sm transition-colors hover:bg-accent"
    >
      <span className="text-muted-foreground">Seu identificador</span>
      <span className="font-medium tabular-nums">{handle}</span>
      <span className="relative size-3.5 text-muted-foreground">
        <Copy className={cn("absolute inset-0 size-3.5 transition-all duration-200", copied && "scale-50 opacity-0")} />
        <Check
          className={cn(
            "absolute inset-0 size-3.5 text-emerald-500 transition-all duration-200",
            copied ? "scale-100 opacity-100" : "scale-50 opacity-0",
          )}
        />
      </span>
    </button>
  )
}

function FriendCard({ friend, onRemove }: { friend: Friendship; onRemove: () => Promise<void> }) {
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [removing, setRemoving] = useState(false)

  function handleMenuChange(open: boolean) {
    setMenuOpen(open)
    if (!open) setConfirming(false)
  }

  async function remove() {
    setRemoving(true)
    try {
      await onRemove()
    } finally {
      setRemoving(false)
      handleMenuChange(false)
    }
  }

  return (
    <div className="flex h-full flex-col gap-5 rounded-2xl border p-5 transition-colors duration-200 hover:border-foreground/20">
      <div className="flex items-start gap-3">
        <UserAvatar userId={friend.friendUserId} name={friend.friendName} size="lg" />
        <div className="min-w-0 flex-1">
          <p className="flex items-center gap-1 truncate font-medium">
            {friend.friendName}
            {friend.friendAdmin && <AdminBadge />}
          </p>
          <p className="truncate text-sm text-muted-foreground">{friend.friendBio || "Sem bio"}</p>
        </div>
        <Popover open={menuOpen} onOpenChange={handleMenuChange}>
          <PopoverTrigger asChild>
            <Button type="button" variant="ghost" size="icon" className="-mt-1 -mr-2 size-8 text-muted-foreground" aria-label="Mais opções">
              <MoreHorizontal className="size-4" />
            </Button>
          </PopoverTrigger>
          <PopoverContent align="end" className="w-56 gap-0 p-1">
            {confirming ? (
              <div className="flex flex-col gap-3 p-2">
                <p className="text-sm text-popover-foreground">Remover {friend.friendName} dos seus amigos?</p>
                <div className="flex gap-2">
                  <Button type="button" variant="outline" size="sm" className="flex-1" onClick={() => setConfirming(false)}>
                    Cancelar
                  </Button>
                  <Button type="button" variant="destructive" size="sm" className="flex-1" disabled={removing} onClick={remove}>
                    Remover
                  </Button>
                </div>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => setConfirming(true)}
                className="flex w-full items-center gap-2.5 rounded-md px-2 py-1.5 text-left text-sm text-destructive transition-colors hover:bg-accent"
              >
                <UserMinus className="size-4" />
                Remover amigo
              </button>
            )}
          </PopoverContent>
        </Popover>
      </div>

      <div className="mt-auto flex justify-end">
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="rounded-full"
          onClick={() => navigate(`/chat/${friend.friendUserId}`)}
        >
          <MessageCircle data-icon="inline-start" />
          Mensagem
        </Button>
      </div>
    </div>
  )
}

export function FriendsPage() {
  const { user } = useAuth()
  const { friends, requests, loading, sendRequest, acceptRequest, removeFriendship } = useFriends()
  const [handle, setHandle] = useState("")
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await sendRequest(handle)
      setHandle("")
      toast.success("Convite enviado.")
    } catch {
      setError("Não foi possível enviar o convite. Confira o identificador (nome#0000) e tente de novo.")
    } finally {
      setSending(false)
    }
  }

  async function accept(id: string) {
    try {
      await acceptRequest(id)
    } catch {
      toast.error("Não foi possível aceitar o convite.")
    }
  }

  async function remove(id: string) {
    try {
      await removeFriendship(id)
    } catch {
      toast.error("Não foi possível concluir a ação.")
    }
  }

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  const received = requests.filter((r) => r.incoming)
  const sent = requests.filter((r) => !r.incoming)
  const myHandle = user ? `${user.name}#${user.tag}` : ""
  const sortedFriends = [...friends].sort((a, b) => a.friendName.localeCompare(b.friendName))

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Amigos</h1>
          <p className="mt-1 text-sm text-muted-foreground tabular-nums">
            {friends.length} {friends.length === 1 ? "amigo" : "amigos"}
            {received.length > 0 && ` · ${received.length} ${received.length === 1 ? "pedido" : "pedidos"}`}
          </p>
        </div>
        {myHandle && <HandleChip handle={myHandle} />}
      </div>

      <form onSubmit={onSubmit} className="flex flex-col gap-2">
        <div className="flex items-center gap-2 rounded-2xl border p-2 pl-4">
          <UserPlus className="size-4 shrink-0 text-muted-foreground" />
          <Input
            type="text"
            autoComplete="off"
            aria-label="Identificador do amigo"
            pattern="^.{1,120}#\d{4}$"
            title="Use o formato nome#0000"
            placeholder="nome#0000 do seu amigo"
            value={handle}
            onChange={(e) => setHandle(e.target.value)}
            required
            className="h-9 border-0 bg-transparent px-0 shadow-none focus-visible:ring-0 dark:bg-transparent"
          />
          <Button type="submit" disabled={sending} className="rounded-full">
            {sending ? "Enviando..." : "Convidar"}
          </Button>
        </div>
        {error && <p className="px-1 text-sm text-destructive">{error}</p>}
      </form>

      {received.length > 0 && (
        <section className="flex flex-col gap-3 rounded-2xl border border-foreground/20 bg-muted/20 p-5">
          <h2 className="text-sm font-medium">Pedidos recebidos</h2>
          <ul className="flex flex-col divide-y divide-border/60">
            {received.map((r) => (
              <li key={r.id} className="flex items-center justify-between gap-3 py-3 first:pt-1 last:pb-0">
                <span className="flex min-w-0 items-center gap-3">
                  <UserAvatar userId={r.friendUserId} name={r.friendName} size="sm" />
                  <span className="flex items-center gap-1 truncate font-medium">
                    {r.friendName}
                    {r.friendAdmin && <AdminBadge />}
                  </span>
                </span>
                <div className="flex shrink-0 gap-2">
                  <Button size="sm" className="rounded-full" onClick={() => accept(r.id)}>
                    <Check data-icon="inline-start" />
                    Aceitar
                  </Button>
                  <Button size="sm" variant="ghost" className="rounded-full text-muted-foreground" onClick={() => remove(r.id)}>
                    <X data-icon="inline-start" />
                    Recusar
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {friends.length === 0 ? (
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed px-6 py-14 text-center">
          <Users className="size-6 text-muted-foreground" />
          <p className="font-medium">Estudar junto é melhor</p>
          <p className="max-w-sm text-sm text-muted-foreground">
            Mande seu identificador para um amigo ou adicione o dele acima. Vocês aparecem no ranking um do outro.
          </p>
          {myHandle && <HandleChip handle={myHandle} />}
        </div>
      ) : (
        <motion.div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3" variants={gridStagger} initial="hidden" animate="show">
          {sortedFriends.map((friend) => (
            <motion.div key={friend.id} variants={gridItem}>
              <FriendCard friend={friend} onRemove={() => remove(friend.id)} />
            </motion.div>
          ))}
        </motion.div>
      )}

      {sent.length > 0 && (
        <section className="flex flex-col gap-2">
          <h2 className="text-sm font-medium text-muted-foreground">Aguardando resposta</h2>
          <ul className="flex flex-col">
            {sent.map((r) => (
              <li key={r.id} className="flex items-center justify-between gap-3 border-b border-border/50 py-2.5 last:border-b-0">
                <span className="flex min-w-0 items-center gap-3 text-sm">
                  <UserAvatar userId={r.friendUserId} name={r.friendName} size="sm" />
                  <span className="truncate">{r.friendName}</span>
                </span>
                <Button size="sm" variant="ghost" className="h-7 text-muted-foreground" onClick={() => remove(r.id)}>
                  Cancelar
                </Button>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
