import { useState } from "react"
import { CheckCheck, PlayCircle, Plus } from "lucide-react"
import { api } from "@/lib/api"
import { useModuleProgress } from "@/hooks/useModuleProgress"
import type { ModuleItem } from "@/lib/types"
import { CourseProgressPath } from "@/components/CourseProgressPath"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"

type Props = {
  module: ModuleItem
  courseId: string
  onChanged: () => Promise<void>
}

export function ModuleRow({ module, courseId, onChanged }: Props) {
  const { saving, setLessonProgress, toggleModuleCompleted } = useModuleProgress(courseId, module, onChanged)
  const [addingLesson, setAddingLesson] = useState(false)

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

  return (
    <div className="flex flex-col gap-3 rounded-lg border p-4">
      <div className="flex items-center justify-between gap-3">
        <span className="font-medium">{module.title}</span>
        <div className="flex items-center gap-2">
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
          <CourseProgressPath items={module.lessons} onSetProgress={setLessonProgress} saving={saving} />
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
