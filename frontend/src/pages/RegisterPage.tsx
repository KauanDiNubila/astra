import { useState } from "react"
import type { FormEvent } from "react"
import { motion } from "motion/react"
import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { getErrorMessage } from "@/lib/api"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await register(name, email, password)
      navigate("/dashboard")
    } catch (err) {
      setError(getErrorMessage(err, "Não foi possível criar a conta. Tente novamente."))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-svh items-center justify-center bg-background p-6">
      <motion.div
        initial={{ opacity: 0, y: 16, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
        className="w-full max-w-sm"
      >
        <Card className="w-full">
          <CardHeader>
            <CardTitle className="text-2xl">Criar conta</CardTitle>
            <CardDescription>Comece a acompanhar seu tempo de estudo.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={onSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="name">Nome</Label>
                <Input
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  pattern="[^#]*"
                  title="Nome não pode conter '#'"
                  required
                />
              </div>
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
              <div className="flex flex-col gap-2">
                <Label htmlFor="password">Senha (min. 8 caracteres)</Label>
                <Input
                  id="password"
                  type="password"
                  minLength={8}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
              <Button type="submit" disabled={loading}>
                {loading ? "Criando..." : "Criar conta"}
              </Button>
              <p className="text-center text-sm text-muted-foreground">
                Já tem conta?{" "}
                <Link to="/login" className="underline underline-offset-4">
                  Entrar
                </Link>
              </p>
            </form>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  )
}
