import type { ReactNode } from "react"
import { motion } from "motion/react"
import { formatMinutes } from "@/lib/format"
import { PomodoroRing } from "@/components/PomodoroRing"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"

type Mode = "focus" | "break"

type Props = {
  ring: { mode: Mode; isLongBreak: boolean; timeLeft: number; totalSeconds: number; sessionCaption: string }
  primaryLabel: string
  onPrimaryClick: () => void
  focusedMinutes: number
  header: { title: string; subtitle?: string; progress?: number } | null
  bottom: {
    currentLabel?: string
    objective?: string
    nextLabel?: string
    lessonsProgress?: string
    moduleProgress?: ReactNode
  }
}

const fadeUp = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  transition: { delay: 0.2, duration: 0.3, ease: [0.22, 1, 0.36, 1] as const },
}

export function PomodoroFocusView({ ring, primaryLabel, onPrimaryClick, focusedMinutes, header, bottom }: Props) {
  const hasBottomContent = bottom.currentLabel || bottom.objective || bottom.nextLabel || bottom.moduleProgress

  return (
    <div className="flex min-h-full flex-1 flex-col gap-12">
      {header && (
        <motion.div {...fadeUp} className="flex flex-col items-center gap-2 text-center">
          <div className="flex flex-col items-center">
            <span className="font-medium">{header.title}</span>
            {header.subtitle && <span className="text-sm text-muted-foreground">{header.subtitle}</span>}
          </div>
          {header.progress !== undefined && (
            <Progress value={Math.round(header.progress * 100)} className="max-w-xs" />
          )}
        </motion.div>
      )}

      <div className="flex flex-1 flex-col items-center justify-center gap-6">
        <PomodoroRing
          size={280}
          emphasize
          mode={ring.mode}
          isLongBreak={ring.isLongBreak}
          timeLeft={ring.timeLeft}
          totalSeconds={ring.totalSeconds}
          caption={ring.sessionCaption}
        />
        <div className="flex flex-col items-center gap-2">
          <Button type="button" variant="outline" onClick={onPrimaryClick}>
            {primaryLabel}
          </Button>
          <p className="text-sm text-muted-foreground">{formatMinutes(focusedMinutes)} focados</p>
        </div>
      </div>

      {hasBottomContent && (
        <motion.div {...fadeUp} className="flex flex-wrap justify-center gap-x-10 gap-y-3 text-center">
          {bottom.currentLabel && (
            <div className="flex flex-col items-center gap-0.5">
              <span className="text-xs text-muted-foreground/70">Aula atual</span>
              <span className="text-sm">{bottom.currentLabel}</span>
              {bottom.lessonsProgress && (
                <span className="text-xs text-muted-foreground/70">{bottom.lessonsProgress}</span>
              )}
              {bottom.moduleProgress && <div className="mt-1">{bottom.moduleProgress}</div>}
            </div>
          )}
          {bottom.objective && (
            <div className="flex flex-col gap-0.5">
              <span className="text-xs text-muted-foreground/70">Objetivo</span>
              <span className="max-w-52 truncate text-sm">{bottom.objective}</span>
            </div>
          )}
          {bottom.nextLabel && (
            <div className="flex flex-col gap-0.5">
              <span className="text-xs text-muted-foreground/70">Próxima etapa</span>
              <span className="text-sm">{bottom.nextLabel}</span>
            </div>
          )}
        </motion.div>
      )}
    </div>
  )
}
