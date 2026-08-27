import { useRef } from "react"
import { AnimatePresence, motion } from "motion/react"
import { Bell, Check, UserPlus, X } from "lucide-react"
import { toast } from "sonner"
import { AdminBadge } from "@/components/AdminBadge"
import { useFriends } from "@/context/FriendsContext"
import { formatRelativeTime } from "@/lib/format"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

const REFRESH_COOLDOWN_MS = 15_000

export function NotificationsPopover() {
  const { incomingRequests: requests, refresh, acceptRequest, removeFriendship } = useFriends()
  const lastRefreshRef = useRef(0)

  function handleOpenChange(next: boolean) {
    if (!next) return
    const now = Date.now()
    // Evita reconsultar /friends toda vez que o sino é reaberto em sequência
    // (clique curioso, abrir/fechar) — só busca de novo depois de um tempo.
    if (now - lastRefreshRef.current < REFRESH_COOLDOWN_MS) return
    lastRefreshRef.current = now
    refresh()
  }

  async function accept(id: string) {
    try {
      await acceptRequest(id)
      toast.success("Pedido de amizade aceito.")
    } catch {
      toast.error("Não foi possível aceitar o convite.")
    }
  }

  async function decline(id: string) {
    try {
      await removeFriendship(id)
    } catch {
      toast.error("Não foi possível concluir a ação.")
    }
  }

  return (
    <Popover onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="size-4.5" />
          {requests.length > 0 && (
            <span className="absolute top-0.5 right-0.5 flex size-4 items-center justify-center rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
              {requests.length}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-80 gap-0 p-0">
        <div className="flex items-center gap-3 border-b border-border p-3.5">
          <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-muted text-muted-foreground">
            <Bell className="size-4" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold text-popover-foreground">Notificações</p>
            <p className="truncate text-xs text-muted-foreground">
              {requests.length === 0
                ? "Nenhum pedido pendente"
                : `${requests.length} pedido${requests.length > 1 ? "s" : ""} de amizade`}
            </p>
          </div>
        </div>

        {requests.length === 0 ? (
          <p className="p-4 text-center text-sm text-muted-foreground">
            Nenhuma notificação por enquanto.
          </p>
        ) : (
          <div className="max-h-80 overflow-y-auto py-1">
            <AnimatePresence initial={false}>
              {requests.map((r) => (
                <motion.div
                  key={r.id}
                  layout
                  initial={{ opacity: 0, x: -8 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0 }}
                  className="flex items-center gap-3 px-3.5 py-2.5"
                >
                  <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-muted text-muted-foreground">
                    <UserPlus className="size-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="flex items-center gap-1 text-sm font-medium text-popover-foreground">
                      <span className="min-w-0 truncate">{r.friendName}</span>
                      {r.friendAdmin && <AdminBadge />}
                    </p>
                    <p className="truncate text-xs text-muted-foreground">
                      Quer ser seu amigo · {formatRelativeTime(r.createdAt)}
                    </p>
                  </div>
                  <div className="flex shrink-0 gap-1">
                    <Button size="icon-sm" variant="outline" onClick={() => accept(r.id)}>
                      <Check className="size-3.5" />
                    </Button>
                    <Button
                      size="icon-sm"
                      variant="ghost"
                      className="text-muted-foreground hover:text-destructive"
                      onClick={() => decline(r.id)}
                    >
                      <X className="size-3.5" />
                    </Button>
                  </div>
                </motion.div>
              ))}
            </AnimatePresence>
          </div>
        )}
      </PopoverContent>
    </Popover>
  )
}
