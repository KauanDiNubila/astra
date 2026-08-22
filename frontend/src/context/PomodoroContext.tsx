import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { api } from "@/lib/api"
import { loadPomodoroSettings, savePomodoroSettings } from "@/lib/pomodoroSettings"
import type { PomodoroSettings } from "@/lib/pomodoroSettings"
import type { Category, CourseDetail, CourseSummary } from "@/lib/types"

export type Mode = "focus" | "break"

type PomodoroContextValue = {
  settings: PomodoroSettings
  setSettings: (settings: PomodoroSettings) => void
  mode: Mode
  isLongBreak: boolean
  timeLeft: number
  totalSeconds: number
  running: boolean
  focusedSeconds: number
  focusedMinutes: number
  completedPomodoros: number
  primaryLabel: string
  handlePrimaryClick: () => void
  resetCycle: () => void
  discard: () => void

  categories: Category[]
  courses: CourseSummary[]
  loadCategories: () => Promise<void>
  loadCourses: () => Promise<void>
  categoryId: string
  setCategoryId: (id: string) => void
  courseId: string
  setCourseId: (id: string) => void
  courseDetail: CourseDetail | null
  loadCourseDetail: () => Promise<void>
  note: string
  setNote: (note: string) => void
  saving: boolean
  error: string | null
  saveSession: (event: { preventDefault: () => void }) => Promise<void>
  sessionSavedAt: number | null

  focusMode: boolean
  setFocusMode: (open: boolean) => void
  selectedModuleId: string | null
  setSelectedModuleId: (id: string | null) => void
}

const PomodoroContext = createContext<PomodoroContextValue | undefined>(undefined)

