import { useEffect } from "react"
import { Phone, PhoneOff } from "lucide-react"
import { motion, useReducedMotion } from "motion/react"
import { useCall } from "@/context/CallContext"
import { useDelayedUnmount, useFrozen } from "@/hooks/useDelayedUnmount"
import { startRingtone } from "@/lib/callAudio"
import { cn } from "@/lib/utils"
import { UserAvatar } from "@/components/UserAvatar"
import { Button } from "@/components/ui/button"

export function IncomingCallDialog() {
  const { incoming: liveIncoming, acceptIncoming, declineIncoming } = useCall()
  const reducedMotion = useReducedMotion()
  const visible = !!liveIncoming
  const rendered = useDelayedUnmount(visible)
  const frozen = useFrozen(liveIncoming, !liveIncoming)
  const incoming = rendered ? frozen : null
  const callId = liveIncoming?.callId
  const caller = liveIncoming?.fromName
  const groupName = liveIncoming?.groupName

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

  const hidden = reducedMotion ? { opacity: 0 } : { opacity: 0, y: -16, scale: 0.96 }
  const shown = reducedMotion ? { opacity: 1 } : { opacity: 1, y: 0, scale: 1 }

  return (
    <motion.div
      role="alertdialog"
      aria-label={`Chamada de ${incoming.fromName}`}
      initial={hidden}
      animate={visible ? shown : hidden}
      transition={{ duration: reducedMotion ? 0.15 : 0.3, ease: [0.22, 1, 0.36, 1] }}
      className={cn(
        "fixed inset-x-3 top-[max(0.75rem,env(safe-area-inset-top))] z-[60] flex items-center gap-3 rounded-2xl border bg-card p-3 shadow-xl sm:left-auto sm:right-4 sm:w-96",
        !visible && "pointer-events-none",
      )}
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
    </motion.div>
  )
}
