import { useEffect, useState } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { useAttachmentUrl } from "@/context/ChatContext"

type Props = {
  messageId: string | null
  onClose: () => void
}

export function ImageLightbox({ messageId, onClose }: Props) {
  const reducedMotion = useReducedMotion()
  const open = !!messageId
  const url = useAttachmentUrl(messageId)
  const [rendered, setRendered] = useState(open)

  useEffect(() => {
    if (open) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 200)
    return () => clearTimeout(timeout)
  }, [open])

  useEffect(() => {
    if (!rendered) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [rendered])

  useEffect(() => {
    if (!rendered) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [rendered, onClose])

  if (!rendered) return null

  return createPortal(
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: open ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className="fixed inset-0 z-100 flex items-center justify-center p-4"
    >
      <div onClick={onClose} className="fixed inset-0 bg-black/80 backdrop-blur-sm" />

      {url && (
        <motion.img
          src={url}
          alt="Imagem em tamanho grande"
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94 }}
          animate={
            reducedMotion
              ? { opacity: open ? 1 : 0 }
              : { opacity: open ? 1 : 0, scale: open ? 1 : 0.96 }
          }
          transition={{ type: "spring", damping: 22, stiffness: 320, mass: 0.8 }}
          className="relative z-10 max-h-[90vh] max-w-[90vw] rounded-lg object-contain shadow-lg"
        />
      )}

      <button
        type="button"
        title="Fechar"
        onClick={onClose}
        className="fixed top-4 right-4 z-20 rounded-full bg-background/90 p-2 text-foreground shadow-lg transition-colors hover:bg-background"
      >
        <X size={20} />
      </button>
    </motion.div>,
    document.body,
  )
}
