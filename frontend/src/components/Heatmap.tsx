import { Tooltip as TooltipPrimitive } from "radix-ui"
import type { DailyMinutes, GitHubDailyPoint } from "@/lib/types"
import { formatMinutes } from "@/lib/format"
import { Tooltip, TooltipTrigger } from "@/components/ui/tooltip"

function levelClass(minutes: number): string {
  if (minutes <= 0) return "bg-muted"
  if (minutes < 30) return "bg-emerald-200 dark:bg-emerald-900"
  if (minutes < 60) return "bg-emerald-300 dark:bg-emerald-700"
  if (minutes < 120) return "bg-emerald-500 dark:bg-emerald-600"
  return "bg-emerald-600 dark:bg-emerald-400"
}

function dayKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}

function formatDayLabel(key: string): string {
  const [year, month, day] = key.split("-").map(Number)
  return new Date(year, month - 1, day).toLocaleDateString("pt-BR", {
    day: "numeric",
    month: "long",
  })
}

export function Heatmap({ data, githubData }: { data: DailyMinutes[]; githubData?: GitHubDailyPoint[] }) {
  const byDay = new Map(data.map((d) => [d.day, d.minutes]))
  const githubByDay = new Map((githubData ?? []).map((d) => [d.date, d.contributionCount]))
  const today = new Date()
  const cursor = new Date(today)
  cursor.setDate(cursor.getDate() - 7 * 51 - today.getDay())

  const weeks: { key: string; minutes: number; githubActivity: number }[][] = []
  while (cursor <= today) {
    const week: { key: string; minutes: number; githubActivity: number }[] = []
    for (let i = 0; i < 7; i++) {
      const key = dayKey(cursor)
      week.push({ key, minutes: byDay.get(key) ?? 0, githubActivity: githubByDay.get(key) ?? 0 })
      cursor.setDate(cursor.getDate() + 1)
    }
    weeks.push(week)
  }

  return (
    <div className="flex gap-1 overflow-x-auto p-1">
      {weeks.map((week, index) => (
        <div key={index} className="flex flex-col gap-1">
          {week.map((cell) => (
            <Tooltip key={cell.key}>
              <TooltipTrigger asChild>
                <div
                  style={{ animationDelay: `${index * 4}ms`, animationFillMode: "backwards" }}
                  className={`size-3 animate-in fade-in zoom-in-50 duration-300 motion-reduce:animate-none ${levelClass(cell.minutes)} ${
                    cell.githubActivity > 0 ? "rounded-full" : "rounded-sm"
                  }`}
                />
              </TooltipTrigger>
              <TooltipPrimitive.Portal>
                <TooltipPrimitive.Content
                  sideOffset={6}
                  className="z-50 min-w-32 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl data-[state=delayed-open]:animate-in data-[state=delayed-open]:fade-in-0 data-[state=delayed-open]:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95"
                >
                  <div className="grid gap-1">
                    <span className="font-medium text-foreground">{formatDayLabel(cell.key)}</span>
                    <div className="flex items-center justify-between gap-4">
                      <span className="text-muted-foreground">estudo</span>
                      <span className="font-mono font-medium text-foreground tabular-nums">
                        {cell.minutes > 0 ? formatMinutes(cell.minutes) : "0 min"}
                      </span>
                    </div>
                    {cell.githubActivity > 0 && (
                      <div className="flex items-center justify-between gap-4">
                        <span className="text-muted-foreground">GitHub</span>
                        <span className="font-mono font-medium text-foreground tabular-nums">
                          {cell.githubActivity}
                        </span>
                      </div>
                    )}
                  </div>
                </TooltipPrimitive.Content>
              </TooltipPrimitive.Portal>
            </Tooltip>
          ))}
        </div>
      ))}
    </div>
  )
}
