import { BookOpen, Clock, LayoutDashboard, Map, Moon, Sun, Target, Trophy } from "lucide-react"
import { NavLink, useLocation } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { useTheme } from "@/context/ThemeContext"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar"

const links = [
  { to: "/", label: "Dashboard", end: true, icon: LayoutDashboard },
  { to: "/sessions", label: "Sessões", end: false, icon: Clock },
  { to: "/goals", label: "Metas", end: false, icon: Target },
  { to: "/courses", label: "Cursos", end: false, icon: BookOpen },
  { to: "/roadmaps", label: "Roadmaps", end: false, icon: Map },
  { to: "/ranking", label: "Ranking", end: false, icon: Trophy },
]

function isLinkActive(pathname: string, to: string, end: boolean) {
  if (end) return pathname === to
  return pathname === to || pathname.startsWith(`${to}/`)
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ""
  const last = parts.length > 1 ? parts[parts.length - 1][0] : ""
  return (first + last).toUpperCase()
}

export function AppSidebar() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { pathname } = useLocation()

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton asChild size="lg" tooltip="Astra">
              <NavLink to="/">
                <span className="text-lg font-semibold">Astra</span>
              </NavLink>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarMenu>
            {links.map((link) => (
              <SidebarMenuItem key={link.to}>
                <SidebarMenuButton
                  asChild
                  tooltip={link.label}
                  isActive={isLinkActive(pathname, link.to, link.end)}
                >
                  <NavLink to={link.to} end={link.end}>
                    <link.icon />
                    <span>{link.label}</span>
                  </NavLink>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <div className="flex items-center gap-2 px-2 py-1 group-data-[collapsible=icon]:hidden">
          <Avatar size="sm">
            <AvatarFallback>{initials(user?.name ?? "")}</AvatarFallback>
          </Avatar>
          <span className="truncate text-sm text-muted-foreground">{user?.name}</span>
        </div>
        <Separator />
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              tooltip={theme === "dark" ? "Modo claro" : "Modo escuro"}
              onClick={toggleTheme}
            >
              {theme === "dark" ? <Sun /> : <Moon />}
              <span>{theme === "dark" ? "Modo claro" : "Modo escuro"}</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
        <Button variant="outline" size="sm" onClick={logout} className="group-data-[collapsible=icon]:hidden">
          Sair
        </Button>
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
