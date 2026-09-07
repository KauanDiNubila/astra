export type User = {
  id: string
  name: string
  email: string
  bio: string | null
  role: "USER" | "ADMIN" | "OWNER"
  tag: string
}

export type AdminUser = {
  id: string
  name: string
  email: string
  role: "USER" | "ADMIN" | "OWNER"
  banned: boolean
  createdAt: string
}

export type AuthResponse = {
  accessToken: string
  user: User
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

export type CategoryMinutes = {
  categoryId: string
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
  githubRepositoryId: string | null
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

export type StepStatus = "LEARNING" | "DONE" | "SKIPPED"

export type RoadmapStepResource = {
  id: string
  label: string
  url: string
  position: number
}

export type RoadmapStep = {
  id: string
  title: string
  position: number
  parentStepId: string | null
  status: StepStatus | null
  description: string | null
  resources: RoadmapStepResource[]
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
  admin: boolean
  founder: boolean
}

export type Friendship = {
  id: string
  friendUserId: string
  friendName: string
  friendBio: string | null
  friendAdmin: boolean
  friendFounder: boolean
  status: "PENDING" | "ACCEPTED"
  incoming: boolean
  createdAt: string
}

export type ReplyPreview = {
  id: string
  senderId: string
  contentPreview: string
}

export type Message = {
  id: string
  senderId: string
  recipientId: string | null
  groupId: string | null
  content: string | null
  createdAt: string
  read: boolean
  attachmentId: string | null
  replyTo: ReplyPreview | null
}

export type ConversationSummary = {
  friendUserId: string
  friendName: string
  friendTag: string
  friendBio: string | null
  friendAdmin: boolean
  friendFounder: boolean
  friendGithubLogin: string | null
  lastMessage: string | null
  lastMessageAt: string | null
  unreadCount: number
}

export type GroupMember = {
  userId: string
  name: string
  joinedAt: string
}

export type GroupConversationSummary = {
  groupId: string
  groupName: string
  memberNames: string[]
  lastMessage: string | null
  lastMessageAt: string | null
  unreadCount: number
}

export type GitHubConnectionStatus = {
  connected: boolean
  login: string | null
  avatarUrl: string | null
  connectedAt: string | null
  lastSyncedAt: string | null
  lastSyncError: string | null
  visibleToFriends: boolean
}

export type ActivityPeriod = "TODAY" | "WEEK" | "MONTH" | "QUARTER" | "YEAR"

export type GitHubPeriodSummary = {
  period: ActivityPeriod
  commitCount: number
  pullRequestOpenedCount: number
  pullRequestMergedCount: number
  issueClosedCount: number
  activeRepoCount: number
}

export type GitHubActivityResponse = {
  connected: boolean
  periods: GitHubPeriodSummary[]
  lastSyncedAt: string | null
  lastSyncError: string | null
}

export type GitHubRepositoryInsight = {
  id: string
  name: string
  fullName: string
  htmlUrl: string
  primaryLanguage: string | null
  recentCommitCount: number
  isPrivate: boolean
}

export type GitHubLanguageShare = {
  language: string
  commitCount: number
  percentage: number
}

export type GitHubDailyPoint = {
  date: string
  contributionCount: number
}

export type GitHubInsightsResponse = {
  connected: boolean
  repositories: GitHubRepositoryInsight[]
  languages: GitHubLanguageShare[]
  series: GitHubDailyPoint[]
  lastSyncedAt: string | null
  lastSyncError: string | null
}

export type GitHubCommitInfo = {
  sha: string
  message: string
  committedAt: string
  repositoryFullName: string
}

export type SessionGitHubActivity = {
  connected: boolean
  commits: GitHubCommitInfo[]
  suggestedRepositoryId: string | null
  suggestedRepositoryName: string | null
}

export type CourseGithubSummary = {
  repositories: GitHubRepositoryInsight[]
  totalRecentCommits: number
}

export type GitHubStepEvidence = {
  connected: boolean
  matchedRepositories: GitHubRepositoryInsight[]
}
