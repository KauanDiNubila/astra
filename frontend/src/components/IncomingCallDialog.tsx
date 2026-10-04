import { useEffect } from "react"
import { Phone, PhoneOff } from "lucide-react"
import { useCall } from "@/context/CallContext"
import { startRingtone } from "@/lib/callAudio"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"

export function IncomingCallDialog() {
  const { incoming, acceptIncoming, declineIncoming } = useCall()
  const callId = incoming?.callId
  const caller = incoming?.fromName
  const groupName = incoming?.groupName

  useEffect(() => {
    if (!callId) return
    const stopRing = startRingtone()
    const originalTitle = document.title
    const callerLabel = groupName ? `${caller} · ${groupName}` : caller
    let flash = false
    const interval = window.setInterval(() => {
      flash = !flash
      document.title = flash ? `📞 ${callerLabel} está ligando…` : originalTitle
    }, 1000)

    let notification: Notification | null = null
    if (typeof Notification !== "undefined" && Notification.permission === "granted" && document.hidden) {
      notification = new Notification(`Chamada de ${caller}`, {
        body: groupName ? `Grupo ${groupName}` : "Abra o Astra para atender",
        tag: callId,
      })
    }

    return () => {
      stopRing()
      window.clearInterval(interval)
      document.title = originalTitle
      notification?.close()
    }
  }, [callId, caller, groupName])

  if (!incoming) return null

  return (
    <div
      role="alertdialog"
      aria-label={`Chamada de ${incoming.fromName}`}
      className="fixed inset-x-3 top-[max(0.75rem,env(safe-area-inset-top))] z-[60] flex items-center gap-3 rounded-2xl border bg-card p-3 shadow-xl sm:left-auto sm:right-4 sm:w-96"
    >
      <UserAvatar userId={incoming.fromUserId} name={incoming.fromName} size="lg" />
      <div className="min-w-0 flex-1">
        <p className="truncate font-medium">{incoming.fromName}</p>
        <p className="truncate text-xs text-muted-foreground">
          {incoming.groupName ? `Chamando no grupo ${incoming.groupName}` : "Chamada de voz"}
        </p>
      </div>
      <Button
        type="button"
        size="icon"
        variant="destructive"
        className="size-11 rounded-full"
        onClick={declineIncoming}
        title="Recusar"
        aria-label="Recusar chamada"
      >
        <PhoneOff className="size-5" />
      </Button>
      <Button
        type="button"
        size="icon"
        className="size-11 rounded-full bg-emerald-600 text-white hover:bg-emerald-700"
        onClick={() => void acceptIncoming()}
        title="Atender"
        aria-label="Atender chamada"
      >
        <Phone className="size-5" />
      </Button>
    </div>
  )
}
