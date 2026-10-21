import { BookOpen, Clock, GitBranch, LayoutDashboard, Map, MessageCircle, ShieldCheck, Trophy, Users } from "lucide-react"
import type { LucideIcon } from "lucide-react"

export type NavItem = { to: string; label: string; end: boolean; icon: LucideIcon }

export type NavGroup = { label: string; items: NavItem[] }

export const NAV_GROUPS: NavGroup[] = [
  {
    label: "Estudo",
    items: [
      { to: "/dashboard", label: "Dashboard", end: true, icon: LayoutDashboard },
      { to: "/sessions", label: "Sessões", end: false, icon: Clock },
      { to: "/courses", label: "Cursos", end: false, icon: BookOpen },
      { to: "/roadmaps", label: "Roadmaps", end: false, icon: Map },
    ],
  },
  {
    label: "Social",
    items: [
      { to: "/friends", label: "Amigos", end: false, icon: Users },
      { to: "/chat", label: "Chat", end: false, icon: MessageCircle },
      { to: "/ranking", label: "Ranking", end: false, icon: Trophy },
    ],
  },
  {
    label: "Integrações",
    items: [{ to: "/github", label: "GitHub", end: false, icon: GitBranch }],
  },
]

export const ADMIN_GROUP: NavGroup = {
  label: "Administração",
  items: [{ to: "/admin", label: "Admin", end: false, icon: ShieldCheck }],
}

export function navGroupsFor(role: string | undefined) {
  return role === "ADMIN" || role === "OWNER" ? [...NAV_GROUPS, ADMIN_GROUP] : NAV_GROUPS
}

export function isLinkActive(pathname: string, to: string, end: boolean) {
  if (end) return pathname === to
  return pathname === to || pathname.startsWith(`${to}/`)
}
