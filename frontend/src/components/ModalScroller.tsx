import { useRef } from "react"
import type { ReactNode } from "react"
import { motion } from "motion/react"
import { OverlayScrollbar } from "@/components/OverlayScrollbar"

export function ModalScroller({
  open,
  layerClass = "z-100",
  children,
}: {
  open: boolean
  layerClass?: string
  children: ReactNode
}) {
  const scrollRef = useRef<HTMLDivElement>(null)

  return (
    <motion.div
      ref={scrollRef}
      initial={{ opacity: 0 }}
      animate={{ opacity: open ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className={`overlay-scroll fixed inset-0 ${layerClass} flex items-center justify-center overflow-y-auto p-4`}
    >
      {children}
      <OverlayScrollbar target={scrollRef} zIndex={layerClass === "z-100" ? 102 : 122} />
    </motion.div>
  )
}
