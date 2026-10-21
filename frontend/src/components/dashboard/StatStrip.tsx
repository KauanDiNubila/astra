import { ArrowDownRight, ArrowUpRight } from "lucide-react"
import { motion } from "motion/react"
import { useCountUp } from "@/hooks/use-count-up"
import { formatMinutes } from "@/lib/format"
import { formatDays } from "@/lib/studyStats"
import { gridItem, gridStagger } from "@/lib/utils"
import type { ReactNode } from "react"

function Stat({ label, value, children }: { label: string; value: string; children: ReactNode }) {
  return (
    <motion.div variants={gridItem} className="flex flex-col gap-1.5 p-5">
      <span className="text-xs font-medium tracking-wide text-muted-foreground uppercase">{label}</span>
      <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">{value}</span>
      <span className="text-xs text-muted-foreground">{children}</span>
    </motion.div>
  )
}

function WeekDelta({ current, previous }: { current: number; previous: number }) {
  if (previous === 0) {
    return <>{current > 0 ? "Sem estudo na semana passada até hoje" : "Nada ainda nesta semana"}</>
  }
  const pct = Math.round(((current - previous) / previous) * 100)
  const up = pct >= 0
  const Icon = up ? ArrowUpRight : ArrowDownRight
  return (
    <span className="inline-flex items-center gap-1">
      <span className={up ? "inline-flex items-center text-emerald-500" : "inline-flex items-center text-foreground/70"}>
        <Icon className="size-3.5" />
        {Math.abs(pct)}%
      </span>
      vs. semana passada
    </span>
  )
}

export function StatStrip({
  weekMinutes,
  previousWeekMinutes,
  totalMinutes,
  dailyAverage,
  streak,
  bestStreak,
}: {
  weekMinutes: number
  previousWeekMinutes: number
  totalMinutes: number
  dailyAverage: number
  streak: number
  bestStreak: number
}) {
  const week = Math.round(useCountUp(weekMinutes))
  const total = Math.round(useCountUp(totalMinutes))
  const days = Math.round(useCountUp(streak))

  return (
    <motion.div
      variants={gridStagger}
      initial="hidden"
      animate="show"
      className="grid divide-y rounded-2xl border sm:grid-cols-3 sm:divide-x sm:divide-y-0"
    >
      <Stat label="Esta semana" value={formatMinutes(week)}>
        <WeekDelta current={weekMinutes} previous={previousWeekMinutes} />
      </Stat>
      <Stat label="Total" value={formatMinutes(total)}>
        {dailyAverage > 0 ? `Média de ${formatMinutes(dailyAverage)} por dia nos últimos 30 dias` : "Sem estudo nos últimos 30 dias"}
      </Stat>
      <Stat label="Streak" value={formatDays(days)}>
        {bestStreak > streak ? `Recorde: ${formatDays(bestStreak)}` : streak > 0 ? "Seu recorde no último ano" : "Estude hoje para começar"}
      </Stat>
    </motion.div>
  )
}
