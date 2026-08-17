import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { motion } from "motion/react"
import { Link } from "react-router-dom"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { useSpotlight } from "@/hooks/useSpotlight"
import { cn, gridItem, gridStagger, INTERACTIVE_CARD_CLASS, SPOTLIGHT_CLASS } from "@/lib/utils"
import type { Roadmap } from "@/lib/types"
import { CardGridSkeleton } from "@/components/CardGridSkeleton"
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
  const { onMouseMove } = useSpotlight()

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
    } catch {
      toast.error("Não foi possível criar o roadmap.")
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <CardGridSkeleton />
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

      <motion.div
        className="grid gap-4 sm:grid-cols-2"
        variants={gridStagger}
        initial="hidden"
        animate="show"
      >
        {roadmaps.map((roadmap) => (
          <motion.div key={roadmap.id} variants={gridItem}>
            <Link to={`/roadmaps/${roadmap.id}`}>
              <Card
                className={cn("h-full", INTERACTIVE_CARD_CLASS, SPOTLIGHT_CLASS)}
                onMouseMove={onMouseMove}
              >
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
          </motion.div>
        ))}
      </motion.div>
    </div>
  )
}
