import type { DailyMinutes } from "@/lib/types"

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

export function Heatmap({ data }: { data: DailyMinutes[] }) {
  const byDay = new Map(data.map((d) => [d.day, d.minutes]))
  const today = new Date()
  const cursor = new Date(today)
  cursor.setDate(cursor.getDate() - 7 * 51 - today.getDay())

  const weeks: { key: string; minutes: number }[][] = []
  while (cursor <= today) {
    const week: { key: string; minutes: number }[] = []
    for (let i = 0; i < 7; i++) {
      const key = dayKey(cursor)
      week.push({ key, minutes: byDay.get(key) ?? 0 })
      cursor.setDate(cursor.getDate() + 1)
    }
    weeks.push(week)
  }

  return (
    <div className="flex gap-1 overflow-x-auto pb-2">
      {weeks.map((week, index) => (
        <div key={index} className="flex flex-col gap-1">
          {week.map((cell) => (
            <div
              key={cell.key}
              className={`size-3 rounded-sm ${levelClass(cell.minutes)}`}
              title={`${cell.key}: ${cell.minutes} min`}
            />
          ))}
        </div>
      ))}
    </div>
  )
}
