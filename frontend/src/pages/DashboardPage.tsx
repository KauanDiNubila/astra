import { useMemo, useState } from "react"
import type { ReactNode } from "react"
import { keepPreviousData, useQuery } from "@tanstack/react-query"
import { fetchJson, invalidateGoals, queryKeys } from "@/lib/queryClient"
import { useGitHub } from "@/context/GitHubContext"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatMinutes } from "@/lib/format"
import { averagePerDay, dayKey, longestStreak, previousWeekSoFar } from "@/lib/studyStats"
import type { Dashboard, DailyMinutes, CategoryMinutes, GitHubDailyPoint } from "@/lib/types"
import { StatGridSkeleton } from "@/components/StatGridSkeleton"
import { GitHubActivityCard } from "@/components/GitHubActivityCard"
import { Heatmap } from "@/components/Heatmap"
import { FocusTrendChart } from "@/components/FocusTrendChart"
import { EditGoalsModal } from "@/components/EditGoalsModal"
import { PillToggleButton } from "@/components/PillToggleButton"
import { CategoryBreakdown } from "@/components/dashboard/CategoryBreakdown"
import { DayHero } from "@/components/dashboard/DayHero"
import { StatStrip } from "@/components/dashboard/StatStrip"

const trendPeriods = [
  { key: 7, label: "7 dias" },
  { key: 30, label: "30 dias" },
  { key: 90, label: "90 dias" },
] as const

const categoryPeriods = [
  { key: 1, label: "Hoje" },
  { key: 7, label: "7 dias" },
  { key: 30, label: "30 dias" },
  { key: 90, label: "90 dias" },
] as const

type TrendPeriodDays = (typeof trendPeriods)[number]["key"]
type CategoryPeriodDays = (typeof categoryPeriods)[number]["key"]

function lastNDays(n: number): string[] {
  const days: string[] = []
  const today = new Date()
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    days.push(dayKey(d))
  }
  return days
}

