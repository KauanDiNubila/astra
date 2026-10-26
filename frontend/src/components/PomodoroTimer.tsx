import { useState } from "react"
import { ArrowLeft, Maximize2, Pause, Play, Plus, RotateCcw, Settings as SettingsIcon, SkipForward, Trash2 } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatMinutes } from "@/lib/format"
import { cn, fadeSwap } from "@/lib/utils"
import { CategoryPicker } from "@/components/CategoryPicker"
import { CoursePicker } from "@/components/CoursePicker"
import { ModuleRow } from "@/components/ModuleRow"
import { PomodoroDisplay } from "@/components/PomodoroDisplay"
import { PomodoroSettingsPanel } from "@/components/PomodoroSettingsPanel"
import { Button } from "@/components/ui/button"
import { Popover, PopoverAnchor, PopoverContent } from "@/components/ui/popover"
import { Textarea } from "@/components/ui/textarea"

type View = "timer" | "settings"

function IconButton({
  label,
  onClick,
  disabled,
  children,
}: {
  label: string
  onClick: () => void
  disabled?: boolean
  children: React.ReactNode
}) {
  return (
    <Button
      type="button"
      variant="ghost"
      size="icon"
      className="size-8 text-muted-foreground"
      title={label}
      aria-label={label}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </Button>
  )
}

