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

export type DailyMinutes = {
  day: string
  minutes: number
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

export type CourseStatus = "PLANNED" | "IN_PROGRESS" | "DONE"

export type CourseSummary = {
  id: string
  title: string
  platform: string | null
  status: CourseStatus
  progress: number
  totalModules: number
  completedModules: number
}

export type ModuleItem = {
  id: string
  title: string
  position: number
  completed: boolean
}

export type CourseDetail = CourseSummary & {
  modules: ModuleItem[]
}

export type Goal = {
  type: "DAILY" | "WEEKLY"
  targetHours: number
}

export type Roadmap = {
  id: string
  title: string
  source: string | null
  predefined: boolean
}

export type RoadmapStep = {
  id: string
  title: string
  position: number
  parentStepId: string | null
}

export type RoadmapDetail = Roadmap & {
  steps: RoadmapStep[]
}

export type Pin = {
  id: string
  courseId: string
  stepId: string
  status: string | null
  rating: number | null
}

export type RankingEntry = {
  position: number
  userId: string
  name: string
  minutes: number
}
