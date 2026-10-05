import { useState } from "react"
import { createPortal } from "react-dom"
import { LegalLink } from "@/context/LegalContext"
import { useAuth } from "@/context/AuthContext"
import { getErrorMessage } from "@/lib/api"
import { ModalScroller } from "@/components/ModalScroller"
import { Button } from "@/components/ui/button"

export function TermsGate() {
  const { user, acceptTerms, logout } = useAuth()
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!user || user.termsAccepted !== false) return null

  async function accept() {
    setSaving(true)
    setError(null)
    try {
      await acceptTerms()
    } catch (err) {
      setError(getErrorMessage(err, "Não foi possível registrar o aceite. Tente de novo."))
      setSaving(false)
    }
  }

  return createPortal(
    <ModalScroller open>
      <div className="fixed inset-0 bg-black/30 backdrop-blur-[1px] dark:bg-black/70" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="terms-gate-title"
        className="relative z-101 my-auto w-full max-w-md rounded-2xl border border-border bg-popover p-6 shadow-lg"
      >
        <h2 id="terms-gate-title" className="text-lg font-semibold text-popover-foreground">
          Termos de Uso e Política de Privacidade
        </h2>
        <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
          Publicamos os{" "}
          <LegalLink doc="terms" className="text-foreground underline underline-offset-4">
            Termos de Uso
          </LegalLink>{" "}
          e a{" "}
          <LegalLink doc="privacy" className="text-foreground underline underline-offset-4">
            Política de Privacidade
          </LegalLink>{" "}
          do Astra, que explicam as regras de uso e como cuidamos dos seus dados. Para continuar usando o app, leia e
          aceite os dois.
        </p>
        {error && <p className="mt-3 text-sm text-destructive">{error}</p>}
        <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <Button type="button" variant="outline" onClick={() => void logout()} disabled={saving}>
            Sair
          </Button>
          <Button type="button" onClick={() => void accept()} disabled={saving}>
            {saving ? "Salvando..." : "Li e aceito"}
          </Button>
        </div>
      </div>
    </ModalScroller>,
    document.body,
  )
}
