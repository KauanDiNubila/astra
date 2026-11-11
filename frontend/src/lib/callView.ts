import type { CallParticipant } from "@/lib/types"

type Media = { camera: MediaStream | null; screen: MediaStream | null }

export type CallViewSource = {
  call: { title: string } | null
  ringing: boolean
  startedAt: number | null
  participants: CallParticipant[]
  cameraOn: boolean
  localStream: MediaStream | null
  screenSharing: boolean
  localScreen: MediaStream | null
  micMissing: boolean
  speaking: Record<string, boolean>
  remoteMedia: (clientId: string) => Media
  connectionOf: (clientId: string) => RTCPeerConnectionState
  peerNoMic: (clientId: string) => boolean
  peerSharing: (clientId: string) => boolean
  watchingScreen: (clientId: string) => boolean
}

export type PersonTile = {
  kind: "person"
  key: string
  clientId: string | null
  userId: string
  name: string
  stream: MediaStream | null
  isSelf: boolean
  speaking: boolean
  noMic: boolean
  connection: RTCPeerConnectionState | null
}

export type ScreenStatus = "own" | "offer" | "loading" | "live"

export type ScreenTile = {
  kind: "screen"
  key: string
  clientId: string | null
  userId: string
  name: string
  stream: MediaStream | null
  status: ScreenStatus
}

export type Tile = PersonTile | ScreenTile

export type CallView = {
  title: string
  ringing: boolean
  startedAt: number | null
  count: number
  troubled: boolean
  tiles: Tile[]
}

const SELF_NAME = "Você"
const UNKNOWN_NAME = "Participante"

export function buildCallView(source: CallViewSource, userId: string): CallView {
  const tiles: Tile[] = [
    {
      kind: "person",
      key: "self",
      clientId: null,
      userId,
      name: SELF_NAME,
      stream: source.cameraOn ? source.localStream : null,
      isSelf: true,
      speaking: !!source.speaking.self,
      noMic: source.micMissing,
      connection: null,
    },
  ]
  if (source.screenSharing && source.localScreen) {
    tiles.push({
      kind: "screen",
      key: "self-screen",
      clientId: null,
      userId,
      name: SELF_NAME,
      stream: source.localScreen,
      status: "own",
    })
  }
  for (const peer of source.participants) {
    const media = source.remoteMedia(peer.clientId)
    const name = peer.name || UNKNOWN_NAME
    tiles.push({
      kind: "person",
      key: peer.clientId,
      clientId: peer.clientId,
      userId: peer.userId,
      name,
      stream: media.camera,
      isSelf: false,
      speaking: !!source.speaking[peer.clientId],
      noMic: source.peerNoMic(peer.clientId),
      connection: source.connectionOf(peer.clientId),
    })
    const watching = source.watchingScreen(peer.clientId)
    const live = watching && !!media.screen
    if (live || source.peerSharing(peer.clientId)) {
      tiles.push({
        kind: "screen",
        key: `${peer.clientId}-screen`,
        clientId: peer.clientId,
        userId: peer.userId,
        name,
        stream: live ? media.screen : null,
        status: live ? "live" : watching ? "loading" : "offer",
      })
    }
  }
  return {
    title: source.call?.title ?? "",
    ringing: source.ringing,
    startedAt: source.startedAt,
    count: source.participants.length + 1,
    troubled: source.participants.some((p) => source.connectionOf(p.clientId) === "failed"),
    tiles,
  }
}

export type MicState = "on" | "muted" | "missing"

export function micStateOf(muted: boolean, micMissing: boolean): MicState {
  if (micMissing) return "missing"
  return muted ? "muted" : "on"
}

export const MIC_LABELS: Record<MicState, string> = {
  on: "Silenciar microfone",
  muted: "Ativar microfone",
  missing: "Sem microfone. Clique para tentar de novo",
}

export function connectionNotice(connection: RTCPeerConnectionState | null): string | null {
  if (connection === null || connection === "connected") return null
  return connection === "failed" ? "Conexão instável" : "Conectando…"
}
