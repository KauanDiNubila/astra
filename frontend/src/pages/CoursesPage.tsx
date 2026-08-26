import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { motion } from "motion/react"
import { Link } from "react-router-dom"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { usePomodoro } from "@/context/PomodoroContext"
import { useSpotlight } from "@/hooks/useSpotlight"
import { cn, gridItem, gridStagger, INTERACTIVE_CARD_CLASS, SPOTLIGHT_CLASS } from "@/lib/utils"
import type { CourseSummary } from "@/lib/types"
import { CardGridSkeleton } from "@/components/CardGridSkeleton"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { Stepper } from "@/components/ui/stepper"

const MAX_INITIAL_MODULES = 30

export function CoursesPage() {
  const [loading, setLoading] = useState(true)
  const [title, setTitle] = useState("")
  const [platform, setPlatform] = useState("")
  const [moduleCount, setModuleCount] = useState(0)
  const [saving, setSaving] = useState(false)
  const { onMouseMove } = useSpotlight()
  const { courses, loadCourses } = usePomodoro()

  useEffect(() => {
    loadCourses().finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      await loadCourses()
    } catch {
      toast.error("Não foi possível criar o curso.")
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <CardGridSkeleton />
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
              <Stepper
                value={moduleCount}
                onChange={setModuleCount}
                min={0}
                max={MAX_INITIAL_MODULES}
                aria-label="Quantidade de módulos"
              />
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
        <motion.div
          className="grid gap-4 sm:grid-cols-2"
          variants={gridStagger}
          initial="hidden"
          animate="show"
        >
          {courses.map((course) => (
            <motion.div key={course.id} variants={gridItem}>
              <Link to={`/courses/${course.id}`}>
                <Card
                  className={cn("h-full", INTERACTIVE_CARD_CLASS, SPOTLIGHT_CLASS)}
                  onMouseMove={onMouseMove}
                >
                  <CardHeader>
                    <div className="flex items-center justify-between gap-2">
                      <CardTitle className="text-base">{course.title}</CardTitle>
                      <Badge variant="secondary">
                        {course.completedLessons}/{course.totalLessons} aulas
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
            </motion.div>
          ))}
        </motion.div>
      )}
    </div>
  )
}
