import { useRef } from "react"
import { BookOpen, Clock, Code2, LayoutDashboard, Trophy } from "lucide-react"
import { motion } from "motion/react"
import { Navigate, Link } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { useTheme } from "@/context/ThemeContext"
import { cn, gridItem, gridStagger, INTERACTIVE_CARD_CLASS, SCROLLBAR_HIDE_CLASS, SPOTLIGHT_CLASS } from "@/lib/utils"
import { useSpotlight } from "@/hooks/useSpotlight"
import { SessionFlowScroll } from "@/components/SessionFlowScroll"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

function SectionDivider() {
  return <div aria-hidden className="h-px w-full bg-gradient-to-r from-transparent via-border to-transparent" />
}

const features = [
  {
    icon: Clock,
    title: "Pomodoro",
    description: "Timer baseado na técnica Pomodoro para organizar períodos de foco e pausas.",
  },
  {
    icon: BookOpen,
    title: "Cursos e Roadmaps",
    description: "Permite organizar cursos e criar roadmaps para acompanhar conteúdos e etapas de aprendizado.",
  },
  {
    icon: LayoutDashboard,
    title: "Dashboard e Heatmap",
    description:
      "Mostra minutos de foco de hoje, da semana e do total, streak e progresso das metas, com um heatmap de atividade dos últimos 365 dias.",
  },
  {
    icon: Trophy,
    title: "Ranking",
    description:
      "Compara seu tempo de foco com o de amigos — diário, semanal ou mensal — com a opção de ver o ranking global também.",
  },
]

export function LandingPage() {
  const { user, loading } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { onMouseMove } = useSpotlight()
  const scrollContainerRef = useRef<HTMLDivElement>(null)

  if (!loading && user) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <div
      ref={scrollContainerRef}
      className={cn("flex h-svh flex-col overflow-y-auto bg-background text-foreground", SCROLLBAR_HIDE_CLASS)}
    >
      <header className="flex items-center justify-between px-6 py-5">
        <span className="text-lg font-semibold">Astra</span>
        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            title={theme === "dark" ? "Modo claro" : "Modo escuro"}
            onClick={toggleTheme}
          >
            <ThemeToggleIcon isDark={theme === "dark"} className="size-5" />
          </Button>
          <Button asChild variant="ghost" size="sm">
            <Link to="/login">Entrar</Link>
          </Button>
          <Button asChild size="sm">
            <Link to="/register">Criar conta</Link>
          </Button>
        </div>
      </header>

      <main className="flex flex-1 flex-col">
        <section className="flex flex-1 flex-col items-center justify-center gap-8 px-4 py-24 text-center">
          <motion.h1
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
            className="text-6xl font-bold tracking-tighter sm:text-8xl"
          >
            Astra
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.05, ease: [0.22, 1, 0.36, 1] }}
            className="text-lg text-muted-foreground"
          >
            Seu ecossistema de estudos.
          </motion.p>
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
            className="flex gap-3"
          >
            <Button asChild size="lg">
              <Link to="/register">Criar conta</Link>
            </Button>
            <Button asChild variant="outline" size="lg">
              <Link to="/login">Entrar</Link>
            </Button>
          </motion.div>
        </section>

        <SessionFlowScroll scrollContainerRef={scrollContainerRef} />

        <SectionDivider />

        <section className="relative overflow-hidden bg-muted/10">
          <div
            aria-hidden
            className="absolute inset-0 [mask-image:radial-gradient(ellipse_at_center,black_40%,transparent_75%)]"
            style={{
              backgroundImage: "radial-gradient(var(--color-border) 1px, transparent 1px)",
              backgroundSize: "24px 24px",
            }}
          />
          <div className="relative mx-auto flex w-full max-w-5xl flex-col gap-8 px-6 py-24">
            <h2 className="text-center text-2xl font-semibold">Funcionalidades</h2>
            <motion.div
              className="grid auto-rows-fr gap-4 sm:grid-cols-2"
              variants={gridStagger}
              initial="hidden"
              whileInView="show"
              viewport={{ once: true, margin: "-80px" }}
            >
              {features.map((f) => (
                <motion.div key={f.title} variants={gridItem} className="h-full">
                  <Card
                    className={cn("h-full", INTERACTIVE_CARD_CLASS, SPOTLIGHT_CLASS)}
                    onMouseMove={onMouseMove}
                  >
                    <CardHeader>
                      <f.icon className="size-5 text-primary" />
                      <CardTitle className="min-h-10 text-base">{f.title}</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm text-muted-foreground">{f.description}</p>
                    </CardContent>
                  </Card>
                </motion.div>
              ))}
            </motion.div>
          </div>
        </section>

        <SectionDivider />

        <section className="relative flex flex-col items-center gap-4 overflow-hidden px-4 py-24 text-center">
          <div
            aria-hidden
            className="pointer-events-none absolute left-1/2 top-1/2 -z-10 h-[420px] w-[420px] -translate-x-1/2 -translate-y-1/2 rounded-full opacity-20 blur-[100px]"
            style={{ backgroundColor: "var(--color-primary)" }}
          />
          <p className="text-muted-foreground">Comece a acompanhar seu tempo hoje.</p>
          <Button asChild size="lg">
            <Link to="/register">Criar conta</Link>
          </Button>
        </section>
      </main>

      <footer className="flex items-center justify-center gap-2 border-t px-6 py-6 text-sm text-muted-foreground">
        <a
          href="https://github.com/KauanDiNubila/astra"
          target="_blank"
          rel="noreferrer"
          className="flex items-center gap-1.5 hover:text-foreground"
        >
          <Code2 className="size-4" />
          Código-fonte
        </a>
      </footer>
    </div>
  )
}
