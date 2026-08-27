import { useEffect, useRef, useState } from "react"
import { motion } from "motion/react"
import { api } from "@/lib/api"
import { useAuth } from "@/context/AuthContext"
import { formatMinutes } from "@/lib/format"
import type { RankingEntry } from "@/lib/types"
import { AdminBadge } from "@/components/AdminBadge"
import { PageSkeleton } from "@/components/PageSkeleton"
import { PillToggleButton } from "@/components/PillToggleButton"
import { UserAvatar } from "@/components/UserAvatar"
import { Card, CardContent } from "@/components/ui/card"

const MEDAL_STYLES: Record<number, string> = {
  1: "bg-amber-400/15 text-amber-500 dark:text-amber-400",
  2: "bg-slate-400/15 text-slate-500 dark:text-slate-300",
  3: "bg-orange-400/15 text-orange-600 dark:text-orange-400",
}

function PositionBadge({ position }: { position: number }) {
  return (
    <span
      className={`flex size-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${
        MEDAL_STYLES[position] ?? "bg-muted text-muted-foreground"
      }`}
    >
      {position}
    </span>
  )
}

const RING_COLORS: Record<number, string> = {
  1: "#f59e0b",
  2: "#94a3b8",
  3: "#f97316",
}

const RING_SIZE = 32
const RING_STROKE = 2.5
const RING_RADIUS = (RING_SIZE - RING_STROKE) / 2
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS

function AvatarRing({ pct, position, children }: { pct: number; position: number; children: React.ReactNode }) {
  const color = RING_COLORS[position] ?? "var(--muted-foreground)"
  return (
    <span className="relative inline-flex shrink-0 items-center justify-center" style={{ width: RING_SIZE, height: RING_SIZE }}>
      <svg width={RING_SIZE} height={RING_SIZE} className="absolute inset-0 -rotate-90">
        <circle
          cx={RING_SIZE / 2}
          cy={RING_SIZE / 2}
          r={RING_RADIUS}
          strokeWidth={RING_STROKE}
          className="stroke-muted"
          fill="none"
        />
        <motion.circle
          cx={RING_SIZE / 2}
          cy={RING_SIZE / 2}
          r={RING_RADIUS}
          strokeWidth={RING_STROKE}
          fill="none"
          strokeLinecap="round"
          stroke={color}
          strokeDasharray={RING_CIRCUMFERENCE}
          initial={{ strokeDashoffset: RING_CIRCUMFERENCE }}
          animate={{ strokeDashoffset: RING_CIRCUMFERENCE * (1 - pct / 100) }}
          transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
        />
      </svg>
      {children}
    </span>
  )
}

const periods = [
  { key: "DAILY", label: "Hoje" },
  { key: "WEEKLY", label: "Semana" },
  { key: "MONTHLY", label: "Mês" },
] as const

type Period = (typeof periods)[number]["key"]

const scopes = [
  { key: "FRIENDS", label: "Amigos" },
  { key: "GLOBAL", label: "Global" },
] as const

type Scope = (typeof scopes)[number]["key"]

export function RankingPage() {
  const { user } = useAuth()
  const [period, setPeriod] = useState<Period>("DAILY")
  const [scope, setScope] = useState<Scope>("FRIENDS")
  const [entries, setEntries] = useState<RankingEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [renderKey, setRenderKey] = useState(0)
  const requestIdRef = useRef(0)
  const cacheRef = useRef<Map<string, RankingEntry[]>>(new Map())

  useEffect(() => {
    const key = `${period}:${scope}`
    const cached = cacheRef.current.get(key)
    if (cached) {
      setEntries(cached)
      setRenderKey((k) => k + 1)
      setLoading(false)
      return
    }
    const requestId = ++requestIdRef.current
    api
      .get<RankingEntry[]>(`/ranking?period=${period}&scope=${scope}`)
      .then((res) => {
        if (requestIdRef.current !== requestId) return
        cacheRef.current.set(key, res.data)
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

  const maxMinutes = entries.reduce((max, e) => Math.max(max, e.minutes), 0)

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
                {entries.map((entry) => {
                  const pct = maxMinutes > 0 ? (entry.minutes / maxMinutes) * 100 : 0
                  const isMe = entry.userId === user?.id
                  return (
                    <li key={entry.userId} className="flex items-center justify-between gap-3 px-6 py-3">
                      <span className="flex min-w-0 items-center gap-3">
                        <PositionBadge position={entry.position} />
                        <AvatarRing pct={pct} position={entry.position}>
                          <UserAvatar userId={entry.userId} name={entry.name} size="sm" />
                        </AvatarRing>
                        <span className="flex min-w-0 items-center gap-1">
                          <span
                            className={`min-w-0 truncate ${isMe ? "font-extrabold" : "font-medium"}`}
                            style={
                              isMe
                                ? { textShadow: "0 0 2px color-mix(in oklch, currentColor 25%, transparent)" }
                                : undefined
                            }
                          >
                            {entry.name}
                          </span>
                          {entry.admin && <AdminBadge />}
                        </span>
                      </span>
                      <span className="shrink-0 text-sm text-muted-foreground">
                        {formatMinutes(entry.minutes)}
                      </span>
                    </li>
                  )
                })}
              </ul>
            )}
          </motion.div>
        </CardContent>
      </Card>
    </div>
  )
}
