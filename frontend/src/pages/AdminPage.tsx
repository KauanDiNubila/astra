import { useEffect, useState } from "react"
import { motion, useReducedMotion } from "motion/react"
import { Ban, MoreHorizontal, Search, ShieldCheck, ShieldOff, Trash2, Undo2, Users } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { useCountUp } from "@/hooks/use-count-up"
import { formatDateTime } from "@/lib/format"
import { cn, enterAt, fadeSwap, gridItem, gridStagger } from "@/lib/utils"
import type { AdminUser } from "@/lib/types"
import { DeleteUserModal } from "@/components/DeleteUserModal"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PillToggleButton } from "@/components/PillToggleButton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

const filters = [
  { key: "ALL", label: "Todos" },
  { key: "ACTIVE", label: "Ativos" },
  { key: "BANNED", label: "Banidos" },
  { key: "ADMIN", label: "Admins" },
] as const

type FilterKey = (typeof filters)[number]["key"]

const ROLE_RANK: Record<AdminUser["role"], number> = { USER: 0, ADMIN: 1, OWNER: 2 }

const WEEK_MS = 7 * 24 * 60 * 60 * 1000

function normalize(text: string) {
  return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
}

function Stat({ label, value, hint }: { label: string; value: number; hint: string | null }) {
  const reducedMotion = useReducedMotion()
  const animated = Math.round(useCountUp(value, reducedMotion ? 1 : 800, reducedMotion ? value : 0))
  return (
    <div className="flex flex-col gap-1.5 p-5">
      <span className="text-xs font-medium tracking-wide text-muted-foreground uppercase">{label}</span>
      <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">{animated.toLocaleString("pt-BR")}</span>
      <span className="min-h-4 text-xs text-muted-foreground">{hint}</span>
    </div>
  )
}

function Chip({ children, tone = "neutral" }: { children: string; tone?: "neutral" | "danger" }) {
  return (
    <span
      className={cn(
        "rounded-full border px-2 py-0.5 text-[11px] leading-none font-medium",
        tone === "danger" ? "border-destructive/40 text-destructive" : "text-muted-foreground",
      )}
    >
      {children}
    </span>
  )
}

function MenuItem({
  icon: Icon,
  children,
  danger,
  disabled,
  onClick,
}: {
  icon: typeof Ban
  children: string
  danger?: boolean
  disabled?: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={cn(
        "flex w-full items-center gap-2.5 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-accent disabled:pointer-events-none disabled:opacity-50",
        danger ? "text-destructive" : "text-popover-foreground",
      )}
    >
      <Icon className="size-4" />
      {children}
    </button>
  )
}

function UserActions({
  target,
  canPromoteOrDemote,
  busy,
  onBan,
  onUnban,
  onPromote,
  onDemote,
  onDelete,
}: {
  target: AdminUser
  canPromoteOrDemote: boolean
  busy: boolean
  onBan: () => Promise<void>
  onUnban: () => Promise<void>
  onPromote: () => Promise<void>
  onDemote: () => Promise<void>
  onDelete: () => void
}) {
  const [open, setOpen] = useState(false)
  const [confirmingBan, setConfirmingBan] = useState(false)

  function handleOpenChange(next: boolean) {
    setOpen(next)
    if (!next) setConfirmingBan(false)
  }

  async function run(action: () => Promise<void>) {
    await action()
    handleOpenChange(false)
  }

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="size-8 shrink-0 text-muted-foreground"
          aria-label={`Ações para ${target.name}`}
        >
          <MoreHorizontal className="size-4" />
        </Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-56 gap-0 p-1">
        {confirmingBan ? (
          <div className="flex flex-col gap-3 p-2">
            <p className="text-sm text-popover-foreground">
              Banir {target.name}? A pessoa perde o acesso na hora, e dá para desfazer depois.
            </p>
            <div className="flex gap-2">
              <Button type="button" variant="outline" size="sm" className="flex-1" onClick={() => setConfirmingBan(false)}>
                Cancelar
              </Button>
              <Button type="button" variant="destructive" size="sm" className="flex-1" disabled={busy} onClick={() => run(onBan)}>
                Banir
              </Button>
            </div>
          </div>
        ) : (
          <>
            {canPromoteOrDemote &&
              (target.role === "ADMIN" ? (
                <MenuItem icon={ShieldOff} disabled={busy} onClick={() => run(onDemote)}>
                  Remover admin
                </MenuItem>
              ) : (
                <MenuItem icon={ShieldCheck} disabled={busy} onClick={() => run(onPromote)}>
                  Tornar admin
                </MenuItem>
              ))}
            {target.banned ? (
              <MenuItem icon={Undo2} disabled={busy} onClick={() => run(onUnban)}>
                Desbanir
              </MenuItem>
            ) : (
              <MenuItem icon={Ban} danger disabled={busy} onClick={() => setConfirmingBan(true)}>
                Banir
              </MenuItem>
            )}
            <div className="my-1 h-px bg-border" />
            <MenuItem
              icon={Trash2}
              danger
              disabled={busy}
              onClick={() => {
                handleOpenChange(false)
                onDelete()
              }}
            >
              Excluir conta
            </MenuItem>
          </>
        )}
      </PopoverContent>
    </Popover>
  )
}

