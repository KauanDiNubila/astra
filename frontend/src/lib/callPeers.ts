export type SignalType = "offer" | "answer" | "ice" | "meta"

export type PeerMedia = {
  camera: MediaStream | null
  screen: MediaStream | null
}

type MeshHandlers = {
  sendSignal: (toClient: string, type: SignalType, data: string) => void
  onChange: () => void
}

const SCREEN_BITRATE_BY_PEERS = [20_000_000, 10_000_000, 6_000_000]
const CAMERA_BITRATE_BY_PEERS = [8_000_000, 4_000_000, 2_500_000]
const MIC_MAX_BITRATE = 96_000
const SCREEN_AUDIO_MAX_BITRATE = 192_000
const SCREEN_MAX_FRAMERATE = 60
const CAMERA_MAX_FRAMERATE = 30

function bitrateFor(table: number[], peers: number) {
  return table[Math.min(Math.max(peers, 1), table.length) - 1]
}

class PeerLink {
  readonly clientId: string
  private readonly mesh: PeerMesh
  readonly pc: RTCPeerConnection
  readonly polite: boolean
  readonly streams = new Map<string, MediaStream>()
  readonly senders = new Map<string, RTCRtpSender>()
  screenStreamId: string | null = null
  noMic = false
  watching = false
  wantsMyScreen = false

  private queue: Promise<void> = Promise.resolve()
  private pendingCandidates: RTCIceCandidateInit[] = []
  private makingOffer = false
  private ignoreOffer = false
  private settingRemoteAnswer = false
  private closed = false

  constructor(clientId: string, selfClientId: string, iceServers: RTCIceServer[], mesh: PeerMesh) {
    this.clientId = clientId
    this.mesh = mesh
    this.polite = selfClientId > clientId
    this.pc = new RTCPeerConnection({ iceServers })

    this.pc.onnegotiationneeded = async () => {
      if (this.polite && !this.pc.remoteDescription) return
      try {
        this.makingOffer = true
        await this.pc.setLocalDescription()
        if (this.pc.localDescription) {
          this.mesh.signal(this.clientId, "offer", JSON.stringify(this.pc.localDescription))
        }
      } catch {
        this.makingOffer = false
      } finally {
        this.makingOffer = false
      }
    }

    this.pc.onicecandidate = ({ candidate }) => {
      if (candidate) this.mesh.signal(this.clientId, "ice", JSON.stringify(candidate.toJSON()))
    }

    this.pc.oniceconnectionstatechange = () => {
      if (this.pc.iceConnectionState === "failed") this.pc.restartIce()
      this.mesh.changed()
    }

    this.pc.onconnectionstatechange = () => this.mesh.changed()

    this.pc.onsignalingstatechange = () => {
      if (this.pc.signalingState === "stable") this.applyEncodings()
    }

    this.pc.ontrack = ({ track, streams }) => {
      const stream = streams[0] ?? new MediaStream([track])
      this.streams.set(stream.id, stream)
      track.onmute = () => this.mesh.changed()
      track.onunmute = () => this.mesh.changed()
      track.onended = () => this.mesh.changed()
      stream.onremovetrack = () => {
        if (stream.getTracks().length === 0) this.streams.delete(stream.id)
        this.mesh.changed()
      }
      this.mesh.changed()
    }
  }

  handleDescriptionOrCandidate(type: SignalType, raw: string) {
    const run = this.queue.then(() => this.process(type, raw))
    this.queue = run.catch(() => {})
    return run
  }

  private async process(type: SignalType, raw: string) {
    if (this.closed) return
    const payload = JSON.parse(raw)

    if (type === "ice") {
      if (!this.pc.remoteDescription) {
        this.pendingCandidates.push(payload as RTCIceCandidateInit)
        return
      }
      await this.addCandidate(payload as RTCIceCandidateInit)
      return
    }

    const description = payload as RTCSessionDescriptionInit
    const readyForOffer =
      !this.makingOffer && (this.pc.signalingState === "stable" || this.settingRemoteAnswer)
    const offerCollision = description.type === "offer" && !readyForOffer
    this.ignoreOffer = !this.polite && offerCollision
    if (this.ignoreOffer) return

    this.settingRemoteAnswer = description.type === "answer"
    await this.pc.setRemoteDescription(description)
    this.settingRemoteAnswer = false

    const pending = this.pendingCandidates
    this.pendingCandidates = []
    for (const candidate of pending) await this.addCandidate(candidate)

    if (description.type === "offer") {
      await this.pc.setLocalDescription()
      if (this.pc.localDescription) {
        this.mesh.signal(this.clientId, "answer", JSON.stringify(this.pc.localDescription))
      }
    }
  }

  private async addCandidate(candidate: RTCIceCandidateInit) {
    try {
      await this.pc.addIceCandidate(candidate)
    } catch (error) {
      if (!this.ignoreOffer) throw error
    }
  }

