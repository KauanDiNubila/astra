import { lazy, Suspense } from "react"
import { pageLoaders } from "@/lib/routeWarmup"
import { Outlet, Route, Routes } from "react-router-dom"
import { AppLoading } from "@/components/AppLoading"
import { DesktopTitleBar } from "@/components/DesktopTitleBar"
import { OverlayScrollbar } from "@/components/OverlayScrollbar"
import { ScrollToTop } from "@/components/ScrollToTop"
import { Toaster } from "@/components/ui/sonner"
import { LegalProvider } from "@/context/LegalContext"
import { LoginPage } from "@/pages/LoginPage"
import { RegisterPage } from "@/pages/RegisterPage"

const AdminRoute = lazy(() => import("@/components/AdminRoute").then((m) => ({ default: m.AdminRoute })))
const ProtectedLayout = lazy(() =>
  import("@/components/ProtectedLayout").then((m) => ({ default: m.ProtectedLayout })),
)
const LandingPage = lazy(() => import("@/pages/LandingPage").then((m) => ({ default: m.LandingPage })))
const AdminPage = lazy(() => import("@/pages/AdminPage").then((m) => ({ default: m.AdminPage })))
const ChatPage = lazy(pageLoaders.chat)
const CourseDetailPage = lazy(() =>
  import("@/pages/CourseDetailPage").then((m) => ({ default: m.CourseDetailPage })),
)
const CoursesPage = lazy(pageLoaders.courses)
const DashboardPage = lazy(pageLoaders.dashboard)
const FriendsPage = lazy(pageLoaders.friends)
const GitHubInsightsPage = lazy(pageLoaders.github)
const RankingPage = lazy(pageLoaders.ranking)
const RoadmapDetailPage = lazy(() =>
  import("@/pages/RoadmapDetailPage").then((m) => ({ default: m.RoadmapDetailPage })),
)
const RoadmapsPage = lazy(pageLoaders.roadmaps)
const PrivacyPage = lazy(() => import("@/pages/PrivacyPage").then((m) => ({ default: m.PrivacyPage })))
const ForgotPasswordPage = lazy(() =>
  import("@/pages/ForgotPasswordPage").then((m) => ({ default: m.ForgotPasswordPage })),
)
const ResetPasswordPage = lazy(() =>
  import("@/pages/ResetPasswordPage").then((m) => ({ default: m.ResetPasswordPage })),
)
const TermsPage = lazy(() => import("@/pages/TermsPage").then((m) => ({ default: m.TermsPage })))
const SessionsPage = lazy(pageLoaders.sessions)

function App() {
  return (
    <>
      <ScrollToTop />
      <DesktopTitleBar />
      <OverlayScrollbar />
      <Suspense fallback={<AppLoading />}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route
            element={
              <LegalProvider>
                <Outlet />
              </LegalProvider>
            }
          >
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
          </Route>
          <Route path="/privacidade" element={<PrivacyPage />} />
          <Route path="/termos" element={<TermsPage />} />
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
