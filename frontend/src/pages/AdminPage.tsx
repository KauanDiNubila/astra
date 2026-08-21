import { useEffect, useRef, useState } from "react"
import { motion } from "motion/react"
import { Trash2 } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { formatDateTime } from "@/lib/format"
import { cn, gridItem, gridStagger } from "@/lib/utils"
import type { AdminUser } from "@/lib/types"
import { DeleteUserModal } from "@/components/DeleteUserModal"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PillToggleButton } from "@/components/PillToggleButton"
import { StatCard } from "@/components/StatCard"
import { UserAvatar } from "@/components/UserAvatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

const filters = [
  { key: "ALL", label: "Todos" },
  { key: "ACTIVE", label: "Ativos" },
  { key: "BANNED", label: "Banidos" },
  { key: "ADMIN", label: "Admins" },
] as const

type FilterKey = (typeof filters)[number]["key"]

export function AdminPage() {
  const { user } = useAuth()

  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<FilterKey>("ALL")
  const [search, setSearch] = useState("")
  const [renderKey, setRenderKey] = useState(0)

  const [confirmBanId, setConfirmBanId] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<AdminUser | null>(null)
  const banTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  function loadUsers() {
    return api.get<AdminUser[]>("/admin/users").then((res) => {
      setUsers([...res.data].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()))
      setRenderKey((k) => k + 1)
    })
  }

  useEffect(() => {
    loadUsers().finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    return () => {
      if (banTimeoutRef.current) clearTimeout(banTimeoutRef.current)
    }
  }, [])

  function armBan(id: string) {
    setConfirmBanId(id)
    if (banTimeoutRef.current) clearTimeout(banTimeoutRef.current)
    banTimeoutRef.current = setTimeout(() => setConfirmBanId(null), 3000)
  }

  async function confirmBan(id: string) {
    if (banTimeoutRef.current) clearTimeout(banTimeoutRef.current)
    setConfirmBanId(null)
    setBusyId(id)
    try {
      await api.post(`/admin/users/${id}/ban`)
      await loadUsers()
    } catch {
      toast.error("Não foi possível banir esse usuário.")
    } finally {
      setBusyId(null)
    }
  }

  async function unban(id: string) {
    setBusyId(id)
    try {
      await api.post(`/admin/users/${id}/unban`)
      await loadUsers()
    } catch {
      toast.error("Não foi possível desbanir esse usuário.")
    } finally {
      setBusyId(null)
    }
  }

  if (loading) {
    return <PageSkeleton rows={5} />
  }

  const totalCount = users.length
  const activeCount = users.filter((u) => !u.banned).length
  const bannedCount = users.filter((u) => u.banned).length

  const query = search.trim().toLowerCase()
  const filteredUsers = users
    .filter((u) => {
      if (filter === "ACTIVE") return !u.banned
      if (filter === "BANNED") return u.banned
      if (filter === "ADMIN") return u.role === "ADMIN"
      return true
    })
    .filter((u) => !query || u.name.toLowerCase().includes(query) || u.email.toLowerCase().includes(query))

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Administração</h1>

      <motion.div className="grid gap-4 sm:grid-cols-3" variants={gridStagger} initial="hidden" animate="show">
        <motion.div variants={gridItem}>
          <StatCard label="Total" value={totalCount} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Ativos" value={activeCount} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Banidos" value={bannedCount} />
        </motion.div>
      </motion.div>

      <Card>
        <CardHeader className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <CardTitle>Usuários</CardTitle>
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
            <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
              {filters.map((f) => (
                <PillToggleButton
                  key={f.key}
                  active={filter === f.key}
                  layoutId="admin-filter-pill"
                  onClick={() => setFilter(f.key)}
                  className="h-7 px-2"
                >
                  {f.label}
                </PillToggleButton>
              ))}
            </div>
            <Input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por nome ou e-mail"
              className="w-full sm:w-56"
            />
          </div>
        </CardHeader>
        <CardContent className="p-0">
          <motion.div
            key={`${filter}-${search}-${renderKey}`}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
          >
            {filteredUsers.length === 0 ? (
              <p className="p-6 text-sm text-muted-foreground">Nenhum usuário encontrado.</p>
            ) : (
              <ul className="flex flex-col divide-y">
                {filteredUsers.map((u) => {
                  const isMe = u.id === user?.id
                  return (
                    <li
                      key={u.id}
                      className={cn("flex items-center justify-between gap-4 px-6 py-3", isMe && "bg-muted/50")}
                    >
                      <span className="flex min-w-0 items-center gap-3">
                        <UserAvatar userId={u.id} name={u.name} size="sm" />
                        <span className="flex min-w-0 flex-col">
                          <span className="flex flex-wrap items-center gap-2 font-medium">
                            {u.name}
                            {u.role === "ADMIN" && <Badge variant="secondary">Admin</Badge>}
                            {u.banned && <Badge variant="destructive">Banido</Badge>}
                          </span>
                          <span className="truncate text-sm text-muted-foreground">
                            {u.email} &middot; {formatDateTime(u.createdAt)}
                          </span>
                        </span>
                      </span>

                      {!isMe && (
                        <div className="flex shrink-0 items-center gap-2">
                          {u.banned ? (
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              disabled={busyId === u.id}
                              onClick={() => unban(u.id)}
                            >
                              Desbanir
                            </Button>
                          ) : (
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              className={
                                confirmBanId === u.id
                                  ? "text-destructive"
                                  : "text-muted-foreground hover:text-destructive"
                              }
                              disabled={busyId === u.id}
                              onClick={() => (confirmBanId === u.id ? confirmBan(u.id) : armBan(u.id))}
                            >
                              {confirmBanId === u.id ? "Confirmar?" : "Banir"}
                            </Button>
                          )}
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            className="text-muted-foreground hover:text-destructive"
                            disabled={busyId === u.id}
                            onClick={() => setDeleteTarget(u)}
                          >
                            <Trash2 className="size-4" />
                          </Button>
                        </div>
                      )}
                    </li>
                  )
                })}
              </ul>
            )}
          </motion.div>
        </CardContent>
      </Card>

      <DeleteUserModal user={deleteTarget} onClose={() => setDeleteTarget(null)} onDeleted={loadUsers} />
    </div>
  )
}
