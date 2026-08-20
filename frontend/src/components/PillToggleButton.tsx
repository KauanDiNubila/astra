import type { ReactNode } from "react"
import { motion } from "motion/react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

type Props = {
  active: boolean
  layoutId: string
  onClick: () => void
  className?: string
  children: ReactNode
}

export function PillToggleButton({ active, layoutId, onClick, className, children }: Props) {
  return (
    <div className="relative">
      {active && (
        <motion.div
          layoutId={layoutId}
          className="absolute inset-0 rounded-md bg-primary"
          transition={{ type: "spring", stiffness: 500, damping: 35 }}
        />
      )}
      <Button
        type="button"
        variant="ghost"
        size="sm"
        onClick={onClick}
        className={cn(
          "relative z-10",
          active && "text-primary-foreground hover:bg-transparent hover:text-primary-foreground",
          className,
        )}
      >
        {children}
      </Button>
    </div>
  )
}
