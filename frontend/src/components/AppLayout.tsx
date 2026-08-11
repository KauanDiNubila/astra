import { useState } from "react"
import type { ReactNode } from "react"
import { Moon, Sun } from "lucide-react"
import { NavLink } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { Button } from "@/components/ui/button"

const navClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "text-foreground" : "text-muted-foreground hover:text-foreground"

export function AppLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const [dark, setDark] = useState(false)

  function toggleTheme() {
    const next = !dark
    setDark(next)
    document.documentElement.classList.toggle("dark", next)
  }

  return (
    <div className="min-h-svh bg-background text-foreground">
      <header className="border-b">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-6">
            <span className="text-lg font-semibold">Astra</span>
            <nav className="flex items-center gap-4 text-sm">
              <NavLink to="/" end className={navClass}>
                Dashboard
              </NavLink>
              <NavLink to="/sessions" className={navClass}>
                Sessoes
              </NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-muted-foreground sm:inline">{user?.name}</span>
            <Button variant="ghost" size="icon" onClick={toggleTheme} aria-label="Alternar tema">
              {dark ? <Sun className="size-5" /> : <Moon className="size-5" />}
            </Button>
            <Button variant="outline" size="sm" onClick={logout}>
              Sair
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-8">{children}</main>
    </div>
  )
}
