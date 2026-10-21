import { useMemo, useRef, useState } from "react"
import type { KeyboardEvent } from "react"
import { BookOpen, Check, ChevronDown, Search } from "lucide-react"
import { formatDaysAgo } from "@/lib/format"
import type { CourseSummary } from "@/lib/types"
import { cn } from "@/lib/utils"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

const SEARCH_THRESHOLD = 5

function isDone(course: CourseSummary) {
  return course.status === "DONE" || (course.totalLessons > 0 && course.completedLessons >= course.totalLessons)
}

function normalize(text: string) {
  return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
}

function percent(course: CourseSummary) {
  return Math.round(course.progress * 100)
}

export function CoursePicker({
  courses,
  value,
  onChange,
}: {
  courses: CourseSummary[]
  value: string
  onChange: (id: string) => void
}) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")
  const [index, setIndex] = useState(0)
  const listRef = useRef<HTMLDivElement>(null)
  const selected = courses.find((c) => c.id === value)
  const searchable = courses.length > SEARCH_THRESHOLD

  const sections = useMemo(() => {
    const q = normalize(query.trim())
    const matches = courses.filter((c) => !q || normalize(`${c.title} ${c.platform ?? ""}`).includes(q))
    const rank = (c: CourseSummary) => (c.status === "IN_PROGRESS" ? 0 : 1)
    const studied = (c: CourseSummary) => (c.lastStudiedAt ? new Date(c.lastStudiedAt).getTime() : 0)
    const active = matches
      .filter((c) => !isDone(c))
      .sort((a, b) => studied(b) - studied(a) || rank(a) - rank(b) || b.progress - a.progress)
    const done = matches.filter(isDone).sort((a, b) => a.title.localeCompare(b.title))
    return [
      { label: "Em andamento", items: active },
      { label: "Concluídos", items: done },
    ].filter((s) => s.items.length > 0)
  }, [courses, query])

  const options = useMemo(() => ["", ...sections.flatMap((s) => s.items.map((c) => c.id))], [sections])

  function handleOpenChange(next: boolean) {
    setOpen(next)
    if (next) {
      setQuery("")
      setIndex(Math.max(0, options.indexOf(value)))
    }
  }

  function choose(id: string) {
    onChange(id)
    setOpen(false)
  }

  function onKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      event.preventDefault()
      const step = event.key === "ArrowDown" ? 1 : -1
      const next = (index + step + options.length) % options.length
      setIndex(next)
      listRef.current?.querySelector(`[data-index="${next}"]`)?.scrollIntoView({ block: "nearest" })
    } else if (event.key === "Enter") {
      event.preventDefault()
      if (options[index] !== undefined) choose(options[index])
    }
  }

  function optionClass(i: number) {
    return cn(
      "flex w-full items-center gap-3 rounded-md px-2 py-2 text-left text-sm transition-colors",
      i === index ? "bg-accent" : "hover:bg-accent/60",
    )
  }

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <button
          type="button"
          className="flex h-8 max-w-72 items-center gap-2 rounded-full border px-3 text-sm transition-colors hover:bg-accent data-[state=open]:bg-accent"
        >
          <BookOpen className="size-3.5 shrink-0 text-muted-foreground" />
          {selected ? (
            <>
              <span className="truncate">{selected.title}</span>
              <span className="shrink-0 text-muted-foreground tabular-nums">· {percent(selected)}%</span>
            </>
          ) : (
            <span className="text-muted-foreground">Curso</span>
          )}
          <ChevronDown className="size-3.5 shrink-0 opacity-50" />
        </button>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-96 max-w-[calc(100vw-2rem)] gap-0 p-0" onKeyDown={onKeyDown}>
        {searchable && (
          <div className="flex items-center gap-2 border-b px-3">
            <Search className="size-3.5 shrink-0 text-muted-foreground" />
            <input
              autoFocus
              value={query}
              onChange={(e) => {
                setQuery(e.target.value)
                setIndex(0)
              }}
              placeholder="Buscar curso"
              aria-label="Buscar curso"
              className="h-10 min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            />
          </div>
        )}
        <div ref={listRef} className="max-h-80 overflow-y-auto p-1">
          <button type="button" data-index={0} onMouseMove={() => setIndex(0)} onClick={() => choose("")} className={optionClass(0)}>
            <span className="flex-1 text-muted-foreground">Sem curso</span>
            {!value && <Check className="size-4 text-muted-foreground" />}
          </button>
          <div className="my-1 h-px bg-border" />
          {sections.length === 0 && (
            <p className="px-2 py-6 text-center text-sm text-muted-foreground">Nenhum curso encontrado.</p>
          )}
          {sections.map((section) => (
            <div key={section.label}>
              <p className="px-2 pb-1 pt-2 text-[11px] font-medium tracking-wide text-muted-foreground/70 uppercase">
                {section.label}
              </p>
              {section.items.map((course) => {
                const i = options.indexOf(course.id)
                const done = isDone(course)
                return (
                  <button
                    key={course.id}
                    type="button"
                    data-index={i}
                    onMouseMove={() => setIndex(i)}
                    onClick={() => choose(course.id)}
                    className={cn(optionClass(i), done && "text-muted-foreground")}
                  >
                    <span className="flex min-w-0 flex-1 flex-col">
                      <span className="truncate">{course.title}</span>
                      {(course.platform || course.lastStudiedAt) && (
                        <span className="truncate text-xs text-muted-foreground">
                          {[course.platform, course.lastStudiedAt && `estudado ${formatDaysAgo(course.lastStudiedAt)}`]
                            .filter(Boolean)
                            .join(" · ")}
                        </span>
                      )}
                    </span>
                    <span className="flex w-20 shrink-0 flex-col items-end gap-1">
                      <span className="text-xs text-muted-foreground tabular-nums">
                        {course.completedLessons}/{course.totalLessons}
                      </span>
                      <span className="h-1 w-full overflow-hidden rounded-full bg-muted">
                        <span
                          className={cn("block h-full rounded-full", done ? "bg-emerald-500" : "bg-foreground/70")}
                          style={{ width: `${percent(course)}%` }}
                        />
                      </span>
                    </span>
                    <Check className={cn("size-4 shrink-0", course.id === value ? "opacity-100" : "opacity-0")} />
                  </button>
                )
              })}
            </div>
          ))}
        </div>
      </PopoverContent>
    </Popover>
  )
}
