import { useEffect, useState } from "react"
import { Check, X } from "lucide-react"
import { Link } from "react-router-dom"
import { api } from "@/lib/api"
import { formatMinutes } from "@/lib/format"
import type { Dashboard, DailyMinutes } from "@/lib/types"
import { StatCard } from "@/components/StatCard"
import { Heatmap } from "@/components/Heatmap"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null)
  const [heatmap, setHeatmap] = useState<DailyMinutes[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    Promise.all([
      api.get<Dashboard>("/dashboard").then((res) => setData(res.data)),
      api.get<DailyMinutes[]>("/heatmap").then((res) => setHeatmap(res.data)),
    ])
      .catch(() => setError(true))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  if (error || !data) {
    return <p className="text-destructive">Não foi possível carregar o dashboard.</p>
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Dashboard</h1>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Hoje" value={formatMinutes(data.todayMinutes)} />
        <StatCard label="Esta semana" value={formatMinutes(data.weekMinutes)} />
        <StatCard label="Total" value={formatMinutes(data.totalMinutes)} />
        <StatCard label="Streak" value={`${data.currentStreak} dia(s)`} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Atividade (último ano)</CardTitle>
        </CardHeader>
        <CardContent>
          <Heatmap data={heatmap} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Metas</CardTitle>
            <Link to="/goals" className="text-sm underline underline-offset-4">
              Definir metas
            </Link>
          </div>
        </CardHeader>
        <CardContent>
          {data.goals.length === 0 ? (
            <p className="text-sm text-muted-foreground">Você ainda não definiu metas.</p>
          ) : (
            <ul className="flex flex-col gap-3">
              {data.goals.map((goal) => (
                <li key={goal.type} className="flex items-center justify-between">
                  <span className="text-sm">
                    Meta {goal.type === "DAILY" ? "diária" : "semanal"}:{" "}
                    <span className="font-medium">{goal.achievedHours.toFixed(1)}h</span> / {goal.targetHours}h
                  </span>
                  {goal.reached ? (
                    <span className="flex items-center gap-1 text-sm text-emerald-600">
                      <Check className="size-4" /> Batida
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-sm text-muted-foreground">
                      <X className="size-4" /> Em andamento
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
