import { useEffect, useLayoutEffect, useRef, useState } from "react"
import type { ComponentType } from "react"
import { ArrowRight, ArrowUpRight, Code2, Monitor } from "lucide-react"
import { Navigate, Link } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { useTheme } from "@/context/ThemeContext"
import { cn } from "@/lib/utils"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"
import { Button } from "@/components/ui/button"
import { AppFrame } from "@/components/landing/AppFrame"
import { AstraMark } from "@/components/landing/AstraMark"
import { SplitWords } from "@/components/landing/SplitWords"
import { setupLandingMotion } from "@/components/landing/landingMotion"
import { CoursesArt, GoalArt, GroupArt, OrbitArt, RoadmapArt } from "@/components/landing/featureArt"
import { GithubScreen, HeatmapScreen, OverviewScreen, PomodoroScreen, RankingScreen } from "@/components/landing/screens"

const STORE_URL = "https://apps.microsoft.com/detail/9NRB7QNCJGSP?hl=pt-br&gl=BR"

type Chapter = {
  label: string
  title: string
  text: string
  Screen: ComponentType
}

const CHAPTERS: Chapter[] = [
  {
    label: "Foco",
    title: "Pomodoro com modo foco",
    text: "Timer com ciclos e pausas configuráveis e modo foco em tela cheia. Cada minuto concluído entra no seu histórico automaticamente.",
    Screen: PomodoroScreen,
  },
  {
    label: "Constância",
    title: "Heatmap, streak e metas",
    text: "Veja seus dias de estudo no heatmap, acompanhe o streak e defina metas diárias e semanais.",
    Screen: HeatmapScreen,
  },
  {
    label: "Social",
    title: "Ranking entre amigos",
    text: "Compare seu tempo de foco com o dos seus amigos no dia, na semana e no mês, ou participe do ranking global.",
    Screen: RankingScreen,
  },
  {
    label: "GitHub",
    title: "Integração com GitHub",
    text: "Conecte sua conta e veja commits, PRs e linguagens ao lado do seu tempo de foco.",
    Screen: GithubScreen,
  },
]

const FEATURES = [
  {
    title: "Cursos e módulos",
    text: "Cadastre os cursos que você está fazendo e marque cada módulo concluído para acompanhar o progresso.",
    Art: CoursesArt,
    inverted: false,
  },
  {
    title: "Roadmaps",
    text: "Siga trilhas prontas de Backend Java e Frontend ou monte a sua, etapa por etapa.",
    Art: RoadmapArt,
    inverted: true,
  },
  {
    title: "Metas de estudo",
    text: "Defina quantas horas quer estudar por dia e por semana e acompanhe direto no dashboard.",
    Art: GoalArt,
    inverted: true,
  },
  {
    title: "Estudo em grupo",
    text: "Converse com seus amigos, crie grupos e faça chamadas para estudar junto.",
    Art: GroupArt,
    inverted: false,
  },
]

const PRIMARY_CTA = "h-12 rounded-full px-6 text-[15px] max-sm:h-12"
const SECONDARY_CTA = "h-12 rounded-full px-6 text-[15px] max-sm:h-12"

function Nav() {
  const { theme, toggleTheme } = useTheme()
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8)
    onScroll()
    window.addEventListener("scroll", onScroll, { passive: true })
    return () => window.removeEventListener("scroll", onScroll)
  }, [])

  return (
    <header
      className={cn(
        "fixed inset-x-0 top-0 z-50 border-b border-transparent transition-[background-color,border-color,backdrop-filter] duration-300",
        scrolled && "border-border/60 bg-background/85 backdrop-blur-xl",
      )}
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-8">
        <Link to="/" className="flex items-center gap-2 text-[15px] font-semibold tracking-tight">
          <AstraMark className="size-5" />
          Astra
        </Link>
        <div className="flex items-center gap-1 sm:gap-2">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="rounded-full"
            title={theme === "dark" ? "Modo claro" : "Modo escuro"}
            onClick={toggleTheme}
          >
            <ThemeToggleIcon isDark={theme === "dark"} className="size-[18px]" />
          </Button>
          <Button asChild variant="ghost" size="sm" className="rounded-full">
            <Link to="/login">Entrar</Link>
          </Button>
          <Button asChild size="sm" className="rounded-full px-3.5">
            <Link to="/register">Criar conta</Link>
          </Button>
        </div>
      </div>
    </header>
  )
}

