import { useLocation } from "react-router-dom"
import { usePomodoro } from "@/context/PomodoroContext"
import { FocusModeOverlay } from "@/components/FocusModeOverlay"
import { FocusModuleProgress } from "@/components/FocusModuleProgress"
import { PomodoroFocusView } from "@/components/PomodoroFocusView"

export function GlobalPomodoroFocus() {
  const { pathname } = useLocation()
  const {
    mode,
    isLongBreak,
    timeLeft,
    totalSeconds,
    focusedMinutes,
    completedPomodoros,
    dailyGoal,
    settings,
    primaryLabel,
    handlePrimaryClick,
    courseId,
    courseDetail,
    note,
    focusMode,
    setFocusMode,
    selectedModuleId,
    setSelectedModuleId,
    loadCourseDetail,
  } = usePomodoro()

  const allLessons = courseDetail?.modules.flatMap((m) =>
    m.lessons.map((l) => ({ ...l, moduleTitle: m.title })),
  ) ?? []
  const currentIndex = allLessons.findIndex((l) => !l.completed)
  const currentLesson = currentIndex >= 0 ? allLessons[currentIndex] : undefined
  const nextLesson = currentIndex >= 0 ? allLessons[currentIndex + 1] : undefined
  const currentModule = courseDetail?.modules.find((m) => !m.completed) ?? courseDetail?.modules.at(-1)

  const sessionsUntilLongBreak = settings.pomodorosUntilLongBreak
  const sessionCaption = settings.disableBreaks
    ? `Sessão ${completedPomodoros + 1}`
    : `Sessão ${
        sessionsUntilLongBreak > 0 ? (completedPomodoros % sessionsUntilLongBreak) + 1 : completedPomodoros + 1
      } de ${sessionsUntilLongBreak}`

  const header = courseDetail
    ? {
        title: courseDetail.title,
        subtitle: currentLesson?.moduleTitle ?? "Curso concluído",
        progress: courseDetail.progress,
      }
    : null

  return (
    <FocusModeOverlay
      open={focusMode && pathname === "/sessions"}
      onExit={() => setFocusMode(false)}
      dailyGoal={dailyGoal}
      focusedMinutes={focusedMinutes}
      pomodoroMinutes={settings.focusMinutes}
      completedPomodoros={completedPomodoros}
      pomodorosUntilLongBreak={sessionsUntilLongBreak}
      disableBreaks={settings.disableBreaks}
      sessionCaption={sessionCaption}
    >
      <PomodoroFocusView
        ring={{ mode, isLongBreak, timeLeft, totalSeconds, sessionCaption }}
        primaryLabel={primaryLabel}
        onPrimaryClick={handlePrimaryClick}
        header={header}
        bottom={{
          currentLabel: currentLesson?.title,
          objective: note.trim() || undefined,
          nextLabel: nextLesson?.title,
          lessonsProgress: courseDetail
            ? `${courseDetail.completedLessons}/${courseDetail.totalLessons} aulas`
            : undefined,
          moduleProgress: courseDetail && courseDetail.modules.length > 0 && (
            <FocusModuleProgress
              courseId={courseId}
              modules={courseDetail.modules}
              selectedModuleId={selectedModuleId ?? currentModule?.id ?? courseDetail.modules[0].id}
              onSelectModule={setSelectedModuleId}
              onChanged={loadCourseDetail}
            />
          ),
        }}
      />
    </FocusModeOverlay>
  )
}
