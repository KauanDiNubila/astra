import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { CallProvider } from "@/context/CallContext"
import { ChatProvider } from "@/context/ChatContext"
import { FriendsProvider } from "@/context/FriendsContext"
import { GitHubProvider } from "@/context/GitHubContext"
import { PomodoroProvider } from "@/context/PomodoroContext"
import { AppLayout } from "@/components/AppLayout"
import { TermsGate } from "@/components/TermsGate"

export function ProtectedLayout() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex min-h-app items-center justify-center text-muted-foreground">
        Carregando...
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return (
    <PomodoroProvider>
      <FriendsProvider>
        <ChatProvider>
          <CallProvider>
            <GitHubProvider>
              <AppLayout>
                <Outlet />
              </AppLayout>
              <TermsGate />
            </GitHubProvider>
          </CallProvider>
        </ChatProvider>
      </FriendsProvider>
    </PomodoroProvider>
  )
}
