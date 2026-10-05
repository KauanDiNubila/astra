export type GithubProfileSummary = {
  publicRepos: number
  followers: number
  since: number
}

const cache = new Map<string, Promise<GithubProfileSummary | null>>()

export function fetchGithubProfile(login: string) {
  const key = login.toLowerCase()
  let request = cache.get(key)
  if (!request) {
    request = load(login)
    cache.set(key, request)
  }
  return request
}

async function load(login: string): Promise<GithubProfileSummary | null> {
  try {
    const response = await fetch(`https://api.github.com/users/${encodeURIComponent(login)}`, {
      headers: { Accept: "application/vnd.github+json" },
    })
    if (!response.ok) return null
    const data = (await response.json()) as { public_repos?: number; followers?: number; created_at?: string }
    if (typeof data.public_repos !== "number" || typeof data.followers !== "number" || !data.created_at) return null
    return {
      publicRepos: data.public_repos,
      followers: data.followers,
      since: new Date(data.created_at).getFullYear(),
    }
  } catch {
    return null
  }
}
