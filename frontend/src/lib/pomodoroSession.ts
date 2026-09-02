export type PersistedPomodoroSession = {
  mode: "focus" | "break"
  isLongBreak: boolean
  running: boolean
  phaseEndAt: number | null
  timeLeft: number
  focusedSeconds: number
  completedPomodoros: number
  startedAt: string | null
  categoryId: string
  courseId: string
  note: string
  focusMode: boolean
}

const STORAGE_KEY = "astra:pomodoro-session"

// sessionStorage, não localStorage: sobrevive a um F5 (sobe o mesmo estado
// de volta), mas some ao fechar a aba/navegador — não ressuscita um
// cronômetro de dias atrás quando o usuário volta ao app.
export function loadPomodoroSession(): PersistedPomodoroSession | null {
  if (typeof window === "undefined") return null
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    return JSON.parse(raw) as PersistedPomodoroSession
  } catch {
    return null
  }
}

export function savePomodoroSession(session: PersistedPomodoroSession) {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch {
    // sessionStorage indisponível (aba anônima restrita, quota) não deve quebrar o timer
  }
}
