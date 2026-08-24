import axios from "axios"
import type { AuthResponse } from "@/lib/types"

export const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080"

export const api = axios.create({ baseURL, withCredentials: true })

export function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === "string") {
    return error.response.data.message
  }
  return fallback
}

let accessToken: string | null = null

export function getAccessToken() {
  return accessToken
}

export function setAccessToken(token: string) {
  accessToken = token
}

export function clearAccessToken() {
  accessToken = null
}

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string> | null = null

async function performRefresh(): Promise<string> {
  const res = await axios.post<AuthResponse>(`${baseURL}/auth/refresh`, {}, { withCredentials: true })
  setAccessToken(res.data.accessToken)
  return res.data.accessToken
}

function logoutLocally() {
  clearAccessToken()
  if (!window.location.pathname.startsWith("/login")) {
    window.location.href = "/login"
  }
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const isAuthCall = typeof original?.url === "string" && original.url.includes("/auth/")

    if (error.response?.status === 401 && original && !original._retry && !isAuthCall) {
      original._retry = true
      try {
        refreshPromise ??= performRefresh().finally(() => {
          refreshPromise = null
        })
        const newAccessToken = await refreshPromise
        original.headers = original.headers ?? {}
        original.headers.Authorization = `Bearer ${newAccessToken}`
        return api(original)
      } catch {
        logoutLocally()
        return Promise.reject(error)
      }
    }

    if (error.response?.status === 401 && !isAuthCall) {
      logoutLocally()
    }
    return Promise.reject(error)
  },
)
