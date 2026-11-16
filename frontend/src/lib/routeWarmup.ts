import type { FetchQueryOptions } from "@tanstack/react-query"
import {
  byCategoryQuery,
  dashboardQuery,
  githubActivityQuery,
  githubInsightsQuery,
  heatmapQuery,
  queryClient,
  rankingQuery,
  roadmapsQuery,
  sessionsQuery,
} from "@/lib/queryClient"

export const pageLoaders = {
  dashboard: () => import("@/pages/DashboardPage").then((m) => ({ default: m.DashboardPage })),
  sessions: () => import("@/pages/SessionsPage").then((m) => ({ default: m.SessionsPage })),
  courses: () => import("@/pages/CoursesPage").then((m) => ({ default: m.CoursesPage })),
  roadmaps: () => import("@/pages/RoadmapsPage").then((m) => ({ default: m.RoadmapsPage })),
  friends: () => import("@/pages/FriendsPage").then((m) => ({ default: m.FriendsPage })),
  chat: () => import("@/pages/ChatPage").then((m) => ({ default: m.ChatPage })),
  ranking: () => import("@/pages/RankingPage").then((m) => ({ default: m.RankingPage })),
  github: () => import("@/pages/GitHubInsightsPage").then((m) => ({ default: m.GitHubInsightsPage })),
}

export type WarmContext = { githubConnected: boolean }

type WarmRoute = { load: () => Promise<unknown>; data?: (context: WarmContext) => void }

const WARM_STALE_MS = 30_000
const FIRST_DELAY_MS = 1500
const GAP_MS = 400
const HIDDEN_RETRY_MS = 2000
const IDLE_TIMEOUT_MS = 2000

function warm<TData>(options: FetchQueryOptions<TData, Error, TData, readonly unknown[]>) {
  void queryClient.prefetchQuery({ ...options, staleTime: WARM_STALE_MS })
}

function warmSessions() {
  void queryClient.prefetchInfiniteQuery({ ...sessionsQuery, pages: 1, staleTime: WARM_STALE_MS })
}

const WARM_ROUTES: Record<string, WarmRoute> = {
  "/dashboard": {
    load: pageLoaders.dashboard,
    data: ({ githubConnected }) => {
      warm(dashboardQuery)
      warm(heatmapQuery)
      warm(byCategoryQuery(30))
      if (githubConnected) warm(githubInsightsQuery)
    },
  },
  "/sessions": { load: pageLoaders.sessions, data: warmSessions },
  "/ranking": { load: pageLoaders.ranking, data: () => warm(rankingQuery("DAILY", "FRIENDS")) },
  "/github": {
    load: pageLoaders.github,
    data: ({ githubConnected }) => {
      if (!githubConnected) return
      warm(githubActivityQuery)
      warm(githubInsightsQuery)
    },
  },
  "/roadmaps": { load: pageLoaders.roadmaps, data: () => warm(roadmapsQuery) },
  "/courses": { load: pageLoaders.courses },
  "/friends": { load: pageLoaders.friends },
  "/chat": { load: pageLoaders.chat },
}

const IDLE_ORDER = ["/dashboard", "/sessions", "/ranking", "/github", "/roadmaps", "/courses", "/friends", "/chat"]

export function warmRoute(path: string, context: WarmContext) {
  const route = WARM_ROUTES[path]
  if (!route) return
  route.load().catch(() => {})
  route.data?.(context)
}

export function warmFocusData() {
  warm(dashboardQuery)
  warmSessions()
}

type SavingConnection = { saveData?: boolean; effectiveType?: string }

export function connectionAllowsWarmup() {
  const connection = (navigator as Navigator & { connection?: SavingConnection }).connection
  return !connection?.saveData && connection?.effectiveType !== "slow-2g" && connection?.effectiveType !== "2g"
}

function whenIdle(task: () => void) {
  if (typeof window.requestIdleCallback !== "function") {
    const id = window.setTimeout(task, 1)
    return () => window.clearTimeout(id)
  }
  const id = window.requestIdleCallback(task, { timeout: IDLE_TIMEOUT_MS })
  return () => window.cancelIdleCallback(id)
}

export function startIdleWarmup(context: WarmContext) {
  let index = 0
  let cancelled = false
  let timer: number | undefined
  let cancelIdle = () => {}

  function next() {
    if (cancelled || index >= IDLE_ORDER.length) return
    cancelIdle = whenIdle(() => {
      if (document.hidden) {
        timer = window.setTimeout(next, HIDDEN_RETRY_MS)
        return
      }
      warmRoute(IDLE_ORDER[index++], context)
      timer = window.setTimeout(next, GAP_MS)
    })
  }

  timer = window.setTimeout(next, FIRST_DELAY_MS)
  return () => {
    cancelled = true
    window.clearTimeout(timer)
    cancelIdle()
  }
}
