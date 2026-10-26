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
      )}
    >
      {children}
    </button>
  )
}

const WINDOW_GLYPHS = {
  minimize: "\uE921",
  maximize: "\uE922",
  restore: "\uE923",
  close: "\uE8BB",
}

function Glyph({ name }: { name: keyof typeof WINDOW_GLYPHS }) {
  return (
    <span aria-hidden className="font-['Segoe_Fluent_Icons','Segoe_MDL2_Assets'] text-[10px] leading-none">
      {WINDOW_GLYPHS[name]}
    </span>
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
    "flex h-full w-[46px] items-center justify-center text-muted-foreground transition-colors hover:bg-black/[0.04] hover:text-foreground dark:hover:bg-white/[0.06]"

  return (
    <div className="flex h-full items-stretch">
      <button type="button" aria-label="Minimizar" className={button} onClick={() => controls.minimize()}>
        <Glyph name="minimize" />
      </button>
      <button
        type="button"
        aria-label={maximized ? "Restaurar" : "Maximizar"}
        className={button}
        onClick={() => controls.toggleMaximize()}
      >
        <Glyph name={maximized ? "restore" : "maximize"} />
      </button>
      <button
        type="button"
        aria-label="Fechar"
        className={cn(button, "hover:bg-[#c42b1c] hover:text-white dark:hover:bg-[#c42b1c]")}
        onClick={() => controls.close()}
      >
        <Glyph name="close" />
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
      className="fixed inset-x-0 top-0 z-[200] h-(--titlebar-h) bg-background text-foreground select-none"
    >
      <div
        aria-hidden
        className={cn(
          "pointer-events-none absolute inset-y-0 left-0 w-0 bg-sidebar transition-[width] duration-200 ease-linear max-md:hidden",
          "[html[data-app-sidebar]_&]:border-r [html[data-app-sidebar=collapsed]_&]:w-12 [html[data-app-sidebar=expanded]_&]:w-64",
          "[html[data-app-covered]_&]:w-0! [html[data-app-covered]_&]:border-r-0!",
        )}
      />
      <div
        className="relative flex h-full items-center"
        style={{ marginLeft: "env(titlebar-area-x, 0px)", width: "env(titlebar-area-width, 100%)" }}
      >
        <div className="flex items-center gap-0.5 px-2">
          <NavButton label="Voltar" disabled={!canGoBack} onClick={() => window.history.back()}>
            <ChevronLeft className="size-3.5" />
          </NavButton>
          <NavButton label="Avançar" disabled={!canGoForward} onClick={() => window.history.forward()}>
            <ChevronRight className="size-3.5" />
          </NavButton>
        </div>
        <div data-slot="title-bar-drag" className="h-full flex-1 [-webkit-app-region:drag]" />
        <p className="pointer-events-none absolute inset-x-0 text-center text-[11px] font-medium text-muted-foreground/60">
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
