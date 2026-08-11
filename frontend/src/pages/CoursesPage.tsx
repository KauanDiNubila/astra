import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link } from "react-router-dom"
import { api } from "@/lib/api"
import type { CourseSummary } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"

export function CoursesPage() {
  const [courses, setCourses] = useState<CourseSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [title, setTitle] = useState("")
  const [platform, setPlatform] = useState("")
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
      await api.post("/courses", { title: title.trim(), platform: platform.trim() || null })
      setTitle("")
      setPlatform("")
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
              <Label htmlFor="title">Titulo</Label>
              <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            </div>
            <div className="flex flex-1 flex-col gap-2">
              <Label htmlFor="platform">Plataforma</Label>
              <Input id="platform" value={platform} onChange={(e) => setPlatform(e.target.value)} />
            </div>
            <Button type="submit" disabled={saving}>
              Criar
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
                      {course.completedModules}/{course.totalModules}
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
