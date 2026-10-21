import { useState } from "react"
import { ChevronsUpDown, FileText, LogOut, Search, Shield, Sparkle, UserPen } from "lucide-react"
import { motion } from "motion/react"
import { NavLink, useLocation } from "react-router-dom"
import { LegalLink } from "@/context/LegalContext"
import { useAuth } from "@/context/AuthContext"
import { useChat } from "@/context/ChatContext"
import { useTheme } from "@/context/ThemeContext"
import { baseURL } from "@/lib/api"
import { openCommandPalette } from "@/lib/commandPalette"
import { isLinkActive, navGroupsFor } from "@/lib/navigation"
import { cn } from "@/lib/utils"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { AdminBadge } from "@/components/AdminBadge"
import { EditProfileModal } from "@/components/EditProfileModal"
import { SidebarFocus } from "@/components/SidebarFocus"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Separator } from "@/components/ui/separator"
import { ThemeToggleIcon } from "@/components/ThemeToggleIcon"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  useSidebar,
} from "@/components/ui/sidebar"

function initials(name: string) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ""
  const last = parts.length > 1 ? parts[parts.length - 1][0] : ""
  return (first + last).toUpperCase()
}

function UserMenu() {
  const { user, logout, avatarVersion } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const { state, isMobile } = useSidebar()
  const [open, setOpen] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const isStaff = user?.role === "ADMIN" || user?.role === "OWNER"
  const collapsed = state === "collapsed" && !isMobile

  function handleOpenChange(next: boolean) {
    setOpen(next)
    if (!next) setConfirming(false)
  }

  async function confirmLogout() {
    setLoggingOut(true)
    await logout()
  }

  const itemClass =
    "flex w-full items-center gap-2.5 rounded-md px-2 py-1.5 text-left text-sm text-popover-foreground transition-colors hover:bg-accent [&_svg]:size-4 [&_svg]:shrink-0 [&_svg]:text-muted-foreground"

  return (
    <>
      <Popover open={open} onOpenChange={handleOpenChange}>
        <PopoverTrigger asChild>
          <button
            type="button"
            className="flex w-full items-center gap-2 rounded-md p-1.5 text-left transition-colors hover:bg-sidebar-accent data-[state=open]:bg-sidebar-accent group-data-[collapsible=icon]:justify-center group-data-[collapsible=icon]:p-0"
          >
            <Avatar size="sm" className="shrink-0">
              {user && <AvatarImage src={`${baseURL}/users/${user.id}/avatar?v=${avatarVersion}`} />}
              <AvatarFallback>{initials(user?.name ?? "")}</AvatarFallback>
            </Avatar>
            <span className="flex min-w-0 flex-1 flex-col group-data-[collapsible=icon]:hidden">
              <span className="flex items-center gap-1 truncate text-sm font-medium">
                {user?.name}
                {isStaff && <AdminBadge />}
              </span>
              {user?.bio && <span className="truncate text-xs text-muted-foreground">{user.bio}</span>}
            </span>
            <ChevronsUpDown className="size-4 shrink-0 text-muted-foreground group-data-[collapsible=icon]:hidden" />
          </button>
        </PopoverTrigger>
        <PopoverContent
          side={collapsed ? "right" : "top"}
          align={collapsed ? "end" : "start"}
          className={cn("gap-0 p-1", collapsed ? "w-60" : "w-(--radix-popover-trigger-width) min-w-56")}
        >
          {confirming ? (
            <div className="flex flex-col gap-3 p-2">
              <p className="text-sm text-popover-foreground">Deseja mesmo sair?</p>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" className="flex-1" onClick={() => setConfirming(false)}>
                  Cancelar
                </Button>
                <Button variant="destructive" size="sm" className="flex-1" disabled={loggingOut} onClick={confirmLogout}>
                  {loggingOut ? "Saindo..." : "Sair"}
                </Button>
              </div>
            </div>
          ) : (
            <>
              <button
                type="button"
                className={itemClass}
                onClick={() => {
                  handleOpenChange(false)
                  setProfileOpen(true)
                }}
              >
                <UserPen />
                Editar perfil
              </button>
              <button type="button" className={itemClass} onClick={toggleTheme}>
                <ThemeToggleIcon isDark={theme === "dark"} />
                {theme === "dark" ? "Modo claro" : "Modo escuro"}
              </button>
              <Separator className="my-1" />
              <div onClick={() => handleOpenChange(false)}>
                <LegalLink doc="terms" className={itemClass}>
                  <FileText />
                  Termos de Uso
                </LegalLink>
                <LegalLink doc="privacy" className={itemClass}>
                  <Shield />
                  Política de Privacidade
                </LegalLink>
              </div>
              <Separator className="my-1" />
              <button type="button" className={itemClass} onClick={() => setConfirming(true)}>
                <LogOut />
                Sair
              </button>
            </>
          )}
        </PopoverContent>
      </Popover>
      <EditProfileModal open={profileOpen} onClose={() => setProfileOpen(false)} />
    </>
  )
}

export function AppSidebar() {
  const { user } = useAuth()
  const { totalUnread: totalDmUnread, totalGroupUnread } = useChat()
  const totalUnread = totalDmUnread + totalGroupUnread
  const { pathname } = useLocation()
  const visibleGroups = navGroupsFor(user?.role)

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton asChild size="default" tooltip="Astra">
              <NavLink to="/dashboard">
                <Sparkle className="fill-current" />
                <span className="text-base font-semibold">Astra</span>
              </NavLink>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
        <SidebarFocus />
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton tooltip="Buscar (Ctrl K)" onClick={openCommandPalette} className="text-muted-foreground">
              <Search />
              <span>Buscar</span>
              <kbd className="ml-auto rounded border border-sidebar-border px-1.5 font-sans text-[10px] text-muted-foreground group-data-[collapsible=icon]:hidden">
                Ctrl K
              </kbd>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent className="gap-0">
        {visibleGroups.map((group) => (
          <SidebarGroup key={group.label} className="py-1">
            <SidebarGroupLabel className="h-7 text-[11px] font-medium tracking-wide text-sidebar-foreground/50 uppercase">
              {group.label}
            </SidebarGroupLabel>
            <SidebarMenu>
              {group.items.map((link) => {
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
                      <NavLink to={link.to} end={link.end}>
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
        ))}
      </SidebarContent>
      <SidebarFooter>
        <UserMenu />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
