import { Route, Routes } from "react-router-dom"
import { ProtectedLayout } from "@/components/ProtectedLayout"
import { Toaster } from "@/components/ui/sonner"
import { CourseDetailPage } from "@/pages/CourseDetailPage"
import { CoursesPage } from "@/pages/CoursesPage"
import { DashboardPage } from "@/pages/DashboardPage"
import { FriendsPage } from "@/pages/FriendsPage"
import { GoalsPage } from "@/pages/GoalsPage"
import { LandingPage } from "@/pages/LandingPage"
import { LoginPage } from "@/pages/LoginPage"
import { RankingPage } from "@/pages/RankingPage"
import { RegisterPage } from "@/pages/RegisterPage"
import { RoadmapDetailPage } from "@/pages/RoadmapDetailPage"
import { RoadmapsPage } from "@/pages/RoadmapsPage"
import { SessionsPage } from "@/pages/SessionsPage"

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<ProtectedLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/sessions" element={<SessionsPage />} />
          <Route path="/goals" element={<GoalsPage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/courses/:id" element={<CourseDetailPage />} />
          <Route path="/roadmaps" element={<RoadmapsPage />} />
          <Route path="/roadmaps/:id" element={<RoadmapDetailPage />} />
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="/ranking" element={<RankingPage />} />
        </Route>
      </Routes>
      <Toaster />
    </>
  )
}

export default App
