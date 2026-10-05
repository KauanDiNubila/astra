import { lazy, Suspense } from "react"
import { Route, Routes } from "react-router-dom"
import { DesktopTitleBar } from "@/components/DesktopTitleBar"
import { Toaster } from "@/components/ui/sonner"
import { LandingPage } from "@/pages/LandingPage"
import { LoginPage } from "@/pages/LoginPage"
import { RegisterPage } from "@/pages/RegisterPage"

const AdminRoute = lazy(() => import("@/components/AdminRoute").then((m) => ({ default: m.AdminRoute })))
const ProtectedLayout = lazy(() =>
  import("@/components/ProtectedLayout").then((m) => ({ default: m.ProtectedLayout })),
)
const AdminPage = lazy(() => import("@/pages/AdminPage").then((m) => ({ default: m.AdminPage })))
const ChatPage = lazy(() => import("@/pages/ChatPage").then((m) => ({ default: m.ChatPage })))
const CourseDetailPage = lazy(() =>
  import("@/pages/CourseDetailPage").then((m) => ({ default: m.CourseDetailPage })),
)
const CoursesPage = lazy(() => import("@/pages/CoursesPage").then((m) => ({ default: m.CoursesPage })))
const DashboardPage = lazy(() => import("@/pages/DashboardPage").then((m) => ({ default: m.DashboardPage })))
const FriendsPage = lazy(() => import("@/pages/FriendsPage").then((m) => ({ default: m.FriendsPage })))
const GitHubInsightsPage = lazy(() =>
  import("@/pages/GitHubInsightsPage").then((m) => ({ default: m.GitHubInsightsPage })),
)
const RankingPage = lazy(() => import("@/pages/RankingPage").then((m) => ({ default: m.RankingPage })))
const RoadmapDetailPage = lazy(() =>
  import("@/pages/RoadmapDetailPage").then((m) => ({ default: m.RoadmapDetailPage })),
)
const RoadmapsPage = lazy(() => import("@/pages/RoadmapsPage").then((m) => ({ default: m.RoadmapsPage })))
const SessionsPage = lazy(() => import("@/pages/SessionsPage").then((m) => ({ default: m.SessionsPage })))

function RouteFallback() {
  return (
    <div className="flex min-h-app items-center justify-center text-muted-foreground">Carregando...</div>
  )
}

function App() {
  return (
    <>
      <DesktopTitleBar />
      <Suspense fallback={<RouteFallback />}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<ProtectedLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/sessions" element={<SessionsPage />} />
            <Route path="/courses" element={<CoursesPage />} />
            <Route path="/courses/:id" element={<CourseDetailPage />} />
            <Route path="/roadmaps" element={<RoadmapsPage />} />
            <Route path="/roadmaps/:id" element={<RoadmapDetailPage />} />
            <Route path="/friends" element={<FriendsPage />} />
            <Route path="/chat" element={<ChatPage />} />
            <Route path="/chat/g/:groupId" element={<ChatPage />} />
            <Route path="/chat/:friendId" element={<ChatPage />} />
            <Route path="/ranking" element={<RankingPage />} />
            <Route path="/github" element={<GitHubInsightsPage />} />
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>
          </Route>
        </Routes>
      </Suspense>
      <Toaster />
    </>
  )
}

export default App
