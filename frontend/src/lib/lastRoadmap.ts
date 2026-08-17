const STORAGE_KEY = "astra:last-roadmap"

export function getLastRoadmapId(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem(STORAGE_KEY)
}

export function setLastRoadmapId(id: string) {
  localStorage.setItem(STORAGE_KEY, id)
}
