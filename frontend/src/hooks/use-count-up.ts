import { useEffect, useRef, useState } from "react"

export function useCountUp(target: number, durationMs = 600) {
  const [value, setValue] = useState(target)
  const frameRef = useRef<number>(0)
  const startRef = useRef<number>(0)
  const fromRef = useRef(target)

  useEffect(() => {
    fromRef.current = value
    startRef.current = 0

    function tick(timestamp: number) {
      if (startRef.current === 0) startRef.current = timestamp
      const elapsed = timestamp - startRef.current
      const progress = Math.min(elapsed / durationMs, 1)
      const eased = 1 - Math.pow(1 - progress, 3)
      setValue(fromRef.current + (target - fromRef.current) * eased)
      if (progress < 1) frameRef.current = requestAnimationFrame(tick)
    }

    frameRef.current = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frameRef.current)
  }, [target, durationMs])

  return value
}
