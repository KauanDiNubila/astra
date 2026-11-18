import { useState } from "react"
import type { FormEvent } from "react"
import { Link } from "react-router-dom"
import { api, getErrorMessage } from "@/lib/api"
import { AuthCard } from "@/components/AuthCard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

function BackToLogin() {
  return (
    <p className="mt-4 text-center text-sm text-muted-foreground">
      <Link to="/login" className="underline underline-offset-4">
        Voltar para o login
      </Link>
    </p>
  )
}

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("")
  const [sent, setSent] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await api.post("/auth/forgot-password", { email })
      setSent(true)
    } catch (err) {
      setError(getErrorMessage(err, "Não foi possível enviar o link. Tente novamente."))
    } finally {
      setLoading(false)
    }
  }

  if (sent) {
    return (
      <AuthCard
        title="Esqueceu a senha?"
        description="Se existir uma conta com esse e-mail, enviamos um link para criar uma nova senha."
      >
        <p className="text-center text-sm text-muted-foreground">
          Não chegou? Confira o spam ou{" "}
          <button type="button" onClick={() => setSent(false)} className="underline underline-offset-4">
            tente de novo
          </button>
          .
        </p>
        <BackToLogin />
      </AuthCard>
    )
  }

  return (
    <AuthCard
      title="Esqueceu a senha?"
      description="Informe o e-mail da sua conta e enviaremos um link para criar uma nova senha."
    >
      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label htmlFor="email">E-mail</Label>
          <Input
            id="email"
            type="text"
            inputMode="email"
            autoComplete="email"
            pattern="[^\s@]+@[^\s@]+\.[^\s@]+"
            title="Digite um e-mail válido"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button type="submit" disabled={loading}>
          {loading ? "Enviando..." : "Enviar link"}
        </Button>
      </form>
      <BackToLogin />
    </AuthCard>
  )
}
