import { Check, Phone } from "lucide-react"
import { cn } from "@/lib/utils"
import { AstraMark } from "@/components/landing/AstraMark"

export function CoursesArt() {
  const modules = [
    { name: "Fundamentos", done: true },
    { name: "Spring Data JPA", done: true },
    { name: "Segurança com JWT", done: false },
  ]

  return (
    <div className="w-full max-w-56 rounded-2xl border border-current/15 bg-current/[0.04] p-4">
      <p className="text-xs font-semibold">Spring Boot</p>
      <div className="mt-2 flex items-center gap-2">
        <div className="h-1.5 flex-1 rounded-full bg-current/15">
          <div className="h-full w-2/3 rounded-full bg-current" />
        </div>
        <span className="text-[10px] tabular-nums opacity-60">6/9</span>
      </div>
      <ul className="mt-3 flex flex-col gap-2">
        {modules.map((m) => (
          <li key={m.name} className="flex items-center gap-2 text-[11px]">
            <span
              className={cn(
                "flex size-4 shrink-0 items-center justify-center rounded-full border border-current/30",
                m.done && "border-transparent bg-current",
              )}
            >
              {m.done && <Check className="size-2.5 text-background group-data-[inverted]:text-foreground" />}
            </span>
            <span className={cn(!m.done && "opacity-60")}>{m.name}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

export function RoadmapArt() {
  const steps = [
    { name: "Java", state: "done" },
    { name: "Spring", state: "done" },
    { name: "Docker", state: "current" },
    { name: "Cloud", state: "todo" },
  ]

  return (
    <div className="relative flex flex-col gap-3.5 py-1">
      <div className="absolute bottom-3 left-[7px] top-3 w-px bg-current/20" />
      {steps.map((s) => (
        <div key={s.name} className="relative flex items-center gap-3 text-xs">
          <span
            className={cn(
              "size-[15px] shrink-0 rounded-full border-2",
              s.state === "done" && "border-current bg-current",
              s.state === "current" && "border-current bg-background group-data-[inverted]:bg-foreground",
              s.state === "todo" && "border-current/25 bg-background group-data-[inverted]:bg-foreground",
            )}
          />
          <span
            className={cn(
              "rounded-lg border border-current/15 px-2.5 py-1",
              s.state === "current" && "border-current/50 font-medium",
              s.state === "todo" && "opacity-50",
            )}
          >
            {s.name}
          </span>
        </div>
      ))}
    </div>
  )
}

export function GoalArt() {
  return (
    <div className="flex flex-col items-center gap-2">
      <div className="relative size-32">
        <svg viewBox="0 0 100 100" className="size-full -rotate-90" aria-hidden="true">
          <circle cx="50" cy="50" r="42" fill="none" strokeWidth="8" className="stroke-current opacity-15" />
          <circle
            cx="50"
            cy="50"
            r="42"
            fill="none"
            strokeWidth="8"
            strokeLinecap="round"
            pathLength={1}
            strokeDasharray="1"
            strokeDashoffset="0.33"
            className="stroke-current"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-xl font-semibold tabular-nums">8h</span>
          <span className="text-[10px] opacity-60">de 12h</span>
        </div>
      </div>
      <span className="text-[11px] opacity-60">Meta semanal</span>
    </div>
  )
}

export function GroupArt() {
  const people = ["LU", "BI", "RA"]

  return (
    <div className="flex w-full max-w-56 flex-col gap-2 text-[11px]">
      <div className="self-start rounded-2xl rounded-bl-md border border-current/15 px-3 py-2">Bora uma sessão às 20h?</div>
      <div className="self-end rounded-2xl rounded-br-md bg-current/90 px-3 py-2">
        <span className="text-background group-data-[inverted]:text-foreground">Fechado, chama aí</span>
      </div>
      <div className="mt-2 flex items-center gap-2 self-start rounded-full border border-current/15 py-1 pl-1 pr-3">
        <div className="flex -space-x-1.5">
          {people.map((p) => (
            <span
              key={p}
              className="flex size-6 items-center justify-center rounded-full border-2 border-background bg-current/15 text-[8px] font-semibold group-data-[inverted]:border-foreground"
            >
              {p}
            </span>
          ))}
        </div>
        <Phone className="size-3" />
        Em chamada
      </div>
    </div>
  )
}

export function OrbitArt({ className }: { className?: string }) {
  return (
    <div className={cn("relative aspect-square", className)} aria-hidden="true">
      <svg viewBox="0 0 200 200" className="absolute inset-0 size-full" fill="none" stroke="currentColor">
        <ellipse cx="100" cy="100" rx="92" ry="34" strokeOpacity="0.35" transform="rotate(-20 100 100)" />
        <ellipse cx="100" cy="100" rx="70" ry="24" strokeOpacity="0.25" transform="rotate(25 100 100)" />
        <circle cx="100" cy="100" r="96" strokeOpacity="0.12" />
        <circle cx="186" cy="70" r="4" fill="currentColor" stroke="none" />
        <circle cx="42" cy="146" r="3" fill="currentColor" stroke="none" fillOpacity="0.6" />
      </svg>
      <AstraMark className="absolute inset-[28%] size-[44%]" />
      <AstraMark className="absolute right-[6%] top-[8%] size-[12%] opacity-60" />
    </div>
  )
}
