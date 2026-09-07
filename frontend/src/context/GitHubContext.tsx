import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import { api } from "@/lib/api"
import type { GitHubConnectionStatus } from "@/lib/types"

type GitHubContextValue = {
  status: GitHubConnectionStatus | null
  loading: boolean
  syncing: boolean
  connect: () => Promise<void>
  disconnect: () => Promise<void>
  sync: () => Promise<void>
  refresh: () => Promise<void>
  setVisibleToFriends: (visible: boolean) => Promise<void>
}

const GitHubContext = createContext<GitHubContextValue | undefined>(undefined)

export function GitHubProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<GitHubConnectionStatus | null>(null)
  const [loading, setLoading] = useState(true)
  const [syncing, setSyncing] = useState(false)

  async function refresh() {
    try {
      const res = await api.get<GitHubConnectionStatus>("/github/status")
      setStatus(res.data)
    } catch {
      /* empty */
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function connect() {
    const res = await api.get<{ url: string }>("/github/connect/authorize-url")
    window.location.href = res.data.url
  }

  async function disconnect() {
    await api.delete("/github/connection")
    await refresh()
  }

  async function sync() {
    setSyncing(true)
    try {
      await api.post("/github/sync")
      await refresh()
    } finally {
      setSyncing(false)
    }
  }

  async function setVisibleToFriends(visible: boolean) {
    setStatus((prev) => (prev ? { ...prev, visibleToFriends: visible } : prev))
    try {
      await api.patch("/github/visibility", { visibleToFriends: visible })
    } catch {
      await refresh()
    }
  }

  return (
    <GitHubContext.Provider
      value={{ status, loading, syncing, connect, disconnect, sync, refresh, setVisibleToFriends }}>
      {children}
    </GitHubContext.Provider>
  )
}

export function useGitHub() {
  const ctx = useContext(GitHubContext)
  if (!ctx) {
    throw new Error("useGitHub precisa estar dentro de um GitHubProvider")
  }
  return ctx
}
