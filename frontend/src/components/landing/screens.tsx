import { ArrowUp, Check, Flame, GitPullRequest, GitCommitHorizontal, Target } from "lucide-react"
import { cn } from "@/lib/utils"
import { GitHubIcon } from "@/components/icons/GitHubIcon"
import { formatHours } from "@/lib/format"

export const RANK_STEP = 52

function ScreenLabel({ children }: { children: string }) {
  return <p className="whitespace-nowrap text-[11px] font-medium uppercase tracking-[0.14em] text-muted-foreground">{children}</p>
}

const WEEK = ["S", "T", "Q", "Q", "S", "S", "D"]
const WEEK_FOCUS = [62, 88, 45, 100, 74, 30, 52]

export function OverviewScreen() {
  const stats = [
    { label: "Hoje", value: "2h 15min" },
    { label: "Semana", value: "11h 40min" },
    { label: "Streak", value: "23 dias" },
  ]

  return (
    <div className="flex h-full flex-col gap-4 p-5">
      <div>
        <ScreenLabel>Dashboard</ScreenLabel>
        <p className="mt-1 text-lg font-semibold tracking-tight">Olá, Marina</p>
      </div>
      <div className="grid grid-cols-3 gap-2.5">
        {stats.map((s) => (
          <div key={s.label} className="rounded-xl border bg-background/60 p-3">
            <p className="text-[11px] text-muted-foreground">{s.label}</p>
            <p className="mt-1 whitespace-nowrap text-sm font-semibold tabular-nums xl:text-base">{s.value}</p>
          </div>
        ))}
      </div>
      <div className="flex min-h-0 flex-1 flex-col rounded-xl border bg-background/60 p-4">
        <div className="flex items-center justify-between">
          <p className="text-xs font-medium">Esta semana</p>
          <p className="flex items-center gap-1 text-[11px] text-muted-foreground">
            <Target className="size-3" /> Meta 12h
          </p>
        </div>
        <div className="mt-3 flex min-h-0 flex-1 items-end gap-2">
          {WEEK_FOCUS.map((h, i) => (
            <div key={i} className="flex h-full flex-1 flex-col items-center justify-end gap-1.5">
              <div className="w-full rounded-md bg-emerald-500/85 dark:bg-emerald-400/85" style={{ height: `${h}%` }} />
              <span className="text-[10px] text-muted-foreground">{WEEK[i]}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export function PomodoroScreen() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-5 p-5">
      <div className="rounded-full border px-3 py-1 text-[11px] font-medium text-muted-foreground">
        Foco · ciclo 3 de 4
      </div>
      <div className="relative size-40 xl:size-48">
        <svg viewBox="0 0 100 100" className="size-full -rotate-90" aria-hidden="true">
          <circle cx="50" cy="50" r="45" fill="none" strokeWidth="3" className="stroke-muted" />
          <circle
            data-ring
            cx="50"
            cy="50"
            r="45"
            fill="none"
            strokeWidth="3"
            strokeLinecap="round"
            pathLength={1}
            strokeDasharray="1"
            strokeDashoffset="0"
            className="stroke-foreground"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span data-time className="text-4xl font-semibold tabular-nums tracking-tight xl:text-5xl">
            00:00
          </span>
          <span className="mt-1 text-[11px] text-muted-foreground">Algoritmos · Grafos</span>
        </div>
      </div>
      <div
        data-done
        className="flex items-center gap-1.5 rounded-full border bg-background px-3 py-1.5 text-xs shadow-sm"
      >
        <Check className="size-3.5 text-emerald-500" />
        Sessão salva · +25 min
      </div>
    </div>
  )
}

const HEAT_WEEKS = 26
const HEAT_STREAK = 23

function noise(i: number) {
  const x = Math.sin(i * 12.9898 + 78.233) * 43758.5453
  return x - Math.floor(x)
}

const HEAT_LEVELS = Array.from({ length: HEAT_WEEKS * 7 }, (_, i) => {
  const week = Math.floor(i / 7)
  const chance = 0.3 + (week / HEAT_WEEKS) * 0.55
  const r = noise(i)
  const intensity = 1 + Math.floor(noise(i + 500) * (1.5 + (week / HEAT_WEEKS) * 3))
  if (i >= HEAT_WEEKS * 7 - HEAT_STREAK) return Math.min(4, Math.max(1, intensity))
  return r < chance ? Math.min(4, intensity) : 0
})

const HEAT_CLASS = [
  "",
  "bg-emerald-200 dark:bg-emerald-900",
  "bg-emerald-300 dark:bg-emerald-700",
  "bg-emerald-500 dark:bg-emerald-600",
  "bg-emerald-600 dark:bg-emerald-400",
]

export function HeatmapScreen() {
  return (
    <div className="flex h-full flex-col justify-center gap-5 p-5">
      <div className="flex items-end justify-between gap-4">
        <div>
          <ScreenLabel>Últimos 6 meses</ScreenLabel>
          <p className="mt-1 text-3xl font-semibold tabular-nums tracking-tight">
            <span data-counter="312" data-format="int">
              312
            </span>
            h
          </p>
        </div>
        <div className="flex items-center gap-1.5 whitespace-nowrap rounded-full border px-3 py-1 text-xs">
          <Flame className="size-3.5 text-orange-500" />
          <span data-counter="23" data-format="days" className="tabular-nums">
            23 dias
          </span>{" "}
          seguidos
        </div>
      </div>
      <div className="grid grid-flow-col grid-rows-7 gap-[3px]" style={{ gridTemplateColumns: `repeat(${HEAT_WEEKS}, minmax(0, 1fr))` }}>
        {HEAT_LEVELS.map((level, i) => (
          <div key={i} className="relative aspect-square rounded-[3px] bg-muted">
            {level > 0 && <span data-cell className={cn("absolute inset-0 rounded-[3px]", HEAT_CLASS[level])} />}
          </div>
        ))}
      </div>
      <div className="flex items-center justify-end gap-1.5 text-[10px] text-muted-foreground">
        Menos
        {HEAT_CLASS.map((c, i) => (
          <span key={i} className={cn("size-2.5 rounded-[2px]", i === 0 ? "bg-muted" : c)} />
        ))}
        Mais
      </div>
    </div>
  )
}

type RankRow = { name: string; initials: string; minutes: number; from: number; you?: boolean }

const RANKING: RankRow[] = [
  { name: "Você", initials: "MA", minutes: 860, from: 3, you: true },
  { name: "Lucas", initials: "LU", minutes: 835, from: 0 },
  { name: "Bia", initials: "BI", minutes: 730, from: 1 },
  { name: "Rafa", initials: "RA", minutes: 570, from: 2 },
  { name: "Duda", initials: "DU", minutes: 465, from: 4 },
]

export function RankingScreen() {
  const max = RANKING[0].minutes

  return (
    <div className="flex h-full flex-col justify-center gap-5 p-5">
      <div className="flex items-center justify-between">
        <ScreenLabel>Ranking</ScreenLabel>
        <div className="flex rounded-full border p-0.5 text-[11px]">
          {["Hoje", "Semana", "Mês"].map((t) => (
            <span
              key={t}
              className={cn("rounded-full px-2.5 py-0.5", t === "Semana" ? "bg-foreground text-background" : "text-muted-foreground")}
            >
              {t}
            </span>
          ))}
        </div>
      </div>
      <div className="flex gap-3">
        <div className="flex flex-col" style={{ gap: RANK_STEP - 44 }}>
          {RANKING.map((_, i) => (
            <span key={i} className="flex h-11 w-4 items-center text-xs font-medium tabular-nums text-muted-foreground">
              {i + 1}
            </span>
          ))}
        </div>
        <div className="flex min-w-0 flex-1 flex-col" style={{ gap: RANK_STEP - 44 }}>
          {RANKING.map((row, i) => (
            <div
              key={row.name}
              data-row
              data-shift={(row.from - i) * RANK_STEP}
              className={cn(
                "relative flex h-11 items-center gap-3 rounded-xl border px-3",
                row.you ? "z-10 border-foreground/20 bg-background shadow-sm" : "bg-background/60",
              )}
            >
              <span
                className={cn(
                  "flex size-7 shrink-0 items-center justify-center rounded-full text-[10px] font-semibold",
                  row.you ? "bg-foreground text-background" : "bg-muted",
                )}
              >
                {row.initials}
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <p className="truncate text-xs font-medium">{row.name}</p>
                  {row.you && (
                    <span
                      data-badge
                      className="flex items-center gap-0.5 rounded-full bg-emerald-500/15 px-1.5 py-px text-[10px] font-medium text-emerald-600 dark:text-emerald-400"
                    >
                      <ArrowUp className="size-2.5" />3
                    </span>
                  )}
                </div>
                <div className="mt-1 h-1 rounded-full bg-muted">
                  <div
                    {...(row.you ? { "data-bar": "" } : {})}
                    className={cn("h-full origin-left rounded-full", row.you ? "bg-foreground" : "bg-foreground/30")}
                    style={{ width: `${(row.minutes / max) * 100}%` }}
                  />
                </div>
              </div>
              <span
                {...(row.you ? { "data-counter": String(row.minutes), "data-from": "485", "data-format": "hours" } : {})}
                className="w-16 shrink-0 text-right text-xs tabular-nums text-muted-foreground"
              >
                {formatHours(row.minutes)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

const GH_FOCUS = [70, 95, 40, 100, 80, 25, 55]
const GH_COMMITS = [45, 80, 30, 100, 65, 10, 35]
const LANGS = [
  { name: "Java", share: 54, className: "bg-foreground" },
  { name: "TypeScript", share: 31, className: "bg-foreground/50" },
  { name: "SQL", share: 15, className: "bg-foreground/20" },
]

export function GithubScreen() {
  return (
    <div className="flex h-full flex-col justify-center gap-4 p-5">
      <div className="flex items-center gap-2">
        <GitHubIcon className="size-4" />
        <ScreenLabel>Esta semana</ScreenLabel>
      </div>
      <div className="grid grid-cols-3 gap-2.5">
        <div className="rounded-xl border bg-background/60 p-3">
          <p className="flex items-center gap-1 text-[11px] text-muted-foreground">
            <GitCommitHorizontal className="size-3" /> Commits
          </p>
          <p data-counter="38" data-format="int" className="mt-1 text-base font-semibold tabular-nums">
            38
          </p>
        </div>
        <div className="rounded-xl border bg-background/60 p-3">
          <p className="flex items-center gap-1 text-[11px] text-muted-foreground">
            <GitPullRequest className="size-3" /> PRs
          </p>
          <p data-counter="6" data-format="int" className="mt-1 text-base font-semibold tabular-nums">
            6
          </p>
        </div>
        <div className="rounded-xl border bg-background/60 p-3">
          <p className="text-[11px] text-muted-foreground">Foco</p>
          <p className="mt-1 text-base font-semibold tabular-nums">11h 40</p>
        </div>
      </div>
      <div className="rounded-xl border bg-background/60 p-4">
        <div className="flex h-24 items-end gap-3 xl:h-28">
          {WEEK.map((_, i) => (
            <div key={i} className="flex h-full flex-1 items-end justify-center gap-[3px]">
              <div data-gbar className="w-full max-w-2.5 origin-bottom rounded-sm bg-foreground/80" style={{ height: `${GH_FOCUS[i]}%` }} />
              <div
                data-gbar
                className="w-full max-w-2.5 origin-bottom rounded-sm bg-emerald-500 dark:bg-emerald-400"
                style={{ height: `${GH_COMMITS[i]}%` }}
              />
            </div>
          ))}
        </div>
        <div className="mt-2 flex gap-3">
          {WEEK.map((d, i) => (
            <span key={i} className="flex-1 text-center text-[10px] text-muted-foreground">
              {d}
            </span>
          ))}
        </div>
        <div className="mt-3 flex items-center gap-3 text-[10px] text-muted-foreground">
          <span className="flex items-center gap-1">
            <span className="size-2 rounded-sm bg-foreground/80" /> Foco
          </span>
          <span className="flex items-center gap-1">
            <span className="size-2 rounded-sm bg-emerald-500 dark:bg-emerald-400" /> Commits
          </span>
        </div>
      </div>
      <div>
        <div className="flex h-1.5 gap-0.5 overflow-hidden rounded-full">
          {LANGS.map((l) => (
            <div key={l.name} data-lang className={cn("h-full origin-left", l.className)} style={{ width: `${l.share}%` }} />
          ))}
        </div>
        <div className="mt-2 flex gap-4 text-[11px] text-muted-foreground">
          {LANGS.map((l) => (
            <span key={l.name}>
              {l.name} <span className="tabular-nums">{l.share}%</span>
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}
