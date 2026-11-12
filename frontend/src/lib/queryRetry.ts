import axios from "axios"

const MAX_RETRIES = 4
const MAX_DELAY_MS = 8000
const SERVER_DOWN = new Set([502, 503, 504])

export function shouldRetry(failureCount: number, error: unknown) {
  if (axios.isCancel(error)) return false
  if (!axios.isAxiosError(error)) return failureCount < 1
  if (!error.response || SERVER_DOWN.has(error.response.status)) return failureCount < MAX_RETRIES
  return error.response.status >= 500 && failureCount < 1
}

export function backoff(failureCount: number) {
  return Math.min(1000 * 2 ** failureCount, MAX_DELAY_MS)
}

export function retryDelay(failureCount: number) {
  return backoff(failureCount) * (0.5 + Math.random())
}
