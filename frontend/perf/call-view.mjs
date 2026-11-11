import assert from "node:assert/strict"
import { buildCallView, connectionNotice, micStateOf, MIC_LABELS } from "../src/lib/callView.ts"

const stream = (id) => ({ id })
const noMedia = { camera: null, screen: null }

function source(overrides = {}) {
  return {
    call: { title: "Ana e Beto" },
    ringing: false,
    startedAt: 1000,
    participants: [],
    cameraOn: false,
    localStream: stream("local"),
    screenSharing: false,
    localScreen: null,
    micMissing: false,
    speaking: {},
    remoteMedia: () => noMedia,
    connectionOf: () => "connected",
    peerNoMic: () => false,
    peerSharing: () => false,
    watchingScreen: () => false,
    ...overrides,
  }
}

const peer = (clientId, name = "Beto") => ({ clientId, userId: `u-${clientId}`, name })

let failures = 0
function test(label, fn) {
  try {
    fn()
    console.log("OK  ", label)
  } catch (error) {
    failures++
    console.log("FAIL", label, error.message.split("\n")[0])
  }
}

test("sozinho na chamada há só o próprio tile, sem vídeo com a câmera desligada", () => {
  const view = buildCallView(source(), "me")
  assert.equal(view.count, 1)
  assert.equal(view.tiles.length, 1)
  assert.deepEqual(view.tiles[0], {
    kind: "person", key: "self", clientId: null, userId: "me", name: "Você",
    stream: null, isSelf: true, speaking: false, noMic: false, connection: null,
  })
})

test("com a câmera ligada o tile próprio recebe o stream local", () => {
  const view = buildCallView(source({ cameraOn: true }), "me")
  assert.equal(view.tiles[0].stream.id, "local")
})

test("compartilhando a própria tela, aparece um tile de tela com status own", () => {
  const view = buildCallView(source({ screenSharing: true, localScreen: stream("screen") }), "me")
  assert.equal(view.tiles.length, 2)
  assert.deepEqual(view.tiles[1], {
    kind: "screen", key: "self-screen", clientId: null, userId: "me", name: "Você",
    stream: view.tiles[1].stream, status: "own",
  })
  assert.equal(view.tiles[1].stream.id, "screen")
})

test("participante sem nome vira Participante, inclusive no tile da tela", () => {
  const view = buildCallView(source({ participants: [peer("c1", "")], peerSharing: () => true }), "me")
  assert.equal(view.tiles[1].name, "Participante")
  assert.equal(view.tiles[2].name, "Participante")
})

test("a tela de outra pessoa passa por oferta, carregando e ao vivo", () => {
  const participants = [peer("c1")]
  const offer = buildCallView(source({ participants, peerSharing: () => true }), "me")
  assert.equal(offer.tiles[2].status, "offer")
  assert.equal(offer.tiles[2].stream, null)

  const loading = buildCallView(
    source({ participants, peerSharing: () => true, watchingScreen: () => true }),
    "me",
  )
  assert.equal(loading.tiles[2].status, "loading")

  const live = buildCallView(
    source({
      participants,
      peerSharing: () => true,
      watchingScreen: () => true,
      remoteMedia: () => ({ camera: null, screen: stream("remote-screen") }),
    }),
    "me",
  )
  assert.equal(live.tiles[2].status, "live")
  assert.equal(live.tiles[2].stream.id, "remote-screen")
})

test("tela ao vivo continua aparecendo mesmo que peerSharing já tenha voltado a falso", () => {
  const view = buildCallView(
    source({
      participants: [peer("c1")],
      peerSharing: () => false,
      watchingScreen: () => true,
      remoteMedia: () => ({ camera: null, screen: stream("remote-screen") }),
    }),
    "me",
  )
  assert.equal(view.tiles[2].status, "live")
})

test("quem fala, quem está sem microfone e a conexão vêm do participante certo", () => {
  const view = buildCallView(
    source({
      participants: [peer("c1"), peer("c2", "Caio")],
      speaking: { c2: true },
      peerNoMic: (id) => id === "c1",
      connectionOf: (id) => (id === "c2" ? "connecting" : "connected"),
    }),
    "me",
  )
  const [, beto, caio] = view.tiles
  assert.equal(beto.noMic, true)
  assert.equal(beto.speaking, false)
  assert.equal(caio.speaking, true)
  assert.equal(caio.connection, "connecting")
  assert.equal(view.count, 3)
})

test("conexão falhada marca a chamada como instável", () => {
  const view = buildCallView(source({ participants: [peer("c1")], connectionOf: () => "failed" }), "me")
  assert.equal(view.troubled, true)
})

test("título, chamando e início vêm da chamada", () => {
  const view = buildCallView(source({ ringing: true, startedAt: 5, call: { title: "Estudo" } }), "me")
  assert.equal(view.title, "Estudo")
  assert.equal(view.ringing, true)
  assert.equal(view.startedAt, 5)
  assert.equal(buildCallView(source({ call: null }), "me").title, "")
})

test("estado do microfone: sem microfone vence mudo, mudo vence ligado", () => {
  assert.equal(micStateOf(false, false), "on")
  assert.equal(micStateOf(true, false), "muted")
  assert.equal(micStateOf(false, true), "missing")
  assert.equal(micStateOf(true, true), "missing")
  assert.equal(MIC_LABELS.missing, "Sem microfone. Clique para tentar de novo")
})

test("aviso de conexão: só aparece fora de conectado, e falha é instável", () => {
  assert.equal(connectionNotice(null), null)
  assert.equal(connectionNotice("connected"), null)
  assert.equal(connectionNotice("connecting"), "Conectando…")
  assert.equal(connectionNotice("new"), "Conectando…")
  assert.equal(connectionNotice("disconnected"), "Conectando…")
  assert.equal(connectionNotice("failed"), "Conexão instável")
})

console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
