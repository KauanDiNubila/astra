import { useEffect, useRef } from "react"
import { toast } from "sonner"
import { useCall } from "@/context/CallContext"

export function useScreenShareToast(active: boolean) {
  const call = useCall()
  const sharingKey = active
    ? call.participants
        .filter((p) => call.peerSharing(p.clientId))
        .map((p) => p.clientId)
        .join(",")
    : ""
  const sharingRef = useRef<string[]>([])

  useEffect(() => {
    const now = sharingKey ? sharingKey.split(",") : []
    const started = now.filter((id) => !sharingRef.current.includes(id))
    sharingRef.current = now
    if (!call.minimized) return
    for (const clientId of started) {
      const name = call.participants.find((p) => p.clientId === clientId)?.name || "Alguém"
      toast(`${name} está compartilhando a tela`, {
        action: {
          label: "Assistir",
          onClick: () => {
            call.setWatchingScreen(clientId, true)
            call.setMinimized(false)
          },
        },
      })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sharingKey])
}
