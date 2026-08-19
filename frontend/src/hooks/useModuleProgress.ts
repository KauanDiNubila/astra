import { useState } from "react"
import { api } from "@/lib/api"
import type { ModuleItem } from "@/lib/types"

export function useModuleProgress(courseId: string, module: ModuleItem, onChanged: () => Promise<void>) {
  const [saving, setSaving] = useState(false)

  async function setLessonProgress(uptoPosition: number) {
    setSaving(true)
    try {
      await Promise.all(
        module.lessons.map((l) =>
          api.patch(`/courses/${courseId}/modules/${module.id}/lessons/${l.id}`, {
            completed: l.position <= uptoPosition,
          }),
        ),
      )
      await onChanged()
    } finally {
      setSaving(false)
    }
  }

  async function toggleModuleCompleted() {
    if (module.lessons.length > 0) {
      await setLessonProgress(module.completed ? 0 : module.lessons.length)
      return
    }
    setSaving(true)
    try {
      await api.patch(`/courses/${courseId}/modules/${module.id}`, { completed: !module.completed })
      await onChanged()
    } finally {
      setSaving(false)
    }
  }

  return { saving, setLessonProgress, toggleModuleCompleted }
}
