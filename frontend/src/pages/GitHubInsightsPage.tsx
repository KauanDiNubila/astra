import { useEffect, useState } from "react"
import { RefreshCw } from "lucide-react"
import { motion } from "motion/react"
import { useGitHub } from "@/context/GitHubContext"
import { api } from "@/lib/api"
import { formatRelativeTime } from "@/lib/format"
import { gridItem, gridStagger } from "@/lib/utils"
import type { ActivityPeriod, GitHubActivityResponse, GitHubInsightsResponse } from "@/lib/types"
import { StatCard } from "@/components/StatCard"
import { StatGridSkeleton } from "@/components/StatGridSkeleton"
import { GitHubCommitChart } from "@/components/GitHubCommitChart"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const periods: { key: ActivityPeriod; label: string }[] = [
  { key: "TODAY", label: "Hoje" },
  { key: "WEEK", label: "7 dias" },
  { key: "MONTH", label: "30 dias" },
  { key: "QUARTER", label: "90 dias" },
  { key: "YEAR", label: "1 ano" },
]

export function GitHubInsightsPage() {
  const { status, loading: statusLoading, syncing, connect, sync } = useGitHub()
  const [activity, setActivity] = useState<GitHubActivityResponse | null>(null)
  const [insights, setInsights] = useState<GitHubInsightsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [period, setPeriod] = useState<ActivityPeriod>("MONTH")

  useEffect(() => {
    if (statusLoading) return
    if (!status?.connected) {
      setLoading(false)
      return
    }
    const controller = new AbortController()
    Promise.all([
      api.get<GitHubActivityResponse>("/github/activity", { signal: controller.signal }).then((res) => setActivity(res.data)),
      api.get<GitHubInsightsResponse>("/github/insights", { signal: controller.signal }).then((res) => setInsights(res.data)),
    ])
      .catch(() => {})
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [status?.connected, statusLoading])

  async function handleSync() {
    await sync()
    const [activityRes, insightsRes] = await Promise.all([
      api.get<GitHubActivityResponse>("/github/activity"),
      api.get<GitHubInsightsResponse>("/github/insights"),
    ])
    setActivity(activityRes.data)
    setInsights(insightsRes.data)
  }

  if (statusLoading || loading) {
    return <StatGridSkeleton />
  }

  if (!status?.connected) {
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-semibold">GitHub Insights</h1>
        <Card>
          <CardContent className="flex flex-col items-center gap-4 py-12 text-center">
            <p className="text-muted-foreground">
              Conecte sua conta do GitHub pra ver commits, pull requests, issues e linguagens cruzados com seu tempo
              de estudo.
            </p>
            <Button type="button" onClick={connect}>
              Conectar GitHub
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const selected = activity?.periods.find((p) => p.period === period)

  return (
    <div className="flex flex-col gap-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">GitHub Insights</h1>
        <div className="flex items-center gap-3">
          {status.lastSyncedAt && (
            <span className="text-xs text-muted-foreground">
              Última sincronização: {formatRelativeTime(status.lastSyncedAt)}
            </span>
          )}
          <Button type="button" variant="outline" size="sm" disabled={syncing} onClick={handleSync}>
            <RefreshCw className={syncing ? "size-4 animate-spin" : "size-4"} />
            {syncing ? "Sincronizando..." : "Sincronizar agora"}
          </Button>
        </div>
      </div>

      {status.lastSyncError && (
        <p className="text-sm text-destructive">{status.lastSyncError}</p>
      )}

      <div className="flex gap-1 self-start rounded-md border bg-muted/30 p-1">
        {periods.map((p) => (
          <PillToggleButton
            key={p.key}
            active={period === p.key}
            layoutId="github-period-pill"
            onClick={() => setPeriod(p.key)}
            className="h-7 px-2"
          >
            {p.label}
          </PillToggleButton>
        ))}
      </div>

      <motion.div
        className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
        variants={gridStagger}
        initial="hidden"
        animate="show"
      >
        <motion.div variants={gridItem}>
          <StatCard label="Commits" value={selected?.commitCount ?? 0} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard
            label="Pull Requests"
            value={selected?.pullRequestOpenedCount ?? 0}
            format={(n) => `${Math.round(n)} (${selected?.pullRequestMergedCount ?? 0} merged)`}
          />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Issues resolvidas" value={selected?.issueClosedCount ?? 0} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Repositórios ativos" value={selected?.activeRepoCount ?? 0} />
        </motion.div>
      </motion.div>

      <Card>
        <CardHeader>
          <CardTitle>Atividade (últimos 90 dias)</CardTitle>
        </CardHeader>
        <CardContent>
          <GitHubCommitChart data={insights?.series.slice(-90) ?? []} />
        </CardContent>
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Linguagens</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {insights && insights.languages.length > 0 ? (
              insights.languages.map((lang) => (
                <div key={lang.language} className="flex flex-col gap-1">
                  <div className="flex items-center justify-between text-sm">
                    <span>{lang.language}</span>
                    <span className="text-muted-foreground">{Math.round(lang.percentage)}%</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-muted">
                    <div className="h-full rounded-full bg-primary" style={{ width: `${lang.percentage}%` }} />
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm text-muted-foreground">Sem dados de linguagem ainda.</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Repositórios</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-2">
            {insights && insights.repositories.length > 0 ? (
              insights.repositories.map((repo) => (
                <a
                  key={repo.id}
                  href={repo.htmlUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-between rounded-md px-2 py-1.5 text-sm transition-colors hover:bg-muted/50"
                >
                  <span className="flex min-w-0 flex-col">
                    <span className="truncate font-medium">{repo.name}</span>
                    <span className="text-xs text-muted-foreground">{repo.primaryLanguage ?? "—"}</span>
                  </span>
                  <span className="shrink-0 text-xs text-muted-foreground">{repo.recentCommitCount} commits</span>
                </a>
              ))
            ) : (
              <p className="text-sm text-muted-foreground">Nenhum repositório sincronizado ainda.</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
