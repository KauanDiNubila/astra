import { Pencil } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useAuth } from "@/context/AuthContext"
import { useCountUp } from "@/hooks/use-count-up"
import { formatMinutes } from "@/lib/format"
import type { GoalProgress } from "@/lib/types"
import { Button } from "@/components/ui/button"

function greeting(hour: number) {
  if (hour >= 5 && hour < 12) return "Bom dia"
  if (hour >= 12 && hour < 18) return "Boa tarde"
  return "Boa noite"
}

function todayLabel(date: Date) {
  const label = date.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" })
  return label.charAt(0).toUpperCase() + label.slice(1)
}

function formatHours(hours: number) {
  return `${Number.isInteger(hours) ? hours : hours.toFixed(1).replace(".", ",")}h`
}

function Duration({ minutes }: { minutes: number }) {
  const animated = Math.round(useCountUp(minutes))
  const hours = Math.floor(animated / 60)
  const mins = animated % 60
  const unit = "ml-0.5 text-2xl font-medium tracking-normal text-muted-foreground sm:text-3xl"
  return (
    <p className="text-[3.5rem] leading-none font-semibold tracking-[-0.045em] sm:text-7xl">
      {hours > 0 && (
        <>
          {hours}
          <span className={`${unit} mr-3`}>h</span>
        </>
      )}
      {mins}
      <span className={unit}>min</span>
    </p>
  )
}

const RINGS = [
  { type: "DAILY", label: "Meta do dia", radius: 52, className: "stroke-emerald-500" },
  { type: "WEEKLY", label: "Meta da semana", radius: 38, className: "stroke-foreground/80" },
] as const

function GoalRings({ goals }: { goals: GoalProgress[] }) {
  const reducedMotion = useReducedMotion()
  return (
    <svg viewBox="0 0 120 120" className="size-28 -rotate-90 sm:size-32" aria-hidden="true">
      {RINGS.map((ring) => {
        const goal = goals.find((g) => g.type === ring.type)
        const progress = goal && goal.targetHours > 0 ? Math.min(1, goal.achievedHours / goal.targetHours) : 0
        return (
          <g key={ring.type}>
            <circle cx="60" cy="60" r={ring.radius} fill="none" strokeWidth="10" className="stroke-muted" />
            <motion.circle
              cx="60"
              cy="60"
              r={ring.radius}
              fill="none"
              strokeWidth="10"
              strokeLinecap="round"
              pathLength={1}
              strokeDasharray="1 1"
              className={ring.className}
              initial={{ strokeDashoffset: 1, opacity: 0 }}
              animate={{ strokeDashoffset: progress > 0 ? 1 - progress : 1, opacity: progress > 0 ? 1 : 0 }}
              transition={reducedMotion ? { duration: 0 } : { duration: 1.1, ease: [0.22, 1, 0.36, 1], delay: 0.15 }}
            />
          </g>
        )
      })}
    </svg>
  )
}

export function DayHero({
  todayMinutes,
  goals,
  onEditGoals,
}: {
  todayMinutes: number
  goals: GoalProgress[]
  onEditGoals: () => void
}) {
  const { user } = useAuth()
  const now = new Date()
  const firstName = user?.name.trim().split(/\s+/)[0] ?? ""
  const hasGoals = goals.some((g) => g.targetHours > 0)

  return (
    <section className="flex flex-col gap-8 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">
          {greeting(now.getHours())}
          {firstName && `, ${firstName}`}
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">{todayLabel(now)}</p>
        <p className="mt-8 text-xs font-medium tracking-wide text-muted-foreground uppercase">Hoje</p>
        <div className="mt-2">
          <Duration minutes={todayMinutes} />
        </div>
      </div>

      <div className="flex items-center gap-5">
        <GoalRings goals={goals} />
        <div className="flex flex-col gap-3">
          {hasGoals ? (
            RINGS.map((ring) => {
              const goal = goals.find((g) => g.type === ring.type)
              return (
                <div key={ring.type} className="flex items-start gap-2.5">
                  <span className={`mt-1.5 size-2 rounded-full ${ring.type === "DAILY" ? "bg-emerald-500" : "bg-foreground/80"}`} />
                  <div>
                    <p className="text-xs text-muted-foreground">{ring.label}</p>
                    <p className="text-sm font-medium tabular-nums">
                      {goal && goal.targetHours > 0
                        ? `${formatMinutes(Math.round(goal.achievedHours * 60))} de ${formatHours(goal.targetHours)}`
                        : "Sem meta"}
                    </p>
                  </div>
                </div>
              )
            })
          ) : (
            <p className="max-w-40 text-sm text-muted-foreground">Defina metas para acompanhar o dia e a semana.</p>
          )}
          <Button variant="ghost" size="sm" className="-ml-2 w-fit text-muted-foreground" onClick={onEditGoals}>
            <Pencil data-icon="inline-start" />
            {hasGoals ? "Editar metas" : "Definir metas"}
          </Button>
        </div>
      </div>
    </section>
  )
}
