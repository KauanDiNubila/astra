import type { ReactNode } from "react"
import { useLocation } from "react-router-dom"
import { AppSidebar } from "@/components/AppSidebar"
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
          </header>
          <div
            key={pathname}
            className="mx-auto w-full max-w-5xl animate-in px-6 py-8 fade-in duration-300 ease-out"
          >
            {children}
          </div>
        </SidebarInset>
      </SidebarProvider>
    </TooltipProvider>
  )
}
