import type { ActivityPeriod, GitHubDailyPoint } from "@/lib/types"

export type BucketUnit = "day" | "week" | "month"

export type ChartBucket = { from: string; to: string; count: number }

export type ChartRange = { days: number; unit: BucketUnit; buckets: ChartBucket[] }

const DAY_MS = 86_400_000

export type ChartPeriod = Exclude<ActivityPeriod, "TODAY">

const RANGES: Record<ChartPeriod, { days: number; unit: BucketUnit }> = {
  WEEK: { days: 7, unit: "day" },
  MONTH: { days: 30, unit: "day" },
  QUARTER: { days: 90, unit: "week" },
  YEAR: { days: 365, unit: "month" },
}

function toMs(key: string) {
  const [year, month, day] = key.split("-").map(Number)
  return Date.UTC(year, month - 1, day)
}

function toKey(ms: number) {
  return new Date(ms).toISOString().slice(0, 10)
}

export function chartRange(period: ChartPeriod, series: GitHubDailyPoint[], today: string): ChartRange {
  const { days, unit } = RANGES[period]
  const counts = new Map(series.map((point) => [point.date, point.contributionCount]))
  const end = toMs(today)
  const start = end - (days - 1) * DAY_MS
  const buckets: ChartBucket[] = []
  let current: ChartBucket | null = null
  let currentEnd = end

  function startsNewBucket(ms: number, key: string, bucket: ChartBucket) {
    if (unit === "day") return true
    if (unit === "week") return ms < currentEnd - 6 * DAY_MS
    return key.slice(0, 7) !== bucket.to.slice(0, 7)
  }

  for (let ms = end; ms >= start; ms -= DAY_MS) {
    const key = toKey(ms)
    if (current === null || startsNewBucket(ms, key, current)) {
      current = { from: key, to: key, count: 0 }
      buckets.push(current)
      currentEnd = ms
    }
    current.from = key
    current.count += counts.get(key) ?? 0
  }
  return { days, unit, buckets: buckets.reverse() }
}
