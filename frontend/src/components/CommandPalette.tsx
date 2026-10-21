import { useEffect, useMemo, useRef, useState } from "react"
import type { ComponentType, KeyboardEvent } from "react"
import { createPortal } from "react-dom"
import { Maximize2, MessageCircle, Pause, PencilLine, Play, Search, Users } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { useChat } from "@/context/ChatContext"
import { usePomodoro } from "@/context/PomodoroContext"
import { useTheme } from "@/context/ThemeContext"
import { useDelayedUnmount } from "@/hooks/useDelayedUnmount"
import { COMMAND_PALETTE_EVENT } from "@/lib/commandPalette"
import { navGroupsFor } from "@/lib/navigation"
import { cn } from "@/lib/utils"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"

const CONVERSATIONS_WITHOUT_QUERY = 5

type Command = {
  id: string
  group: string
  label: string
  keywords?: string
  hint?: string
  icon: ComponentType<{ className?: string }>
  run: () => void
}

function normalize(text: string) {
  return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
}

function Kbd({ children }: { children: string }) {
  return <kbd className="rounded border px-1.5 py-px font-sans text-[10px] text-muted-foreground">{children}</kbd>
}

export function CommandPalette() {
  const navigate = useNavigate()
  const reducedMotion = useReducedMotion()
  const { user } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { running, focusedSeconds, mode, handlePrimaryClick, setFocusMode } = usePomodoro()
  const { conversations, groupConversations } = useChat()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")
  const [index, setIndex] = useState(0)
  const rendered = useDelayedUnmount(open, 200)
  const inputRef = useRef<HTMLInputElement>(null)
  const listRef = useRef<HTMLDivElement>(null)
  const openRef = useRef(open)

  useEffect(() => {
    openRef.current = open
  }, [open])

  useEffect(() => {
    function show() {
      setQuery("")
      setIndex(0)
      setOpen(true)
    }
    function onKeyDown(event: globalThis.KeyboardEvent) {
      if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== "k") return
      event.preventDefault()
      if (openRef.current) setOpen(false)
      else show()
    }
    window.addEventListener(COMMAND_PALETTE_EVENT, show)
    window.addEventListener("keydown", onKeyDown)
    return () => {
      window.removeEventListener(COMMAND_PALETTE_EVENT, show)
      window.removeEventListener("keydown", onKeyDown)
    }
  }, [])

  useEffect(() => {
    if (open) inputRef.current?.focus()
  }, [open, rendered])

  const commands = useMemo<Command[]>(() => {
    const active = running || focusedSeconds > 0 || mode === "break"
    const actions: Command[] = [
      {
        id: "focus",
        group: "Ações",
        label: running ? "Pausar foco" : active ? "Continuar foco" : "Iniciar foco",
        keywords: "pomodoro timer",
        icon: running ? Pause : Play,
        run: handlePrimaryClick,
      },
      {
        id: "focus-mode",
        group: "Ações",
        label: "Abrir modo foco",
        keywords: "tela cheia pomodoro",
        icon: Maximize2,
        run: () => setFocusMode(true),
      },
      {
        id: "manual",
        group: "Ações",
        label: "Registrar sessão manual",
        keywords: "adicionar sessao",
        icon: PencilLine,
        run: () => navigate("/sessions"),
      },
      {
        id: "theme",
        group: "Ações",
        label: theme === "dark" ? "Mudar para modo claro" : "Mudar para modo escuro",
        keywords: "tema",
        icon: (props) => <ThemeToggleIcon isDark={theme === "dark"} {...props} />,
        run: toggleTheme,
      },
    ]
    const pages: Command[] = navGroupsFor(user?.role).flatMap((group) =>
      group.items.map((item) => ({
        id: `page:${item.to}`,
        group: "Páginas",
        label: item.label,
        hint: group.label,
        icon: item.icon,
        run: () => navigate(item.to),
      })),
    )
    const chats: Command[] = [
      ...conversations.map((c) => ({
        id: `dm:${c.friendUserId}`,
        group: "Conversas",
        label: c.friendName,
        keywords: `chat mensagem ${c.friendTag}`,
        hint: c.unreadCount > 0 ? `${c.unreadCount} nova${c.unreadCount > 1 ? "s" : ""}` : undefined,
        icon: MessageCircle,
        run: () => navigate(`/chat/${c.friendUserId}`),
      })),
      ...groupConversations.map((g) => ({
        id: `group:${g.groupId}`,
        group: "Conversas",
        label: g.groupName,
        keywords: `chat grupo ${g.memberNames.join(" ")}`,
        hint: g.unreadCount > 0 ? `${g.unreadCount} nova${g.unreadCount > 1 ? "s" : ""}` : "Grupo",
        icon: Users,
        run: () => navigate(`/chat/g/${g.groupId}`),
      })),
    ]
    return [...actions, ...pages, ...chats]
  }, [running, focusedSeconds, mode, handlePrimaryClick, setFocusMode, navigate, theme, toggleTheme, user?.role, conversations, groupConversations])

  const results = useMemo(() => {
    const q = normalize(query.trim())
    if (!q) {
      let shownChats = 0
      return commands.filter((c) => c.group !== "Conversas" || shownChats++ < CONVERSATIONS_WITHOUT_QUERY)
    }
    return commands.filter((c) => normalize(`${c.label} ${c.keywords ?? ""} ${c.hint ?? ""}`).includes(q))
  }, [commands, query])

  const sections = useMemo(() => {
    const map = new Map<string, { command: Command; i: number }[]>()
    results.forEach((command, i) => {
      if (!map.has(command.group)) map.set(command.group, [])
      map.get(command.group)!.push({ command, i })
    })
    return [...map.entries()]
  }, [results])

  useEffect(() => {
    listRef.current?.querySelector(`[data-index="${index}"]`)?.scrollIntoView({ block: "nearest" })
  }, [index])

  function close() {
    setOpen(false)
  }

  function run(command: Command) {
    close()
    command.run()
  }

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      event.preventDefault()
      if (results.length === 0) return
      const step = event.key === "ArrowDown" ? 1 : -1
      setIndex((current) => (current + step + results.length) % results.length)
    } else if (event.key === "Enter") {
      event.preventDefault()
      const command = results[index]
      if (command) run(command)
    } else if (event.key === "Escape") {
      event.preventDefault()
      event.stopPropagation()
      close()
    }
  }

  if (!rendered) return null

  return createPortal(
    <div className={cn("fixed inset-0 z-[130] flex items-start justify-center px-4 pt-[14vh]", !open && "pointer-events-none")}>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: open ? 1 : 0 }}
        transition={{ duration: 0.16 }}
        onClick={close}
        className="absolute inset-0 bg-black/40 dark:bg-black/60"
      />
      <motion.div
        role="dialog"
        aria-modal="true"
        aria-label="Buscar"
        initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.97, y: -8 }}
        animate={
          reducedMotion
            ? { opacity: open ? 1 : 0 }
            : { opacity: open ? 1 : 0, scale: open ? 1 : 0.97, y: open ? 0 : -8 }
        }
        transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}
        className="relative w-full max-w-xl overflow-hidden rounded-xl border bg-popover text-popover-foreground shadow-2xl"
      >
        <div className="flex items-center gap-3 border-b px-4">
          <Search className="size-4 shrink-0 text-muted-foreground" />
          <input
            ref={inputRef}
            value={query}
            onChange={(event) => {
              setQuery(event.target.value)
              setIndex(0)
            }}
            onKeyDown={onKeyDown}
            placeholder="Buscar páginas, ações e conversas"
            aria-label="Buscar páginas, ações e conversas"
            className="h-12 min-w-0 flex-1 bg-transparent text-[15px] outline-none placeholder:text-muted-foreground"
          />
          <Kbd>Esc</Kbd>
        </div>
        <div ref={listRef} className="max-h-[min(60vh,420px)] overflow-y-auto p-1.5">
          {results.length === 0 ? (
            <p className="px-3 py-10 text-center text-sm text-muted-foreground">Nada encontrado para “{query.trim()}”.</p>
          ) : (
            sections.map(([group, items]) => (
              <div key={group}>
                <p className="px-2.5 pb-1 pt-2.5 text-[11px] font-medium tracking-wide text-muted-foreground/70 uppercase">
                  {group}
                </p>
                {items.map(({ command, i }) => (
                  <button
                    key={command.id}
                    type="button"
                    data-index={i}
                    onMouseMove={() => setIndex(i)}
                    onClick={() => run(command)}
                    className={cn(
                      "flex w-full items-center gap-3 rounded-md px-2.5 py-2 text-left text-sm transition-colors",
                      i === index ? "bg-accent text-accent-foreground" : "text-popover-foreground",
                    )}
                  >
                    <command.icon className="size-4 shrink-0 text-muted-foreground" />
                    <span className="min-w-0 flex-1 truncate">{command.label}</span>
                    {command.hint && <span className="shrink-0 text-xs text-muted-foreground">{command.hint}</span>}
                  </button>
                ))}
              </div>
            ))
          )}
        </div>
        <div className="flex items-center gap-4 border-t px-4 py-2 text-[11px] text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <Kbd>↑↓</Kbd> navegar
          </span>
          <span className="flex items-center gap-1.5">
            <Kbd>Enter</Kbd> abrir
          </span>
          <span className="flex items-center gap-1.5">
            <Kbd>Ctrl K</Kbd> fechar
          </span>
        </div>
      </motion.div>
    </div>,
    document.body,
  )
}
