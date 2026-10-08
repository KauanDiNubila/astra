import { createContext, useContext, useEffect, useRef, useState } from "react"
import type { ReactNode } from "react"
import { toast } from "sonner"
import { api } from "@/lib/api"
import { startRingback, watchSpeaking } from "@/lib/callAudio"
import { loadDevicePrefs, listDevices, saveDevicePrefs } from "@/lib/callDevices"
import type { DeviceLists, DevicePrefs, ShareQuality } from "@/lib/callDevices"
import { PeerMesh } from "@/lib/callPeers"
import type { PeerMedia, SignalType } from "@/lib/callPeers"
import { useAuth } from "@/context/AuthContext"
import { useChat } from "@/context/ChatContext"
import type { CallEvent, CallParticipant } from "@/lib/types"

type Phase = "idle" | "starting" | "active"

export type CallInfo = {
  callId: string
  groupId: string | null
  title: string
}

export type IncomingCall = {
  callId: string
  groupId: string | null
  groupName: string | null
  fromUserId: string
  fromName: string
}

export type CallContextValue = {
  phase: Phase
  call: CallInfo | null
  incoming: IncomingCall | null
  ringing: boolean
  startedAt: number | null
  participants: CallParticipant[]
  selfClientId: string | null
  mediaVersion: number
  remoteMedia: (clientId: string) => PeerMedia
  connectionOf: (clientId: string) => RTCPeerConnectionState
  localStream: MediaStream | null
  localScreen: MediaStream | null
  speaking: Record<string, boolean>
  muted: boolean
  micMissing: boolean
  peerNoMic: (clientId: string) => boolean
  peerSharing: (clientId: string) => boolean
  watchingScreen: (clientId: string) => boolean
  setWatchingScreen: (clientId: string, watching: boolean) => void
  deafened: boolean
  cameraOn: boolean
  screenSharing: boolean
  screenShareSupported: boolean
  minimized: boolean
  setMinimized: (minimized: boolean) => void
  devices: DeviceLists
  devicePrefs: DevicePrefs
  refreshDevices: () => Promise<void>
  selectMic: (deviceId: string) => Promise<void>
  selectCamera: (deviceId: string) => Promise<void>
  selectSpeaker: (deviceId: string) => void
  setMicProcessing: (patch: Partial<Pick<DevicePrefs, "echoCancellation" | "noiseSuppression">>) => Promise<void>
  startDirectCall: (friendId: string, friendName: string) => Promise<void>
  startGroupCall: (groupId: string, groupName: string) => Promise<void>
  joinGroupCall: (callId: string, groupId: string, groupName: string) => Promise<void>
  acceptIncoming: () => Promise<void>
  declineIncoming: () => void
  leave: () => void
  toggleMute: () => void
  toggleDeafen: () => void
  toggleCamera: () => Promise<void>
  toggleScreenShare: () => Promise<void>
  screenPickerSources: AstraScreenSource[] | null
  confirmScreenShare: (sourceId: string | null, withAudio: boolean, quality: ShareQuality) => Promise<void>
  cancelScreenShare: () => void
}

const CallContext = createContext<CallContextValue | undefined>(undefined)

const FALLBACK_ICE: RTCIceServer[] = [{ urls: "stun:stun.l.google.com:19302" }]
const ICE_CACHE_MS = 45 * 60 * 1000
const START_TIMEOUT_MS = 10_000

const END_REASON_MESSAGES: Record<string, string> = {
  "no-answer": "Ninguém atendeu a chamada",
  declined: "A chamada foi recusada",
  left: "A chamada foi encerrada",
  ended: "A chamada já foi encerrada",
}

