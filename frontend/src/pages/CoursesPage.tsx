import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Minus, Plus } from "lucide-react"
import { Link } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseSummary } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"

const MAX_INITIAL_MODULES = 30

export function CoursesPage() {
  const [courses, setCourses] = useState<CourseSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [title, setTitle] = useState("")
  const [platform, setPlatform] = useState("")
  const [moduleCount, setModuleCount] = useState(0)
  const [saving, setSaving] = useState(false)

  function load() {
    return api.get<CourseSummary[]>("/courses").then((res) => setCourses(res.data))
  }

  useEffect(() => {
    load().finally(() => setLoading(false))
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) return
    setSaving(true)
    try {
      const res = await api.post<CourseSummary>("/courses", {
        title: title.trim(),
        platform: platform.trim() || null,
      })
      for (let i = 1; i <= moduleCount; i++) {
        await api.post(`/courses/${res.data.id}/modules`, { title: `Módulo ${i}`, position: i })
      }
      setTitle("")
      setPlatform("")
      setModuleCount(0)
      await load()
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <p className="text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">Cursos</h1>

      <Card>
        <CardHeader>
          <CardTitle>Novo curso</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="flex flex-col gap-4 sm:flex-row sm:items-end">
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="title">Título</Label>
              <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            </div>
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="platform">Plataforma</Label>
              <Input id="platform" value={platform} onChange={(e) => setPlatform(e.target.value)} />
            </div>
            <div className="flex flex-col gap-2">
              <Label>Módulos</Label>
              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  onClick={() => setModuleCount((n) => Math.max(0, n - 1))}
                  disabled={moduleCount === 0}
                >
                  <Minus className="size-4" />
                </Button>
                <span className="w-6 text-center tabular-nums">{moduleCount}</span>
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  onClick={() => setModuleCount((n) => Math.min(MAX_INITIAL_MODULES, n + 1))}
                  disabled={moduleCount === MAX_INITIAL_MODULES}
                >
                  <Plus className="size-4" />
                </Button>
              </div>
            </div>
            <Button type="submit" disabled={saving}>
              {saving ? "Criando..." : "Criar"}
            </Button>
          </form>
        </CardContent>
      </Card>

      {courses.length === 0 ? (
        <p className="text-sm text-muted-foreground">Nenhum curso ainda.</p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {courses.map((course) => (
            <Link key={course.id} to={`/courses/${course.id}`}>
              <Card className="h-full transition-colors hover:border-foreground/30">
                <CardHeader>
                  <div className="flex items-center justify-between gap-2">
                    <CardTitle className="text-base">{course.title}</CardTitle>
                    <Badge variant="secondary">
                      {course.completedLessons}/{course.totalLessons}
                    </Badge>
                  </div>
                  {course.platform && (
                    <p className="text-sm text-muted-foreground">{course.platform}</p>
                  )}
                </CardHeader>
                <CardContent>
                  <Progress value={Math.round(course.progress * 100)} />
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
