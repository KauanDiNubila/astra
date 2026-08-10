import { useState } from "react"
import { Moon, Sun } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"

function App() {
  const [count, setCount] = useState(0)
  const [dark, setDark] = useState(false)

  function toggleTheme() {
    const next = !dark
    setDark(next)
    document.documentElement.classList.toggle("dark", next)
  }

  return (
    <div className="flex min-h-svh items-center justify-center bg-background p-6 text-foreground">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-2xl">Astra</CardTitle>
            <Button variant="ghost" size="icon" onClick={toggleTheme} aria-label="Alternar tema">
              {dark ? <Sun className="size-5" /> : <Moon className="size-5" />}
            </Button>
          </div>
          <CardDescription>Front-end com Tailwind + shadcn/ui no ar.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          <p className="text-sm text-muted-foreground">
            Teste a interatividade e o bot&atilde;o de tema (canto superior).
          </p>
          <Button onClick={() => setCount((c) => c + 1)}>Contei {count} vez(es)</Button>
          <Button variant="outline">Bot&atilde;o secund&aacute;rio</Button>
        </CardContent>
        <CardFooter>
          <p className="text-xs text-muted-foreground">Pr&oacute;ximo: cliente da API + login.</p>
        </CardFooter>
      </Card>
    </div>
  )
}

export default App
