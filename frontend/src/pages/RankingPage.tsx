import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { formatMinutes } from "@/lib/format"
import type { RankingEntry } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"

const periods = [
  { key: "DAILY", label: "Hoje" },
  { key: "WEEKLY", label: "Semana" },
  { key: "MONTHLY", label: "Mês" },
] as const

type Period = (typeof periods)[number]["key"]

export function RankingPage() {
  const { user } = useAuth()
  const [period, setPeriod] = useState<Period>("DAILY")
  const [entries, setEntries] = useState<RankingEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api
      .get<RankingEntry[]>(`/ranking?period=${period}`)
      .then((res) => setEntries(res.data))
      .finally(() => setLoading(false))
  }, [period])

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Ranking</h1>

      <div className="flex gap-2">
        {periods.map((p) => (
          <Button
            key={p.key}
            variant={period === p.key ? "default" : "outline"}
            size="sm"
            onClick={() => setPeriod(p.key)}
          >
            {p.label}
          </Button>
        ))}
      </div>

      <Card>
        <CardContent className="p-0">
          {loading ? (
            <p className="p-6 text-muted-foreground">Carregando...</p>
          ) : entries.length === 0 ? (
            <p className="p-6 text-sm text-muted-foreground">
              Ninguém registrou tempo neste período.
            </p>
          ) : (
            <ul className="divide-y">
              {entries.map((entry) => (
                <li
                  key={entry.userId}
                  className={`flex items-center justify-between px-6 py-3 ${
                    entry.userId === user?.id ? "bg-muted/50" : ""
                  }`}
                >
                  <span className="flex items-center gap-3">
                    <span className="w-6 text-muted-foreground">{entry.position}</span>
                    <span className="font-medium">{entry.name}</span>
                  </span>
                  <span className="text-sm text-muted-foreground">
                    {formatMinutes(entry.minutes)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
