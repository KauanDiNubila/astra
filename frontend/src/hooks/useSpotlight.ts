import { useCallback } from "react"
import type { MouseEvent } from "react"

export function useSpotlight() {
  const onMouseMove = useCallback((e: MouseEvent<HTMLElement>) => {
    const rect = e.currentTarget.getBoundingClientRect()
    e.currentTarget.style.setProperty("--x", `${e.clientX - rect.left}px`)
    e.currentTarget.style.setProperty("--y", `${e.clientY - rect.top}px`)
  }, [])
  return { onMouseMove }
}