export function AdminPage() {
  const { user } = useAuth()
  const reducedMotion = useReducedMotion()

  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<FilterKey>("ALL")
  const [search, setSearch] = useState("")
  const [busyId, setBusyId] = useState<string | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<AdminUser | null>(null)

  function loadUsers() {
    return api.get<AdminUser[]>("/admin/users").then((res) => {
      setUsers([...res.data].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()))
    })
  }

  useEffect(() => {
    loadUsers().finally(() => setLoading(false))
  }, [])

  async function act(id: string, path: string, patch: Partial<AdminUser>, error: string) {
    setBusyId(id)
    try {
      await api.post(`/admin/users/${id}/${path}`)
      setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, ...patch } : u)))
    } catch {
      toast.error(error)
    } finally {
      setBusyId(null)
    }
  }

  if (loading) {
    return <PageSkeleton rows={5} />
  }

  const bannedCount = users.filter((u) => u.banned).length
  const adminCount = users.filter((u) => u.role !== "USER").length
  const now = Date.now()
  const newThisWeek = users.filter((u) => now - new Date(u.createdAt).getTime() < WEEK_MS).length

  const stats = [
    { label: "Contas", value: users.length, hint: newThisWeek > 0 ? `+${newThisWeek} nos últimos 7 dias` : "Nenhuma nova nos últimos 7 dias" },
    { label: "Ativas", value: users.length - bannedCount, hint: null },
    { label: "Banidas", value: bannedCount, hint: null },
    { label: "Admins", value: adminCount, hint: null },
  ]

  const query = normalize(search.trim())
  const filteredUsers = users
    .filter((u) => {
      if (filter === "ACTIVE") return !u.banned
      if (filter === "BANNED") return u.banned
      if (filter === "ADMIN") return u.role !== "USER"
      return true
    })
    .filter((u) => !query || normalize(u.name).includes(query) || normalize(u.email).includes(query))

  const myRank = ROLE_RANK[user?.role ?? "USER"]

  return (
    <div className="flex flex-col gap-8">
      <motion.div {...enterAt(0, reducedMotion)}>
        <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Administração</h1>
        <p className="mt-1 text-sm text-muted-foreground tabular-nums">
          {users.length} {users.length === 1 ? "conta" : "contas"}
          {bannedCount > 0 && ` · ${bannedCount} ${bannedCount === 1 ? "banida" : "banidas"}`}
        </p>
      </motion.div>

      <motion.div
        variants={{ hidden: {}, show: { transition: { ...gridStagger.show.transition, delayChildren: 0.1 } } }}
        initial="hidden"
        animate="show"
        className="grid grid-cols-2 gap-px overflow-hidden rounded-2xl border bg-border lg:grid-cols-4"
      >
        {stats.map((stat) => (
          <div key={stat.label} className="bg-background">
            <motion.div variants={gridItem}>
              <Stat label={stat.label} value={stat.value} hint={stat.hint} />
            </motion.div>
          </div>
        ))}
      </motion.div>

      <motion.section {...enterAt(0.25, reducedMotion)} className="flex flex-col gap-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex w-fit gap-1 rounded-md border bg-muted/30 p-1">
            {filters.map((f) => (
              <PillToggleButton
                key={f.key}
                active={filter === f.key}
                layoutId="admin-filter-pill"
                onClick={() => setFilter(f.key)}
                className="h-7 px-2.5"
              >
                {f.label}
              </PillToggleButton>
            ))}
          </div>
          <div className="flex items-center gap-2 rounded-full border px-3 sm:w-64">
            <Search className="size-4 shrink-0 text-muted-foreground" />
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por nome ou e-mail"
              aria-label="Buscar usuário"
              className="h-9 border-0 bg-transparent px-0 shadow-none focus-visible:ring-0 dark:bg-transparent"
            />
          </div>
        </div>

        <motion.div key={filter} {...fadeSwap(reducedMotion)}>
          {filteredUsers.length === 0 ? (
            <div className="flex flex-col items-center gap-2 rounded-2xl border border-dashed px-6 py-12 text-center">
              <Users className="size-5 text-muted-foreground" />
              <p className="text-sm font-medium">Nenhum usuário encontrado</p>
              <p className="text-sm text-muted-foreground">
                {query ? "Tente outro nome ou e-mail." : "Nenhuma conta nesse filtro."}
              </p>
            </div>
          ) : (
            <ul className="flex flex-col divide-y overflow-hidden rounded-2xl border">
              {filteredUsers.map((u) => {
                const isMe = u.id === user?.id
                const canAct = !isMe && myRank > ROLE_RANK[u.role]
                return (
                  <li key={u.id} className="flex items-center gap-3 px-4 py-3 sm:px-5">
                    <UserAvatar userId={u.id} name={u.name} size="sm" />
                    <div className={cn("flex min-w-0 flex-1 flex-col gap-0.5", u.banned && "opacity-60")}>
                      <span className="flex min-w-0 flex-wrap items-center gap-1.5">
                        <span className="truncate font-medium">{u.name}</span>
                        {isMe && <Chip>Você</Chip>}
                        {u.role === "OWNER" && <Chip>Owner</Chip>}
                        {u.role === "ADMIN" && <Chip>Admin</Chip>}
                        {u.banned && <Chip tone="danger">Banido</Chip>}
                      </span>
                      <span className="truncate text-sm text-muted-foreground">
                        {u.email}
                        <span className="hidden sm:inline"> · entrou em {formatDateTime(u.createdAt)}</span>
                      </span>
                    </div>
                    {canAct ? (
                      <UserActions
                        target={u}
                        canPromoteOrDemote={user?.role === "OWNER"}
                        busy={busyId === u.id}
                        onBan={() => act(u.id, "ban", { banned: true }, "Não foi possível banir esse usuário.")}
                        onUnban={() => act(u.id, "unban", { banned: false }, "Não foi possível desbanir esse usuário.")}
                        onPromote={() => act(u.id, "promote", { role: "ADMIN" }, "Não foi possível promover esse usuário.")}
                        onDemote={() => act(u.id, "demote", { role: "USER" }, "Não foi possível remover admin desse usuário.")}
                        onDelete={() => setDeleteTarget(u)}
                      />
                    ) : (
                      <span className="size-8 shrink-0" />
                    )}
                  </li>
                )
              })}
            </ul>
          )}
        </motion.div>
      </motion.section>

      <DeleteUserModal user={deleteTarget} onClose={() => setDeleteTarget(null)} onDeleted={loadUsers} />
    </div>
  )
}