export function DashboardPage() {
  const { categories } = usePomodoro()
  const { status: githubStatus } = useGitHub()
  const [trendPeriod, setTrendPeriod] = useState<TrendPeriodDays>(30)
  const [categoryPeriod, setCategoryPeriod] = useState<CategoryPeriodDays>(30)
  const [editGoalsOpen, setEditGoalsOpen] = useState(false)

  const dashboardQuery = useQuery({ queryKey: queryKeys.dashboard, queryFn: fetchJson<Dashboard>("/dashboard") })
  const heatmapQuery = useQuery({ queryKey: queryKeys.heatmap, queryFn: fetchJson<DailyMinutes[]>("/heatmap") })
  const githubQuery = useQuery({
    queryKey: queryKeys.githubInsights,
    queryFn: fetchJson<{ series: GitHubDailyPoint[] }>("/github/insights"),
    enabled: !!githubStatus?.connected,
  })
  const categoryQuery = useQuery({
    queryKey: queryKeys.byCategory(categoryPeriod),
    queryFn: fetchJson<CategoryMinutes[]>("/dashboard/by-category", { days: categoryPeriod }),
    placeholderData: keepPreviousData,
  })

  const data = dashboardQuery.data ?? null
  const heatmap = useMemo(() => heatmapQuery.data ?? [], [heatmapQuery.data])
  const githubHeatmap = githubStatus?.connected ? (githubQuery.data?.series ?? []) : []
  const categoryMinutes = useMemo(() => categoryQuery.data ?? [], [categoryQuery.data])
  const loading = dashboardQuery.isPending || heatmapQuery.isPending
  const error = (dashboardQuery.isError && !dashboardQuery.data) || (heatmapQuery.isError && !heatmapQuery.data)

  const trendData = useMemo(() => {
    const byDay = new Map(heatmap.map((d) => [d.day, d.minutes]))
    return lastNDays(trendPeriod).map((day) => ({ day, minutes: byDay.get(day) ?? 0 }))
  }, [heatmap, trendPeriod])

  const categoryData = useMemo(() => {
    const minutesByCategory = new Map(categoryMinutes.map((c) => [c.categoryId, c.minutes]))
    return categories.map((c) => ({
      categoryId: c.id,
      name: c.name,
      minutes: minutesByCategory.get(c.id) ?? 0,
      color: c.color,
    }))
  }, [categoryMinutes, categories])

  const derived = useMemo(
    () => ({
      previousWeek: previousWeekSoFar(heatmap),
      dailyAverage: averagePerDay(heatmap, 30),
      bestStreak: longestStreak(heatmap),
    }),
    [heatmap],
  )

  const trendTotal = trendData.reduce((sum, d) => sum + d.minutes, 0)
  const categoryTotal = categoryData.reduce((sum, c) => sum + c.minutes, 0)

  if (loading) {
    return <StatGridSkeleton />
  }

  if (error || !data) {
    return <p className="text-destructive">Não foi possível carregar o dashboard.</p>
  }

  const dailyGoal = data.goals.find((g) => g.type === "DAILY")
  const weeklyGoal = data.goals.find((g) => g.type === "WEEKLY")

  return (
    <div className="flex flex-col gap-6">
      <DayHero todayMinutes={data.todayMinutes} goals={data.goals} onEditGoals={() => setEditGoalsOpen(true)} />

      <div className="mt-4">
        <StatStrip
          weekMinutes={data.weekMinutes}
          previousWeekMinutes={derived.previousWeek}
          totalMinutes={data.totalMinutes}
          dailyAverage={derived.dailyAverage}
          streak={data.currentStreak}
          bestStreak={derived.bestStreak}
        />
      </div>

      <Panel title="Atividade" summary="Último ano">
        <Heatmap data={heatmap} githubData={githubHeatmap} />
      </Panel>

      <div className="grid gap-6 lg:grid-cols-2">
        <Panel
          title="Tempo de foco"
          summary={trendTotal > 0 ? `${formatMinutes(trendTotal)} · média de ${formatMinutes(Math.round(trendTotal / trendPeriod))} por dia` : undefined}
          actions={
            <PeriodToggle
              periods={trendPeriods}
              value={trendPeriod}
              onChange={setTrendPeriod}
              layoutId="trend-period-pill"
            />
          }
        >
          <FocusTrendChart key={trendPeriod} data={trendData} />
        </Panel>

        <Panel
          title="Por categoria"
          summary={categoryTotal > 0 ? `${formatMinutes(categoryTotal)} no período` : undefined}
          actions={
            <PeriodToggle
              periods={categoryPeriods}
              value={categoryPeriod}
              onChange={setCategoryPeriod}
              layoutId="category-period-pill"
            />
          }
        >
          <CategoryBreakdown key={categoryPeriod} data={categoryData} />
        </Panel>
      </div>

      <GitHubActivityCard />

      <EditGoalsModal
        open={editGoalsOpen}
        onClose={() => setEditGoalsOpen(false)}
        dailyTarget={dailyGoal?.targetHours ?? 0}
        weeklyTarget={weeklyGoal?.targetHours ?? 0}
        onSaved={invalidateGoals}
      />
    </div>
  )
}

function Panel({
  title,
  summary,
  actions,
  children,
}: {
  title: string
  summary?: string
  actions?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="flex min-w-0 flex-col gap-5 rounded-2xl border p-5 sm:p-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-sm font-medium">{title}</h2>
          {summary && <p className="mt-0.5 text-xs text-muted-foreground tabular-nums">{summary}</p>}
        </div>
        {actions}
      </div>
      {children}
    </section>
  )
}

function PeriodToggle<T extends number>({
  periods,
  value,
  onChange,
  layoutId,
}: {
  periods: readonly { key: T; label: string }[]
  value: T
  onChange: (value: T) => void
  layoutId: string
}) {
  return (
    <div className="flex w-fit gap-1 rounded-md border bg-muted/30 p-1">
      {periods.map((p) => (
        <PillToggleButton
          key={p.key}
          active={value === p.key}
          layoutId={layoutId}
          onClick={() => onChange(p.key)}
          className="h-7 px-2 max-sm:h-9 max-sm:px-3"
        >
          {p.label}
        </PillToggleButton>
      ))}
    </div>
  )
}
