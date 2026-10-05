import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react"
import type { ReactNode } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { Link } from "react-router-dom"
import { PrivacyContent } from "@/pages/PrivacyPage"
import { TermsContent } from "@/pages/TermsPage"
import { ModalScroller } from "@/components/ModalScroller"
import { LEGAL_LAST_UPDATED, LEGAL_PROSE_CLASS } from "@/lib/legal"
import { useDelayedUnmount, useFrozen } from "@/hooks/useDelayedUnmount"

export type LegalDoc = "terms" | "privacy"

type LegalContextValue = {
  openLegal: (doc: LegalDoc) => void
}

const LegalContext = createContext<LegalContextValue | undefined>(undefined)

const TITLES: Record<LegalDoc, string> = {
  terms: "Termos de Uso",
  privacy: "Política de Privacidade",
}

const PATHS: Record<LegalDoc, string> = {
  terms: "/termos",
  privacy: "/privacidade",
}

export function LegalProvider({ children }: { children: ReactNode }) {
  const [doc, setDoc] = useState<LegalDoc | null>(null)
  const value = useMemo<LegalContextValue>(() => ({ openLegal: setDoc }), [])
  const close = useCallback(() => setDoc(null), [])

  return (
    <LegalContext.Provider value={value}>
      {children}
      <LegalModal doc={doc} onClose={close} onSwitch={setDoc} />
    </LegalContext.Provider>
  )
}

export function LegalLink({
  doc,
  className,
  tabIndex,
  children,
}: {
  doc: LegalDoc
  className?: string
  tabIndex?: number
  children: ReactNode
}) {
  const ctx = useContext(LegalContext)
  if (!ctx) {
    return (
      <Link to={PATHS[doc]} className={className} tabIndex={tabIndex}>
        {children}
      </Link>
    )
  }
  return (
    <a
      href={PATHS[doc]}
      className={className}
      tabIndex={tabIndex}
      onClick={(event) => {
        event.preventDefault()
        ctx.openLegal(doc)
      }}
    >
      {children}
    </a>
  )
}

function LegalModal({
  doc,
  onClose,
  onSwitch,
}: {
  doc: LegalDoc | null
  onClose: () => void
  onSwitch: (doc: LegalDoc) => void
}) {
  const open = doc !== null
  const rendered = useDelayedUnmount(open, 300)
  const shown = useFrozen(doc, !open)
  const reducedMotion = useReducedMotion()

  useEffect(() => {
    if (!open) return
    function onKeyDown(event: KeyboardEvent) {
      if (event.key !== "Escape") return
      event.stopPropagation()
      onClose()
    }
    window.addEventListener("keydown", onKeyDown, true)
    return () => window.removeEventListener("keydown", onKeyDown, true)
  }, [open, onClose])

  useEffect(() => {
    if (!rendered) return
    const previous = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previous
    }
  }, [rendered])

  if (!rendered || !shown) return null

  return createPortal(
    <ModalScroller open={open} layerClass="z-[120]">
      <div onClick={onClose} className="fixed inset-0 bg-black/30 backdrop-blur-[1px] dark:bg-black/70" />
      <div className="pointer-events-none relative z-101 my-auto w-full max-w-3xl">
        <motion.div
          role="dialog"
          aria-modal="true"
          aria-label={TITLES[shown]}
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94, y: 16 }}
          animate={
            reducedMotion
              ? { opacity: open ? 1 : 0 }
              : { opacity: open ? 1 : 0, scale: open ? 1 : 0.96, y: open ? 0 : 16 }
          }
          transition={{ type: "spring", damping: 22, stiffness: 320, mass: 0.8 }}
          className="pointer-events-auto w-full overflow-hidden rounded-2xl border border-border bg-popover shadow-lg"
        >
          <div className="flex items-center justify-between border-b border-border/50 px-6 py-4">
            <div>
              <h2 className="text-lg font-semibold text-popover-foreground">{TITLES[shown]}</h2>
              <p className="text-xs text-muted-foreground">Última atualização: {LEGAL_LAST_UPDATED}</p>
            </div>
            <button
              type="button"
              title="Fechar"
              onClick={onClose}
              className="p-1 text-muted-foreground transition-colors hover:text-foreground"
            >
              <X size={20} />
            </button>
          </div>
          <div key={shown} className={`px-6 py-6 text-[15px] leading-relaxed ${LEGAL_PROSE_CLASS}`}>
            {shown === "terms" ? <TermsContent /> : <PrivacyContent />}
          </div>
          <div className="flex justify-end gap-4 border-t border-border/50 px-6 py-3 text-sm text-muted-foreground">
            {shown === "terms" ? (
              <button type="button" onClick={() => onSwitch("privacy")} className="hover:text-foreground">
                Ver Política de Privacidade
              </button>
            ) : (
              <button type="button" onClick={() => onSwitch("terms")} className="hover:text-foreground">
                Ver Termos de Uso
              </button>
            )}
          </div>
        </motion.div>
      </div>
    </ModalScroller>,
    document.body,
  )
}
