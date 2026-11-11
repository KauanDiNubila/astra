import type { ReactNode } from "react"
import { Reply } from "lucide-react"
import { motion, useMotionValue, useTransform } from "motion/react"

const IS_TOUCH_DEVICE = typeof window !== "undefined" && "ontouchstart" in window
const SWIPE_REPLY_TRIGGER_PX = 40
const SWIPE_REPLY_MAX_PX = 56

export function SwipeableMessage({ onReply, children }: { onReply: () => void; children: ReactNode }) {
  const x = useMotionValue(0)
  const iconOpacity = useTransform(x, [0, SWIPE_REPLY_TRIGGER_PX], [0, 1])

  if (!IS_TOUCH_DEVICE) return <>{children}</>

  return (
    <div className="relative">
      <motion.div
        style={{ opacity: iconOpacity }}
        className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-1 text-muted-foreground"
      >
        <Reply className="size-4" />
      </motion.div>
      <motion.div
        drag="x"
        style={{ x }}
        dragConstraints={{ left: 0, right: SWIPE_REPLY_MAX_PX }}
        dragElastic={0.15}
        dragSnapToOrigin
        onDragEnd={(_, info) => {
          if (info.offset.x > SWIPE_REPLY_TRIGGER_PX) onReply()
        }}
      >
        {children}
      </motion.div>
    </div>
  )
}
