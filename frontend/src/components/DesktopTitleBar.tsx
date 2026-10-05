import { useEffect, useState } from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { useLocation } from "react-router-dom"
import { useTheme } from "@/context/ThemeContext"
import { pageTitleFor } from "@/lib/pageTitles"
import { cn } from "@/lib/utils"

interface NavigationApi extends EventTarget {
  canGoBack: boolean
  canGoForward: boolean
}

function readHistoryState() {
  const navigation = (window as unknown as { navigation?: NavigationApi }).navigation
  return {
    canGoBack: navigation ? navigation.canGoBack : window.history.length > 1,
    canGoForward: navigation ? navigation.canGoForward : true,
  }
}

function useHistoryState() {
  const { key } = useLocation()
  const [state, setState] = useState(readHistoryState)

  useEffect(() => {
    setState(readHistoryState())
    const navigation = (window as unknown as { navigation?: NavigationApi }).navigation
    if (!navigation) return
    const update = () => setState(readHistoryState())
    navigation.addEventListener("currententrychange", update)
    return () => navigation.removeEventListener("currententrychange", update)
  }, [key])

  return state
}

const NO_DRAG = "[-webkit-app-region:no-drag]"

function NavButton({
  label,
  disabled,
  onClick,
  children,
}: {
  label: string
  disabled: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      aria-label={label}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        "flex size-5 items-center justify-center rounded text-muted-foreground transition-colors",
        "hover:bg-sidebar-accent hover:text-sidebar-foreground disabled:pointer-events-none disabled:opacity-35",
        NO_DRAG,
      )}
    >
      {children}
    </button>
  )
}

function Glyph({ children }: { children: React.ReactNode }) {
  return (
    <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="1" aria-hidden>
      {children}
    </svg>
  )
}

function WindowControls() {
  const controls = window.astraDesktop?.windowControls
  const [maximized, setMaximized] = useState(false)

  useEffect(() => {
    if (!controls) return
    let active = true
    void controls.isMaximized().then((value) => {
      if (active) setMaximized(value)
    })
    const stop = controls.onMaximizedChange(setMaximized)
    return () => {
      active = false
      stop()
    }
  }, [controls])

  if (!controls) return null

  const button =
    "flex h-full w-10 items-center justify-center text-muted-foreground transition-colors hover:bg-sidebar-accent hover:text-sidebar-foreground " +
    NO_DRAG

  return (
    <div className="ml-auto flex h-full items-stretch">
      <button type="button" aria-label="Minimizar" className={button} onClick={controls.minimize}>
        <Glyph>
          <path d="M0.5 5.5h9" />
        </Glyph>
      </button>
      <button
        type="button"
        aria-label={maximized ? "Restaurar" : "Maximizar"}
        className={button}
        onClick={controls.toggleMaximize}
      >
        <Glyph>
          <rect x="1.5" y="1.5" width="7" height="7" />
        </Glyph>
      </button>
      <button
        type="button"
        aria-label="Fechar"
        className={cn(button, "hover:bg-red-600 hover:text-white")}
        onClick={controls.close}
      >
        <Glyph>
          <path d="M0.5 0.5l9 9M9.5 0.5l-9 9" />
        </Glyph>
      </button>
    </div>
  )
}

function TitleBar() {
  const { pathname } = useLocation()
  const { theme } = useTheme()
  const { canGoBack, canGoForward } = useHistoryState()

  useEffect(() => {
    window.astraDesktop?.setTitleBarTheme?.(theme)
  }, [theme])

  return (
    <div
      data-slot="desktop-title-bar"
      className="fixed inset-x-0 top-0 z-[200] h-(--titlebar-h) bg-sidebar text-sidebar-foreground select-none [-webkit-app-region:drag]"
    >
      <div
        className="relative flex h-full items-center pl-2"
        style={{ marginLeft: "env(titlebar-area-x, 0px)", width: "env(titlebar-area-width, 100%)" }}
      >
        <div className="flex items-center gap-0.5">
          <NavButton label="Voltar" disabled={!canGoBack} onClick={() => window.history.back()}>
            <ChevronLeft className="size-3.5" />
          </NavButton>
          <NavButton label="Avançar" disabled={!canGoForward} onClick={() => window.history.forward()}>
            <ChevronRight className="size-3.5" />
          </NavButton>
        </div>
        <p className="pointer-events-none absolute inset-x-0 text-center text-[11px] font-medium text-muted-foreground">
          {pageTitleFor(pathname) ?? "Astra"}
        </p>
        <WindowControls />
      </div>
    </div>
  )
}

export function DesktopTitleBar() {
  if (!window.astraDesktop?.customTitleBar) return null
  return <TitleBar />
}
