import { useEffect, useRef } from "react"
import { useCall } from "@/context/CallContext"
import { applySpeaker } from "@/lib/callDevices"

function MediaAudio({ stream, deafened, speakerId }: { stream: MediaStream; deafened: boolean; speakerId: string }) {
  const ref = useRef<HTMLAudioElement>(null)
  useEffect(() => {
    if (ref.current && ref.current.srcObject !== stream) ref.current.srcObject = stream
  }, [stream])
  useEffect(() => {
    if (ref.current) void applySpeaker(ref.current, speakerId)
  }, [speakerId])
  useEffect(() => {
    if (ref.current) ref.current.muted = deafened
  }, [deafened])
  return <audio ref={ref} autoPlay />
}

export function CallAudio() {
  const { participants, remoteMedia, deafened, devicePrefs } = useCall()
  return (
    <>
      {participants.map((peer) => {
        const media = remoteMedia(peer.clientId)
        return (
          <span key={peer.clientId}>
            {media.camera && (
              <MediaAudio
                key={media.camera.id}
                stream={media.camera}
                deafened={deafened}
                speakerId={devicePrefs.speakerId}
              />
            )}
            {media.screen && (
              <MediaAudio
                key={media.screen.id}
                stream={media.screen}
                deafened={deafened}
                speakerId={devicePrefs.speakerId}
              />
            )}
          </span>
        )
      })}
    </>
  )
}
