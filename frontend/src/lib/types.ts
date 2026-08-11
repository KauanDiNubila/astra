export type User = {
  id: string
  name: string
  email: string
}

export type GoalProgress = {
  type: "DAILY" | "WEEKLY"
  targetHours: number
  achievedHours: number
  reached: boolean
}

export type Dashboard = {
  todayMinutes: number
  weekMinutes: number
  totalMinutes: number
  currentStreak: number
  goals: GoalProgress[]
}

export type Category = {
  id: string
  name: string
  color: string | null
}

export type Session = {
  id: string
  categoryId: string
  courseId: string | null
  focusedMinutes: number
  startedAt: string
  note: string | null
  createdAt: string
}
