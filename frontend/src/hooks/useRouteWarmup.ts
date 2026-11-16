import { useEffect, useRef } from "react"
import { useAuth } from "@/context/AuthContext"
import { useGitHub } from "@/context/GitHubContext"
import { connectionAllowsWarmup, startIdleWarmup, warmRoute } from "@/lib/routeWarmup"

const INTENT_DELAY_MS = 50

export function useIdleWarmup() {
  const { user } = useAuth()
  const { status, loading } = useGitHub()
  const userId = user?.id
  const githubConnected = !!status?.connected

  useEffect(() => {
    if (!userId || loading || !connectionAllowsWarmup()) return
    return startIdleWarmup({ githubConnected })
  }, [userId, loading, githubConnected])
}

export function useRouteIntent() {
  const { status } = useGitHub()
  const githubConnected = !!status?.connected
  const timer = useRef<number | undefined>(undefined)

  useEffect(() => () => window.clearTimeout(timer.current), [])

  return (to: string) => ({
    onPointerEnter: () => {
      window.clearTimeout(timer.current)
      timer.current = window.setTimeout(() => warmRoute(to, { githubConnected }), INTENT_DELAY_MS)
    },
    onPointerLeave: () => window.clearTimeout(timer.current),
    onFocus: () => warmRoute(to, { githubConnected }),
  })
}
