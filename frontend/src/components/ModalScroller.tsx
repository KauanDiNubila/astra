import { useRef } from "react"
import type { ReactNode } from "react"
import { motion } from "motion/react"
import { OverlayScrollbar } from "@/components/OverlayScrollbar"

export function ModalScroller({ open, children }: { open: boolean; children: ReactNode }) {
  const scrollRef = useRef<HTMLDivElement>(null)

  return (
    <motion.div
      ref={scrollRef}
      initial={{ opacity: 0 }}
      animate={{ opacity: open ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className="overlay-scroll fixed inset-0 z-100 flex items-center justify-center overflow-y-auto p-4"
    >
      {children}
      <OverlayScrollbar target={scrollRef} zIndex={102} />
    </motion.div>
  )
}
