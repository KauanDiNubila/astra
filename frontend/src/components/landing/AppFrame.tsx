import type { ReactNode } from "react"
import { CalendarDays, LayoutDashboard, Timer, Trophy } from "lucide-react"
import { cn } from "@/lib/utils"
import { GitHubIcon } from "@/components/icons/GitHubIcon"
import { AstraMark } from "@/components/landing/AstraMark"

export const NAV_STEP = 44

const NAV_ICONS = [LayoutDashboard, Timer, CalendarDays, Trophy, GitHubIcon]

type AppFrameProps = {
  active?: number
  className?: string
  children: ReactNode
}

export function AppFrame({ active = 0, className, children }: AppFrameProps) {
  return (
    <div
      className={cn(
        "flex aspect-[5/6] w-full overflow-hidden rounded-2xl border bg-card text-left text-card-foreground sm:aspect-[6/5]",
        "shadow-[0_40px_80px_-30px_rgb(0_0_0/0.25)] ring-1 ring-black/5 dark:shadow-[0_40px_80px_-30px_rgb(0_0_0/0.8)] dark:ring-white/5",
        className,
      )}
    >
      <div className="flex w-12 shrink-0 flex-col items-center gap-5 border-r bg-muted/40 py-4 sm:w-14">
        <AstraMark className="size-5" />
        <div className="relative flex flex-col gap-2">
          <div
            data-indicator
            className="absolute left-0 top-0 size-9 rounded-lg bg-background shadow-sm ring-1 ring-border"
            style={{ transform: `translateY(${active * NAV_STEP}px)` }}
          />
          {NAV_ICONS.map((Icon, i) => (
            <div
              key={i}
              data-nav-icon
              className={cn("relative flex size-9 items-center justify-center", i === active ? "opacity-100" : "opacity-40")}
            >
              <Icon className="size-4" />
            </div>
          ))}
        </div>
      </div>
      <div className="relative min-w-0 flex-1">{children}</div>
    </div>
  )
}
