import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import { api, clearToken, getToken, setToken } from "@/lib/api"
import type { User } from "@/lib/types"

type AuthContextValue = {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!getToken()) {
      setLoading(false)
      return
    }
    api
      .get<User>("/me")
      .then((res) => setUser(res.data))
      .catch(() => clearToken())
      .finally(() => setLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const res = await api.post<{ token: string }>("/auth/login", { email, password })
    setToken(res.data.token)
    const me = await api.get<User>("/me")
    setUser(me.data)
  }

  async function register(name: string, email: string, password: string) {
    await api.post("/auth/register", { name, email, password })
    await login(email, password)
  }

  function logout() {
    clearToken()
    setUser(null)
  }

  async function refreshUser() {
    const res = await api.get<User>("/me")
    setUser(res.data)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error("useAuth precisa estar dentro de um AuthProvider")
  }
  return ctx
}
