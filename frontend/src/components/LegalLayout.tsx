import type { ReactNode } from "react"
import { ArrowLeft } from "lucide-react"
import { Link, useNavigate } from "react-router-dom"
import { LEGAL_LAST_UPDATED } from "@/lib/legal"

export function LegalLayout({ title, children }: { title: string; children: ReactNode }) {
  const navigate = useNavigate()

  function goBack() {
    if (window.history.length > 1) navigate(-1)
    else navigate("/")
  }

  return (
    <div className="min-h-app bg-background px-4 py-10 text-foreground sm:px-6">
      <article className="mx-auto w-full max-w-3xl">
        <button
          type="button"
          onClick={goBack}
          className="mb-8 flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Voltar
        </button>
        <h1 className="text-3xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-2 text-sm text-muted-foreground">Última atualização: {LEGAL_LAST_UPDATED}</p>
        <div className="mt-8 space-y-8 text-[15px] leading-relaxed text-foreground/90 [&_a]:underline [&_a]:underline-offset-4 [&_h2]:mb-3 [&_h2]:text-lg [&_h2]:font-semibold [&_h2]:text-foreground [&_li]:mt-1.5 [&_p+p]:mt-3 [&_ul]:list-disc [&_ul]:space-y-1 [&_ul]:pl-5">
          {children}
        </div>
        <footer className="mt-12 flex gap-4 border-t pt-6 text-sm text-muted-foreground">
          <Link to="/termos" className="hover:text-foreground">
            Termos de Uso
          </Link>
          <Link to="/privacidade" className="hover:text-foreground">
            Política de Privacidade
          </Link>
        </footer>
      </article>
    </div>
  )
}
