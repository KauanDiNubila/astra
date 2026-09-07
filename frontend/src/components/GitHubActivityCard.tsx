import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { useGitHub } from "@/context/GitHubContext"
import { useCountUp } from "@/hooks/use-count-up"
import { api } from "@/lib/api"
import type { ActivityPeriod, GitHubActivityResponse } from "@/lib/types"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

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
      <Card>
        <CardHeader>
          <CardTitle>GitHub</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center justify-between gap-4">
          <p className="text-sm text-muted-foreground">Conecte o GitHub pra ver sua atividade aqui.</p>
          <Button type="button" size="sm" variant="outline" onClick={connect}>
            Conectar
          </Button>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>GitHub</CardTitle>
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
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="flex flex-col">
            <span className="text-lg font-semibold tabular-nums">{Math.round(commitCount)}</span>
            <span className="text-xs text-muted-foreground">commits</span>
          </div>
          <div className="flex flex-col">
            <span className="text-lg font-semibold tabular-nums">{Math.round(pullRequestCount)}</span>
            <span className="text-xs text-muted-foreground">PRs</span>
          </div>
          <div className="flex flex-col">
            <span className="text-lg font-semibold tabular-nums">{Math.round(issueCount)}</span>
            <span className="text-xs text-muted-foreground">issues</span>
          </div>
          <div className="flex flex-col">
            <span className="text-lg font-semibold tabular-nums">{Math.round(repoCount)}</span>
            <span className="text-xs text-muted-foreground">repos</span>
          </div>
        </div>
        <Link to="/github" className="text-sm text-primary hover:underline">
          Ver insights →
        </Link>
      </CardContent>
    </Card>
  )
}
