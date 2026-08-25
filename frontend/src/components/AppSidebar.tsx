import { useState } from "react"
import {
  BookOpen,
  Clock,
  LayoutDashboard,
  Map,
  MessageCircle,
  ShieldCheck,
  Trophy,
  Users,
} from "lucide-react"
import { motion } from "motion/react"
import { NavLink, useLocation } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { useChat } from "@/context/ChatContext"
import { useTheme } from "@/context/ThemeContext"
import { baseURL } from "@/lib/api"
import { getLastRoadmapId } from "@/lib/lastRoadmap"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { EditProfileModal } from "@/components/EditProfileModal"
import { Separator } from "@/components/ui/separator"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"
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
  { to: "/dashboard", label: "Dashboard", end: true, icon: LayoutDashboard },
  { to: "/sessions", label: "Sessões", end: false, icon: Clock },
  { to: "/courses", label: "Cursos", end: false, icon: BookOpen },
  { to: "/roadmaps", label: "Roadmaps", end: false, icon: Map },
  { to: "/friends", label: "Amigos", end: false, icon: Users },
  { to: "/chat", label: "Chat", end: false, icon: MessageCircle },
  { to: "/ranking", label: "Ranking", end: false, icon: Trophy },
]

const adminLink = { to: "/admin", label: "Admin", end: false, icon: ShieldCheck }

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
  const { user, logout, avatarVersion } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { totalUnread } = useChat()
  const { pathname } = useLocation()
  const lastRoadmapId = getLastRoadmapId()
  const [profileOpen, setProfileOpen] = useState(false)
  const visibleLinks = user?.role === "ADMIN" ? [...links, adminLink] : links

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton asChild size="lg" tooltip="Astra">
              <NavLink to="/dashboard">
                <span className="text-lg font-semibold">Astra</span>
              </NavLink>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarMenu>
            {visibleLinks.map((link) => {
              const to = link.to === "/roadmaps" && lastRoadmapId ? `/roadmaps/${lastRoadmapId}` : link.to
              const active = isLinkActive(pathname, link.to, link.end)
              return (
                <SidebarMenuItem key={link.to}>
                  {active && (
                    <motion.div
                      layoutId="active-nav-pill"
                      className="absolute inset-0 rounded-md bg-sidebar-accent"
                      transition={{ type: "spring", stiffness: 500, damping: 35 }}
                    />
                  )}
                  <SidebarMenuButton
                    asChild
                    tooltip={link.label}
                    isActive={active}
                    className="relative z-10 data-active:bg-transparent"
                  >
                    <NavLink to={to} end={link.end}>
                      <link.icon />
                      <span>{link.label}</span>
                      {link.to === "/chat" && totalUnread > 0 && (
                        <span className="ml-auto flex size-4.5 items-center justify-center rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
                          {totalUnread}
                        </span>
                      )}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              )
            })}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter>
        <button
          type="button"
          onClick={() => setProfileOpen(true)}
          className="flex items-center gap-2 rounded-md px-2 py-1 text-left transition-colors hover:bg-sidebar-accent group-data-[collapsible=icon]:hidden"
        >
          <Avatar size="sm">
            {user && <AvatarImage src={`${baseURL}/users/${user.id}/avatar?v=${avatarVersion}`} />}
            <AvatarFallback>{initials(user?.name ?? "")}</AvatarFallback>
          </Avatar>
          <span className="flex min-w-0 flex-col">
            <span className="truncate text-sm text-muted-foreground">{user?.name}</span>
            {user?.bio && <span className="truncate text-xs text-muted-foreground/70">{user.bio}</span>}
          </span>
        </button>
        <EditProfileModal open={profileOpen} onClose={() => setProfileOpen(false)} />
        <Separator />
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              tooltip={theme === "dark" ? "Modo claro" : "Modo escuro"}
              onClick={toggleTheme}
            >
              <ThemeToggleIcon isDark={theme === "dark"} className="size-5" />
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
