import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { useGitHub } from "@/context/GitHubContext"
import { useCountUp } from "@/hooks/use-count-up"
import { api } from "@/lib/api"
import type { ActivityPeriod, GitHubActivityResponse } from "@/lib/types"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"
import { GitHubIcon } from "@/components/icons/GitHubIcon"

const periods: { key: ActivityPeriod; label: string }[] = [
  { key: "TODAY", label: "Hoje" },
  { key: "WEEK", label: "7 dias" },
  { key: "MONTH", label: "30 dias" },
  { key: "QUARTER", label: "90 dias" },
]

export function GitHubActivityCard() {
  const { status, loading: statusLoading, connect } = useGitHub()
  const [activity, setActivity] = useState<GitHubActivityResponse | null>(null)
  const [period, setPeriod] = useState<ActivityPeriod>("WEEK")

  useEffect(() => {
    if (statusLoading || !status?.connected) return
    const controller = new AbortController()
    api
      .get<GitHubActivityResponse>("/github/activity", { signal: controller.signal })
      .then((res) => setActivity(res.data))
      .catch(() => {})
    return () => controller.abort()
  }, [status?.connected, statusLoading])

  const selected = activity?.periods.find((p) => p.period === period)
  const commitCount = useCountUp(selected?.commitCount ?? 0)
  const pullRequestCount = useCountUp(selected?.pullRequestOpenedCount ?? 0)
  const issueCount = useCountUp(selected?.issueClosedCount ?? 0)
  const repoCount = useCountUp(selected?.activeRepoCount ?? 0)

  if (statusLoading) {
    return null
  }

  if (!status?.connected) {
    return (
      <section className="flex items-center gap-4 rounded-2xl border border-dashed px-5 py-4">
        <GitHubIcon className="size-5 shrink-0 text-muted-foreground" />
        <p className="min-w-0 flex-1 text-sm text-muted-foreground">
          Conecte o GitHub para ver commits e PRs ao lado do seu tempo de foco.
        </p>
        <Button type="button" size="sm" variant="outline" onClick={connect}>
          Conectar
        </Button>
      </section>
    )
  }

  return (
    <section className="flex flex-col gap-5 rounded-2xl border p-5 sm:p-6">
      <div>
        <div className="flex items-center justify-between">
          <h2 className="flex items-center gap-2 text-sm font-medium">
            <GitHubIcon className="size-4" />
            GitHub
          </h2>
          <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
            {periods.map((p) => (
              <PillToggleButton
                key={p.key}
                active={period === p.key}
                layoutId="github-card-period-pill"
                onClick={() => setPeriod(p.key)}
                className="h-7 px-2"
              >
                {p.label}
              </PillToggleButton>
            ))}
          </div>
        </div>
      </div>
      <div className="flex flex-col gap-4">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="flex flex-col">
            <span className="text-2xl font-semibold tracking-tight tabular-nums">{Math.round(commitCount)}</span>
            <span className="text-xs text-muted-foreground">commits</span>
          </div>
          <div className="flex flex-col">
            <span className="text-2xl font-semibold tracking-tight tabular-nums">{Math.round(pullRequestCount)}</span>
            <span className="text-xs text-muted-foreground">PRs</span>
          </div>
          <div className="flex flex-col">
            <span className="text-2xl font-semibold tracking-tight tabular-nums">{Math.round(issueCount)}</span>
            <span className="text-xs text-muted-foreground">issues</span>
          </div>
          <div className="flex flex-col">
            <span className="text-2xl font-semibold tracking-tight tabular-nums">{Math.round(repoCount)}</span>
            <span className="text-xs text-muted-foreground">repos</span>
          </div>
        </div>
        <Link to="/github" className="w-fit text-sm text-muted-foreground transition-colors hover:text-foreground">
          Ver insights →
        </Link>
      </div>
    </section>
  )
}
