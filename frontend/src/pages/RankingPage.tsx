import { useEffect, useRef, useState } from "react"
import { motion } from "motion/react"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { formatMinutes } from "@/lib/format"
import type { RankingEntry } from "@/lib/types"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Card, CardContent } from "@/components/ui/card"

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
  const [renderKey, setRenderKey] = useState(0)
  const requestIdRef = useRef(0)

  useEffect(() => {
    const requestId = ++requestIdRef.current
    api
      .get<RankingEntry[]>(`/ranking?period=${period}&scope=${scope}`)
      .then((res) => {
        if (requestIdRef.current !== requestId) return
        setEntries(res.data)
        setRenderKey((k) => k + 1)
      })
      .finally(() => {
        if (requestIdRef.current === requestId) setLoading(false)
      })
  }, [period, scope])

  if (loading) {
    return <PageSkeleton rows={5} />
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Ranking</h1>

      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
          {periods.map((p) => (
            <PillToggleButton
              key={p.key}
              active={period === p.key}
              layoutId="ranking-period-pill"
              onClick={() => setPeriod(p.key)}
              className="h-7 px-2"
            >
              {p.label}
            </PillToggleButton>
          ))}
        </div>
        <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
          {scopes.map((s) => (
            <PillToggleButton
              key={s.key}
              active={scope === s.key}
              layoutId="ranking-scope-pill"
              onClick={() => setScope(s.key)}
              className="h-7 px-2"
            >
              {s.label}
            </PillToggleButton>
          ))}
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          <motion.div
            key={renderKey}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
          >
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
          </motion.div>
        </CardContent>
      </Card>
    </div>
  )
}
