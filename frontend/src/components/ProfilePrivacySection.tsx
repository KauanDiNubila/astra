import { useState } from "react"
import type { KeyboardEvent } from "react"
import { ChevronDown, Download, ShieldCheck } from "lucide-react"
import { useReducedMotion } from "motion/react"
import { LegalLink } from "@/context/LegalContext"
import { toast } from "sonner"
import { useAuth } from "@/context/AuthContext"
import { api, getErrorMessage } from "@/lib/api"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

function exportFilename() {
  const today = new Date().toLocaleDateString("sv-SE")
  return `astra-meus-dados-${today}.json`
}

export function ProfilePrivacySection({ onAccountDeleted }: { onAccountDeleted: () => void }) {
  const { user, deleteAccount } = useAuth()
  const reducedMotion = useReducedMotion()
  const [open, setOpen] = useState(false)
  const [exporting, setExporting] = useState(false)
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [secret, setSecret] = useState("")
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!user) return null
  const usesPassword = user.hasPassword !== false
  const isOwner = user.role === "OWNER"

  async function downloadData() {
    setExporting(true)
    try {
      const res = await api.get<Blob>("/me/export", { responseType: "blob" })
      const url = URL.createObjectURL(res.data)
      const link = document.createElement("a")
      link.href = url
      link.download = exportFilename()
      document.body.appendChild(link)
      link.click()
      link.remove()
      setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (err) {
      toast.error(getErrorMessage(err, "Não foi possível gerar o arquivo. Tente de novo."))
    } finally {
      setExporting(false)
    }
  }

  function cancelDelete() {
    setConfirmingDelete(false)
    setSecret("")
    setError(null)
  }

  async function confirmDelete() {
    if (!secret.trim()) return
    setDeleting(true)
    setError(null)
    try {
      await deleteAccount(usesPassword ? { password: secret } : { confirmation: secret })
      onAccountDeleted()
      toast("Sua conta e seus dados foram excluídos.")
    } catch (err) {
      setError(getErrorMessage(err, "Não foi possível excluir a conta."))
      setDeleting(false)
    }
  }

  function onSecretKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter") {
      event.preventDefault()
      void confirmDelete()
    }
  }

  const tabIndex = open ? 0 : -1

  return (
    <div className="px-6 pb-2">
      <div className="border-t border-border/50">
        <button
          type="button"
          onClick={() => {
            if (open) cancelDelete()
            setOpen((v) => !v)
          }}
          aria-expanded={open}
          className="-mx-3 flex w-[calc(100%+1.5rem)] items-center justify-between rounded-lg px-3 py-3 text-left transition-colors hover:bg-muted/50"
        >
          <span className="flex items-center gap-2 text-sm font-medium text-foreground">
            <ShieldCheck size={16} className="text-muted-foreground" />
            Privacidade e dados
          </span>
          <ChevronDown
            size={16}
            className={cn("text-muted-foreground transition-transform duration-200", open && "rotate-180")}
          />
        </button>

        <div
          className={cn(
            "grid ease-out",
            open ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
            reducedMotion ? "duration-0" : "duration-300",
          )}
          style={{ transitionProperty: "grid-template-rows" }}
        >
          <div className="overflow-hidden">
            <div className="space-y-4 pb-4 pt-1">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-medium text-foreground">Baixar meus dados</p>
                  <p className="text-xs text-muted-foreground">
                    Um arquivo JSON com seu perfil, sessões, cursos, metas, amizades e mensagens.
                  </p>
                </div>
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  onClick={() => void downloadData()}
                  disabled={exporting}
                  tabIndex={tabIndex}
                >
                  <Download className="size-4" />
                  {exporting ? "Gerando..." : "Baixar"}
                </Button>
              </div>

              <p className="text-xs text-muted-foreground">
                Veja como cuidamos dos seus dados na{" "}
                <LegalLink doc="privacy" className="underline underline-offset-4 hover:text-foreground" tabIndex={tabIndex}>
                  Política de Privacidade
                </LegalLink>{" "}
                e as regras nos{" "}
                <LegalLink doc="terms" className="underline underline-offset-4 hover:text-foreground" tabIndex={tabIndex}>
                  Termos de Uso
                </LegalLink>
                .
              </p>

              <div className="rounded-lg bg-destructive/5 px-3 py-3">
                <p className="text-sm font-medium text-destructive">Excluir minha conta</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  Apaga para sempre seu perfil, sessões, cursos, metas, roadmaps, amizades, mensagens e a conexão com
                  o GitHub. Não dá para desfazer.
                </p>
                {isOwner ? (
                  <p className="mt-2 text-xs text-muted-foreground">A conta owner não pode ser excluída por aqui.</p>
                ) : confirmingDelete ? (
                  <div className="mt-3 space-y-2">
                    <Label htmlFor="delete-account-secret" className="text-xs">
                      {usesPassword ? "Digite sua senha para confirmar" : `Digite seu e-mail (${user.email}) para confirmar`}
                    </Label>
                    <Input
                      id="delete-account-secret"
                      type={usesPassword ? "password" : "email"}
                      autoComplete={usesPassword ? "current-password" : "off"}
                      value={secret}
                      onChange={(e) => setSecret(e.target.value)}
                      onKeyDown={onSecretKeyDown}
                      tabIndex={tabIndex}
                      autoFocus
                    />
                    {error && <p className="text-xs text-destructive">{error}</p>}
                    <div className="flex justify-end gap-2">
                      <Button type="button" size="sm" variant="ghost" onClick={cancelDelete} tabIndex={tabIndex}>
                        Cancelar
                      </Button>
                      <Button
                        type="button"
                        size="sm"
                        variant="destructive"
                        disabled={deleting || !secret.trim()}
                        onClick={() => void confirmDelete()}
                        tabIndex={tabIndex}
                      >
                        {deleting ? "Excluindo..." : "Excluir conta"}
                      </Button>
                    </div>
                  </div>
                ) : (
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    className="mt-3 border-destructive/40 text-destructive hover:bg-destructive/10 hover:text-destructive"
                    onClick={() => setConfirmingDelete(true)}
                    tabIndex={tabIndex}
                  >
                    Excluir minha conta
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
