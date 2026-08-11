import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { api } from "@/lib/api"
import { formatDateTime, formatMinutes, nowForInput } from "@/lib/format"
import type { Category, Session } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

export function SessionsPage() {
  const [sessions, setSessions] = useState<Session[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)

  const [categoryId, setCategoryId] = useState("")
  const [minutes, setMinutes] = useState("")
  const [startedAt, setStartedAt] = useState(nowForInput())
  const [note, setNote] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [newCategory, setNewCategory] = useState("")

  function loadSessions() {
    return api.get<Session[]>("/sessions").then((res) => setSessions(res.data))
  }

  function loadCategories() {
    return api.get<Category[]>("/categories").then((res) => setCategories(res.data))
  }

  useEffect(() => {
    Promise.all([loadSessions(), loadCategories()]).finally(() => setLoading(false))
  }, [])

  async function createCategory() {
    const name = newCategory.trim()
    if (!name) return
    const res = await api.post<Category>("/categories", { name })
    setNewCategory("")
    await loadCategories()
    setCategoryId(res.data.id)
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    if (!categoryId) {
      setError("Escolha uma categoria.")
      return
    }
    setSaving(true)
    try {
      await api.post("/sessions", {
        categoryId,
        focusedMinutes: Number(minutes),
        startedAt: new Date(startedAt).toISOString(),
        note: note.trim() || null,
      })
      setMinutes("")
      setNote("")
      setStartedAt(nowForInput())
      await loadSessions()
    } catch {
      setError("Nao foi possivel registrar a sessao.")
    } finally {
      setSaving(false)
    }
  }

  function categoryName(id: string) {
    return categories.find((c) => c.id === id)?.name ?? "?"
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Sessoes</h1>

      <Card>
        <CardHeader>
          <CardTitle>Registrar sessao</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <Label>Categoria</Label>
              {categories.length > 0 && (
                <Select value={categoryId} onValueChange={setCategoryId}>
                  <SelectTrigger>
                    <SelectValue placeholder="Escolha uma categoria" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((c) => (
                      <SelectItem key={c.id} value={c.id}>
                        {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
              <div className="flex gap-2">
                <Input
                  placeholder="Nova categoria"
                  value={newCategory}
                  onChange={(e) => setNewCategory(e.target.value)}
                />
                <Button type="button" variant="outline" onClick={createCategory}>
                  Criar
                </Button>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="flex flex-col gap-2">
                <Label htmlFor="minutes">Minutos focados</Label>
                <Input
                  id="minutes"
                  type="number"
                  min={1}
                  value={minutes}
                  onChange={(e) => setMinutes(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="startedAt">Quando</Label>
                <Input
                  id="startedAt"
                  type="datetime-local"
                  value={startedAt}
                  onChange={(e) => setStartedAt(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="note">Nota (opcional)</Label>
              <Textarea id="note" value={note} onChange={(e) => setNote(e.target.value)} />
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Registrar"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Minhas sessoes</CardTitle>
        </CardHeader>
        <CardContent>
          {sessions.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma sessao registrada ainda.</p>
          ) : (
            <ul className="flex flex-col divide-y">
              {sessions.map((s) => (
                <li key={s.id} className="flex items-center justify-between gap-4 py-3">
                  <div className="flex flex-col">
                    <span className="font-medium">{formatMinutes(s.focusedMinutes)}</span>
                    <span className="text-sm text-muted-foreground">
                      {categoryName(s.categoryId)} &middot; {formatDateTime(s.startedAt)}
                    </span>
                  </div>
                  {s.note && (
                    <span className="max-w-[50%] truncate text-sm text-muted-foreground">
                      {s.note}
                    </span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
