import { useEffect, useMemo, useState } from "react"
import { Pencil } from "lucide-react"
import { motion } from "motion/react"
import { api } from "@/lib/api"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatMinutes } from "@/lib/format"
import { gridItem, gridStagger } from "@/lib/utils"
import type { Dashboard, DailyMinutes, CategoryMinutes } from "@/lib/types"
import { StatCard } from "@/components/StatCard"
import { StatGridSkeleton } from "@/components/StatGridSkeleton"
import { Heatmap } from "@/components/Heatmap"
import { FocusTrendChart } from "@/components/FocusTrendChart"
import { CategoryDonutChart } from "@/components/CategoryDonutChart"
import { GoalRadialChart } from "@/components/GoalRadialChart"
import { EditGoalsModal } from "@/components/EditGoalsModal"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

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

function localDayKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}

function lastNDays(n: number): string[] {
  const days: string[] = []
  const today = new Date()
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    days.push(localDayKey(d))
  }
  return days
}

export function DashboardPage() {
  const { categories } = usePomodoro()
  const [data, setData] = useState<Dashboard | null>(null)
  const [heatmap, setHeatmap] = useState<DailyMinutes[]>([])
  const [categoryMinutes, setCategoryMinutes] = useState<CategoryMinutes[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [trendPeriod, setTrendPeriod] = useState<TrendPeriodDays>(30)
  const [categoryPeriod, setCategoryPeriod] = useState<CategoryPeriodDays>(30)
  const [editGoalsOpen, setEditGoalsOpen] = useState(false)

  function loadDashboard(signal?: AbortSignal) {
    return api.get<Dashboard>("/dashboard", { signal }).then((res) => setData(res.data))
  }

  useEffect(() => {
    const controller = new AbortController()
    const { signal } = controller
    // Categorias vêm do PomodoroContext — evita refazer a mesma busca aqui.
    Promise.all([
      loadDashboard(signal),
      api.get<DailyMinutes[]>("/heatmap", { signal }).then((res) => setHeatmap(res.data)),
    ])
      .catch(() => {
        if (!signal.aborted) setError(true)
      })
      .finally(() => {
        if (!signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    api
      .get<CategoryMinutes[]>("/dashboard/by-category", {
        params: { days: categoryPeriod },
        signal: controller.signal,
      })
      .then((res) => setCategoryMinutes(res.data))
      .catch(() => {})
    return () => controller.abort()
  }, [categoryPeriod])

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

  if (loading) {
    return <StatGridSkeleton />
  }

  if (error || !data) {
    return <p className="text-destructive">Não foi possível carregar o dashboard.</p>
  }

  const dailyGoal = data.goals.find((g) => g.type === "DAILY")
  const weeklyGoal = data.goals.find((g) => g.type === "WEEKLY")

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Dashboard</h1>

      <motion.div
        className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
        variants={gridStagger}
        initial="hidden"
        animate="show"
      >
        <motion.div variants={gridItem}>
          <StatCard label="Hoje" value={data.todayMinutes} format={(n) => formatMinutes(Math.round(n))} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Esta semana" value={data.weekMinutes} format={(n) => formatMinutes(Math.round(n))} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Total" value={data.totalMinutes} format={(n) => formatMinutes(Math.round(n))} />
        </motion.div>
        <motion.div variants={gridItem}>
          <StatCard label="Streak" value={data.currentStreak} format={(n) => `${Math.round(n)} dia(s)`} />
        </motion.div>
      </motion.div>

      <Card>
        <CardHeader>
          <CardTitle>Atividade (último ano)</CardTitle>
        </CardHeader>
        <CardContent>
          <Heatmap data={heatmap} />
        </CardContent>
      </Card>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Tempo de foco</CardTitle>
              <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
                {trendPeriods.map((p) => (
                  <PillToggleButton
                    key={p.key}
                    active={trendPeriod === p.key}
                    layoutId="trend-period-pill"
                    onClick={() => setTrendPeriod(p.key)}
                    className="h-7 px-2"
                  >
                    {p.label}
                  </PillToggleButton>
                ))}
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <FocusTrendChart key={trendPeriod} data={trendData} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Tempo por categoria</CardTitle>
              <div className="flex gap-1 rounded-md border bg-muted/30 p-1">
                {categoryPeriods.map((p) => (
                  <PillToggleButton
                    key={p.key}
                    active={categoryPeriod === p.key}
                    layoutId="category-period-pill"
                    onClick={() => setCategoryPeriod(p.key)}
                    className="h-7 px-2"
                  >
                    {p.label}
                  </PillToggleButton>
                ))}
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <CategoryDonutChart key={categoryPeriod} data={categoryData} />
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Metas</CardTitle>
            <Button variant="ghost" size="icon" title="Editar metas" onClick={() => setEditGoalsOpen(true)}>
              <Pencil className="size-4" />
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 sm:grid-cols-2">
            <GoalRadialChart
              label="diária"
              achievedHours={dailyGoal?.achievedHours ?? 0}
              targetHours={dailyGoal?.targetHours ?? 0}
            />
            <GoalRadialChart
              label="semanal"
              achievedHours={weeklyGoal?.achievedHours ?? 0}
              targetHours={weeklyGoal?.targetHours ?? 0}
            />
          </div>
        </CardContent>
      </Card>

      <EditGoalsModal
        open={editGoalsOpen}
        onClose={() => setEditGoalsOpen(false)}
        dailyTarget={dailyGoal?.targetHours ?? 0}
        weeklyTarget={weeklyGoal?.targetHours ?? 0}
        onSaved={loadDashboard}
      />
    </div>
  )
}