export function CallProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const { connected, publishStomp, subscribeCallEvents } = useChat()

  const [phase, setPhaseState] = useState<Phase>("idle")
  const [call, setCall] = useState<CallInfo | null>(null)
  const [incoming, setIncoming] = useState<IncomingCall | null>(null)
  const [ringing, setRinging] = useState(false)
  const [startedAt, setStartedAt] = useState<number | null>(null)
  const [participants, setParticipants] = useState<CallParticipant[]>([])
  const [mediaVersion, setMediaVersion] = useState(0)
  const [speaking, setSpeaking] = useState<Record<string, boolean>>({})
  const [muted, setMuted] = useState(false)
  const [micMissing, setMicMissing] = useState(false)
  const [deafened, setDeafened] = useState(false)
  const [cameraOn, setCameraOn] = useState(false)
  const [screenSharing, setScreenSharing] = useState(false)
  const [minimized, setMinimized] = useState(false)
  const [screenPickerSources, setScreenPickerSources] = useState<AstraScreenSource[] | null>(null)
  const [devices, setDevices] = useState<DeviceLists>({ mics: [], cameras: [], speakers: [] })
  const [devicePrefs, setDevicePrefs] = useState<DevicePrefs>(loadDevicePrefs)

  const phaseRef = useRef<Phase>("idle")
  const callRef = useRef<CallInfo | null>(null)
  const incomingRef = useRef<IncomingCall | null>(null)
  const participantsRef = useRef<CallParticipant[]>([])
  const clientIdRef = useRef<string | null>(null)
  const meshRef = useRef<PeerMesh | null>(null)
  const micRef = useRef<MediaStream | null>(null)
  const micMissingRef = useRef(false)
  const deafenedRef = useRef(false)
  const cameraTrackRef = useRef<MediaStreamTrack | null>(null)
  const screenRef = useRef<MediaStream | null>(null)
  const pendingRef = useRef<{ title: string; groupId: string | null } | null>(null)
  const startTimeoutRef = useRef<number | null>(null)
  const mutedBeforeDeafenRef = useRef(false)
  const iceCacheRef = useRef<{ servers: RTCIceServer[]; at: number } | null>(null)
  const watchersRef = useRef(new Map<string, { streamId: string; stop: () => void }>())
  const selfWatcherRef = useRef<(() => void) | null>(null)
  const devicePrefsRef = useRef(devicePrefs)
  const publishRef = useRef(publishStomp)
  const handleEventRef = useRef<(event: CallEvent) => void>(() => {})

  useEffect(() => {
    publishRef.current = publishStomp
  })

  function updateDevicePrefs(patch: Partial<DevicePrefs>) {
    const next = { ...devicePrefsRef.current, ...patch }
    devicePrefsRef.current = next
    setDevicePrefs(next)
    saveDevicePrefs(next)
  }

  async function refreshDevices() {
    try {
      setDevices(await listDevices())
    } catch {
      setDevices({ mics: [], cameras: [], speakers: [] })
    }
  }

  function setPhase(next: Phase) {
    phaseRef.current = next
    setPhaseState(next)
  }

  function updateParticipants(next: CallParticipant[]) {
    participantsRef.current = next
    setParticipants(next)
  }

  function bump() {
    setMediaVersion((v) => v + 1)
  }

  function clearStartTimeout() {
    if (startTimeoutRef.current !== null) {
      window.clearTimeout(startTimeoutRef.current)
      startTimeoutRef.current = null
    }
  }

  function stopWatchers() {
    selfWatcherRef.current?.()
    selfWatcherRef.current = null
    watchersRef.current.forEach((watcher) => watcher.stop())
    watchersRef.current.clear()
    setSpeaking({})
  }

  function teardown() {
    clearStartTimeout()
    stopWatchers()
    meshRef.current?.close()
    meshRef.current = null
    micRef.current?.getTracks().forEach((t) => t.stop())
    micRef.current = null
    markMicMissing(false)
    cameraTrackRef.current?.stop()
    cameraTrackRef.current = null
    screenRef.current?.getTracks().forEach((t) => t.stop())
    screenRef.current = null
    clientIdRef.current = null
    pendingRef.current = null
    callRef.current = null
    updateParticipants([])
    setCall(null)
    setRinging(false)
    setStartedAt(null)
    setMinimized(false)
    setPhase("idle")
  }

  async function loadIceServers() {
    const cached = iceCacheRef.current
    if (cached && Date.now() - cached.at < ICE_CACHE_MS) return cached.servers
    try {
      const { data } = await api.get<{ iceServers: RTCIceServer[] }>("/call/ice-servers")
      iceCacheRef.current = { servers: data.iceServers, at: Date.now() }
      return data.iceServers
    } catch {
      return FALLBACK_ICE
    }
  }

  async function openMic(deviceId: string) {
    const { echoCancellation, noiseSuppression } = devicePrefsRef.current
    const base = { echoCancellation, noiseSuppression, autoGainControl: true }
    if (deviceId) {
      try {
        return await navigator.mediaDevices.getUserMedia({ audio: { ...base, deviceId: { exact: deviceId } } })
      } catch {
        updateDevicePrefs({ micId: "" })
      }
    }
    return navigator.mediaDevices.getUserMedia({ audio: base })
  }

  async function openCamera(deviceId: string) {
    const base = { width: { ideal: 1920 }, height: { ideal: 1080 }, aspectRatio: { ideal: 16 / 9 }, frameRate: { ideal: 30 } }
    if (deviceId) {
      try {
        return await navigator.mediaDevices.getUserMedia({ video: { ...base, deviceId: { exact: deviceId } } })
      } catch {
        updateDevicePrefs({ cameraId: "" })
      }
    }
    return navigator.mediaDevices.getUserMedia({ video: base })
  }

  function watchSelf(mic: MediaStream) {
    selfWatcherRef.current?.()
    selfWatcherRef.current = watchSpeaking(mic, (isSpeaking) =>
      setSpeaking((prev) => ({ ...prev, self: isSpeaking })),
    )
  }

  async function prepare() {
    if (!navigator.mediaDevices?.getUserMedia || typeof RTCPeerConnection === "undefined") {
      toast.error("Seu navegador não suporta chamadas de voz.")
      return false
    }
    let mic: MediaStream | null = null
    try {
      mic = await openMic(devicePrefsRef.current.micId)
    } catch {
      mic = null
    }
    const servers = await loadIceServers()

    const clientId = crypto.randomUUID()
    clientIdRef.current = clientId
    micRef.current = mic ?? new MediaStream()
    const mesh = new PeerMesh(clientId, servers, {
      sendSignal: (toClient, type: SignalType, data) => {
        const callId = callRef.current?.callId
        if (callId) publishRef.current("/app/call.signal", { callId, toClient, type, data })
      },
      onChange: bump,
    })
    meshRef.current = mesh
    const micTrack = mic?.getAudioTracks()[0]
    if (mic && micTrack) {
      micTrack.onended = handleMicLost
      mesh.publishTrack(micTrack, mic)
      watchSelf(mic)
      markMicMissing(false)
    } else {
      markMicMissing(true)
    }
    setMuted(false)
    setDeafened(false)
    setCameraOn(false)
    setScreenSharing(false)
    return true
  }

  function requestNotificationPermission() {
    if (typeof Notification !== "undefined" && Notification.permission === "default") {
      Notification.requestPermission().catch(() => {})
    }
  }

  async function start(target: { targetUserId?: string; groupId?: string }, title: string) {
    if (phaseRef.current !== "idle" || incomingRef.current) {
      toast("Você já está em uma call.")
      return
    }
    setPhase("starting")
    requestNotificationPermission()
    if (!(await prepare())) {
      teardown()
      return
    }
    pendingRef.current = { title, groupId: target.groupId ?? null }
    const sent = publishRef.current("/app/call.start", { ...target, clientId: clientIdRef.current })
    if (!sent) {
      toast.error("Sem conexão com o servidor. Tente de novo em instantes.")
      teardown()
      return
    }
    startTimeoutRef.current = window.setTimeout(() => {
      if (phaseRef.current === "starting") {
        toast.error("Não foi possível iniciar a chamada.")
        teardown()
      }
    }, START_TIMEOUT_MS)
  }

  async function join(callId: string, groupId: string | null, title: string) {
    if (phaseRef.current !== "idle") {
      toast("Você já está em uma call.")
      return
    }
    setPhase("starting")
    requestNotificationPermission()
    if (!(await prepare())) {
      teardown()
      return
    }
    const info = { callId, groupId, title }
    callRef.current = info
    setCall(info)
    const sent = publishRef.current("/app/call.join", { callId, clientId: clientIdRef.current })
    if (!sent) {
      toast.error("Sem conexão com o servidor. Tente de novo em instantes.")
      teardown()
      return
    }
    startTimeoutRef.current = window.setTimeout(() => {
      if (phaseRef.current === "starting") {
        toast.error("Não foi possível entrar na chamada.")
        teardown()
      }
    }, START_TIMEOUT_MS)
  }

  async function startDirectCall(friendId: string, friendName: string) {
    await start({ targetUserId: friendId }, friendName)
  }

  async function startGroupCall(groupId: string, groupName: string) {
    await start({ groupId }, groupName)
  }

  async function joinGroupCall(callId: string, groupId: string, groupName: string) {
    await join(callId, groupId, groupName)
  }

  async function acceptIncoming() {
    const current = incomingRef.current
    if (!current) return
    incomingRef.current = null
    setIncoming(null)
    await join(current.callId, current.groupId, current.groupName ?? current.fromName)
  }

  function declineIncoming() {
    const current = incomingRef.current
    if (!current) return
    incomingRef.current = null
    setIncoming(null)
    publishRef.current("/app/call.decline", { callId: current.callId })
  }

  function leave() {
    const info = callRef.current
    if (info) publishRef.current("/app/call.leave", { callId: info.callId, clientId: clientIdRef.current })
    teardown()
  }

  function markMicMissing(missing: boolean) {
    micMissingRef.current = missing
    setMicMissing(missing)
    meshRef.current?.setNoMic(missing)
  }

  async function acquireMic() {
    const mic = micRef.current
    const mesh = meshRef.current
    if (!mic || !mesh || !micMissingRef.current) return false
    try {
      const stream = await openMic(devicePrefsRef.current.micId)
      const track = stream.getAudioTracks()[0]
      if (micRef.current !== mic || !micMissingRef.current) {
        track.stop()
        return false
      }
      track.enabled = !deafenedRef.current
      track.onended = handleMicLost
      mic.addTrack(track)
      mesh.publishTrack(track, mic)
      markMicMissing(false)
      setMuted(deafenedRef.current)
      watchSelf(mic)
      return true
    } catch {
      return false
    }
  }

  function toggleMute() {
    if (micMissingRef.current) {
      void acquireMic().then((ok) => {
        if (!ok) toast.error("Não encontrei um microfone para usar.")
      })
      return
    }
    const track = micRef.current?.getAudioTracks()[0]
    if (!track) return
    if (deafened) {
      mutedBeforeDeafenRef.current = false
      setDeafened(false)
      track.enabled = true
      setMuted(false)
      return
    }
    track.enabled = !track.enabled
    setMuted(!track.enabled)
  }

  function toggleDeafen() {
    const track = micRef.current?.getAudioTracks()[0]
    if (!deafened) {
      mutedBeforeDeafenRef.current = muted
      setDeafened(true)
      if (track) track.enabled = false
      setMuted(true)
      return
    }
    setDeafened(false)
    const restoreMuted = mutedBeforeDeafenRef.current
    if (track) track.enabled = !restoreMuted
    setMuted(restoreMuted)
  }

  function handleMicLost() {
    if (phaseRef.current !== "active" && phaseRef.current !== "starting") return
    toast("Microfone desconectado. Usando o padrão do sistema.")
    updateDevicePrefs({ micId: "" })
    void selectMic("")
  }

  async function selectMic(deviceId: string) {
    updateDevicePrefs({ micId: deviceId })
    await swapMic()
  }

  async function setMicProcessing(patch: Partial<Pick<DevicePrefs, "echoCancellation" | "noiseSuppression">>) {
    updateDevicePrefs(patch)
    await swapMic()
  }

  async function swapMic() {
    const deviceId = devicePrefsRef.current.micId
    const mic = micRef.current
    const mesh = meshRef.current
    const previous = mic?.getAudioTracks()[0]
    if (micMissingRef.current) {
      await acquireMic()
      return
    }
    if (!mic || !mesh || !previous) return
    const wasEnabled = previous.enabled
    previous.onended = null
    previous.stop()
    try {
      const stream = await openMic(deviceId)
      const next = stream.getAudioTracks()[0]
      if (micRef.current !== mic) {
        next.stop()
        return
      }
      next.enabled = wasEnabled
      next.onended = handleMicLost
      mic.addTrack(next)
      await mesh.replaceTrack(previous, next, mic)
      mic.removeTrack(previous)
      watchSelf(mic)
    } catch {
      if (micRef.current !== mic) return
      mic.removeTrack(previous)
      mesh.unpublishTrack(previous)
      selfWatcherRef.current?.()
      selfWatcherRef.current = null
      setSpeaking((prev) => ({ ...prev, self: false }))
      markMicMissing(true)
    }
  }

  async function selectCamera(deviceId: string) {
    updateDevicePrefs({ cameraId: deviceId })
    const mic = micRef.current
    const mesh = meshRef.current
    const previous = cameraTrackRef.current
    if (!mic || !mesh || !previous) return
    try {
      const stream = await openCamera(deviceId)
      const next = stream.getVideoTracks()[0]
      if (cameraTrackRef.current !== previous) {
        next.stop()
        return
      }
      next.onended = stopCamera
      previous.onended = null
      mic.addTrack(next)
      await mesh.replaceTrack(previous, next, mic)
      mic.removeTrack(previous)
      previous.stop()
      cameraTrackRef.current = next
      bump()
    } catch {
      toast.error("Não consegui trocar a câmera.")
    }
  }

  function selectSpeaker(deviceId: string) {
    updateDevicePrefs({ speakerId: deviceId })
  }

  function stopCamera() {
    const track = cameraTrackRef.current
    if (!track) return
    cameraTrackRef.current = null
    track.onended = null
    track.stop()
    micRef.current?.removeTrack(track)
    meshRef.current?.unpublishTrack(track)
    setCameraOn(false)
    bump()
  }

  async function toggleCamera() {
    if (cameraTrackRef.current) {
      stopCamera()
      return
    }
    const mic = micRef.current
    const mesh = meshRef.current
    if (!mic || !mesh) return
    try {
      const stream = await openCamera(devicePrefsRef.current.cameraId)
      const track = stream.getVideoTracks()[0]
      if (!micRef.current || !meshRef.current) {
        track.stop()
        return
      }
      track.onended = stopCamera
      cameraTrackRef.current = track
      mic.addTrack(track)
      mesh.publishTrack(track, mic)
      setCameraOn(true)
      bump()
      void refreshDevices()
    } catch {
      toast.error("Não consegui acessar a câmera. Verifique a permissão do navegador.")
    }
  }

  function stopScreen() {
    const screen = screenRef.current
    if (!screen) return
    screenRef.current = null
    for (const track of screen.getTracks()) {
      track.onended = null
      track.stop()
      meshRef.current?.unpublishTrack(track)
    }
    setScreenSharing(false)
    bump()
  }

  async function toggleScreenShare() {
    if (screenRef.current) {
      stopScreen()
      return
    }
    const desktop = window.astraDesktop
    if (desktop) {
      try {
        setScreenPickerSources(await desktop.listScreenSources())
      } catch {
        toast.error("Não consegui listar as telas para compartilhar.")
      }
      return
    }
    setScreenPickerSources([])
  }

  function cancelScreenShare() {
    setScreenPickerSources(null)
  }

  async function confirmScreenShare(sourceId: string | null, withAudio: boolean, quality: ShareQuality) {
    setScreenPickerSources(null)
    updateDevicePrefs({ shareQuality: quality })
    const desktop = window.astraDesktop
    if (desktop) {
      if (!sourceId || !(await desktop.selectScreenSource(sourceId, withAudio))) return
    }
    await startScreenShare(withAudio, quality)
  }

  async function startScreenShare(withAudio: boolean, quality: ShareQuality) {
    const mesh = meshRef.current
    if (!mesh || !navigator.mediaDevices?.getDisplayMedia) return
    const size =
      quality === "motion"
        ? { width: { ideal: 1920 }, height: { ideal: 1080 } }
        : { width: { ideal: 3840 }, height: { ideal: 2160 } }
    try {
      const screen = await navigator.mediaDevices.getDisplayMedia({
        video: { ...size, frameRate: { ideal: 60, max: 60 } },
        audio: withAudio
          ? {
              suppressLocalAudioPlayback: true,
              restrictOwnAudio: true,
              echoCancellation: false,
              noiseSuppression: false,
              autoGainControl: false,
            }
          : false,
        systemAudio: withAudio ? "include" : "exclude",
      } as DisplayMediaStreamOptions)
      if (!meshRef.current) {
        screen.getTracks().forEach((t) => t.stop())
        return
      }
      screenRef.current = screen
      const video = screen.getVideoTracks()[0]
      video.contentHint = quality
      video.onended = stopScreen
      mesh.publishTrack(video, screen, true)
      screen.getAudioTracks().forEach((track) => mesh.publishTrack(track, screen, true))
      const wholeScreenAudio =
        screen.getAudioTracks().length > 0 &&
        (video.getSettings() as MediaTrackSettings & { displaySurface?: string }).displaySurface === "monitor"
      const ownAudioExcluded = !!(
        navigator.mediaDevices.getSupportedConstraints() as MediaTrackSupportedConstraints & {
          restrictOwnAudio?: boolean
        }
      ).restrictOwnAudio
      if (wholeScreenAudio && !ownAudioExcluded) {
        toast("Você está compartilhando o som do computador inteiro, inclusive a voz da call. Se alguém ouvir a própria voz, compartilhe uma aba e marque 'Compartilhar áudio da aba'.", { duration: 10000 })
      }
      setScreenSharing(true)
      bump()
    } catch (error) {
      if (error instanceof DOMException && error.name === "NotAllowedError") return
      toast.error("Não consegui compartilhar a tela.")
    }
  }

  function handleEvent(event: CallEvent) {
    const mesh = meshRef.current

    switch (event.type) {
      case "started": {
        const pending = pendingRef.current
        if (phaseRef.current !== "starting" || !pending || !event.callId) return
        clearStartTimeout()
        const info = { callId: event.callId, groupId: pending.groupId, title: pending.title }
        callRef.current = info
        setCall(info)
        setRinging(true)
        setStartedAt(Date.now())
        setPhase("active")
        return
      }
      case "incoming": {
        if (phaseRef.current !== "idle" || incomingRef.current || !event.callId || !event.userId) return
        const info: IncomingCall = {
          callId: event.callId,
          groupId: event.groupId ?? null,
          groupName: event.groupName ?? null,
          fromUserId: event.userId,
          fromName: event.userName ?? "",
        }
        incomingRef.current = info
        setIncoming(info)
        return
      }
      case "state": {
        if (event.callId !== callRef.current?.callId || !mesh || !clientIdRef.current) return
        clearStartTimeout()
        const others = (event.participants ?? []).filter((p) => p.clientId !== clientIdRef.current)
        for (const peer of participantsRef.current) {
          const state = mesh.connectionState(peer.clientId)
          const gone = !others.some((o) => o.clientId === peer.clientId)
          if (gone || state === "failed" || state === "closed") mesh.removePeer(peer.clientId)
        }
        updateParticipants(others)
        others.forEach((peer) => mesh.addPeer(peer.clientId))
        setRinging(others.length === 0)
        setStartedAt((prev) => prev ?? Date.now())
        setPhase("active")
        return
      }
      case "joined": {
        if (event.callId !== callRef.current?.callId || !event.clientId || !event.userId || !mesh) return
        const known = participantsRef.current.filter((p) => p.clientId !== event.clientId)
        updateParticipants([...known, { userId: event.userId, clientId: event.clientId, name: event.userName ?? "" }])
        mesh.addPeer(event.clientId)
        setRinging(false)
        return
      }
      case "left": {
        if (event.callId !== callRef.current?.callId || !event.clientId) return
        mesh?.removePeer(event.clientId)
        updateParticipants(participantsRef.current.filter((p) => p.clientId !== event.clientId))
        return
      }
      case "declined": {
        if (event.callId !== callRef.current?.callId) return
        if (!callRef.current?.groupId) toast("A chamada foi recusada.")
        return
      }
      case "dismissed": {
        if (incomingRef.current?.callId === event.callId) {
          incomingRef.current = null
          setIncoming(null)
        }
        return
      }
      case "ended": {
        if (incomingRef.current?.callId === event.callId) {
          incomingRef.current = null
          setIncoming(null)
        }
        if (callRef.current?.callId === event.callId) {
          const message = END_REASON_MESSAGES[event.reason ?? "ended"]
          if (message && event.reason !== "declined") toast(message)
          teardown()
        }
        return
      }
      case "signal": {
        if (event.callId !== callRef.current?.callId || !event.clientId || !event.signalType || !event.data || !mesh) {
          return
        }
        const fromClient = event.clientId
        if (event.userId && !participantsRef.current.some((p) => p.clientId === fromClient)) {
          updateParticipants([...participantsRef.current, { userId: event.userId, clientId: fromClient, name: "" }])
        }
        mesh.handleSignal(fromClient, event.signalType, event.data).catch(() => {})
        return
      }
      case "error": {
        toast.error(event.message ?? "Erro na chamada")
        if (phaseRef.current === "starting") teardown()
        return
      }
    }
  }

  useEffect(() => {
    handleEventRef.current = handleEvent
  })

  useEffect(() => {
    if (!user) return
    return subscribeCallEvents((event) => handleEventRef.current(event))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  useEffect(() => {
    if (!connected) return
    const info = callRef.current
    if (phaseRef.current === "active" && info && clientIdRef.current) {
      publishRef.current("/app/call.join", { callId: info.callId, clientId: clientIdRef.current })
    }
  }, [connected])

  useEffect(() => {
    if (!user) {
      if (phaseRef.current !== "idle") teardown()
      incomingRef.current = null
      setIncoming(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user])

  useEffect(() => {
    return () => {
      if (phaseRef.current !== "idle") teardown()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    const mesh = meshRef.current
    const watchers = watchersRef.current
    const live = new Set<string>()
    for (const peer of participants) {
      const stream = mesh?.media(peer.clientId).camera ?? null
      if (!stream || stream.getAudioTracks().length === 0) continue
      live.add(peer.clientId)
      if (watchers.get(peer.clientId)?.streamId === stream.id) continue
      watchers.get(peer.clientId)?.stop()
      const stop = watchSpeaking(stream, (isSpeaking) =>
        setSpeaking((prev) => ({ ...prev, [peer.clientId]: isSpeaking })),
      )
      watchers.set(peer.clientId, { streamId: stream.id, stop })
    }
    for (const [clientId, watcher] of watchers) {
      if (!live.has(clientId)) {
        watcher.stop()
        watchers.delete(clientId)
      }
    }
  }, [participants, mediaVersion])

  useEffect(() => {
    if (phase !== "active" || !ringing) return
    return startRingback()
  }, [phase, ringing])

  useEffect(() => {
    if (phase !== "active" || !navigator.mediaDevices) return
    void refreshDevices()
    const onChange = async () => {
      await refreshDevices()
      if (!micMissingRef.current) return
      const list = await navigator.mediaDevices.enumerateDevices().catch(() => [])
      if (list.some((device) => device.kind === "audioinput")) await acquireMic()
    }
    navigator.mediaDevices.addEventListener("devicechange", onChange)
    return () => navigator.mediaDevices.removeEventListener("devicechange", onChange)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [phase])

  useEffect(() => {
    deafenedRef.current = deafened
  }, [deafened])

  useEffect(() => {
    window.astraDesktop?.setInCall(phase !== "idle")
    return () => window.astraDesktop?.setInCall(false)
  }, [phase])

  const screenShareSupported =
    typeof navigator !== "undefined" && typeof navigator.mediaDevices?.getDisplayMedia === "function"

  return (
    <CallContext.Provider
      value={{
        phase,
        call,
        incoming,
        ringing,
        startedAt,
        participants,
        selfClientId: clientIdRef.current,
        mediaVersion,
        remoteMedia: (clientId) => meshRef.current?.media(clientId) ?? { camera: null, screen: null },
        connectionOf: (clientId) => meshRef.current?.connectionState(clientId) ?? "new",
        localStream: micRef.current,
        localScreen: screenRef.current,
        speaking,
        muted,
        micMissing,
        peerNoMic: (clientId) => meshRef.current?.peerNoMic(clientId) ?? false,
        peerSharing: (clientId) => meshRef.current?.peerSharing(clientId) ?? false,
        watchingScreen: (clientId) => meshRef.current?.watching(clientId) ?? false,
        setWatchingScreen: (clientId, watching) => meshRef.current?.setWatching(clientId, watching),
        deafened,
        cameraOn,
        screenSharing,
        screenShareSupported,
        minimized,
        setMinimized,
        devices,
        devicePrefs,
        refreshDevices,
        selectMic,
        selectCamera,
        selectSpeaker,
        setMicProcessing,
        startDirectCall,
        startGroupCall,
        joinGroupCall,
        acceptIncoming,
        declineIncoming,
        leave,
        toggleMute,
        toggleDeafen,
        toggleCamera,
        toggleScreenShare,
        screenPickerSources,
        confirmScreenShare,
        cancelScreenShare,
      }}
    >
      {children}
    </CallContext.Provider>
  )
}

export function useCall() {
  const ctx = useContext(CallContext)
  if (!ctx) {
    throw new Error("useCall precisa estar dentro de um CallProvider")
  }
  return ctx
}
