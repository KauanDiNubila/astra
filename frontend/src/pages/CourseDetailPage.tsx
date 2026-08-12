import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseDetail } from "@/lib/types"
import { CourseProgressPath } from "@/components/CourseProgressPath"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Progress } from "@/components/ui/progress"

export function CourseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [moduleTitle, setModuleTitle] = useState("")
  const [settingProgress, setSettingProgress] = useState(false)

  function load() {
    return api.get<CourseDetail>(`/courses/${id}`).then((res) => setCourse(res.data))
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [id])

  async function setProgress(uptoPosition: number) {
    if (!course) return
    setSettingProgress(true)
    try {
      await Promise.all(
        course.modules.map((m) =>
          api.patch(`/courses/${id}/modules/${m.id}`, { completed: m.position <= uptoPosition }),
        ),
      )
      await load()
    } finally {
      setSettingProgress(false)
    }
  }

  async function addModule(event: FormEvent) {
    event.preventDefault()
    if (!moduleTitle.trim() || !course) return
    await api.post(`/courses/${id}/modules`, {
      title: moduleTitle.trim(),
      position: course.modules.length + 1,
    })
    setModuleTitle("")
    await load()
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
          {course.completedModules}/{course.totalModules}
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
            <CourseProgressPath
              modules={course.modules}
              onSetProgress={setProgress}
              saving={settingProgress}
            />
          )}
          <form onSubmit={addModule} className="flex gap-2">
            <Input
              placeholder="Novo módulo"
              value={moduleTitle}
              onChange={(e) => setModuleTitle(e.target.value)}
            />
            <Button type="submit" variant="outline">
              Adicionar
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
