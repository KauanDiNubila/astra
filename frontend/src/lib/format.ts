export function formatMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) {
    return `${mins} min`
  }
  if (mins === 0) {
    return `${hours}h`
  }
  return `${hours}h ${mins} min`
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

  if (!/^\d+$/.test(trimmed)) return null

  // Número com 3+ dígitos: lê como "horas + minutos" (últimos dois dígitos),
  // igual ao formato de exibição (ex.: "220" -> 2h20), desde que os dois
  // últimos dígitos formem um minuto válido (00-59). Números de até 2
  // dígitos continuam minutos literais (ex.: "90" -> 90 min).
  if (trimmed.length > 2) {
    const mins = Number.parseInt(trimmed.slice(-2), 10)
    const hours = Number.parseInt(trimmed.slice(0, -2), 10)
    if (mins < 60) {
      return hours * 60 + mins
    }
  }

  return Number.parseInt(trimmed, 10)
}

export function formatRelativeTime(iso: string): string {
  const minutes = Math.floor((Date.now() - new Date(iso).getTime()) / 60000)
  if (minutes < 1) return "agora"
  if (minutes < 60) return `${minutes}min atrás`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h atrás`
  const days = Math.floor(hours / 24)
  return `${days}d atrás`
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  })
}
