import { useEffect, useRef } from "react"
import { Tooltip as TooltipPrimitive } from "radix-ui"
import type { DailyMinutes, GitHubDailyPoint } from "@/lib/types"
import { formatMinutes } from "@/lib/format"
import { dayKey } from "@/lib/studyStats"
import { cn } from "@/lib/utils"
import { Tooltip, TooltipTrigger } from "@/components/ui/tooltip"

const LEVELS = [
  "bg-muted",
  "bg-emerald-200 dark:bg-emerald-900",
  "bg-emerald-300 dark:bg-emerald-700",
  "bg-emerald-500 dark:bg-emerald-600",
  "bg-emerald-600 dark:bg-emerald-400",
]

function levelClass(minutes: number): string {
  if (minutes <= 0) return LEVELS[0]
  if (minutes < 30) return LEVELS[1]
  if (minutes < 60) return LEVELS[2]
  if (minutes < 120) return LEVELS[3]
  return LEVELS[4]
}

function formatDayLabel(key: string): string {
  const [year, month, day] = key.split("-").map(Number)
  return new Date(year, month - 1, day).toLocaleDateString("pt-BR", {
    weekday: "short",
    day: "numeric",
    month: "long",
  })
}

function monthLabel(date: Date) {
  const label = date.toLocaleDateString("pt-BR", { month: "short" }).replace(".", "")
  return label.charAt(0).toUpperCase() + label.slice(1)
}

type Cell = { key: string; minutes: number; githubActivity: number; future: boolean }

export function Heatmap({ data, githubData }: { data: DailyMinutes[]; githubData?: GitHubDailyPoint[] }) {
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const el = scrollRef.current
    if (el) el.scrollLeft = el.scrollWidth
  }, [])

  const byDay = new Map(data.map((d) => [d.day, d.minutes]))
  const githubByDay = new Map((githubData ?? []).map((d) => [d.date, d.contributionCount]))
  const today = new Date()
  const todayKey = dayKey(today)
  const cursor = new Date(today)
  cursor.setDate(cursor.getDate() - 7 * 52 - today.getDay())

  const weeks: Cell[][] = []
  const months: { index: number; label: string }[] = []
  while (cursor <= today) {
    const week: Cell[] = []
    for (let i = 0; i < 7; i++) {
      const key = dayKey(cursor)
      if (cursor.getDate() === 1) {
        const last = months[months.length - 1]
        if (!last || weeks.length - last.index >= 3) months.push({ index: weeks.length, label: monthLabel(cursor) })
      }
      week.push({
        key,
        minutes: byDay.get(key) ?? 0,
        githubActivity: githubByDay.get(key) ?? 0,
        future: key > todayKey,
      })
      cursor.setDate(cursor.getDate() + 1)
    }
    weeks.push(week)
  }

  const activeDays = weeks.flat().filter((c) => c.minutes > 0).length
  const columns = { gridTemplateColumns: `repeat(${weeks.length}, minmax(0, 1fr))` }

  return (
    <div className="flex flex-col gap-3">
      <div ref={scrollRef} className="overflow-x-auto">
        <div className="flex min-w-[640px] flex-col gap-1.5 p-0.5">
          <div className="grid text-[10px] text-muted-foreground" style={columns}>
            {months.map((m) => (
              <span key={m.index} style={{ gridColumnStart: m.index + 1, gridRowStart: 1 }} className="col-span-3 whitespace-nowrap">
                {m.label}
              </span>
            ))}
          </div>
          <div className="grid gap-[3px]" style={columns}>
            {weeks.map((week, index) => (
              <div key={index} className="flex flex-col gap-[3px]">
                {week.map((cell) =>
                  cell.future ? (
                    <div key={cell.key} className="aspect-square w-full" />
                  ) : (
                    <Tooltip key={cell.key}>
                      <TooltipTrigger asChild>
                        <div
                          style={{ animationDelay: `${index * 6}ms`, animationFillMode: "backwards" }}
                          className={cn(
                            "aspect-square w-full animate-in fade-in zoom-in-50 duration-300 motion-reduce:animate-none",
                            levelClass(cell.minutes),
                            cell.githubActivity > 0 ? "rounded-full" : "rounded-[3px]",
                          )}
                        />
                      </TooltipTrigger>
                      <TooltipPrimitive.Portal>
                        <TooltipPrimitive.Content
                          sideOffset={6}
                          className="z-50 min-w-36 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl data-[state=delayed-open]:animate-in data-[state=delayed-open]:fade-in-0 data-[state=delayed-open]:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
                        >
                          <div className="grid gap-1">
                            <span className="font-medium text-foreground">{formatDayLabel(cell.key)}</span>
                            <div className="flex items-center justify-between gap-4">
                              <span className="text-muted-foreground">estudo</span>
                              <span className="font-medium text-foreground tabular-nums">
                                {cell.minutes > 0 ? formatMinutes(cell.minutes) : "0 min"}
                              </span>
                            </div>
                            {cell.githubActivity > 0 && (
                              <div className="flex items-center justify-between gap-4">
                                <span className="text-muted-foreground">GitHub</span>
                                <span className="font-medium text-foreground tabular-nums">{cell.githubActivity}</span>
                              </div>
                            )}
                          </div>
                        </TooltipPrimitive.Content>
                      </TooltipPrimitive.Portal>
                    </Tooltip>
                  ),
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-muted-foreground">
        <span>
          <span className="font-medium text-foreground tabular-nums">{activeDays}</span> dias com estudo no último ano
        </span>
        <span className="flex items-center gap-1.5">
          Menos
          {LEVELS.map((level) => (
            <span key={level} className={cn("size-2.5 rounded-[2px]", level)} />
          ))}
          Mais
          {githubData && githubData.length > 0 && (
            <span className="ml-3 flex items-center gap-1.5">
              <span className="size-2.5 rounded-full bg-muted-foreground/60" />
              GitHub
            </span>
          )}
        </span>
      </div>
    </div>
  )
}
