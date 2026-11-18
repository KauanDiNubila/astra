import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useNavigate } from "react-router-dom"
import { toast } from "sonner"
import { api, getErrorMessage } from "@/lib/api"
import { AuthCard } from "@/components/AuthCard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

function readTokenFromUrl() {
  return new URLSearchParams(window.location.hash.slice(1)).get("token")
}

function RequestNewLink() {
  return (
    <p className="mt-4 text-center text-sm text-muted-foreground">
      <Link to="/forgot-password" className="underline underline-offset-4">
        Pedir um novo link
      </Link>
    </p>
  )
}

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [token] = useState(readTokenFromUrl)
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    window.history.replaceState(window.history.state, "", window.location.pathname)
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await api.post("/auth/reset-password", { token, newPassword: password })
      toast.success("Senha alterada. Entre com a nova senha.")
      navigate("/login", { replace: true })
    } catch (err) {
      setError(getErrorMessage(err, "Não foi possível alterar a senha. Tente novamente."))
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <AuthCard
        title="Criar nova senha"
        description="Esse link está incompleto. Abra o link do e-mail de novo ou peça um novo."
      >
        <RequestNewLink />
      </AuthCard>
    )
  }

  return (
    <AuthCard
      title="Criar nova senha"
      description="Escolha a nova senha da sua conta. Depois disso, as sessões abertas em outros aparelhos são encerradas."
    >
      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label htmlFor="password">Nova senha (min. 8 caracteres)</Label>
          <Input
            id="password"
            type="password"
            autoComplete="new-password"
            minLength={8}
            maxLength={100}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button type="submit" disabled={loading}>
          {loading ? "Salvando..." : "Salvar nova senha"}
        </Button>
      </form>
      <RequestNewLink />
    </AuthCard>
  )
}
