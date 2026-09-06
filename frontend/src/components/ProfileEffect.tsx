import { useMemo } from "react"
import { motion, useReducedMotion } from "motion/react"
import type { ProfileEffect as ProfileEffectName } from "@/lib/types"
import { cn } from "@/lib/utils"

function useParticles(count: number, seedKey: string) {
  // seedKey força recalcular quando o preset muda, sem precisar guardar
  // estado — cada partícula só precisa de posição/atraso fixos por render.
  return useMemo(
    () =>
      Array.from({ length: count }, (_, i) => ({
        id: `${seedKey}-${i}`,
        left: Math.random() * 100,
        delay: Math.random() * 3,
        duration: 2 + Math.random() * 2,
        drift: (Math.random() - 0.5) * 30,
      })),
    [count, seedKey],
  )
}

function Sparkles() {
  const particles = useParticles(10, "sparkles")
  return (
    <>
      {particles.map((p) => (
        <motion.span
          key={p.id}
          className="absolute size-1 rounded-full bg-white shadow-[0_0_6px_2px_rgba(255,255,255,0.8)]"
          style={{ left: `${p.left}%`, top: `${20 + (p.left % 60)}%` }}
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: [0, 1, 0], scale: [0, 1, 0] }}
          transition={{ duration: p.duration, delay: p.delay, repeat: Infinity, repeatDelay: 1.5 }}
        />
      ))}
    </>
  )
}

function Confetti() {
  const particles = useParticles(14, "confetti")
  const colors = ["#f43f5e", "#3b82f6", "#f59e0b", "#10b981", "#8b5cf6"]
  return (
    <>
      {particles.map((p, i) => (
        <motion.span
          key={p.id}
          className="absolute top-0 h-2 w-1 rounded-sm"
          style={{ left: `${p.left}%`, backgroundColor: colors[i % colors.length] }}
          initial={{ y: -10, opacity: 0, rotate: 0 }}
          animate={{ y: 110, opacity: [0, 1, 1, 0], rotate: 360, x: p.drift }}
          transition={{ duration: p.duration + 1.5, delay: p.delay, repeat: Infinity, ease: "linear" }}
        />
      ))}
    </>
  )
}

function Snow() {
  const particles = useParticles(12, "snow")
  return (
    <>
      {particles.map((p) => (
        <motion.span
          key={p.id}
          className="absolute top-0 size-1.5 rounded-full bg-white/80"
          style={{ left: `${p.left}%` }}
          initial={{ y: -10, opacity: 0 }}
          animate={{ y: 110, opacity: [0, 0.9, 0.9, 0], x: p.drift }}
          transition={{ duration: p.duration + 2, delay: p.delay, repeat: Infinity, ease: "linear" }}
        />
      ))}
    </>
  )
}

const EFFECTS: Record<ProfileEffectName, () => React.JSX.Element> = {
  SPARKLES: Sparkles,
  CONFETTI: Confetti,
  SNOW: Snow,
}

export function ProfileEffectOverlay({ effect, className }: { effect: ProfileEffectName | null; className?: string }) {
  const reducedMotion = useReducedMotion()
  if (!effect || reducedMotion) return null
  const Effect = EFFECTS[effect]
  return (
    <div className={cn("pointer-events-none absolute inset-0 overflow-hidden", className)}>
      <Effect />
    </div>
  )
}

export const PROFILE_EFFECT_OPTIONS: { value: ProfileEffectName; label: string }[] = [
  { value: "SPARKLES", label: "Brilho" },
  { value: "CONFETTI", label: "Confete" },
  { value: "SNOW", label: "Neve" },
]
