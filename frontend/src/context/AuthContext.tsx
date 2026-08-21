import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import {
  api,
  clearAccessToken,
  clearRefreshToken,
  getAccessToken,
  getRefreshToken,
  setAccessToken,
  setRefreshToken,
} from "@/lib/api"
import type { AuthResponse, User } from "@/lib/types"

type AuthContextValue = {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!getAccessToken()) {
      setLoading(false)
      return
    }
    api
      .get<User>("/me")
      .then((res) => setUser(res.data))
      .catch(() => {
        clearAccessToken()
        clearRefreshToken()
      })
      .finally(() => setLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const res = await api.post<AuthResponse>("/auth/login", { email, password })
    setAccessToken(res.data.accessToken)
    setRefreshToken(res.data.refreshToken)
    const me = await api.get<User>("/me")
    setUser(me.data)
  }

  async function register(name: string, email: string, password: string) {
    await api.post("/auth/register", { name, email, password })
    await login(email, password)
  }

  async function logout() {
    const refreshToken = getRefreshToken()
    if (refreshToken) {
      try {
        await api.post("/auth/logout", { refreshToken })
      } catch {
        /* empty */
      }
    }
    clearAccessToken()
    clearRefreshToken()
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
