import { QueryClient } from "@tanstack/react-query"
import type { QueryFunctionContext } from "@tanstack/react-query"
import { api } from "@/lib/api"

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 0,
      gcTime: 10 * 60 * 1000,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      retry: 1,
    },
  },
})

export const queryKeys = {
  dashboard: ["dashboard"] as const,
  heatmap: ["heatmap"] as const,
  byCategory: (days: number) => ["by-category", days] as const,
  byCategoryAll: ["by-category"] as const,
  githubInsights: ["github-insights"] as const,
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

function invalidate(...keys: readonly (readonly unknown[])[]) {
  return Promise.all(keys.map((queryKey) => queryClient.invalidateQueries({ queryKey }))).then(() => undefined)
}

export function invalidateStudyStats() {
  return invalidate(queryKeys.dashboard, queryKeys.heatmap, queryKeys.byCategoryAll, queryKeys.rankingAll)
}

export function invalidateGoals() {
  return invalidate(queryKeys.dashboard)
}

export function invalidateCategories() {
  return invalidate(queryKeys.categories, queryKeys.byCategoryAll)
}

export function invalidateCourses() {
  return invalidate(queryKeys.courses)
}

export function invalidateRoadmaps() {
  return invalidate(queryKeys.roadmaps)
}

export function invalidateRanking() {
  return invalidate(queryKeys.rankingAll)
}