  expectAudio() {
    if (this.closed) return
    const hasAudio = this.pc.getTransceivers().some((t) => t.receiver.track.kind === "audio")
    if (!hasAudio) this.pc.addTransceiver("audio", { direction: "recvonly" })
  }

  addLocalTrack(track: MediaStreamTrack, stream: MediaStream) {
    if (this.closed || this.senders.has(track.id)) return
    this.senders.set(track.id, this.pc.addTrack(track, stream))
  }

  async replaceLocalTrack(oldId: string, next: MediaStreamTrack) {
    const sender = this.senders.get(oldId)
    if (!sender || this.closed) return
    await sender.replaceTrack(next)
    this.senders.delete(oldId)
    this.senders.set(next.id, sender)
  }

  removeLocalTrack(trackId: string) {
    const sender = this.senders.get(trackId)
    if (!sender || this.closed) return
    this.senders.delete(trackId)
    try {
      this.pc.removeTrack(sender)
    } catch {
      return
    }
  }

  applyEncodings() {
    if (this.closed) return
    const peers = this.mesh.peerCount()
    for (const [trackId, sender] of this.senders) {
      const kind = sender.track?.kind
      if (!kind) continue
      const params = sender.getParameters()
      if (!params.encodings || params.encodings.length === 0) continue
      const isScreen = this.mesh.isScreenTrack(trackId)
      let max: number
      let maxFramerate: number | undefined
      let degradation: RTCDegradationPreference | undefined
      if (kind === "video") {
        max = bitrateFor(isScreen ? SCREEN_BITRATE_BY_PEERS : CAMERA_BITRATE_BY_PEERS, peers)
        maxFramerate = isScreen ? SCREEN_MAX_FRAMERATE : CAMERA_MAX_FRAMERATE
        if (isScreen) {
          degradation = sender.track?.contentHint === "motion" ? "maintain-framerate" : "maintain-resolution"
        }
      } else {
        max = isScreen ? SCREEN_AUDIO_MAX_BITRATE : MIC_MAX_BITRATE
      }
      const withDegradation = params as RTCRtpSendParameters & { degradationPreference?: RTCDegradationPreference }
      const encoding = params.encodings[0]
      if (
        encoding.maxBitrate === max &&
        encoding.maxFramerate === maxFramerate &&
        withDegradation.degradationPreference === degradation
      ) {
        continue
      }
      encoding.maxBitrate = max
      if (maxFramerate !== undefined) encoding.maxFramerate = maxFramerate
      if (degradation) withDegradation.degradationPreference = degradation
      sender.setParameters(params).catch(() => {})
    }
  }

  media(): PeerMedia {
    const live = (stream: MediaStream) => stream.getTracks().some((t) => t.readyState === "live")
    const screen = this.screenStreamId ? (this.streams.get(this.screenStreamId) ?? null) : null
    let camera: MediaStream | null = null
    for (const [id, stream] of this.streams) {
      if (id !== this.screenStreamId && live(stream)) {
        camera = stream
        break
      }
    }
    return { camera, screen: screen && live(screen) ? screen : null }
  }

  connectionState(): RTCPeerConnectionState {
    return this.pc.connectionState
  }

  close() {
    this.closed = true
    this.pc.onnegotiationneeded = null
    this.pc.onicecandidate = null
    this.pc.ontrack = null
    this.pc.onconnectionstatechange = null
    this.pc.oniceconnectionstatechange = null
    this.pc.onsignalingstatechange = null
    this.pc.close()
  }
}

export class PeerMesh {
  private readonly selfClientId: string
  private readonly iceServers: RTCIceServer[]
  private readonly handlers: MeshHandlers
  private readonly links = new Map<string, PeerLink>()
  private readonly localTracks = new Map<string, { track: MediaStreamTrack; stream: MediaStream }>()
  private readonly screenTrackIds = new Set<string>()
  private screenStreamId: string | null = null
  private noMic = false
  private readonly pendingMeta = new Map<string, string>()

  constructor(selfClientId: string, iceServers: RTCIceServer[], handlers: MeshHandlers) {
    this.selfClientId = selfClientId
    this.iceServers = iceServers
    this.handlers = handlers
  }

  signal(toClient: string, type: SignalType, data: string) {
    this.handlers.sendSignal(toClient, type, data)
  }

  changed() {
    this.handlers.onChange()
  }

  peerCount() {
    return this.links.size
  }

  isScreenTrack(trackId: string) {
    return this.screenTrackIds.has(trackId)
  }

  has(clientId: string) {
    return this.links.has(clientId)
  }

