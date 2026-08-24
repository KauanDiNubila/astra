export type ChimeId = "sino" | "suave" | "classico"

export const CHIME_OPTIONS: { id: ChimeId; label: string }[] = [
  { id: "sino", label: "Sino" },
  { id: "suave", label: "Suave" },
  { id: "classico", label: "Clássico" },
]

let audioCtx: AudioContext | null = null

function getAudioContext(): AudioContext | null {
  if (typeof window === "undefined") return null
  const Ctor = window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext
  if (!Ctor) return null
  if (!audioCtx) audioCtx = new Ctor()
  if (audioCtx.state === "suspended") audioCtx.resume()
  return audioCtx
}

function tone(ctx: AudioContext, freq: number, startTime: number, duration: number) {
  const osc = ctx.createOscillator()
  const gain = ctx.createGain()
  osc.type = "sine"
  osc.frequency.value = freq
  gain.gain.setValueAtTime(0, startTime)
  gain.gain.linearRampToValueAtTime(0.25, startTime + 0.02)
  gain.gain.linearRampToValueAtTime(0, startTime + duration)
  osc.connect(gain)
  gain.connect(ctx.destination)
  osc.start(startTime)
  osc.stop(startTime + duration)
}

const CHIMES: Record<ChimeId, (ctx: AudioContext, now: number) => void> = {
  sino: (ctx, now) => {
    tone(ctx, 740, now, 0.18)
    tone(ctx, 988, now + 0.16, 0.22)
  },
  suave: (ctx, now) => {
    tone(ctx, 523, now, 0.4)
  },
  classico: (ctx, now) => {
    tone(ctx, 659, now, 0.14)
    tone(ctx, 659, now + 0.16, 0.14)
    tone(ctx, 880, now + 0.32, 0.26)
  },
}

export function playChime(id: ChimeId) {
  const ctx = getAudioContext()
  if (!ctx) return
  const play = CHIMES[id] ?? CHIMES.sino
  play(ctx, ctx.currentTime)
}
