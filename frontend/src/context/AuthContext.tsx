import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import { api, clearAccessToken, setAccessToken } from "@/lib/api"
import type { AuthResponse, User } from "@/lib/types"

type AuthContextValue = {
  user: User | null
  loading: boolean
  avatarVersion: number
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [avatarVersion, setAvatarVersion] = useState(() => Date.now())

  useEffect(() => {
    api
      .post<AuthResponse>("/auth/refresh")
      .then((res) => {
        setAccessToken(res.data.accessToken)
        return api.get<User>("/me")
      })
      .then((res) => setUser(res.data))
      .catch(() => clearAccessToken())
      .finally(() => setLoading(false))
  }, [])

  async function login(email: string, password: string) {
    const res = await api.post<AuthResponse>("/auth/login", { email, password })
    setAccessToken(res.data.accessToken)
    const me = await api.get<User>("/me")
    setUser(me.data)
  }

  async function register(name: string, email: string, password: string) {
    await api.post("/auth/register", { name, email, password })
    await login(email, password)
  }

  async function logout() {
    try {
      await api.post("/auth/logout")
    } catch {
      /* empty */
    }
    clearAccessToken()
    setUser(null)
  }

  async function refreshUser() {
    const res = await api.get<User>("/me")
    setUser(res.data)
    setAvatarVersion(Date.now())
  }

  return (
    <AuthContext.Provider value={{ user, loading, avatarVersion, login, register, logout, refreshUser }}>
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
