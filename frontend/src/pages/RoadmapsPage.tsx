import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link } from "react-router-dom"
import { api } from "@/lib/api"
import { cn, INTERACTIVE_CARD_CLASS } from "@/lib/utils"
import type { Roadmap } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardHeader, CardTitle } from "@/components/ui/card"
import { CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

export function RoadmapsPage() {
  const [roadmaps, setRoadmaps] = useState<Roadmap[]>([])
  const [loading, setLoading] = useState(true)
  const [title, setTitle] = useState("")
  const [saving, setSaving] = useState(false)

  function load() {
    return api.get<Roadmap[]>("/roadmaps").then((res) => setRoadmaps(res.data))
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) return
    setSaving(true)
    try {
      await api.post("/roadmaps", { title: title.trim(), source: null })
      setTitle("")
      await load()
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Roadmaps</h1>

      <Card>
        <CardHeader>
          <CardTitle>Novo roadmap</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="flex gap-2">
            <Input
              placeholder="Título do roadmap"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
            <Button type="submit" disabled={saving}>
              Criar
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2">
        {roadmaps.map((roadmap) => (
          <Link key={roadmap.id} to={`/roadmaps/${roadmap.id}`}>
            <Card className={cn("h-full", INTERACTIVE_CARD_CLASS)}>
              <CardHeader>
                <div className="flex items-center justify-between gap-2">
                  <CardTitle className="text-base">{roadmap.title}</CardTitle>
                  {roadmap.predefined && <Badge variant="secondary">Pré-definido</Badge>}
                </div>
                {roadmap.source && (
                  <p className="text-sm text-muted-foreground">Fonte: {roadmap.source}</p>
                )}
              </CardHeader>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
