import type { ChimeId } from "@/lib/sound"

export type PomodoroSettings = {
  focusMinutes: number
  shortBreakMinutes: number
  longBreakMinutes: number
  pomodorosUntilLongBreak: number
  autoStartNextPomodoro: boolean
  autoStartBreak: boolean
  disableBreaks: boolean
  soundEnabled: boolean
  soundId: ChimeId
}

export const DEFAULT_POMODORO_SETTINGS: PomodoroSettings = {
  focusMinutes: 25,
  shortBreakMinutes: 5,
  longBreakMinutes: 15,
  pomodorosUntilLongBreak: 4,
  autoStartNextPomodoro: false,
  autoStartBreak: false,
  disableBreaks: false,
  soundEnabled: true,
  soundId: "sino",
}

const STORAGE_KEY = "astra:pomodoro-settings"

export function loadPomodoroSettings(): PomodoroSettings {
  if (typeof window === "undefined") return DEFAULT_POMODORO_SETTINGS
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return DEFAULT_POMODORO_SETTINGS
    return { ...DEFAULT_POMODORO_SETTINGS, ...JSON.parse(raw) }
  } catch {
    return DEFAULT_POMODORO_SETTINGS
  }
}

export function savePomodoroSettings(settings: PomodoroSettings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
}