export function PomodoroProvider({ children }: { children: ReactNode }) {
  const [settings, setSettings] = useState(loadPomodoroSettings)

  useEffect(() => {
    savePomodoroSettings(settings)
  }, [settings])

  const [mode, setMode] = useState<Mode>("focus")
  const [isLongBreak, setIsLongBreak] = useState(false)
  const [timeLeft, setTimeLeft] = useState(settings.focusMinutes * 60)
  const [running, setRunning] = useState(false)
  const [focusedSeconds, setFocusedSeconds] = useState(0)
  const [completedPomodoros, setCompletedPomodoros] = useState(0)
  const completedRef = useRef(0)
  const startedAtRef = useRef<string | null>(null)

  // Ancoragem por relógio real, não por contagem de ticks — sobrevive a
  // throttling de setInterval quando a aba do navegador fica em segundo
  // plano. phaseEndAtRef é null enquanto pausado; enquanto rodando, é o
  // epoch ms em que a fase atual (foco/pausa) termina.
  const phaseEndAtRef = useRef<number | null>(null)
  const lastTickAtRef = useRef<number>(Date.now())

  const modeRef = useRef(mode)
  useEffect(() => {
    modeRef.current = mode
  }, [mode])

  const isLongBreakRef = useRef(isLongBreak)
  useEffect(() => {
    isLongBreakRef.current = isLongBreak
  }, [isLongBreak])

  const settingsRef = useRef(settings)
  useEffect(() => {
    settingsRef.current = settings
  }, [settings])

  const [categoryId, setCategoryId] = useState("")
  const [courseId, setCourseId] = useState("")
  const [courseDetail, setCourseDetail] = useState<CourseDetail | null>(null)
  const [note, setNote] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [sessionSavedAt, setSessionSavedAt] = useState<number | null>(null)

  const [focusMode, setFocusMode] = useState(false)
  const [selectedModuleId, setSelectedModuleId] = useState<string | null>(null)

  const [categories, setCategories] = useState<Category[]>([])
  const [courses, setCourses] = useState<CourseSummary[]>([])

  function loadCategories() {
    return api.get<Category[]>("/categories").then((res) => setCategories(res.data))
  }

  function loadCourses() {
    return api.get<CourseSummary[]>("/courses").then((res) => setCourses(res.data))
  }

  useEffect(() => {
    loadCategories()
    loadCourses()
  }, [])

  function loadCourseDetail() {
    if (!courseId) {
      setCourseDetail(null)
      return Promise.resolve()
    }
    return api.get<CourseDetail>(`/courses/${courseId}`).then((res) => setCourseDetail(res.data))
  }

  useEffect(() => {
    loadCourseDetail()
    setSelectedModuleId(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [courseId])

  function currentModeSeconds() {
    if (modeRef.current === "focus") return settingsRef.current.focusMinutes * 60
    return (isLongBreakRef.current ? settingsRef.current.longBreakMinutes : settingsRef.current.shortBreakMinutes) * 60
  }

  function startPhase(seconds: number) {
    phaseEndAtRef.current = Date.now() + seconds * 1000
    lastTickAtRef.current = Date.now()
    setTimeLeft(seconds)
  }

  function advancePhase() {
    const s = settingsRef.current

    if (modeRef.current === "focus") {
      completedRef.current += 1
      setCompletedPomodoros(completedRef.current)

      if (s.disableBreaks) {
        startPhase(s.focusMinutes * 60)
        return
      }

      const longBreak = s.pomodorosUntilLongBreak > 0 && completedRef.current % s.pomodorosUntilLongBreak === 0
      setIsLongBreak(longBreak)
      setMode("break")
      const breakSeconds = (longBreak ? s.longBreakMinutes : s.shortBreakMinutes) * 60
      if (s.autoStartBreak) {
        startPhase(breakSeconds)
      } else {
        setRunning(false)
        phaseEndAtRef.current = null
        setTimeLeft(breakSeconds)
      }
      return
    }

    setMode("focus")
    const focusSeconds = s.focusMinutes * 60
    if (s.autoStartNextPomodoro) {
      startPhase(focusSeconds)
    } else {
      setRunning(false)
      phaseEndAtRef.current = null
      setTimeLeft(focusSeconds)
    }
  }

  function recompute() {
    if (phaseEndAtRef.current === null) return
    const now = Date.now()
    const deltaSeconds = Math.max(0, Math.round((now - lastTickAtRef.current) / 1000))
    const remainingSeconds = Math.max(0, Math.round((phaseEndAtRef.current - now) / 1000))
    lastTickAtRef.current = now

    if (modeRef.current === "focus" && deltaSeconds > 0) {
      setFocusedSeconds((sec) => sec + deltaSeconds)
    }

    if (remainingSeconds > 0) {
      setTimeLeft(remainingSeconds)
    } else {
      advancePhase()
    }
  }

  useEffect(() => {
    if (!running) return
    const id = setInterval(recompute, 1000)
    function handleVisibility() {
      if (document.visibilityState === "visible") recompute()
    }
    document.addEventListener("visibilitychange", handleVisibility)
    return () => {
      clearInterval(id)
      document.removeEventListener("visibilitychange", handleVisibility)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [running])

  useEffect(() => {
    if (!running) {
      setTimeLeft(currentModeSeconds())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings])

  function toggleRunning() {
    if (running) {
      setRunning(false)
      phaseEndAtRef.current = null
      return
    }
    if (!startedAtRef.current) {
      startedAtRef.current = new Date().toISOString()
    }
    phaseEndAtRef.current = Date.now() + timeLeft * 1000
    lastTickAtRef.current = Date.now()
    setRunning(true)
  }

  function handlePrimaryClick() {
    const startingUp = !running
    toggleRunning()
    if (startingUp) setFocusMode(true)
  }

  function resetCycle() {
    setRunning(false)
    phaseEndAtRef.current = null
    setTimeLeft(currentModeSeconds())
  }

  function discard() {
    setRunning(false)
    phaseEndAtRef.current = null
    setMode("focus")
    setIsLongBreak(false)
    setTimeLeft(settings.focusMinutes * 60)
    setFocusedSeconds(0)
    completedRef.current = 0
    setCompletedPomodoros(0)
    startedAtRef.current = null
    setFocusMode(false)
  }

  const focusedMinutes = Math.floor(focusedSeconds / 60)
  const totalSeconds = currentModeSeconds()
  const primaryLabel = running ? "Pausar" : focusMode ? "Continuar" : "Iniciar foco"

  async function saveSession(event: { preventDefault: () => void }) {
    event.preventDefault()
    setError(null)
    if (!categoryId) {
      setError("Escolha uma categoria.")
      return
    }
    if (focusedMinutes < 1) {
      setError("Foque por pelo menos 1 minuto antes de salvar.")
      return
    }
    setSaving(true)
    try {
      await api.post("/sessions", {
        categoryId,
        courseId: courseId || null,
        focusedMinutes,
        startedAt: startedAtRef.current ?? new Date().toISOString(),
        note: note.trim() || null,
      })
      discard()
      setNote("")
      setSessionSavedAt(Date.now())
    } catch {
      setError("Não foi possível registrar a sessão.")
    } finally {
      setSaving(false)
    }
  }

  return (
    <PomodoroContext.Provider
      value={{
        settings,
        setSettings,
        mode,
        isLongBreak,
        timeLeft,
        totalSeconds,
        running,
        focusedSeconds,
        focusedMinutes,
        completedPomodoros,
        primaryLabel,
        handlePrimaryClick,
        resetCycle,
        discard,
        categories,
        courses,
        loadCategories,
        loadCourses,
        categoryId,
        setCategoryId,
        courseId,
        setCourseId,
        courseDetail,
        loadCourseDetail,
        note,
        setNote,
        saving,
        error,
        saveSession,
        sessionSavedAt,
        focusMode,
        setFocusMode,
        selectedModuleId,
        setSelectedModuleId,
      }}
    >
      {children}
    </PomodoroContext.Provider>
  )
}

export function usePomodoro() {
  const ctx = useContext(PomodoroContext)
  if (!ctx) {
    throw new Error("usePomodoro precisa estar dentro de um PomodoroProvider")
  }
  return ctx
}
