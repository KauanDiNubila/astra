import axios from "axios"
import type { AuthResponse } from "@/lib/types"

export const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080"

export const api = axios.create({ baseURL })

const ACCESS_TOKEN_KEY = "astra_token"
const REFRESH_TOKEN_KEY = "astra_refresh_token"

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(token: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function clearAccessToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string) {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export function clearRefreshToken() {
  localStorage.removeItem(REFRESH_TOKEN_KEY)
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
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error("Sem refresh token")
  }
  const res = await axios.post<AuthResponse>(`${baseURL}/auth/refresh`, { refreshToken })
  setAccessToken(res.data.accessToken)
  setRefreshToken(res.data.refreshToken)
  return res.data.accessToken
}

function logoutLocally() {
  clearAccessToken()
  clearRefreshToken()
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

    if (error.response?.status === 401) {
      logoutLocally()
    }
    return Promise.reject(error)
  },
)
