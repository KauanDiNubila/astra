import { useEffect } from "react"

export function useCoversApp(active: boolean) {
  useEffect(() => {
    if (!active) return
    const root = document.documentElement
    root.dataset.appCovered = String(Number(root.dataset.appCovered ?? 0) + 1)
    return () => {
      const remaining = Number(root.dataset.appCovered ?? 1) - 1
      if (remaining > 0) root.dataset.appCovered = String(remaining)
      else delete root.dataset.appCovered
    }
  }, [active])
}
