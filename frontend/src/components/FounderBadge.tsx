import { Zap } from "lucide-react"
import { cn } from "@/lib/utils"

export const FOUNDER_EMAIL = "guiribeiro0910@gmail.com"

export function FounderBadge({ className }: { className?: string }) {
  return (
    <Zap
      aria-hidden="true"
      strokeWidth={1.5}
      className={cn("size-3.5 shrink-0 text-(--chart-4)", className)}
    />
  )
}
