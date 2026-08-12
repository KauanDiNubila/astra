import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { ChevronDown, Minus, Plus } from "lucide-react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseDetail, ModuleItem } from "@/lib/types"
import { CourseProgressPath } from "@/components/CourseProgressPath"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"

const MAX_INITIAL_LESSONS = 30

function ModuleRow({
  module,
  courseId,
  expanded,
  onToggle,
  onChanged,
}: {
  module: ModuleItem
  courseId: string
  expanded: boolean
  onToggle: () => void
  onChanged: () => Promise<void>
}) {
  const [lessonTitle, setLessonTitle] = useState("")
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

  async function addLesson(event: FormEvent) {
    event.preventDefault()
    if (!lessonTitle.trim()) return
    await api.post(`/courses/${courseId}/modules/${module.id}/lessons`, {
      title: lessonTitle.trim(),
      position: module.lessons.length + 1,
    })
    setLessonTitle("")
    await onChanged()
  }

  return (
    <div className="rounded-lg border">
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center justify-between gap-3 p-4 text-left"
      >
        <span className="font-medium">{module.title}</span>
        <span className="flex items-center gap-3">
          <Badge variant="secondary">
            {module.completedLessons}/{module.totalLessons}
          </Badge>
          <ChevronDown className={`size-4 transition-transform ${expanded ? "rotate-180" : ""}`} />
        </span>
      </button>
      {expanded && (
        <div className="flex flex-col gap-4 border-t p-4">
          {module.lessons.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhuma aula ainda.</p>
          ) : (
            <CourseProgressPath items={module.lessons} onSetProgress={setLessonProgress} saving={saving} />
          )}
          <form onSubmit={addLesson} className="flex gap-2">
            <Input
              placeholder="Nova aula"
              value={lessonTitle}
              onChange={(e) => setLessonTitle(e.target.value)}
            />
            <Button type="submit" variant="outline">
              Adicionar
            </Button>
          </form>
        </div>
      )}
    </div>
  )
}

export function CourseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [moduleTitle, setModuleTitle] = useState("")
  const [lessonCount, setLessonCount] = useState(0)
  const [expandedModuleId, setExpandedModuleId] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  function load() {
    return api.get<CourseDetail>(`/courses/${id}`).then((res) => setCourse(res.data))
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [id])

  async function addModule(event: FormEvent) {
    event.preventDefault()
    if (!moduleTitle.trim() || !course) return
    setSaving(true)
    try {
      const res = await api.post<ModuleItem>(`/courses/${id}/modules`, {
        title: moduleTitle.trim(),
        position: course.modules.length + 1,
      })
      for (let i = 1; i <= lessonCount; i++) {
        await api.post(`/courses/${id}/modules/${res.data.id}/lessons`, {
          title: `Aula ${i}`,
          position: i,
        })
      }
      setModuleTitle("")
      setLessonCount(0)
      await load()
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
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
          {course.completedLessons}/{course.totalLessons}
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
                <ModuleRow
                  key={module.id}
                  module={module}
                  courseId={id!}
                  expanded={expandedModuleId === module.id}
                  onToggle={() =>
                    setExpandedModuleId((prev) => (prev === module.id ? null : module.id))
                  }
                  onChanged={load}
                />
              ))}
            </div>
          )}

          <form onSubmit={addModule} className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="module-title">Novo módulo</Label>
              <Input
                id="module-title"
                placeholder="Título do módulo"
                value={moduleTitle}
                onChange={(e) => setModuleTitle(e.target.value)}
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label>Aulas</Label>
              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  onClick={() => setLessonCount((n) => Math.max(0, n - 1))}
                  disabled={lessonCount === 0}
                >
                  <Minus className="size-4" />
                </Button>
                <span className="w-6 text-center tabular-nums">{lessonCount}</span>
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  onClick={() => setLessonCount((n) => Math.min(MAX_INITIAL_LESSONS, n + 1))}
                  disabled={lessonCount === MAX_INITIAL_LESSONS}
                >
                  <Plus className="size-4" />
                </Button>
              </div>
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
