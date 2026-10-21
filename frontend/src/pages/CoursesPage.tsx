import { useEffect, useMemo, useState } from "react"
import { motion, useReducedMotion } from "motion/react"
import { ArrowUpRight, BookOpen, Check, Plus } from "lucide-react"
import { Link } from "react-router-dom"
import { usePomodoro } from "@/context/PomodoroContext"
import { formatDaysAgo } from "@/lib/format"
import { cn, fadeSwap, gridItem, gridStagger } from "@/lib/utils"
import type { CourseSummary } from "@/lib/types"
import { CardGridSkeleton } from "@/components/CardGridSkeleton"
import { NewCourseModal } from "@/components/NewCourseModal"
import { PillToggleButton } from "@/components/PillToggleButton"
import { Button } from "@/components/ui/button"

type Filter = "all" | "active" | "done"

function isDone(course: CourseSummary) {
  return course.status === "DONE" || (course.totalLessons > 0 && course.completedLessons >= course.totalLessons)
}

function CourseCard({ course }: { course: CourseSummary }) {
  const reducedMotion = useReducedMotion()
  const done = isDone(course)
  const pct = Math.round(course.progress * 100)
  const remaining = Math.max(0, course.totalLessons - course.completedLessons)

  return (
    <Link
      to={`/courses/${course.id}`}
      className="group flex h-full flex-col gap-6 rounded-2xl border p-5 transition-colors duration-200 hover:border-foreground/20 hover:bg-muted/20 sm:p-6"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-xs font-medium tracking-wide text-muted-foreground uppercase">
            {course.platform || "Curso"}
          </p>
          <h2 className="mt-1.5 line-clamp-2 font-semibold tracking-tight break-words" title={course.title}>
            {course.title}
          </h2>
        </div>
        <ArrowUpRight className="size-4 shrink-0 text-muted-foreground opacity-0 transition-all duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100" />
      </div>

      <div className="mt-auto flex flex-col gap-3">
        <div className="flex items-end justify-between gap-3">
          <span className="text-3xl font-semibold tracking-[-0.03em] tabular-nums">
            {pct}
            <span className="ml-0.5 text-lg font-medium text-muted-foreground">%</span>
          </span>
          {done ? (
            <span className="flex items-center gap-1 rounded-full bg-emerald-500/10 px-2 py-0.5 text-xs font-medium text-emerald-500">
              <Check className="size-3" />
              Concluído
            </span>
          ) : (
            <span className="text-right text-xs text-muted-foreground tabular-nums">
              {course.totalLessons === 0
                ? "Sem aulas ainda"
                : `${course.completedLessons} de ${course.totalLessons} aulas · faltam ${remaining}`}
              {course.lastStudiedAt && (
                <span className="block">Estudado {formatDaysAgo(course.lastStudiedAt)}</span>
              )}
            </span>
          )}
        </div>
        <div className="h-1.5 overflow-hidden rounded-full bg-muted">
          <motion.div
            className={cn("h-full rounded-full", done ? "bg-emerald-500" : "bg-foreground/80")}
            initial={{ width: reducedMotion ? `${pct}%` : 0 }}
            animate={{ width: `${pct}%` }}
            transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1], delay: 0.1 }}
          />
        </div>
      </div>
    </Link>
  )
}

export function CoursesPage() {
  const reducedMotion = useReducedMotion()
  const [modalOpen, setModalOpen] = useState(false)
  const [filter, setFilter] = useState<Filter>("all")
  const { courses, coursesLoaded, loadCourses } = usePomodoro()

  useEffect(() => {
    void loadCourses()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const summary = useMemo(() => {
    const done = courses.filter(isDone).length
    const lessons = courses.reduce((sum, c) => sum + c.totalLessons, 0)
    const completed = courses.reduce((sum, c) => sum + c.completedLessons, 0)
    return { done, active: courses.length - done, overall: lessons > 0 ? Math.round((completed / lessons) * 100) : 0 }
  }, [courses])

  const visible = useMemo(() => {
    const rank = (c: CourseSummary) => (isDone(c) ? 2 : c.status === "IN_PROGRESS" ? 0 : 1)
    const studied = (c: CourseSummary) => (c.lastStudiedAt ? new Date(c.lastStudiedAt).getTime() : 0)
    return courses
      .filter((c) => filter === "all" || (filter === "done" ? isDone(c) : !isDone(c)))
      .sort(
        (a, b) =>
          Number(isDone(a)) - Number(isDone(b)) ||
          studied(b) - studied(a) ||
          rank(a) - rank(b) ||
          b.progress - a.progress ||
          a.title.localeCompare(b.title),
      )
  }, [courses, filter])

  if (!coursesLoaded) {
    return <CardGridSkeleton />
  }

  const filters: { key: Filter; label: string; count: number }[] = [
    { key: "all", label: "Todos", count: courses.length },
    { key: "active", label: "Em andamento", count: summary.active },
    { key: "done", label: "Concluídos", count: summary.done },
  ]

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Cursos</h1>
          <p className="mt-1 text-sm text-muted-foreground tabular-nums">
            {courses.length === 0
              ? "Acompanhe os cursos que você está fazendo, módulo por módulo."
              : `${courses.length} ${courses.length === 1 ? "curso" : "cursos"} · ${summary.done} ${summary.done === 1 ? "concluído" : "concluídos"} · ${summary.overall}% no geral`}
          </p>
        </div>
        <Button type="button" className="rounded-full" onClick={() => setModalOpen(true)}>
          <Plus data-icon="inline-start" />
          Novo curso
        </Button>
      </div>

      {courses.length === 0 ? (
        <button
          type="button"
          onClick={() => setModalOpen(true)}
          className="flex flex-col items-center gap-3 rounded-2xl border border-dashed px-6 py-16 text-center transition-colors hover:bg-muted/20"
        >
          <BookOpen className="size-6 text-muted-foreground" />
          <span className="font-medium">Cadastre seu primeiro curso</span>
          <span className="max-w-xs text-sm text-muted-foreground">
            Marque cada aula concluída e veja o progresso aqui e no seu foco.
          </span>
        </button>
      ) : (
        <>
          <div className="flex w-fit gap-1 rounded-md border bg-muted/30 p-1">
            {filters.map((f) => (
              <PillToggleButton
                key={f.key}
                active={filter === f.key}
                layoutId="courses-filter-pill"
                onClick={() => setFilter(f.key)}
                className="h-7 gap-1.5 px-2.5"
              >
                {f.label}
                <span className="text-xs text-muted-foreground tabular-nums">{f.count}</span>
              </PillToggleButton>
            ))}
          </div>

          <motion.div key={filter} {...fadeSwap(reducedMotion)}>
            {visible.length === 0 ? (
              <p className="rounded-2xl border border-dashed px-6 py-12 text-center text-sm text-muted-foreground">
                {filter === "done" ? "Nenhum curso concluído ainda." : "Nenhum curso em andamento."}
              </p>
            ) : (
              <motion.div
                className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3"
                variants={gridStagger}
                initial="hidden"
                animate="show"
              >
                {visible.map((course) => (
                  <motion.div key={course.id} variants={gridItem}>
                    <CourseCard course={course} />
                  </motion.div>
                ))}
              </motion.div>
            )}
          </motion.div>
        </>
      )}

      <NewCourseModal open={modalOpen} onClose={() => setModalOpen(false)} onCreated={loadCourses} />
    </div>
  )
}
