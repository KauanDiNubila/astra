import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import { usePomodoro } from "@/context/PomodoroContext"
import type { CourseDetail, ModuleItem } from "@/lib/types"
import { ModuleRow } from "@/components/ModuleRow"
import { PageSkeleton } from "@/components/PageSkeleton"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { Stepper } from "@/components/ui/stepper"

const MAX_INITIAL_LESSONS = 30

export function CourseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { courseId: pomodoroCourseId, loadCourseDetail } = usePomodoro()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [moduleTitle, setModuleTitle] = useState("")
  const [lessonCount, setLessonCount] = useState(0)
  const [saving, setSaving] = useState(false)

  function load() {
    return api.get<CourseDetail>(`/courses/${id}`).then((res) => setCourse(res.data))
  }

  async function refreshAll() {
    if (pomodoroCourseId === id) {
      await Promise.all([load(), loadCourseDetail()])
    } else {
      await load()
    }
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [id])

  async function addModule(event: FormEvent) {
    event.preventDefault()
    if (!course) return
    setSaving(true)
    try {
      const position = course.modules.length + 1
      await api.post<ModuleItem>(`/courses/${id}/modules`, {
        title: moduleTitle.trim() || `Módulo ${position}`,
        position,
        lessonCount: lessonCount || null,
      })
      setModuleTitle("")
      setLessonCount(0)
      await refreshAll()
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <PageSkeleton rows={4} />
  }

  if (!course) {
    return <p className="text-destructive">Curso não encontrado.</p>
  }

  return (
    <div className="flex flex-col gap-6">
      <Link to="/courses" className="text-sm text-muted-foreground hover:text-foreground">
        &larr; Cursos
      </Link>
      <div>
        <h1 className="text-2xl font-semibold">{course.title}</h1>
        {course.platform && <p className="text-muted-foreground">{course.platform}</p>}
      </div>
      <div className="flex items-center gap-3">
        <Progress value={Math.round(course.progress * 100)} className="max-w-xs" />
        <span className="text-sm text-muted-foreground">
          {course.completedLessons}/{course.totalLessons} aulas
        </span>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Módulos</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {course.modules.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhum módulo ainda.</p>
          ) : (
            <div className="flex flex-col gap-3">
              {course.modules.map((module) => (
                <ModuleRow key={module.id} module={module} courseId={id!} onChanged={refreshAll} />
              ))}
            </div>
          )}

          <form onSubmit={addModule} className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="module-title">Novo módulo (opcional)</Label>
              <Input
                id="module-title"
                placeholder={`Módulo ${course.modules.length + 1}`}
                value={moduleTitle}
                onChange={(e) => setModuleTitle(e.target.value)}
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label>Aulas</Label>
              <Stepper
                value={lessonCount}
                onChange={setLessonCount}
                min={0}
                max={MAX_INITIAL_LESSONS}
                aria-label="Quantidade de aulas"
              />
            </div>
            <Button type="submit" variant="outline" disabled={saving}>
              {saving ? "Adicionando..." : "Adicionar"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
