import type { ReactNode } from "react"
import { motion } from "motion/react"
import { useLocation } from "react-router-dom"
import { useChat } from "@/context/ChatContext"
import { useUnreadTabIndicator } from "@/hooks/useUnreadTabIndicator"
import { cn } from "@/lib/utils"
import { AppSidebar } from "@/components/AppSidebar"
import { GlobalPomodoroFocus } from "@/components/GlobalPomodoroFocus"
import { NotificationsPopover } from "@/components/NotificationsPopover"
import { PomodoroMiniWidget } from "@/components/PomodoroMiniWidget"
import { Separator } from "@/components/ui/separator"
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { TooltipProvider } from "@/components/ui/tooltip"

function getInitialSidebarOpen() {
  const match = document.cookie.match(/(?:^|; )sidebar_state=([^;]+)/)
  return match ? match[1] === "true" : true
}

export function AppLayout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation()
  const { totalUnread, totalGroupUnread } = useChat()
  useUnreadTabIndicator(totalUnread + totalGroupUnread)
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
          <header className="flex h-14 items-center gap-2 border-b px-4">
            <SidebarTrigger />
            <Separator orientation="vertical" className="h-4" />
            <div className="ml-auto">
              <NotificationsPopover />
            </div>
          </header>
          <motion.div
            key={pageKey}
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
            className={cn("mx-auto w-full px-6 py-8", !isChatRoute && "max-w-5xl")}
          >
            {children}
          </motion.div>
        </SidebarInset>
      </SidebarProvider>
      <GlobalPomodoroFocus />
      <PomodoroMiniWidget />
    </TooltipProvider>
  )
}
