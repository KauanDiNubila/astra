import { useEffect, useRef, useState } from "react"

export function useDelayedUnmount(visible: boolean, delayMs = 300) {
  const [rendered, setRendered] = useState(visible)

  useEffect(() => {
    if (visible) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), delayMs)
    return () => clearTimeout(timeout)
  }, [visible, delayMs])

  return visible || rendered
}

export function useFrozen<T>(value: T, frozen: boolean) {
  const ref = useRef(value)
  if (!frozen) ref.current = value
  return ref.current
}
