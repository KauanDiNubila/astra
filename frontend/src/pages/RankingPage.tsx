import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { formatMinutes } from "@/lib/format"
import type { RankingEntry } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { PageSkeleton } from "@/components/PageSkeleton"

const periods = [
  { key: "DAILY", label: "Hoje" },
  { key: "WEEKLY", label: "Semana" },
  { key: "MONTHLY", label: "Mês" },
] as const

type Period = (typeof periods)[number]["key"]

const scopes = [
  { key: "GLOBAL", label: "Global" },
  { key: "FRIENDS", label: "Amigos" },
] as const

type Scope = (typeof scopes)[number]["key"]

export function RankingPage() {
  const { user } = useAuth()
  const [period, setPeriod] = useState<Period>("DAILY")
  const [scope, setScope] = useState<Scope>("GLOBAL")
  const [entries, setEntries] = useState<RankingEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api
      .get<RankingEntry[]>(`/ranking?period=${period}&scope=${scope}`)
      .then((res) => setEntries(res.data))
      .finally(() => setLoading(false))
  }, [period, scope])

  if (loading) {
    return <PageSkeleton rows={5} />
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Ranking</h1>

      <div className="flex flex-wrap items-center justify-between gap-2">
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
        <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
          {scopes.map((s) => (
            <Button
              key={s.key}
              variant={scope === s.key ? "default" : "ghost"}
              size="sm"
              className="h-7 px-2"
              onClick={() => setScope(s.key)}
            >
              {s.label}
            </Button>
          ))}
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          {entries.length === 0 ? (
            <p className="p-6 text-sm text-muted-foreground">
              {scope === "FRIENDS"
                ? "Ninguém do seu grupo de amigos registrou tempo neste período ainda."
                : "Ninguém registrou tempo neste período."}
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
