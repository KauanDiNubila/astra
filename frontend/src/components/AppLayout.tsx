import type { ReactNode } from "react"
import { motion } from "motion/react"
import { useLocation } from "react-router-dom"
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
            key={pathname}
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
            className="mx-auto w-full max-w-5xl px-6 py-8"
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
