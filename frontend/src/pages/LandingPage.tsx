import { useEffect, useLayoutEffect, useRef, useState } from "react"
import type { ComponentType } from "react"
import {
  AppWindow,
  ArrowRight,
  ArrowUpRight,
  BookOpen,
  Code2,
  Download,
  MessageCircle,
  Maximize2,
  Monitor,
  PencilLine,
  Pin,
  Route,
  ScreenShare,
  Tags,
  Users,
  Video,
} from "lucide-react"
import type { LucideIcon } from "lucide-react"
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
import { OrbitArt } from "@/components/landing/featureArt"
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

const FEATURES: { title: string; text: string; Icon: LucideIcon }[] = [
  { title: "Cursos e módulos", text: "Marque cada módulo concluído e veja quanto falta para terminar.", Icon: BookOpen },
  { title: "Roadmaps", text: "Trilhas prontas de Backend Java e Frontend, ou monte a sua.", Icon: Route },
  { title: "Categorias", text: "Separe o tempo por assunto e veja para onde ele está indo.", Icon: Tags },
  { title: "Registro manual", text: "Estudou longe do computador? Lance a sessão depois.", Icon: PencilLine },
  { title: "Modo foco", text: "Tela cheia, sem distração: só o timer e o que você está estudando.", Icon: Maximize2 },
  { title: "Chat", text: "Converse com seus amigos sem sair do app.", Icon: MessageCircle },
  { title: "Grupos", text: "Um espaço para a turma, o projeto ou a maratona de estudo.", Icon: Users },
  { title: "Chamadas", text: "Voz e vídeo para estudar junto, minimizadas enquanto você navega.", Icon: Video },
  { title: "Compartilhar tela", text: "Mostre o código ou o exercício, e cada um escolhe se quer assistir.", Icon: ScreenShare },
  { title: "Mensagens fixadas", text: "O link da aula ou o combinado sempre no topo da conversa.", Icon: Pin },
  { title: "Seus dados", text: "Exporte tudo quando quiser ou exclua a conta de vez.", Icon: Download },
  { title: "App para Windows", text: "Instale pela Microsoft Store e abra direto da barra de tarefas.", Icon: AppWindow },
]

const PRIMARY_CTA =
  "h-12 rounded-full px-6 text-[15px] max-sm:h-12 has-data-[icon=inline-end]:pr-5 has-data-[icon=inline-start]:pl-5"
const SECONDARY_CTA =
  "h-12 rounded-full px-6 text-[15px] max-sm:h-12 has-data-[icon=inline-end]:pr-5 has-data-[icon=inline-start]:pl-5"

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

function Features() {
  return (
    <section className="mx-auto max-w-6xl px-4 py-24 sm:px-8 sm:py-32">
      <h2 className="text-[clamp(2.4rem,4.4vw,4.25rem)] font-semibold leading-[0.98] tracking-[-0.04em]">E ainda mais.</h2>
      <div className="mt-12 grid gap-px overflow-hidden rounded-3xl border bg-border sm:grid-cols-2 lg:grid-cols-4">
        {FEATURES.map(({ title, text, Icon }) => (
          <article key={title} className="bg-background">
            <div data-feature-card className="flex h-full gap-4 p-5 sm:flex-col sm:gap-0 sm:p-7">
              <Icon className="size-5 shrink-0 text-foreground" strokeWidth={1.5} />
              <div className="sm:mt-8">
                <h3 className="font-semibold tracking-[-0.01em]">{title}</h3>
                <p className="mt-1.5 text-pretty text-sm text-muted-foreground">{text}</p>
              </div>
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
        className="grid items-center gap-10 overflow-hidden rounded-3xl bg-foreground p-8 text-background sm:p-12 md:grid-cols-[minmax(0,1fr)_auto]"
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
