export type User = {
  id: string
  name: string
  email: string
  bio: string | null
  role: "USER" | "ADMIN"
}

export type AdminUser = {
  id: string
  name: string
  email: string
  role: "USER" | "ADMIN"
  banned: boolean
  createdAt: string
}

export type AuthResponse = {
  accessToken: string
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
  totalLessons: number
  completedLessons: number
}

export type LessonItem = {
  id: string
  title: string
  position: number
  completed: boolean
}

export type ModuleItem = {
  id: string
  title: string
  position: number
  totalLessons: number
  completedLessons: number
  completed: boolean
  lessons: LessonItem[]
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
  completed: boolean
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

export type Friendship = {
  id: string
  friendUserId: string
  friendName: string
  friendBio: string | null
  status: "PENDING" | "ACCEPTED"
  incoming: boolean
  createdAt: string
}

export type Message = {
  id: string
  senderId: string
  recipientId: string
  content: string
  createdAt: string
  read: boolean
}

export type ConversationSummary = {
  friendUserId: string
  friendName: string
  friendBio: string | null
  lastMessage: string | null
  lastMessageAt: string | null
  unreadCount: number
}
