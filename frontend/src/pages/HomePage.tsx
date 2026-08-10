import { useAuth } from "@/context/AuthContext"
import { Button } from "@/components/ui/button"

export function HomePage() {
  const { user, logout } = useAuth()

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 bg-background p-6 text-foreground">
      <h1 className="text-3xl font-semibold">Ola, {user?.name}</h1>
      <p className="text-muted-foreground">
        Voce esta logado no Astra. Em breve: seu dashboard.
      </p>
      <Button variant="outline" onClick={logout}>
        Sair
      </Button>
    </div>
  )
}
