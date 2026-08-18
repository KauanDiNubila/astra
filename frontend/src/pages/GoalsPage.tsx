import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import type { Goal } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Stepper } from "@/components/ui/stepper"
import { PageSkeleton } from "@/components/PageSkeleton"

export function GoalsPage() {
  const [daily, setDaily] = useState(0)
  const [weekly, setWeekly] = useState(0)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api
      .get<Goal[]>("/goals")
      .then((res) => {
        const d = res.data.find((g) => g.type === "DAILY")
        const w = res.data.find((g) => g.type === "WEEKLY")
        if (d) setDaily(d.targetHours)
        if (w) setWeekly(w.targetHours)
      })
      .finally(() => setLoading(false))
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      if (daily > 0) {
        await api.put("/goals", { type: "DAILY", targetHours: daily })
      }
      if (weekly > 0) {
        await api.put("/goals", { type: "WEEKLY", targetHours: weekly })
      }
      toast.success("Metas salvas.")
    } catch {
      toast.error("Não foi possível salvar as metas.")
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <PageSkeleton rows={2} />
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
              <Stepper
                id="daily"
                min={0}
                max={24}
                value={daily}
                onChange={setDaily}
                aria-label="Meta diária em horas"
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="weekly">Meta semanal (horas)</Label>
              <Stepper
                id="weekly"
                min={0}
                max={168}
                value={weekly}
                onChange={setWeekly}
                aria-label="Meta semanal em horas"
              />
            </div>
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando..." : "Salvar"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
