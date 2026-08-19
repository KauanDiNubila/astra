export function formatMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) {
    return `${mins}min`
  }
  if (mins === 0) {
    return `${hours}h`
  }
  return `${hours}h ${mins}min`
}

export function formatMinutesCompact(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}h${mins.toString().padStart(2, "0")}`
}

export function parseMinutesCompact(text: string): number | null {
  const trimmed = text.trim().toLowerCase()
  if (!trimmed) return null

  const colonMatch = trimmed.match(/^(\d+):(\d{1,2})$/)
  if (colonMatch) {
    return Number.parseInt(colonMatch[1], 10) * 60 + Number.parseInt(colonMatch[2], 10)
  }

  const hourMatch = trimmed.match(/^(\d+)h(\d{0,2})$/)
  if (hourMatch) {
    const hours = Number.parseInt(hourMatch[1], 10)
    const mins = hourMatch[2] ? Number.parseInt(hourMatch[2], 10) : 0
    return hours * 60 + mins
  }

  const plain = Number.parseInt(trimmed, 10)
  return Number.isNaN(plain) ? null : plain
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  })
}
