import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { api } from "@/lib/api"
import type { Goal } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function GoalsPage() {
  const [daily, setDaily] = useState("")
  const [weekly, setWeekly] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    api
      .get<Goal[]>("/goals")
      .then((res) => {
        const d = res.data.find((g) => g.type === "DAILY")
        const w = res.data.find((g) => g.type === "WEEKLY")
        if (d) setDaily(String(d.targetHours))
        if (w) setWeekly(String(w.targetHours))
      })
      .finally(() => setLoading(false))
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setSaved(false)
    if (daily) {
      await api.put("/goals", { type: "DAILY", targetHours: Number(daily) })
    }
    if (weekly) {
      await api.put("/goals", { type: "WEEKLY", targetHours: Number(weekly) })
    }
    setSaving(false)
    setSaved(true)
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Metas</h1>
      <Card className="max-w-md">
        <CardHeader>
          <CardTitle>Objetivo de horas</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="daily">Meta diária (horas)</Label>
              <Input
                id="daily"
                type="number"
                min={1}
                value={daily}
                onChange={(e) => setDaily(e.target.value)}
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="weekly">Meta semanal (horas)</Label>
              <Input
                id="weekly"
                type="number"
                min={1}
                value={weekly}
                onChange={(e) => setWeekly(e.target.value)}
              />
            </div>
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Salvar"}
            </Button>
            {saved && <p className="text-sm text-emerald-600">Metas salvas.</p>}
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
