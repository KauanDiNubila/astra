import { Suspense } from "react"
import type { ReactNode } from "react"
import { motion } from "motion/react"
import { Sparkle } from "lucide-react"
import { NavLink, useLocation } from "react-router-dom"
import { useChat } from "@/context/ChatContext"
import { useUnreadTabIndicator } from "@/hooks/useUnreadTabIndicator"
import { pageTitleFor } from "@/lib/pageTitles"
import { cn } from "@/lib/utils"
import { AppSidebar } from "@/components/AppSidebar"
import { CallOverlay } from "@/components/CallOverlay"
import { CommandPalette } from "@/components/CommandPalette"
import { GlobalPomodoroFocus } from "@/components/GlobalPomodoroFocus"
import { IncomingCallDialog } from "@/components/IncomingCallDialog"
import { MobileBottomNav } from "@/components/MobileBottomNav"
import { NotificationsPopover } from "@/components/NotificationsPopover"
import { PomodoroMiniWidget } from "@/components/PomodoroMiniWidget"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"

function getInitialSidebarOpen() {
  const match = document.cookie.match(/(?:^|; )sidebar_state=([^;]+)/)
  return match ? match[1] === "true" : true
}

export function AppLayout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation()
  const { totalUnread, totalGroupUnread } = useChat()
  useUnreadTabIndicator(totalUnread + totalGroupUnread, pageTitleFor(pathname))
  // Chat se beneficia de ocupar a largura toda, igual apps de mensagem
  // dedicados — as outras páginas (texto/formulário) ficam melhor contidas.
  const isChatRoute = pathname.startsWith("/chat")
  // Trocar de amigo/grupo muda o pathname (/chat/:friendId), mas não é uma
  // "página" nova — sem isso, o key={pathname} do motion.div desmonta e
  // remonta o layout inteiro (incluindo o card do chat) a cada troca de
  // conversa, com a transição de entrada da página por cima.
  const pageKey = isChatRoute ? "/chat" : pathname

  return (
    <TooltipProvider>
      <SidebarProvider defaultOpen={getInitialSidebarOpen()}>
        <AppSidebar />
        <SidebarInset>
          <header className="flex h-14 items-center gap-2 border-b px-4 md:hidden">
            <NavLink to="/dashboard" className="flex items-center gap-2 text-[15px] font-semibold">
              <Sparkle className="size-4 fill-current" />
              Astra
            </NavLink>
            <div className="ml-auto">
              <NotificationsPopover />
            </div>
          </header>
          <motion.div
            key={pageKey}
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
            className={cn(
              "mx-auto w-full",
              isChatRoute ? "px-0 sm:px-6 sm:py-8" : "max-w-5xl px-4 py-6 max-md:pb-24 sm:px-6 sm:py-8",
            )}
          >
            <Suspense fallback={null}>{children}</Suspense>
          </motion.div>
        </SidebarInset>
        <MobileBottomNav />
        <PomodoroMiniWidget />
      </SidebarProvider>
      <GlobalPomodoroFocus />
      <CallOverlay />
      <IncomingCallDialog />
      <CommandPalette />
    </TooltipProvider>
  )
}
