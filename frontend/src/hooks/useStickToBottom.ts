import { useCallback, useEffect, useRef } from "react"
import type { RefObject } from "react"

const SETTLE_MS = 2000
const PINNED_DISTANCE_PX = 40

export function useStickToBottom(
  containerRef: RefObject<HTMLElement | null>,
  contentRef: RefObject<HTMLElement | null>,
  resetKey: string,
  settleKey: string,
) {
  const pinnedRef = useRef(true)
  const echoScrollTopRef = useRef<number | null>(null)
  const frameRef = useRef<number | null>(null)

  const stickToBottom = useCallback(() => {
    const container = containerRef.current
    if (!container || !pinnedRef.current) return
    const target = container.scrollHeight - container.clientHeight
    if (Math.abs(container.scrollTop - target) < 1) return
    echoScrollTopRef.current = target
    container.scrollTop = target
  }, [containerRef])

  const settle = useCallback(
    (durationMs: number) => {
      if (frameRef.current !== null) cancelAnimationFrame(frameRef.current)
      const deadline = performance.now() + durationMs
      function frame() {
        stickToBottom()
        frameRef.current = performance.now() < deadline ? requestAnimationFrame(frame) : null
      }
      frameRef.current = requestAnimationFrame(frame)
    },
    [stickToBottom],
  )

  useEffect(() => {
    const container = containerRef.current
    if (!container) return
    pinnedRef.current = true
    stickToBottom()
    settle(SETTLE_MS)

    function handleScroll() {
      if (!container) return
      const echo = echoScrollTopRef.current
      echoScrollTopRef.current = null
      if (echo !== null && Math.abs(container.scrollTop - echo) < 2) return
      const distance = container.scrollHeight - container.scrollTop - container.clientHeight
      pinnedRef.current = distance < PINNED_DISTANCE_PX
    }
    container.addEventListener("scroll", handleScroll, { passive: true })
    return () => container.removeEventListener("scroll", handleScroll)
  }, [containerRef, resetKey, settle, stickToBottom])

  useEffect(() => {
    settle(SETTLE_MS)
  }, [settleKey, settle])

  useEffect(() => {
    const content = contentRef.current
    if (!content) return
    const observer = new ResizeObserver(() => stickToBottom())
    observer.observe(content)
    return () => observer.disconnect()
  }, [contentRef, resetKey, stickToBottom])

  useEffect(() => {
    return () => {
      if (frameRef.current !== null) cancelAnimationFrame(frameRef.current)
    }
  }, [])

  return { settle }
}