function HeroCopy() {
  return (
    <>
      <h1
        data-hero-fade
        className="text-[clamp(4.5rem,13vw,10.5rem)] font-semibold leading-[0.9] tracking-[-0.06em]"
      >
        Astra
      </h1>
      <p data-hero-fade className="mx-auto mt-6 max-w-2xl text-balance text-2xl font-medium tracking-[-0.02em] sm:text-3xl">
        Ecossistema de estudos.
      </p>
      <p data-hero-fade className="mx-auto mt-4 max-w-xl text-pretty text-base text-muted-foreground sm:text-lg">
        Pomodoro, cursos, roadmaps, ranking com amigos e integração com GitHub, tudo em um lugar.
      </p>
      <div data-hero-end className="mt-8 flex flex-wrap items-center justify-center gap-3">
        <div data-hero-fade>
          <Button asChild size="lg" className={PRIMARY_CTA}>
            <Link to="/register">
              Criar conta grátis
              <ArrowRight data-icon="inline-end" />
            </Link>
          </Button>
        </div>
        <div data-hero-fade>
          <Button asChild variant="outline" size="lg" className={SECONDARY_CTA}>
            <a href={STORE_URL} target="_blank" rel="noreferrer">
              <Monitor data-icon="inline-start" />
              Baixar para Windows
            </a>
          </Button>
        </div>
      </div>
    </>
  )
}

function ChapterCopy({ chapter, index }: { chapter: Chapter; index: number }) {
  return (
    <>
      <p data-chapter-fade className="text-sm font-medium text-muted-foreground">
        <span className="tabular-nums">0{index + 1}</span>
        <span className="mx-2 inline-block h-px w-6 translate-y-[-4px] bg-border" />
        {chapter.label}
      </p>
      <h2 className="mt-5 text-[clamp(2.4rem,4.4vw,4.25rem)] font-semibold leading-[0.98] tracking-[-0.04em]">
        <SplitWords text={chapter.title} />
      </h2>
      <p data-chapter-fade className="mt-6 max-w-md text-pretty text-base text-muted-foreground lg:text-lg">
        {chapter.text}
      </p>
    </>
  )
}

