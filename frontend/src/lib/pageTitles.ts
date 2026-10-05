export const PAGE_TITLES: Record<string, string> = {
  dashboard: "Dashboard",
  sessions: "Sessões",
  courses: "Cursos",
  roadmaps: "Roadmaps",
  friends: "Amigos",
  chat: "Chat",
  ranking: "Ranking",
  github: "GitHub Insights",
  admin: "Admin",
}

export function pageTitleFor(pathname: string) {
  return PAGE_TITLES[pathname.split("/")[1] ?? ""]
}
