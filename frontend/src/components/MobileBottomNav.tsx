import { BookOpen, Clock, LayoutDashboard, Menu, MessageCircle } from "lucide-react"
import { NavLink, useLocation } from "react-router-dom"
import { useChat } from "@/context/ChatContext"
import { cn } from "@/lib/utils"
import { useSidebar } from "@/components/ui/sidebar"

const items = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/sessions", label: "Sessões", icon: Clock },
  { to: "/courses", label: "Cursos", icon: BookOpen },
  { to: "/chat", label: "Chat", icon: MessageCircle },
]

const itemClass =
  "relative flex min-h-14 flex-1 flex-col items-center justify-center gap-0.5 text-[11px] font-medium transition-colors"

export function MobileBottomNav() {
  const { pathname } = useLocation()
  const { totalUnread, totalGroupUnread } = useChat()
  const { setOpenMobile } = useSidebar()
  const unread = totalUnread + totalGroupUnread
  const conversationOpen = pathname.startsWith("/chat/")

  if (conversationOpen) return null

  return (
    <nav
      aria-label="Navegação principal"
      className="fixed inset-x-0 bottom-0 z-40 flex border-t bg-background/95 pb-[env(safe-area-inset-bottom)] backdrop-blur md:hidden"
    >
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) =>
            cn(itemClass, isActive ? "text-primary" : "text-muted-foreground active:text-foreground")
          }
        >
          <item.icon className="size-5" />
          {item.label}
          {item.to === "/chat" && unread > 0 && (
            <span className="absolute left-1/2 top-1.5 ml-2 flex min-w-4 items-center justify-center rounded-full bg-primary px-1 text-[10px] leading-4 text-primary-foreground">
              {unread}
            </span>
          )}
        </NavLink>
      ))}
      <button
        type="button"
        onClick={() => setOpenMobile(true)}
        className={cn(itemClass, "text-muted-foreground active:text-foreground")}
      >
        <Menu className="size-5" />
        Menu
      </button>
    </nav>
  )
}
