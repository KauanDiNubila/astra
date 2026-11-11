import { useEffect, useState } from "react"
import { formatRelativeTime } from "@/lib/format"

const TICK_MS = 30000

export function RelativeTime({ iso, className }: { iso: string; className?: string }) {
  const [, setTick] = useState(0)

  useEffect(() => {
    const interval = setInterval(() => setTick((t) => t + 1), TICK_MS)
    return () => clearInterval(interval)
  }, [])

  return <span className={className}>{formatRelativeTime(iso)}</span>
}
