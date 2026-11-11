import { useEffect, useState } from "react"
import { cn } from "@/lib/utils"

function formatElapsed(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  const h = Math.floor(m / 60)
  const pad = (n: number) => n.toString().padStart(2, "0")
  return h > 0 ? `${h}:${pad(m % 60)}:${pad(s)}` : `${pad(m)}:${pad(s)}`
}

export function CallTimer({ startedAt, className }: { startedAt: number | null; className?: string }) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const interval = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(interval)
  }, [])
  if (!startedAt) return null
  return <span className={cn("tabular-nums", className)}>{formatElapsed(Math.max(0, Math.floor((now - startedAt) / 1000)))}</span>
}
