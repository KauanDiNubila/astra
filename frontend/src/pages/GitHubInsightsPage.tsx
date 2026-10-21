import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { ArrowUpRight, Lock, RefreshCw } from "lucide-react"
import { motion } from "motion/react"
import { useGitHub } from "@/context/GitHubContext"
import { useCountUp } from "@/hooks/use-count-up"
import { formatRelativeTime } from "@/lib/format"
import { fetchJson, queryKeys } from "@/lib/queryClient"
import { gridItem, gridStagger } from "@/lib/utils"
import type { ActivityPeriod, GitHubActivityResponse, GitHubInsightsResponse } from "@/lib/types"
import { StatGridSkeleton } from "@/components/StatGridSkeleton"
import { GitHubCommitChart } from "@/components/GitHubCommitChart"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"
import { GitHubIcon } from "@/components/icons/GitHubIcon"

const periods: { key: ActivityPeriod; label: string }[] = [
  { key: "TODAY", label: "Hoje" },
  { key: "WEEK", label: "7 dias" },
  { key: "MONTH", label: "30 dias" },
  { key: "QUARTER", label: "90 dias" },
  { key: "YEAR", label: "1 ano" },
]

export function GitHubInsightsPage() {
  const { status, loading: statusLoading, syncing, connect, sync } = useGitHub()
  const [period, setPeriod] = useState<ActivityPeriod>("MONTH")
  const connected = !!status?.connected
  const activityQuery = useQuery({
    queryKey: queryKeys.githubActivity,
    queryFn: fetchJson<GitHubActivityResponse>("/github/activity"),
    enabled: connected,
  })
  const insightsQuery = useQuery({
    queryKey: queryKeys.githubInsights,
    queryFn: fetchJson<GitHubInsightsResponse>("/github/insights"),
    enabled: connected,
  })
  const activity = activityQuery.data ?? null
  const insights = insightsQuery.data ?? null
  const loading = connected && ((activityQuery.isPending && !activityQuery.isError) || (insightsQuery.isPending && !insightsQuery.isError))

  if (statusLoading || loading) {
    return <StatGridSkeleton />
  }

  if (!status?.connected) {
    return (
      <div className="flex flex-col gap-8">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">GitHub</h1>
          <p className="mt-1 text-sm text-muted-foreground">Seu código ao lado do seu tempo de foco.</p>
        </div>
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed px-6 py-16 text-center">
          <GitHubIcon className="size-7 text-muted-foreground" />
          <p className="font-medium">Conecte sua conta do GitHub</p>
          <p className="max-w-md text-sm text-muted-foreground">
            Veja commits, pull requests, issues e linguagens cruzados com o tempo que você estudou.
          </p>
          <Button type="button" className="mt-2 rounded-full" onClick={connect}>
            <GitHubIcon className="size-4" />
            Conectar GitHub
          </Button>
        </div>
      </div>
    )
  }

  const selected = activity?.periods.find((p) => p.period === period)
  const stats = [
    { label: "Commits", value: selected?.commitCount ?? 0, hint: null },
    {
      label: "Pull requests",
      value: selected?.pullRequestOpenedCount ?? 0,
      hint: `${selected?.pullRequestMergedCount ?? 0} com merge`,
    },
    { label: "Issues resolvidas", value: selected?.issueClosedCount ?? 0, hint: null },
    { label: "Repositórios ativos", value: selected?.activeRepoCount ?? 0, hint: null },
  ]
  const series = insights?.series.slice(-90) ?? []
  const contributions = series.reduce((sum, d) => sum + d.contributionCount, 0)

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">GitHub</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {status.login && (
              <a
                href={`https://github.com/${status.login}`}
                target="_blank"
                rel="noopener noreferrer"
                className="font-medium text-foreground transition-colors hover:text-muted-foreground"
              >
                @{status.login}
              </a>
            )}
            {status.lastSyncedAt && ` · sincronizado ${formatRelativeTime(status.lastSyncedAt)}`}
          </p>
        </div>
        <Button type="button" variant="outline" size="sm" className="rounded-full" disabled={syncing} onClick={sync}>
          <RefreshCw className={syncing ? "size-4 animate-spin" : "size-4"} />
          {syncing ? "Sincronizando..." : "Sincronizar"}
        </Button>
      </div>

      {status.lastSyncError && (
        <p className="rounded-xl border border-destructive/40 px-4 py-3 text-sm text-destructive">{status.lastSyncError}</p>
      )}

      <div className="flex flex-col gap-4">
        <div className="flex w-fit flex-wrap gap-1 rounded-md border bg-muted/30 p-1">
          {periods.map((p) => (
            <PillToggleButton
              key={p.key}
              active={period === p.key}
              layoutId="github-period-pill"
              onClick={() => setPeriod(p.key)}
              className="h-7 px-2.5"
            >
              {p.label}
            </PillToggleButton>
          ))}
        </div>
        <motion.div
          variants={gridStagger}
          initial="hidden"
          animate="show"
          className="grid grid-cols-2 gap-px overflow-hidden rounded-2xl border bg-border lg:grid-cols-4"
        >
          {stats.map((stat) => (
            <div key={stat.label} className="bg-background">
              <motion.div variants={gridItem}>
                <Stat label={stat.label} value={stat.value} hint={stat.hint} />
              </motion.div>
            </div>
          ))}
        </motion.div>
      </div>

      <section className="flex flex-col gap-5 rounded-2xl border p-5 sm:p-6">
        <div>
          <h2 className="text-sm font-medium">Atividade</h2>
          <p className="mt-0.5 text-xs text-muted-foreground tabular-nums">
            Últimos 90 dias · {contributions} {contributions === 1 ? "contribuição" : "contribuições"}
          </p>
        </div>
        <GitHubCommitChart data={series} />
      </section>

      <div className="grid items-start gap-6 lg:grid-cols-2">
        <section className="flex min-w-0 flex-col gap-5 rounded-2xl border p-5 sm:p-6">
          <h2 className="text-sm font-medium">Linguagens</h2>
          {insights && insights.languages.length > 0 ? (
            <ul className="flex flex-col gap-4">
              {insights.languages.map((lang) => (
                <li key={lang.language} className="flex flex-col gap-2">
                  <div className="flex items-center justify-between text-sm">
                    <span>{lang.language}</span>
                    <span className="text-muted-foreground tabular-nums">{Math.round(lang.percentage)}%</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-muted">
                    <motion.div
                      className="h-full rounded-full bg-foreground/80"
                      initial={{ width: 0 }}
                      animate={{ width: `${lang.percentage}%` }}
                      transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
                    />
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">Sem dados de linguagem ainda.</p>
          )}
        </section>

        <section className="flex min-w-0 flex-col gap-3 rounded-2xl border p-5 sm:p-6">
          <h2 className="text-sm font-medium">Repositórios</h2>
          {insights && insights.repositories.length > 0 ? (
            <ul className="-mx-2 flex flex-col">
              {insights.repositories.map((repo) => (
                <li key={repo.id}>
                  <a
                    href={repo.htmlUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="group flex items-center justify-between gap-3 rounded-lg px-2 py-2.5 text-sm transition-colors hover:bg-muted/40"
                  >
                    <span className="flex min-w-0 flex-col">
                      <span className="flex items-center gap-1.5 truncate font-medium">
                        {repo.name}
                        {repo.isPrivate && <Lock className="size-3 shrink-0 text-muted-foreground" />}
                      </span>
                      <span className="text-xs text-muted-foreground">{repo.primaryLanguage ?? "—"}</span>
                    </span>
                    <span className="flex shrink-0 items-center gap-2 text-xs text-muted-foreground tabular-nums">
                      {repo.recentCommitCount} commits
                      <ArrowUpRight className="size-3.5 opacity-0 transition-opacity group-hover:opacity-100" />
                    </span>
                  </a>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-muted-foreground">Nenhum repositório sincronizado ainda.</p>
          )}
        </section>
      </div>
    </div>
  )
}

function Stat({ label, value, hint }: { label: string; value: number; hint: string | null }) {
  const animated = Math.round(useCountUp(value))
  return (
    <div className="flex flex-col gap-1.5 p-5">
      <span className="text-xs font-medium tracking-wide text-muted-foreground uppercase">{label}</span>
      <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">{animated.toLocaleString("pt-BR")}</span>
      <span className="min-h-4 text-xs text-muted-foreground">{hint}</span>
    </div>
  )
}
