import { useState } from "react"
import { ArrowLeft, Maximize2, Settings as SettingsIcon } from "lucide-react"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatMinutes } from "@/lib/format"
import { CategoryPicker } from "@/components/CategoryPicker"
import { ModuleRow } from "@/components/ModuleRow"
import { PomodoroDisplay } from "@/components/PomodoroDisplay"
import { PomodoroSettingsPanel } from "@/components/PomodoroSettingsPanel"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

type View = "timer" | "settings"

export function PomodoroTimer() {
  const [view, setView] = useState<View>("timer")

  const {
    settings,
    setSettings,
    mode,
    isLongBreak,
    timeLeft,
    totalSeconds,
    focusedMinutes,
    completedPomodoros,
    primaryLabel,
    handlePrimaryClick,
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
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-2">
          <h3 className="font-medium">Configurações do pomodoro</h3>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="gap-1.5"
            onClick={() => setView("timer")}
          >
            <ArrowLeft className="size-3.5" />
            Voltar
          </Button>
        </div>
        <PomodoroSettingsPanel settings={settings} onChange={setSettings} />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="relative flex flex-col items-center gap-3 rounded-lg border bg-muted/20 py-8">
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute left-3 top-3 size-8"
          title="Expandir modo foco"
          onClick={() => setFocusMode(true)}
        >
          <Maximize2 className="size-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="absolute right-3 top-3 size-8"
          title="Configurações do pomodoro"
          onClick={() => setView("settings")}
        >
          <SettingsIcon className="size-4" />
        </Button>

        <PomodoroDisplay size={220} mode={mode} isLongBreak={isLongBreak} timeLeft={timeLeft} totalSeconds={totalSeconds} />

        <div className="flex gap-2">
          <Button type="button" onClick={handlePrimaryClick}>
            {primaryLabel}
          </Button>
          <Button type="button" variant="outline" onClick={resetCycle}>
            Reiniciar ciclo
          </Button>
          <Button type="button" variant="ghost" onClick={discard}>
            Descartar
          </Button>
        </div>
        <p className="text-sm text-muted-foreground">
          {formatMinutes(focusedMinutes)} focados
          {completedPomodoros > 0 && !settings.disableBreaks
            ? ` · ${completedPomodoros % settings.pomodorosUntilLongBreak || settings.pomodorosUntilLongBreak}/${settings.pomodorosUntilLongBreak} até pausa longa`
            : completedPomodoros > 0
              ? ` · ${completedPomodoros} pomodoro(s) completo(s)`
              : ""}
        </p>
      </div>

      <form onSubmit={saveSession} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label>Categoria</Label>
          <CategoryPicker
            categories={categories}
            value={categoryId}
            onChange={setCategoryId}
            onRefresh={loadCategories}
          />
        </div>

        {courses.length > 0 && (
          <div className="flex flex-col gap-2">
            <Label>Curso (opcional)</Label>
            <Select
              value={courseId}
              onValueChange={(v) => setCourseId(v === "none" ? "" : v)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Nenhum curso" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">Nenhum curso</SelectItem>
                {courses.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.title}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}

        {courseDetail && (
          <div className="flex flex-col gap-3">
            {courseDetail.modules.length === 0 ? (
              <p className="text-sm text-muted-foreground">Nenhum módulo ainda.</p>
            ) : (
              courseDetail.modules.map((module) => (
                <ModuleRow
                  key={module.id}
                  module={module}
                  courseId={courseId}
                  onChanged={loadCourseDetail}
                />
              ))
            )}
          </div>
        )}

        <div className="flex flex-col gap-2">
          <Label htmlFor="pomodoro-note">Nota (opcional)</Label>
          <Textarea id="pomodoro-note" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>

        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button type="submit" disabled={saving}>
          {saving ? "Salvando..." : `Salvar sessão (${formatMinutes(focusedMinutes)})`}
        </Button>
      </form>
    </div>
  )
}
