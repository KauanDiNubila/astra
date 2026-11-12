import axios from "axios"

const MAX_RETRIES = 4
const MAX_DELAY_MS = 8000
const SERVER_DOWN = new Set([502, 503, 504])

export function shouldRetry(failureCount: number, error: unknown) {
  if (axios.isCancel(error)) return false
  if (!axios.isAxiosError(error)) return failureCount < 1
  if (failureCount >= MAX_RETRIES) return false
  return !error.response || SERVER_DOWN.has(error.response.status)
}

export function retryDelay(failureCount: number) {
  return Math.min(1000 * 2 ** failureCount, MAX_DELAY_MS)
}
