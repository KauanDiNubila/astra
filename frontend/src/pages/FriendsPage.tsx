import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Check, UserPlus, X } from "lucide-react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import type { Friendship } from "@/lib/types"
import { PageSkeleton } from "@/components/PageSkeleton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function FriendsPage() {
  const [friends, setFriends] = useState<Friendship[]>([])
  const [requests, setRequests] = useState<Friendship[]>([])
  const [loading, setLoading] = useState(true)
  const [email, setEmail] = useState("")
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function loadFriends() {
    return api.get<Friendship[]>("/friends").then((res) => setFriends(res.data))
  }

  function loadRequests() {
    return api.get<Friendship[]>("/friends/requests").then((res) => setRequests(res.data))
  }

  useEffect(() => {
    Promise.all([loadFriends(), loadRequests()]).finally(() => setLoading(false))
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await api.post("/friends", { email })
      setEmail("")
      await Promise.all([loadFriends(), loadRequests()])
    } catch {
      setError("Não foi possível enviar o convite. Confira o e-mail e tente de novo.")
    } finally {
      setSending(false)
    }
  }

  async function accept(id: string) {
    try {
      await api.post(`/friends/${id}/accept`)
      await Promise.all([loadFriends(), loadRequests()])
    } catch {
      toast.error("Não foi possível aceitar o convite.")
    }
  }

  async function remove(id: string) {
    try {
      await api.delete(`/friends/${id}`)
      await Promise.all([loadFriends(), loadRequests()])
    } catch {
      toast.error("Não foi possível concluir a ação.")
    }
  }

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  const received = requests.filter((r) => r.incoming)
  const sent = requests.filter((r) => !r.incoming)

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Amigos</h1>

      <Card>
        <CardHeader>
          <CardTitle>Adicionar amigo</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="flex items-end gap-2">
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="friend-email">E-mail</Label>
              <Input
                id="friend-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <Button type="submit" disabled={sending} className="gap-1.5">
              <UserPlus className="size-4" />
              Convidar
            </Button>
          </form>
          {error && <p className="mt-2 text-sm text-destructive">{error}</p>}
        </CardContent>
      </Card>

      {received.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Pedidos recebidos</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="flex flex-col divide-y">
              {received.map((r) => (
                <li key={r.id} className="flex items-center justify-between py-3">
                  <span className="font-medium">{r.friendName}</span>
                  <div className="flex gap-2">
                    <Button size="sm" variant="outline" className="gap-1.5" onClick={() => accept(r.id)}>
                      <Check className="size-3.5" />
                      Aceitar
                    </Button>
                    <Button
                      size="sm"
                      variant="ghost"
                      className="gap-1.5 text-muted-foreground hover:text-destructive"
                      onClick={() => remove(r.id)}
                    >
                      <X className="size-3.5" />
                      Recusar
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}

      {sent.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Pedidos enviados</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="flex flex-col divide-y">
              {sent.map((r) => (
                <li key={r.id} className="flex items-center justify-between py-3">
                  <span className="font-medium">{r.friendName}</span>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-muted-foreground hover:text-destructive"
                    onClick={() => remove(r.id)}
                  >
                    Cancelar
                  </Button>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Meus amigos</CardTitle>
        </CardHeader>
        <CardContent>
          {friends.length === 0 ? (
            <p className="text-sm text-muted-foreground">Você ainda não tem amigos adicionados.</p>
          ) : (
            <ul className="flex flex-col divide-y">
              {friends.map((f) => (
                <li key={f.id} className="flex items-center justify-between py-3">
                  <span className="font-medium">{f.friendName}</span>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-muted-foreground hover:text-destructive"
                    onClick={() => remove(f.id)}
                  >
                    Remover
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
