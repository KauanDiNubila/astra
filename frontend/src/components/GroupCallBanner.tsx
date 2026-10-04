import { useEffect, useState } from "react"
import { Phone } from "lucide-react"
import { api } from "@/lib/api"
import { useCall } from "@/context/CallContext"
import type { ActiveCall } from "@/lib/types"
import { Button } from "@/components/ui/button"

const POLL_MS = 15_000

export function GroupCallBanner({ groupId, groupName }: { groupId: string; groupName: string }) {
  const { phase, joinGroupCall } = useCall()
  const [active, setActive] = useState<ActiveCall | null>(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const { data } = await api.get<ActiveCall[]>("/call/active")
        if (!cancelled) setActive(data.find((c) => c.groupId === groupId) ?? null)
      } catch {
        if (!cancelled) setActive(null)
      }
    }

    load()
    const interval = window.setInterval(load, POLL_MS)
    return () => {
      cancelled = true
      window.clearInterval(interval)
    }
  }, [groupId, phase])

  if (!active || phase !== "idle") return null

  return (
    <div className="flex items-center gap-3 border-b bg-emerald-500/10 px-4 py-2 text-sm">
      <span className="size-2 shrink-0 animate-pulse rounded-full bg-emerald-500" />
      <span className="min-w-0 flex-1 truncate">
        Call em andamento · {active.participantCount} {active.participantCount === 1 ? "pessoa" : "pessoas"}
      </span>
      <Button
        type="button"
        size="sm"
        className="gap-1.5 bg-emerald-600 text-white hover:bg-emerald-700 max-sm:h-10"
        onClick={() => void joinGroupCall(active.callId, groupId, groupName)}
      >
        <Phone className="size-3.5" />
        Entrar
      </Button>
    </div>
  )
}
