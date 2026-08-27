import { useState } from "react"
import type { FormEvent } from "react"
import { Check, UserPlus, X } from "lucide-react"
import { toast } from "sonner"
import { AdminBadge } from "@/components/AdminBadge"
import { useFriends } from "@/context/FriendsContext"
import { PageSkeleton } from "@/components/PageSkeleton"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function FriendsPage() {
  const { friends, requests, loading, sendRequest, acceptRequest, removeFriendship } = useFriends()
  const [email, setEmail] = useState("")
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSending(true)
    try {
      await sendRequest(email)
      setEmail("")
    } catch {
      setError("Não foi possível enviar o convite. Confira o e-mail e tente de novo.")
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
                type="text"
                inputMode="email"
                autoComplete="email"
                pattern="[^\s@]+@[^\s@]+\.[^\s@]+"
                title="Digite um e-mail válido"
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
                  <span className="flex items-center gap-2">
                    <UserAvatar userId={r.friendUserId} name={r.friendName} size="sm" />
                    <span className="flex items-center gap-1 font-medium">
                      {r.friendName}
                      {r.friendAdmin && <AdminBadge />}
                    </span>
                  </span>
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
                  <span className="flex items-center gap-2">
                    <UserAvatar userId={r.friendUserId} name={r.friendName} size="sm" />
                    <span className="flex items-center gap-1 font-medium">
                      {r.friendName}
                      {r.friendAdmin && <AdminBadge />}
                    </span>
                  </span>
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
                  <span className="flex items-center gap-2">
                    <UserAvatar userId={f.friendUserId} name={f.friendName} size="sm" />
                    <span className="flex flex-col">
                      <span className="flex items-center gap-1 font-medium">
                        {f.friendName}
                        {f.friendAdmin && <AdminBadge />}
                      </span>
                      {f.friendBio && (
                        <span className="text-xs text-muted-foreground">{f.friendBio}</span>
                      )}
                    </span>
                  </span>
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