export function PomodoroTimer() {
  const [view, setView] = useState<View>("timer")
  const [noteOpen, setNoteOpen] = useState(false)
  const [confirmDiscard, setConfirmDiscard] = useState(false)
  const reducedMotion = useReducedMotion()

  const {
    settings,
    setSettings,
    mode,
    isLongBreak,
    timeLeft,
    totalSeconds,
    running,
    focusedMinutes,
    completedPomodoros,
    handlePrimaryClick,
    skipBreak,
    resetCycle,
    discard,
    categories,
    courses,
    loadCategories,
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
    setFocusMode,
  } = usePomodoro()

  if (view === "settings") {
    return (
      <motion.div key="settings" {...fadeSwap(reducedMotion)} className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-2">
          <h2 className="text-sm font-medium">Configurações do pomodoro</h2>
          <Button type="button" variant="ghost" size="sm" className="gap-1.5" onClick={() => setView("timer")}>
            <ArrowLeft className="size-3.5" />
            Voltar
          </Button>
        </div>
        <PomodoroSettingsPanel settings={settings} onChange={setSettings} />
      </motion.div>
    )
  }

  const isBreak = mode === "break"
  const cycleLength = settings.pomodorosUntilLongBreak
  const cycle = isBreak ? Math.max(1, completedPomodoros % cycleLength || cycleLength) : (completedPomodoros % cycleLength) + 1
  const phase = isBreak ? (isLongBreak ? "Pausa longa" : "Pausa") : "Foco"
  const started = running || focusedMinutes > 0 || isBreak
  const showNote = noteOpen || note.length > 0

  function requestDiscard() {
    if (focusedMinutes > 0) setConfirmDiscard(true)
    else discard()
  }

  return (
    <motion.div key="timer" {...fadeSwap(reducedMotion)} className="flex flex-col gap-8">
      <div className="flex items-center justify-between gap-2">
        <span className="flex items-center gap-2 text-sm text-muted-foreground">
          <span
            className={cn(
              "size-1.5 rounded-full",
              running ? (isBreak ? "bg-amber-400" : "bg-emerald-500") : "bg-muted-foreground/50",
              running && "animate-pulse motion-reduce:animate-none",
            )}
          />
          {phase}
          {!settings.disableBreaks && (
            <span className="tabular-nums">
              · ciclo {cycle} de {cycleLength}
            </span>
          )}
        </span>
        <div className="flex items-center gap-0.5">
          {isBreak && (
            <IconButton label="Pular pausa" onClick={skipBreak}>
              <SkipForward className="size-4" />
            </IconButton>
          )}
          <IconButton label="Modo foco em tela cheia" onClick={() => setFocusMode(true)}>
            <Maximize2 className="size-4" />
          </IconButton>
          <IconButton label="Recomeçar este pomodoro" onClick={resetCycle}>
            <RotateCcw className="size-4" />
          </IconButton>
          <Popover open={confirmDiscard} onOpenChange={setConfirmDiscard}>
            <PopoverAnchor asChild>
              <span>
                <IconButton label="Descartar sessão" disabled={!started} onClick={requestDiscard}>
                  <Trash2 className="size-4" />
                </IconButton>
              </span>
            </PopoverAnchor>
            <PopoverContent align="end" className="w-60 gap-3 p-3">
              <p className="text-sm text-popover-foreground">
                Descartar {formatMinutes(focusedMinutes)} focados? Essa sessão não será salva.
              </p>
              <div className="flex gap-2">
                <Button type="button" variant="outline" size="sm" className="flex-1" onClick={() => setConfirmDiscard(false)}>
                  Cancelar
                </Button>
                <Button
                  type="button"
                  variant="destructive"
                  size="sm"
                  className="flex-1"
                  onClick={() => {
                    setConfirmDiscard(false)
                    discard()
                  }}
                >
                  Descartar
                </Button>
              </div>
            </PopoverContent>
          </Popover>
          <IconButton label="Configurações do pomodoro" onClick={() => setView("settings")}>
            <SettingsIcon className="size-4" />
          </IconButton>
        </div>
      </div>

      <div className="flex flex-col items-center gap-8">
        <div className="py-6">
          <PomodoroDisplay
            size={320}
            mode={mode}
            isLongBreak={isLongBreak}
            timeLeft={timeLeft}
            totalSeconds={totalSeconds}
            caption={
              focusedMinutes > 0
                ? `${formatMinutes(focusedMinutes)} focados`
                : started
                  ? isBreak
                    ? "Descansando"
                    : "Em foco"
                  : "Pronto para começar"
            }
          />
        </div>

        <div className="flex items-center gap-2">
          <Button type="button" size="lg" className="h-11 min-w-36 rounded-full px-6 text-[15px] has-data-[icon=inline-start]:pl-6" onClick={handlePrimaryClick}>
            {running ? <Pause data-icon="inline-start" /> : <Play data-icon="inline-start" className="fill-current" />}
            {running ? "Pausar" : started ? "Continuar" : "Iniciar foco"}
          </Button>
        </div>
      </div>

      <form onSubmit={saveSession} className="flex flex-col gap-4 border-t pt-6">
        <div className="flex flex-wrap items-center gap-2">
          <CategoryPicker chip categories={categories} value={categoryId} onChange={setCategoryId} onRefresh={loadCategories} />
          {courses.length > 0 && (
            <CoursePicker courses={courses} value={courseId} onChange={setCourseId} />
          )}
          {!showNote && (
            <button
              type="button"
              onClick={() => setNoteOpen(true)}
              className="flex h-8 items-center gap-1.5 rounded-full border border-dashed px-3 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
            >
              <Plus className="size-3.5" />
              Nota
            </button>
          )}
          {focusedMinutes > 0 && (
            <Button type="submit" size="sm" className="ml-auto h-8 rounded-full px-4" disabled={saving}>
              {saving ? "Salvando..." : `Salvar ${formatMinutes(focusedMinutes)}`}
            </Button>
          )}
        </div>

        {showNote && (
          <Textarea
            autoFocus={noteOpen && note.length === 0}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="O que você estudou?"
            aria-label="Nota"
            className="min-h-20 resize-none"
          />
        )}

        {courseDetail && courseDetail.modules.length > 0 && (
          <div className="flex flex-col gap-3">
            {courseDetail.modules.map((module) => (
              <ModuleRow key={module.id} module={module} courseId={courseId} onChanged={loadCourseDetail} />
            ))}
          </div>
        )}

        {error && <p className="text-sm text-destructive">{error}</p>}
      </form>
    </motion.div>
  )
}
