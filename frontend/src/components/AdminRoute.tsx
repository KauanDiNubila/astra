import { Navigate, Outlet } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"

export function AdminRoute() {
  const { user } = useAuth()

  if (user?.role !== "ADMIN" && user?.role !== "OWNER") {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
