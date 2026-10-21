import { Sparkle } from "lucide-react"

export function AppLoading() {
  return (
    <div role="status" className="flex min-h-app items-center justify-center">
      <Sparkle className="size-6 animate-pulse fill-current text-muted-foreground motion-reduce:animate-none" />
      <span className="sr-only">Carregando</span>
    </div>
  )
}
