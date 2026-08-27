import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"
import { ChatProvider } from "@/context/ChatContext"
import { FriendsProvider } from "@/context/FriendsContext"
import { PomodoroProvider } from "@/context/PomodoroContext"
import { AppLayout } from "@/components/AppLayout"

export function ProtectedLayout() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex min-h-svh items-center justify-center text-muted-foreground">
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
          <AppLayout>
            <Outlet />
          </AppLayout>
        </ChatProvider>
      </FriendsProvider>
    </PomodoroProvider>
  )
}
