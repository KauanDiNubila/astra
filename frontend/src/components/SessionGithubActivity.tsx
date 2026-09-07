import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { formatRelativeTime } from "@/lib/format"
import type { SessionGitHubActivity as SessionGitHubActivityData } from "@/lib/types"
import { Button } from "@/components/ui/button"

export function SessionGithubActivity({
  sessionId,
  linkedRepositoryId,
  onLinked,
}: {
  sessionId: string
  linkedRepositoryId: string | null
  onLinked: (repositoryId: string | null) => void
}) {
  const [data, setData] = useState<SessionGitHubActivityData | null>(null)
  const [loading, setLoading] = useState(true)
  const [linking, setLinking] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    api
      .get<SessionGitHubActivityData>(`/sessions/${sessionId}/github-activity`, { signal: controller.signal })
      .then((res) => setData(res.data))
      .catch(() => {})
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [sessionId])

  async function linkSuggested() {
    if (!data?.suggestedRepositoryId) return
    setLinking(true)
    try {
      await api.put(`/sessions/${sessionId}/github-repository`, { repositoryId: data.suggestedRepositoryId })
      onLinked(data.suggestedRepositoryId)
    } finally {
      setLinking(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-muted-foreground">Buscando atividade do GitHub...</p>
  }

  if (!data?.connected) {
    return null
  }

  if (data.commits.length === 0) {
    return <p className="text-sm text-muted-foreground">Nenhum commit detectado nessa janela de tempo.</p>
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <span className="text-sm font-medium">{data.commits.length} commit(s) detectado(s)</span>
        {data.suggestedRepositoryId && !linkedRepositoryId && (
          <Button type="button" size="sm" variant="outline" disabled={linking} onClick={linkSuggested}>
            {linking ? "Vinculando..." : `Vincular a ${data.suggestedRepositoryName}`}
          </Button>
        )}
      </div>
      <ul className="flex flex-col gap-1">
        {data.commits.slice(0, 10).map((c) => (
          <li key={c.sha} className="text-sm text-muted-foreground">
            <span className="font-mono text-xs">{c.sha.slice(0, 7)}</span> {c.message.split("\n")[0]}{" "}
            <span className="text-xs">— {formatRelativeTime(c.committedAt)}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