function DesktopShowcase() {
  return (
    <section aria-label="Conheça o Astra" className="hidden lg:block lg:motion-reduce:hidden">
      <div data-stage className="relative h-svh overflow-hidden bg-background">
        <div data-stage-hero className="absolute inset-x-0 top-0 z-10 px-8 pt-[max(7rem,15vh)] text-center">
          <HeroCopy />
        </div>
        <div className="mx-auto grid h-full max-w-6xl grid-cols-[minmax(0,5fr)_minmax(0,6fr)] items-center gap-16 px-8 pt-16">
          <div className="relative h-[24rem]">
            {CHAPTERS.map((chapter, i) => (
              <div key={chapter.label} data-chapter className="absolute inset-0 flex flex-col justify-center">
                <ChapterCopy chapter={chapter} index={i} />
              </div>
            ))}
            <div data-progress className="absolute inset-x-0 bottom-0 flex max-w-md gap-2">
              {CHAPTERS.map((chapter) => (
                <div key={chapter.label} className="h-0.5 flex-1 overflow-hidden rounded-full bg-border">
                  <div data-progress-bar className="h-full w-full bg-foreground" />
                </div>
              ))}
            </div>
          </div>
          <div data-frame-slot className="relative z-20">
            <div data-frame>
              <div data-frame-intro>
                <AppFrame>
                  <div data-screen className="absolute inset-0">
                    <OverviewScreen />
                  </div>
                  {CHAPTERS.map(({ label, Screen }) => (
                    <div key={label} data-screen className="absolute inset-0">
                      <Screen />
                    </div>
                  ))}
                </AppFrame>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function StackedShowcase() {
  return (
    <div className="lg:hidden lg:motion-reduce:block">
      <section data-mobile-hero className="px-4 pb-20 pt-32 text-center sm:px-8 sm:pt-40">
        <HeroCopy />
        <div data-frame-intro className="mx-auto mt-14 max-w-xl">
          <AppFrame>
            <OverviewScreen />
          </AppFrame>
        </div>
      </section>
      <div className="mx-auto flex max-w-xl flex-col gap-28 px-4 pb-28 sm:px-8 lg:max-w-6xl">
        {CHAPTERS.map((chapter, i) => {
          const { Screen } = chapter
          return (
            <section key={chapter.label} data-mchapter className="lg:grid lg:grid-cols-2 lg:items-center lg:gap-16">
              <div>
                <ChapterCopy chapter={chapter} index={i} />
              </div>
              <div data-mframe className="mt-10 lg:mt-0">
                <AppFrame active={i + 1}>
                  <div data-screen className="absolute inset-0">
                    <Screen />
                  </div>
                </AppFrame>
              </div>
            </section>
          )
        })}
      </div>
    </div>
  )
}

function SectionLabel({ title, text }: { title: string; text: string }) {
  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-6">
      <h2 className="w-fit rounded-md bg-foreground px-2.5 py-1 text-xl font-semibold tracking-[-0.02em] text-background sm:text-2xl">
        {title}
      </h2>
      <p className="max-w-md text-pretty text-sm text-muted-foreground sm:text-base">{text}</p>
    </div>
  )
}

function Features() {
  return (
    <section className="mx-auto max-w-6xl px-4 py-24 sm:px-8 sm:py-32">
      <SectionLabel
        title="Funcionalidades"
        text="Além do foco e do ranking, o Astra organiza tudo o que você está aprendendo."
      />
      <div className="mt-10 grid gap-4 md:grid-cols-2">
        {FEATURES.map(({ title, text, Art, inverted }) => (
          <article
            key={title}
            data-feature-card
            data-inverted={inverted || undefined}
            className={cn(
              "group flex min-h-72 flex-col justify-between gap-8 rounded-3xl border p-7 sm:flex-row sm:items-center",
              inverted ? "border-transparent bg-foreground text-background" : "bg-card text-card-foreground",
            )}
          >
            <div className="max-w-60">
              <h3 className="text-2xl font-semibold tracking-[-0.03em]">{title}</h3>
              <p className={cn("mt-3 text-pretty text-sm", inverted ? "text-background/65" : "text-muted-foreground")}>
                {text}
              </p>
            </div>
            <div className="flex shrink-0 justify-center sm:w-56">
              <Art />
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

function CtaCard() {
  return (
    <section className="mx-auto max-w-6xl px-4 pb-24 sm:px-8 sm:pb-32">
      <div
        data-cta-card
        data-inverted
        className="group grid items-center gap-10 overflow-hidden rounded-3xl bg-foreground p-8 text-background sm:p-12 md:grid-cols-[minmax(0,1fr)_auto]"
      >
        <div>
          <h2 className="text-balance text-3xl font-semibold tracking-[-0.035em] sm:text-5xl">Comece a estudar com método</h2>
          <p className="mt-4 max-w-lg text-pretty text-background/65 sm:text-lg">
            Crie sua conta grátis e use no navegador, ou instale o app para Windows pela Microsoft Store.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button asChild size="lg" className={cn(PRIMARY_CTA, "bg-background text-foreground hover:bg-background/85")}>
              <Link to="/register">
                Criar conta grátis
                <ArrowRight data-icon="inline-end" />
              </Link>
            </Button>
            <Button
              asChild
              variant="outline"
              size="lg"
              className={cn(
                SECONDARY_CTA,
                "border-background/25 bg-transparent text-background hover:bg-background/10 hover:text-background",
                "dark:border-background/25 dark:bg-transparent dark:hover:bg-background/10",
              )}
            >
              <a href={STORE_URL} target="_blank" rel="noreferrer">
                <Monitor data-icon="inline-start" />
                Microsoft Store
                <ArrowUpRight data-icon="inline-end" />
              </a>
            </Button>
          </div>
        </div>
        <OrbitArt className="mx-auto w-48 sm:w-60" />
      </div>
    </section>
  )
}

function Footer() {
  return (
    <footer className="border-t">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-8 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between sm:px-8">
        <span>Astra · feito por Kauan Di Nubila</span>
        <nav className="flex flex-wrap items-center gap-5">
          <a
            href="https://github.com/KauanDiNubila/astra"
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-1.5 hover:text-foreground"
          >
            <Code2 className="size-4" />
            Código-fonte
          </a>
          <Link to="/termos" className="hover:text-foreground">
            Termos
          </Link>
          <Link to="/privacidade" className="hover:text-foreground">
            Privacidade
          </Link>
        </nav>
      </div>
    </footer>
  )
}

function LandingContent() {
  const rootRef = useRef<HTMLDivElement>(null)

  useLayoutEffect(() => {
    if (!rootRef.current) return
    return setupLandingMotion(rootRef.current)
  }, [])

  return (
    <div ref={rootRef} className="min-h-svh bg-background text-foreground">
      <Nav />
      <main>
        <DesktopShowcase />
        <StackedShowcase />
        <Features />
        <CtaCard />
      </main>
      <Footer />
    </div>
  )
}

export function LandingPage() {
  const { user, loading } = useAuth()

  if (window.astraDesktop || (!loading && user)) {
    return <Navigate to="/dashboard" replace />
  }

  return <LandingContent />
}
