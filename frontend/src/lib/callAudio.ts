import { getAudioContext, tone } from "@/lib/sound"

const SPEAKING_THRESHOLD = 0.04
const QUIET_FRAMES_TO_STOP = 6

export function watchSpeaking(stream: MediaStream, onChange: (speaking: boolean) => void) {
  const ctx = getAudioContext()
  if (!ctx || stream.getAudioTracks().length === 0) return () => {}

  const source = ctx.createMediaStreamSource(stream)
  const analyser = ctx.createAnalyser()
  analyser.fftSize = 512
  source.connect(analyser)
  const samples = new Uint8Array(analyser.fftSize)
  let speaking = false
  let quietFrames = 0

  const interval = setInterval(() => {
    analyser.getByteTimeDomainData(samples)
    let sum = 0
    for (const value of samples) {
      const normalized = (value - 128) / 128
      sum += normalized * normalized
    }
    const level = Math.sqrt(sum / samples.length)
    if (level > SPEAKING_THRESHOLD) {
      quietFrames = 0
      if (!speaking) {
        speaking = true
        onChange(true)
      }
    } else if (speaking && ++quietFrames > QUIET_FRAMES_TO_STOP) {
      speaking = false
      onChange(false)
    }
  }, 100)

  return () => {
    clearInterval(interval)
    source.disconnect()
    if (speaking) onChange(false)
  }
}

export function startRingtone() {
  const ctx = getAudioContext()
  if (!ctx) return () => {}

  function ring() {
    if (!ctx) return
    const now = ctx.currentTime
    tone(ctx, 659, now, 0.22, 0.18)
    tone(ctx, 523, now + 0.24, 0.22, 0.18)
    tone(ctx, 659, now + 0.6, 0.22, 0.18)
    tone(ctx, 523, now + 0.84, 0.22, 0.18)
  }

  ring()
  const interval = setInterval(ring, 2400)
  return () => clearInterval(interval)
}

export function startRingback() {
  const ctx = getAudioContext()
  if (!ctx) return () => {}

  function beep() {
    if (!ctx) return
    tone(ctx, 425, ctx.currentTime, 1.1, 0.1)
  }

  beep()
  const interval = setInterval(beep, 4000)
  return () => clearInterval(interval)
}
