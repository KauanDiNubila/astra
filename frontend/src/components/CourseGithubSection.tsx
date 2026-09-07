import { useEffect, useState } from "react"
import { X } from "lucide-react"
import { useGitHub } from "@/context/GitHubContext"
import { api } from "@/lib/api"
import type { CourseGithubSummary, GitHubInsightsResponse, GitHubRepositoryInsight } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export function CourseGithubSection({ courseId }: { courseId: string }) {
  const { status } = useGitHub()
  const [summary, setSummary] = useState<CourseGithubSummary | null>(null)
  const [availableRepos, setAvailableRepos] = useState<GitHubRepositoryInsight[]>([])
  const [selectedRepoId, setSelectedRepoId] = useState("")
  const [linking, setLinking] = useState(false)

  function load() {
    return api.get<CourseGithubSummary>(`/courses/${courseId}/github-repositories`).then((res) => setSummary(res.data))
  }

  useEffect(() => {
    if (!status?.connected) return
    load()
    api
      .get<GitHubInsightsResponse>("/github/insights")
      .then((res) => setAvailableRepos(res.data.repositories))
      .catch(() => {})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status?.connected, courseId])

  async function linkRepository() {
    if (!selectedRepoId) return
    setLinking(true)
    try {
      await api.post(`/courses/${courseId}/github-repositories`, { repositoryId: selectedRepoId })
      setSelectedRepoId("")
      await load()
    } finally {
      setLinking(false)
    }
  }

  async function unlinkRepository(repositoryId: string) {
    await api.delete(`/courses/${courseId}/github-repositories/${repositoryId}`)
    await load()
  }

  if (!status?.connected || !summary) {
    return null
  }

  const linkableRepos = availableRepos.filter(
    (r) => !summary.repositories.some((linked) => linked.id === r.id),
  )

  return (
    <Card>
      <CardHeader>
        <CardTitle>GitHub</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {summary.repositories.length > 0 && (
          <p className="text-sm text-muted-foreground">{summary.totalRecentCommits} commits recentes</p>
        )}
        {summary.repositories.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhum repositório vinculado ainda.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {summary.repositories.map((repo) => (
              <li key={repo.id} className="flex items-center justify-between gap-2 text-sm">
                <a href={repo.htmlUrl} target="_blank" rel="noopener noreferrer" className="hover:underline">
                  {repo.fullName}
                </a>
                <button
                  type="button"
                  title="Desvincular"
                  onClick={() => unlinkRepository(repo.id)}
                  className="text-muted-foreground hover:text-destructive"
                >
                  <X size={14} />
                </button>
              </li>
            ))}
          </ul>
        )}
        {linkableRepos.length > 0 && (
          <div className="flex gap-2">
            <select
              value={selectedRepoId}
              onChange={(e) => setSelectedRepoId(e.target.value)}
              className="flex-1 rounded-md border border-input bg-transparent px-2.5 py-1.5 text-sm"
            >
              <option value="">Selecionar repositório...</option>
              {linkableRepos.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.fullName}
                </option>
              ))}
            </select>
            <Button type="button" size="sm" variant="outline" disabled={!selectedRepoId || linking} onClick={linkRepository}>
              {linking ? "Vinculando..." : "Vincular"}
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
