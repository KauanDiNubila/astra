import { createContext, useContext, useEffect, useState } from "react"
import type { ReactNode } from "react"
import { flushSync } from "react-dom"
import { runThemeTransition } from "@/lib/themeTransition"

type Theme = "light" | "dark"

type ThemeContextValue = {
  theme: Theme
  toggleTheme: () => void
}

const THEME_KEY = "astra_theme"

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

function getInitialTheme(): Theme {
  const stored = localStorage.getItem(THEME_KEY)
  return stored === "light" ? "light" : "dark"
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>(getInitialTheme)

  useEffect(() => {
    document.documentElement.classList.toggle("dark", theme === "dark")
    localStorage.setItem(THEME_KEY, theme)
  }, [theme])

  function toggleTheme() {
    runThemeTransition(() => {
      flushSync(() => {
        setTheme((current) => (current === "dark" ? "light" : "dark"))
      })
    })
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  const ctx = useContext(ThemeContext)
  if (!ctx) {
    throw new Error("useTheme precisa estar dentro de um ThemeProvider")
  }
  return ctx
}
