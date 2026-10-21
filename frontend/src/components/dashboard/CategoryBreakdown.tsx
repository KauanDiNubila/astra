import { motion, useReducedMotion } from "motion/react"
import { formatMinutes } from "@/lib/format"

type Slice = { categoryId: string; name: string; minutes: number; color: string | null }

export function CategoryBreakdown({ data }: { data: Slice[] }) {
  const reducedMotion = useReducedMotion()
  const rows = data.filter((c) => c.minutes > 0).sort((a, b) => b.minutes - a.minutes)
  const total = rows.reduce((sum, c) => sum + c.minutes, 0)

  if (rows.length === 0) {
    return (
      <div className="flex h-[200px] items-center justify-center text-sm text-muted-foreground">
        Nenhuma sessão nesse período.
      </div>
    )
  }

  return (
    <ul className="flex min-h-[200px] flex-col gap-4">
      {rows.map((row, i) => {
        const pct = (row.minutes / total) * 100
        return (
          <li key={row.categoryId} className="flex flex-col gap-2">
            <div className="flex items-center gap-2.5 text-sm">
              <span
                className="size-2 shrink-0 rounded-full bg-muted-foreground"
                style={row.color ? { backgroundColor: row.color } : undefined}
              />
              <span className="min-w-0 flex-1 truncate">{row.name}</span>
              <span className="font-medium tabular-nums">{formatMinutes(row.minutes)}</span>
              <span className="w-10 text-right text-muted-foreground tabular-nums">{Math.round(pct)}%</span>
            </div>
            <div className="h-1.5 overflow-hidden rounded-full bg-muted">
              <motion.div
                className="h-full rounded-full bg-foreground/80"
                initial={{ width: reducedMotion ? `${pct}%` : 0 }}
                animate={{ width: `${pct}%` }}
                transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1], delay: i * 0.05 }}
              />
            </div>
          </li>
        )
      })}
    </ul>
  )
}
