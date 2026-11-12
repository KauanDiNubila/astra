import { infiniteQueryOptions, QueryClient, queryOptions } from "@tanstack/react-query"
import type { QueryFunctionContext } from "@tanstack/react-query"
import { api } from "@/lib/api"
import { retryDelay, shouldRetry } from "@/lib/queryRetry"
import type { Dashboard, Session } from "@/lib/types"

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 0,
      gcTime: 10 * 60 * 1000,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      retry: shouldRetry,
      retryDelay,
    },
  },
})

export const queryKeys = {
  dashboard: ["dashboard"] as const,
  heatmap: ["heatmap"] as const,
  byCategory: (days: number) => ["by-category", days] as const,
  byCategoryAll: ["by-category"] as const,
  githubInsights: ["github-insights"] as const,
  githubActivity: ["github-activity"] as const,
  sessions: ["sessions"] as const,
  ranking: (period: string, scope: string) => ["ranking", period, scope] as const,
  rankingAll: ["ranking"] as const,
  roadmaps: ["roadmaps"] as const,
  courses: ["courses"] as const,
  course: (id: string) => ["courses", id] as const,
  categories: ["categories"] as const,
}

export function fetchJson<T>(url: string, params?: Record<string, unknown>) {
  return ({ signal }: QueryFunctionContext) => api.get<T>(url, { signal, params }).then((res) => res.data)
}

export const dashboardQuery = queryOptions({
  queryKey: queryKeys.dashboard,
  queryFn: fetchJson<Dashboard>("/dashboard"),
})

const SESSIONS_PAGE_SIZE = 30

export const sessionsQuery = infiniteQueryOptions({
  queryKey: queryKeys.sessions,
  queryFn: ({ pageParam, signal }) =>
    api.get<Session[]>("/sessions", { signal, params: { page: pageParam, size: SESSIONS_PAGE_SIZE } }).then((res) => res.data),
  initialPageParam: 0,
  getNextPageParam: (lastPage, allPages) => (lastPage.length === SESSIONS_PAGE_SIZE ? allPages.length : undefined),
})

const FOCUS_PREFETCH_STALE_MS = 30_000

export function warmFocusData() {
  void queryClient.prefetchQuery(dashboardQuery)
  void queryClient.prefetchInfiniteQuery({ ...sessionsQuery, pages: 1, staleTime: FOCUS_PREFETCH_STALE_MS })
}

function invalidate(...keys: readonly (readonly unknown[])[]) {
  return Promise.all(
    keys.map((queryKey) => queryClient.invalidateQueries({ queryKey, refetchType: "all" })),
  ).then(() => undefined)
}

export function invalidateStudyStats() {
  return invalidate(queryKeys.dashboard, queryKeys.heatmap, queryKeys.byCategoryAll, queryKeys.rankingAll, queryKeys.sessions)
}

export function invalidateGoals() {
  return invalidate(queryKeys.dashboard)
}

export function invalidateCategories() {
  return invalidate(queryKeys.categories, queryKeys.byCategoryAll, queryKeys.sessions)
}

export function invalidateCourses() {
  return invalidate(queryKeys.courses)
}

export function invalidateRoadmaps() {
  return invalidate(queryKeys.roadmaps)
}

export function invalidateGitHub() {
  return invalidate(queryKeys.githubInsights, queryKeys.githubActivity)
}

export function invalidateRanking() {
  return invalidate(queryKeys.rankingAll)
}
