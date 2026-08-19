import { CheckCheck } from "lucide-react"
import { useModuleProgress } from "@/hooks/useModuleProgress"
import type { ModuleItem } from "@/lib/types"
import { cn } from "@/lib/utils"
import { CourseProgressPath } from "@/components/CourseProgressPath"
import { Button } from "@/components/ui/button"

type Props = {
  courseId: string
  modules: ModuleItem[]
  selectedModuleId: string
  onSelectModule: (moduleId: string) => void
  onChanged: () => Promise<void>
}

export function FocusModuleProgress({ courseId, modules, selectedModuleId, onSelectModule, onChanged }: Props) {
  const selected = modules.find((m) => m.id === selectedModuleId) ?? modules[0]
  const { saving, setLessonProgress, toggleModuleCompleted } = useModuleProgress(courseId, selected, onChanged)

  return (
    <div className="flex flex-col items-center gap-2">
      {modules.length > 1 && (
        <div className="flex max-w-xs flex-wrap items-center justify-center gap-1">
          {modules.map((m) => (
            <button
              key={m.id}
              type="button"
              onClick={() => onSelectModule(m.id)}
              className={cn(
                "rounded-full px-2 py-0.5 text-xs transition-colors",
                m.id === selected.id
                  ? "bg-secondary text-secondary-foreground"
                  : "text-muted-foreground/70 hover:text-foreground",
                m.completed && "line-through",
              )}
            >
              {m.title}
            </button>
          ))}
        </div>
      )}

      <div className="flex items-center gap-2">
        {selected.lessons.length > 0 && (
          <CourseProgressPath items={selected.lessons} onSetProgress={setLessonProgress} saving={saving} />
        )}
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="size-6 shrink-0 rounded-full"
          onClick={toggleModuleCompleted}
          disabled={saving}
          title={selected.completed ? "Reabrir módulo" : "Concluir módulo"}
        >
          <CheckCheck className="size-3.5" />
        </Button>
      </div>
    </div>
  )
}