  addPeer(clientId: string) {
    if (this.links.has(clientId)) return
    const link = new PeerLink(clientId, this.selfClientId, this.iceServers, this)
    this.links.set(clientId, link)
    const early = this.pendingMeta.get(clientId)
    if (early) {
      this.pendingMeta.delete(clientId)
      this.applyMeta(link, early)
    }
    if (this.screenStreamId || this.noMic) this.signal(clientId, "meta", this.metaPayload(link))
    if (!this.hasLocalMic()) link.expectAudio()
    for (const { track, stream } of this.localTracks.values()) {
      if (!this.screenTrackIds.has(track.id) || link.wantsMyScreen) link.addLocalTrack(track, stream)
    }
    this.reapplyEncodings()
    this.changed()
  }

  removePeer(clientId: string) {
    const link = this.links.get(clientId)
    if (!link) return
    link.close()
    this.links.delete(clientId)
    this.reapplyEncodings()
    this.changed()
  }

  private reapplyEncodings() {
    for (const link of this.links.values()) link.applyEncodings()
  }

  async handleSignal(fromClient: string, type: SignalType, data: string) {
    if (type === "meta") {
      const link = this.links.get(fromClient)
      if (!link) {
        this.pendingMeta.set(fromClient, data)
        return
      }
      this.applyMeta(link, data)
      this.changed()
      return
    }
    this.addPeer(fromClient)
    await this.links.get(fromClient)?.handleDescriptionOrCandidate(type, data)
  }

  private applyMeta(link: PeerLink, data: string) {
    const meta = JSON.parse(data) as { screenStreamId?: string | null; noMic?: boolean; watching?: boolean }
    link.screenStreamId = meta.screenStreamId ?? null
    link.noMic = meta.noMic === true
    if (!link.screenStreamId) link.watching = false
    const wants = meta.watching === true && this.screenStreamId !== null
    if (wants !== link.wantsMyScreen) {
      link.wantsMyScreen = wants
      for (const { track, stream } of this.localTracks.values()) {
        if (!this.screenTrackIds.has(track.id)) continue
        if (wants) link.addLocalTrack(track, stream)
        else link.removeLocalTrack(track.id)
      }
      link.applyEncodings()
    }
  }

  publishTrack(track: MediaStreamTrack, stream: MediaStream, isScreen = false) {
    this.localTracks.set(track.id, { track, stream })
    if (isScreen) {
      this.screenTrackIds.add(track.id)
      if (track.kind === "video") this.setScreenStream(stream.id)
    }
    for (const link of this.links.values()) {
      if (!isScreen || link.wantsMyScreen) link.addLocalTrack(track, stream)
    }
  }

  async replaceTrack(oldTrack: MediaStreamTrack, next: MediaStreamTrack, stream: MediaStream) {
    this.localTracks.delete(oldTrack.id)
    this.localTracks.set(next.id, { track: next, stream })
    await Promise.all([...this.links.values()].map((link) => link.replaceLocalTrack(oldTrack.id, next).catch(() => {})))
    for (const link of this.links.values()) link.applyEncodings()
  }

  unpublishTrack(track: MediaStreamTrack) {
    this.localTracks.delete(track.id)
    const wasScreen = this.screenTrackIds.delete(track.id)
    for (const link of this.links.values()) link.removeLocalTrack(track.id)
    if (wasScreen && track.kind === "video") this.setScreenStream(null)
  }

  private setScreenStream(streamId: string | null) {
    this.screenStreamId = streamId
    if (!streamId) {
      for (const link of this.links.values()) link.wantsMyScreen = false
    }
    this.broadcastMeta()
  }

  peerSharing(clientId: string) {
    return !!this.links.get(clientId)?.screenStreamId
  }

  watching(clientId: string) {
    return this.links.get(clientId)?.watching ?? false
  }

  setWatching(clientId: string, watching: boolean) {
    const link = this.links.get(clientId)
    if (!link || link.watching === watching || (watching && !link.screenStreamId)) return
    link.watching = watching
    this.signal(clientId, "meta", this.metaPayload(link))
    this.changed()
  }

  setNoMic(noMic: boolean) {
    if (this.noMic === noMic) return
    this.noMic = noMic
    this.broadcastMeta()
  }

  peerNoMic(clientId: string) {
    return this.links.get(clientId)?.noMic ?? false
  }

  private hasLocalMic() {
    for (const { track } of this.localTracks.values()) {
      if (track.kind === "audio" && !this.screenTrackIds.has(track.id)) return true
    }
    return false
  }

  private metaPayload(link: PeerLink) {
    return JSON.stringify({ screenStreamId: this.screenStreamId, noMic: this.noMic, watching: link.watching })
  }

  private broadcastMeta() {
    for (const [clientId, link] of this.links) this.signal(clientId, "meta", this.metaPayload(link))
  }

  media(clientId: string): PeerMedia {
    return this.links.get(clientId)?.media() ?? { camera: null, screen: null }
  }

  connectionState(clientId: string): RTCPeerConnectionState {
    return this.links.get(clientId)?.connectionState() ?? "new"
  }

  close() {
    for (const link of this.links.values()) link.close()
    this.links.clear()
    this.localTracks.clear()
    this.screenTrackIds.clear()
    this.pendingMeta.clear()
  }
}
