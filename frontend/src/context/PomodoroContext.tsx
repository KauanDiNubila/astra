import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { useQuery } from "@tanstack/react-query"
import { api } from "@/lib/api"
import {
  fetchJson,
  invalidateCategories,
  invalidateCourses,
  dashboardQuery,
  invalidateStudyStats,
  queryClient,
  queryKeys,
} from "@/lib/queryClient"
import { loadPomodoroSettings, savePomodoroSettings } from "@/lib/pomodoroSettings"
import { loadPomodoroSession, savePomodoroSession } from "@/lib/pomodoroSession"
import { playChime } from "@/lib/sound"
import type { PomodoroSettings } from "@/lib/pomodoroSettings"
import type { Category, CourseDetail, CourseSummary } from "@/lib/types"

export type Mode = "focus" | "break"

const NO_CATEGORIES: Category[] = []
const NO_COURSES: CourseSummary[] = []

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
  skipBreak: () => void
  resetCycle: () => void
  discard: () => void

  categories: Category[]
  courses: CourseSummary[]
  coursesLoaded: boolean
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

  // Lido uma única vez (useState com inicializador) — sobrevive a um F5:
  // se a fase ainda não tinha terminado no momento do reload, o timer
  // volta rodando de onde parou em vez de resetar pro valor padrão.
  const [persisted] = useState(() => loadPomodoroSession())
  const persistedStillRunning =
    !!persisted?.running && persisted.phaseEndAt !== null && persisted.phaseEndAt > Date.now()

  const [mode, setMode] = useState<Mode>(persisted?.mode ?? "focus")
  const [isLongBreak, setIsLongBreak] = useState(persisted?.isLongBreak ?? false)
  const [timeLeft, setTimeLeft] = useState(() => {
    if (persistedStillRunning) return Math.round((persisted!.phaseEndAt! - Date.now()) / 1000)
    return persisted?.timeLeft ?? settings.focusMinutes * 60
  })
  const [running, setRunning] = useState(persistedStillRunning)
  const [focusedSeconds, setFocusedSeconds] = useState(persisted?.focusedSeconds ?? 0)
  const [completedPomodoros, setCompletedPomodoros] = useState(persisted?.completedPomodoros ?? 0)
  const completedRef = useRef(persisted?.completedPomodoros ?? 0)
  const startedAtRef = useRef<string | null>(persisted?.startedAt ?? null)

  // Ancoragem por relógio real, não por contagem de ticks — sobrevive a
  // throttling de setInterval quando a aba do navegador fica em segundo
  // plano. phaseEndAtRef é null enquanto pausado; enquanto rodando, é o
  // epoch ms em que a fase atual (foco/pausa) termina.
  const phaseEndAtRef = useRef<number | null>(persistedStillRunning ? persisted!.phaseEndAt : null)
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

  const [categoryId, setCategoryId] = useState(persisted?.categoryId ?? "")
  const [courseId, setCourseId] = useState(persisted?.courseId ?? "")
  const [note, setNote] = useState(persisted?.note ?? "")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [sessionSavedAt, setSessionSavedAt] = useState<number | null>(null)

  const [focusMode, setFocusMode] = useState(persisted?.focusMode ?? false)
  const [selectedModuleId, setSelectedModuleId] = useState<string | null>(null)

  useEffect(() => {
    if (focusMode) void queryClient.prefetchQuery(dashboardQuery)
  }, [focusMode])

  const categoriesQuery = useQuery({ queryKey: queryKeys.categories, queryFn: fetchJson<Category[]>("/categories") })
  const coursesQuery = useQuery({ queryKey: queryKeys.courses, queryFn: fetchJson<CourseSummary[]>("/courses") })
  const courseDetailQuery = useQuery({
    queryKey: queryKeys.course(courseId),
    queryFn: fetchJson<CourseDetail>(`/courses/${courseId}`),
    enabled: !!courseId,
  })
  const categories = categoriesQuery.data ?? NO_CATEGORIES
  const courses = coursesQuery.data ?? NO_COURSES
  const coursesLoaded = coursesQuery.isFetched
  const courseDetail = courseId ? (courseDetailQuery.data ?? null) : null

  function loadCategories() {
    return invalidateCategories()
  }

  function loadCourses() {
    return invalidateCourses()
  }

  function loadCourseDetail() {
    return invalidateCourses()
  }

  useEffect(() => {
    setSelectedModuleId(null)
  }, [courseId])

  function currentModeSeconds() {
    if (modeRef.current === "focus") return settingsRef.current.focusMinutes * 60
    return (isLongBreakRef.current ? settingsRef.current.longBreakMinutes : settingsRef.current.shortBreakMinutes) * 60
  }

  function startPhase(seconds: number, base = Date.now()) {
    phaseEndAtRef.current = base + seconds * 1000
    lastTickAtRef.current = base
    setTimeLeft(seconds)
  }

  function advancePhase(phaseEnd: number, late: boolean) {
    const s = settingsRef.current
    if (s.soundEnabled && !late) playChime(s.soundId)

    if (modeRef.current === "focus") {
      completedRef.current += 1
      setCompletedPomodoros(completedRef.current)

      if (s.disableBreaks) {
        startPhase(s.focusMinutes * 60, phaseEnd)
        return
      }

      const longBreak = s.pomodorosUntilLongBreak > 0 && completedRef.current % s.pomodorosUntilLongBreak === 0
      setIsLongBreak(longBreak)
      modeRef.current = "break"
      isLongBreakRef.current = longBreak
      setMode("break")
      const breakSeconds = (longBreak ? s.longBreakMinutes : s.shortBreakMinutes) * 60
      if (s.autoStartBreak) {
        startPhase(breakSeconds, phaseEnd)
      } else {
        setRunning(false)
        phaseEndAtRef.current = null
        setTimeLeft(breakSeconds)
      }
      return
    }

    modeRef.current = "focus"
    setMode("focus")
    const focusSeconds = s.focusMinutes * 60
    if (s.autoStartNextPomodoro) {
      startPhase(focusSeconds, phaseEnd)
    } else {
      setRunning(false)
      phaseEndAtRef.current = null
      setTimeLeft(focusSeconds)
    }
  }

  function recompute() {
    for (let guard = 0; guard < 100 && phaseEndAtRef.current !== null; guard++) {
      const now = Date.now()
      const phaseEnd = phaseEndAtRef.current
      const upTo = Math.min(now, phaseEnd)
      const deltaSeconds = Math.max(0, Math.round((upTo - lastTickAtRef.current) / 1000))
      lastTickAtRef.current = upTo

      if (modeRef.current === "focus" && deltaSeconds > 0) {
        setFocusedSeconds((sec) => sec + deltaSeconds)
      }

      const remainingSeconds = Math.round((phaseEnd - now) / 1000)
      if (remainingSeconds > 0) {
        setTimeLeft(remainingSeconds)
        return
      }
      advancePhase(phaseEnd, now - phaseEnd > 5000)
    }
  }

  // O pulso de 1s mora num Web Worker — o thread principal congela em
  // abas ocultas depois de alguns minutos em navegadores baseados em
  // Chromium, o que travava a troca de fase e o início automático da
  // pausa quando o usuário saía da aba. Workers não sofrem esse mesmo
  // congelamento, então a contagem continua rodando de verdade.
  const tickerWorkerRef = useRef<Worker | null>(null)

  useEffect(() => {
    const worker = new Worker(new URL("../workers/pomodoroTicker.worker.ts", import.meta.url), { type: "module" })
    worker.onmessage = (event: MessageEvent<{ type: string }>) => {
      if (event.data.type === "tick") recompute()
    }
    tickerWorkerRef.current = worker
    return () => {
      worker.terminate()
      tickerWorkerRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    tickerWorkerRef.current?.postMessage({ type: running ? "start" : "stop" })
  }, [running])

  useEffect(() => {
    if (!running) return
    // Recuperação extra pra quando o computador dorme (nem worker roda
    // nesse caso) — assim que a aba volta a ficar visível, recalcula na
    // hora em vez de esperar o próximo pulso do worker.
    function handleVisibility() {
      if (document.visibilityState === "visible") recompute()
    }
    document.addEventListener("visibilitychange", handleVisibility)
    return () => document.removeEventListener("visibilitychange", handleVisibility)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [running])

  const prevSettingsRef = useRef(settings)
  useEffect(() => {
    // Só reage quando settings de fato muda (referência nova, via
    // setSettings) — comparar por valor em vez de "primeira execução"
    // porque o StrictMode do React invoca efeitos de montagem duas vezes,
    // e uma flag de "já rodei uma vez" seria pulada só na primeira,
    // deixando a segunda sobrescrever o timeLeft recuperado do
    // sessionStorage assim que a página carrega.
    if (prevSettingsRef.current === settings) return
    prevSettingsRef.current = settings
    if (!running) {
      setTimeLeft(currentModeSeconds())
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [settings])

  useEffect(() => {
    savePomodoroSession({
      mode,
      isLongBreak,
      running,
      phaseEndAt: phaseEndAtRef.current,
      timeLeft,
      focusedSeconds,
      completedPomodoros,
      startedAt: startedAtRef.current,
      categoryId,
      courseId,
      note,
      focusMode,
    })
  }, [mode, isLongBreak, running, timeLeft, focusedSeconds, completedPomodoros, categoryId, courseId, note, focusMode])

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

  function skipBreak() {
    if (modeRef.current !== "break") return
    advancePhase(Date.now(), true)
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

  // round, não floor — floor sempre descarta o resto de segundos de cada
  // sessão (transições de fase, cliques) a favor de baixo, então mesmo
  // sessões "de 60min exatos" na prática perdiam minuto no total do dia.
  const focusedMinutes = Math.round(focusedSeconds / 60)
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
      void invalidateStudyStats()
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
        skipBreak,
        resetCycle,
        discard,
        categories,
        courses,
        coursesLoaded,
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
