import { useEffect, useRef } from "react"
import { motion, useReducedMotion } from "motion/react"
import { useCountUp } from "@/hooks/use-count-up"
import { formatMinutes } from "@/lib/format"
import { cn } from "@/lib/utils"

type Slice = { categoryId: string; name: string; minutes: number; color: string | null }

const EASE = [0.22, 1, 0.36, 1] as const

function Row({ row, pct }: { row: Slice; pct: number }) {
  const reducedMotion = useReducedMotion()
  const mountedRef = useRef(false)
  useEffect(() => {
    mountedRef.current = true
  }, [])
  const firstPaint = !mountedRef.current
  const minutes = Math.round(useCountUp(row.minutes, 500))
  const percent = Math.round(useCountUp(pct, 500))

  return (
    <motion.li
      layout={reducedMotion ? false : "position"}
      transition={{ duration: 0.45, ease: EASE }}
      className={cn("flex flex-col gap-2 transition-opacity duration-300", row.minutes === 0 && "opacity-40")}
    >
      <div className="flex items-center gap-2.5 text-sm">
        <span
          className="size-2 shrink-0 rounded-full bg-muted-foreground"
          style={row.color ? { backgroundColor: row.color } : undefined}
        />
        <span className="min-w-0 flex-1 truncate">{row.name}</span>
        <span className="font-medium tabular-nums">{formatMinutes(minutes)}</span>
        <span className="w-10 text-right text-muted-foreground tabular-nums">{percent}%</span>
      </div>
      <div className="h-1.5 overflow-hidden rounded-full bg-muted">
        <motion.div
          className="h-full rounded-full bg-foreground/80"
          initial={{ width: reducedMotion ? `${pct}%` : 0 }}
          animate={{ width: `${pct}%` }}
          transition={reducedMotion ? { duration: 0 } : { duration: 0.8, ease: EASE, delay: firstPaint ? 0.6 : 0 }}
        />
      </div>
    </motion.li>
  )
}

export function CategoryBreakdown({ data }: { data: Slice[] }) {
  const total = data.reduce((sum, c) => sum + c.minutes, 0)
  const rows = [...data].sort((a, b) => b.minutes - a.minutes || a.name.localeCompare(b.name))

  if (data.length === 0) {
    return (
      <div className="flex h-[200px] items-center justify-center text-sm text-muted-foreground">
        Nenhuma categoria ainda.
      </div>
    )
  }

  return (
    <div className="flex min-h-[200px] flex-col gap-4">
      <ul className="flex flex-col gap-4">
        {rows.map((row) => (
          <Row key={row.categoryId} row={row} pct={total > 0 ? (row.minutes / total) * 100 : 0} />
        ))}
      </ul>
      <p
        className={cn(
          "text-sm text-muted-foreground transition-opacity duration-300",
          total > 0 ? "pointer-events-none opacity-0" : "opacity-100",
        )}
      >
        Nenhuma sessão nesse período.
      </p>
    </div>
  )
}
