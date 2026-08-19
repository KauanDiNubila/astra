import { useEffect, useRef, useState } from "react"

type MousePosition = { x: number; y: number }

function useMousePosition(): MousePosition {
  const [mousePosition, setMousePosition] = useState<MousePosition>({ x: 0, y: 0 })

  useEffect(() => {
    function handleMouseMove(event: MouseEvent) {
      setMousePosition({ x: event.clientX, y: event.clientY })
    }
    window.addEventListener("mousemove", handleMouseMove)
    return () => window.removeEventListener("mousemove", handleMouseMove)
  }, [])

  return mousePosition
}

function hexToRgb(hex: string): [number, number, number] {
  let value = hex.replace("#", "")
  if (value.length === 3) {
    value = value
      .split("")
      .map((char) => char + char)
      .join("")
  }
  const hexInt = parseInt(value, 16)
  return [(hexInt >> 16) & 255, (hexInt >> 8) & 255, hexInt & 255]
}

type Circle = {
  x: number
  y: number
  translateX: number
  translateY: number
  size: number
  alpha: number
  targetAlpha: number
  dx: number
  dy: number
  magnetism: number
}

type Props = {
  className?: string
  quantity?: number
  staticity?: number
  ease?: number
  size?: number
  color?: string
  vx?: number
  vy?: number
}

export function Particles({
  className = "",
  quantity = 100,
  staticity = 50,
  ease = 50,
  size = 0.4,
  color = "#ffffff",
  vx = 0,
  vy = 0,
}: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const canvasContainerRef = useRef<HTMLDivElement>(null)
  const context = useRef<CanvasRenderingContext2D | null>(null)
  const circles = useRef<Circle[]>([])
  const mousePosition = useMousePosition()
  const mouse = useRef({ x: 0, y: 0 })
  const canvasSize = useRef({ w: 0, h: 0 })
  const dpr = typeof window !== "undefined" ? window.devicePixelRatio : 1
  const rafRef = useRef<number>(0)

  useEffect(() => {
    if (canvasRef.current) {
      context.current = canvasRef.current.getContext("2d")
    }

    const rgb = hexToRgb(color)

    function resizeCanvas() {
      if (!canvasContainerRef.current || !canvasRef.current || !context.current) return
      circles.current.length = 0
      canvasSize.current.w = canvasContainerRef.current.offsetWidth
      canvasSize.current.h = canvasContainerRef.current.offsetHeight
      canvasRef.current.width = canvasSize.current.w * dpr
      canvasRef.current.height = canvasSize.current.h * dpr
      canvasRef.current.style.width = `${canvasSize.current.w}px`
      canvasRef.current.style.height = `${canvasSize.current.h}px`
      context.current.scale(dpr, dpr)
    }

    function circleParams(): Circle {
      const x = Math.floor(Math.random() * canvasSize.current.w)
      const y = Math.floor(Math.random() * canvasSize.current.h)
      const pSize = Math.floor(Math.random() * 2) + size
      const targetAlpha = Number.parseFloat((Math.random() * 0.6 + 0.1).toFixed(1))
      const dx = (Math.random() - 0.5) * 0.2
      const dy = (Math.random() - 0.5) * 0.2
      const magnetism = 0.1 + Math.random() * 4
      return { x, y, translateX: 0, translateY: 0, size: pSize, alpha: 0, targetAlpha, dx, dy, magnetism }
    }

    function clearContext() {
      context.current?.clearRect(0, 0, canvasSize.current.w, canvasSize.current.h)
    }

    function drawCircle(circle: Circle, update = false) {
      if (!context.current) return
      const { x, y, translateX, translateY, size: circleSize, alpha } = circle
      context.current.translate(translateX, translateY)
      context.current.beginPath()
      context.current.arc(x, y, circleSize, 0, 2 * Math.PI)
      context.current.fillStyle = `rgba(${rgb.join(", ")}, ${alpha})`
      context.current.fill()
      context.current.setTransform(dpr, 0, 0, dpr, 0, 0)
      if (!update) circles.current.push(circle)
    }

    function drawParticles() {
      clearContext()
      for (let i = 0; i < quantity; i++) {
        drawCircle(circleParams())
      }
    }

    function remapValue(value: number, start1: number, end1: number, start2: number, end2: number) {
      const remapped = ((value - start1) * (end2 - start2)) / (end1 - start1) + start2
      return remapped > 0 ? remapped : 0
    }

    function animate() {
      clearContext()
      circles.current.forEach((circle, i) => {
        const edge = [
          circle.x + circle.translateX - circle.size,
          canvasSize.current.w - circle.x - circle.translateX - circle.size,
          circle.y + circle.translateY - circle.size,
          canvasSize.current.h - circle.y - circle.translateY - circle.size,
        ]
        const closestEdge = edge.reduce((a, b) => Math.min(a, b))
        const remapClosestEdge = Number.parseFloat(remapValue(closestEdge, 0, 20, 0, 1).toFixed(2))

        if (remapClosestEdge > 1) {
          circle.alpha += 0.02
          if (circle.alpha > circle.targetAlpha) circle.alpha = circle.targetAlpha
        } else {
          circle.alpha = circle.targetAlpha * remapClosestEdge
        }

        circle.x += circle.dx + vx
        circle.y += circle.dy + vy
        circle.translateX +=
          (mouse.current.x / (staticity / circle.magnetism) - circle.translateX) / ease
        circle.translateY +=
          (mouse.current.y / (staticity / circle.magnetism) - circle.translateY) / ease

        drawCircle(circle, true)

        if (
          circle.x < -circle.size ||
          circle.x > canvasSize.current.w + circle.size ||
          circle.y < -circle.size ||
          circle.y > canvasSize.current.h + circle.size
        ) {
          circles.current.splice(i, 1)
          drawCircle(circleParams())
        }
      })
      rafRef.current = window.requestAnimationFrame(animate)
    }

    resizeCanvas()
    drawParticles()
    animate()

    window.addEventListener("resize", resizeCanvas)
    return () => {
      window.removeEventListener("resize", resizeCanvas)
      window.cancelAnimationFrame(rafRef.current)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [color, quantity, staticity, ease, size, vx, vy])

  useEffect(() => {
    if (!canvasRef.current) return
    const rect = canvasRef.current.getBoundingClientRect()
    const { w, h } = canvasSize.current
    const x = mousePosition.x - rect.left - w / 2
    const y = mousePosition.y - rect.top - h / 2
    const inside = x < w / 2 && x > -w / 2 && y < h / 2 && y > -h / 2
    if (inside) {
      mouse.current.x = x
      mouse.current.y = y
    }
  }, [mousePosition])

  return (
    <div className={className} ref={canvasContainerRef} aria-hidden="true">
      <canvas ref={canvasRef} />
    </div>
  )
}
