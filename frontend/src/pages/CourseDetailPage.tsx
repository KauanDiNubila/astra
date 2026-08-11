import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link, useParams } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseDetail } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Progress } from "@/components/ui/progress"

export function CourseDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [moduleTitle, setModuleTitle] = useState("")

  function load() {
    return api.get<CourseDetail>(`/courses/${id}`).then((res) => setCourse(res.data))
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [id])

  async function toggle(moduleId: string, completed: boolean) {
    await api.patch(`/courses/${id}/modules/${moduleId}`, { completed })
    await load()
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
    return <p className="text-destructive">Curso nao encontrado.</p>
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
          <CardTitle>Modulos</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {course.modules.length === 0 ? (
            <p className="text-sm text-muted-foreground">Nenhum modulo ainda.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {course.modules.map((module) => (
                <li key={module.id} className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={module.completed}
                    onChange={(e) => toggle(module.id, e.target.checked)}
                    className="size-4"
                  />
                  <span className={module.completed ? "text-muted-foreground line-through" : ""}>
                    {module.title}
                  </span>
                </li>
              ))}
            </ul>
          )}
          <form onSubmit={addModule} className="flex gap-2">
            <Input
              placeholder="Novo modulo"
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
