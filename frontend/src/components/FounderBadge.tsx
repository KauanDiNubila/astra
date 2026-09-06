import { Zap } from "lucide-react"
import { cn } from "@/lib/utils"

export const FOUNDER_EMAIL = "guiribeiro0910@gmail.com"

export function FounderBadge({ className }: { className?: string }) {
  return <Zap aria-hidden="true" className={cn("size-3.5 shrink-0 fill-current text-(--chart-3)", className)} />
}
