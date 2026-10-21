import type { DailyMinutes } from "@/lib/types"

export function dayKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const day = String(date.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}

function shiftDays(date: Date, days: number) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function sumRange(byDay: Map<string, number>, from: Date, to: Date) {
  let total = 0
  for (let cursor = new Date(from); cursor <= to; cursor = shiftDays(cursor, 1)) {
    total += byDay.get(dayKey(cursor)) ?? 0
  }
  return total
}

export function previousWeekSoFar(data: DailyMinutes[], today = new Date()) {
  const byDay = new Map(data.map((d) => [d.day, d.minutes]))
  const sinceMonday = (today.getDay() + 6) % 7
  const lastWeekEnd = shiftDays(today, -7)
  return sumRange(byDay, shiftDays(lastWeekEnd, -sinceMonday), lastWeekEnd)
}

export function averagePerDay(data: DailyMinutes[], days: number, today = new Date()) {
  const byDay = new Map(data.map((d) => [d.day, d.minutes]))
  return Math.round(sumRange(byDay, shiftDays(today, -(days - 1)), today) / days)
}

export function longestStreak(data: DailyMinutes[]) {
  const days = data
    .filter((d) => d.minutes > 0)
    .map((d) => d.day)
    .sort()
  let best = 0
  let current = 0
  let previous: string | null = null
  for (const day of days) {
    if (previous && dayKey(shiftDays(new Date(`${previous}T12:00:00`), 1)) === day) current += 1
    else current = 1
    best = Math.max(best, current)
    previous = day
  }
  return best
}

export function formatDays(n: number) {
  return n === 1 ? "1 dia" : `${n} dias`
}
