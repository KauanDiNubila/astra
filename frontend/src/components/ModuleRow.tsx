import { useState } from "react"
import { Check, CheckCheck, Pencil, PlayCircle, Plus, X } from "lucide-react"
import { api } from "@/lib/api"
import { useModuleProgress } from "@/hooks/useModuleProgress"
import type { ModuleItem } from "@/lib/types"
import { CourseProgressPath } from "@/components/CourseProgressPath"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

type Props = {
  module: ModuleItem
  courseId: string
  onChanged: () => Promise<void>
}

export function ModuleRow({ module, courseId, onChanged }: Props) {
  const { saving, setLessonProgress, toggleModuleCompleted } = useModuleProgress(courseId, module, onChanged)
  const [addingLesson, setAddingLesson] = useState(false)
  const [editingTitle, setEditingTitle] = useState(false)
  const [titleDraft, setTitleDraft] = useState(module.title)
  const [savingTitle, setSavingTitle] = useState(false)

  async function addLesson() {
    setAddingLesson(true)
    try {
      const position = module.lessons.length + 1
      await api.post(`/courses/${courseId}/modules/${module.id}/lessons`, {
        title: `Aula ${position}`,
        position,
      })
      await onChanged()
    } finally {
      setAddingLesson(false)
    }
  }

  function cancelTitleEdit() {
    setTitleDraft(module.title)
    setEditingTitle(false)
  }

  async function saveTitle() {
    const trimmed = titleDraft.trim()
    if (!trimmed || trimmed === module.title) {
      cancelTitleEdit()
      return
    }
    setSavingTitle(true)
    try {
      await api.patch(`/courses/${courseId}/modules/${module.id}`, { title: trimmed })
      await onChanged()
      setEditingTitle(false)
    } finally {
      setSavingTitle(false)
    }
  }

  async function deleteLesson(lessonId: string) {
    await api.delete(`/courses/${courseId}/modules/${module.id}/lessons/${lessonId}`)
    await onChanged()
  }

  return (
    <div className="flex flex-col gap-3 rounded-lg border p-4">
      <div className="flex items-center justify-between gap-3">
        {editingTitle ? (
          <div className="motion-safe:animate-in motion-safe:fade-in-0 motion-safe:zoom-in-95 flex items-center gap-1 motion-safe:duration-150">
            <Input
              autoFocus
              value={titleDraft}
              onChange={(e) => setTitleDraft(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") saveTitle()
                if (e.key === "Escape") cancelTitleEdit()
              }}
              maxLength={160}
              style={{ width: `${Math.min(32, Math.max(8, titleDraft.length + 2))}ch` }}
              className="h-7 text-sm"
            />
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="size-6 shrink-0"
              onClick={saveTitle}
              disabled={savingTitle}
              title="Salvar"
            >
              <Check className="size-3.5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="size-6 shrink-0"
              onClick={cancelTitleEdit}
              title="Cancelar"
            >
              <X className="size-3.5" />
            </Button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => setEditingTitle(true)}
            className="group motion-safe:animate-in motion-safe:fade-in-0 motion-safe:zoom-in-95 flex items-center gap-1.5 text-left font-medium motion-safe:duration-150"
            title="Renomear módulo"
          >
            {module.title}
            <Pencil className="size-3 text-transparent transition-colors group-hover:text-muted-foreground" />
          </button>
        )}
        <div className="flex shrink-0 items-center gap-2">
          <Badge variant="secondary" className="gap-1">
            <PlayCircle className="size-3" />
            {module.completedLessons}/{module.totalLessons} aulas
          </Badge>
          <Button
            type="button"
            variant={module.completed ? "secondary" : "outline"}
            size="sm"
            className="h-7 gap-1 text-xs"
            onClick={toggleModuleCompleted}
            disabled={saving}
            title={module.completed ? "Desmarcar módulo" : "Marcar módulo como concluído"}
          >
            <CheckCheck className="size-3.5" />
            {module.completed ? "Concluído" : "Marcar tudo"}
          </Button>
        </div>
      </div>
      <div className="flex items-center gap-2">
        {module.lessons.length > 0 && (
          <CourseProgressPath
            items={module.lessons}
            onSetProgress={setLessonProgress}
            onDelete={deleteLesson}
            saving={saving}
          />
        )}
        <Button
          type="button"
          variant="outline"
          size="icon"
          className="size-6 shrink-0 rounded-full"
          onClick={addLesson}
          disabled={addingLesson}
          title="Adicionar aula"
        >
          <Plus className="size-3.5" />
        </Button>
      </div>
    </div>
  )
}
