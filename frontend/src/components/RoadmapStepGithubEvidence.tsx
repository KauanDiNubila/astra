import { useEffect, useState } from "react"
import { useGitHub } from "@/context/GitHubContext"
import { api } from "@/lib/api"
import type { GitHubStepEvidence } from "@/lib/types"

export function RoadmapStepGithubEvidence({ stepId }: { stepId: string }) {
  const { status } = useGitHub()
  const [evidence, setEvidence] = useState<GitHubStepEvidence | null>(null)

  useEffect(() => {
    if (!status?.connected) return
    const controller = new AbortController()
    api
      .get<GitHubStepEvidence>(`/steps/${stepId}/github-evidence`, { signal: controller.signal })
      .then((res) => setEvidence(res.data))
      .catch(() => {})
    return () => controller.abort()
  }, [stepId, status?.connected])

  if (!status?.connected || !evidence || evidence.matchedRepositories.length === 0) {
    return null
  }

  return (
    <div className="flex flex-col gap-1 rounded-md border border-dashed border-border p-2.5">
      <span className="text-xs font-medium text-muted-foreground">Evidência do GitHub (sugestão)</span>
      <div className="flex flex-wrap gap-1.5">
        {evidence.matchedRepositories.map((repo) => (
          <a
            key={repo.id}
            href={repo.htmlUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground hover:text-foreground"
          >
            {repo.name} · {repo.recentCommitCount} commits
          </a>
        ))}
      </div>
    </div>
  )
}
