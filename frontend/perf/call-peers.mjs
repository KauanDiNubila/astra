import assert from "node:assert/strict"

let failNextRemoteDescription = false
const created = []

class FakePeerConnection {
  signalingState = "stable"
  remoteDescription = null
  localDescription = null
  remoteDescriptions = []

  constructor() {
    created.push(this)
  }

  getTransceivers() {
    return []
  }

  getSenders() {
    return []
  }

  addTransceiver() {}

  async setRemoteDescription(description) {
    this.remoteDescriptions.push(description)
    if (failNextRemoteDescription) {
      failNextRemoteDescription = false
      throw new Error("InvalidStateError")
    }
    this.remoteDescription = description
  }

  async setLocalDescription() {
    this.localDescription = { type: "answer", sdp: "x" }
  }

  async addIceCandidate() {}

  close() {}
}

globalThis.RTCPeerConnection = FakePeerConnection

const { PeerMesh } = await import("../src/lib/callPeers.ts")

let failures = 0
async function test(label, fn) {
  created.length = 0
  failNextRemoteDescription = false
  try {
    await fn()
    console.log("OK  ", label)
  } catch (error) {
    failures++
    console.log("FAIL", label, error.message.split("\n")[0])
  }
}

function newMesh(selfClientId) {
  const signals = []
  const mesh = new PeerMesh(selfClientId, [], { sendSignal: (to, type) => signals.push({ to, type }), onChange: () => {} })
  return { mesh, signals }
}

const description = (type) => JSON.stringify({ type, sdp: "x" })

await test("oferta em colisão é ignorada pelo lado que não cede", async () => {
  const { mesh } = newMesh("a")
  mesh.addPeer("b")
  created[0].signalingState = "have-local-offer"

  await mesh.handleSignal("b", "offer", description("offer"))

  assert.equal(created[0].remoteDescriptions.length, 0)
})

await test("oferta com a conexão estável é aceita", async () => {
  const { mesh, signals } = newMesh("a")
  mesh.addPeer("b")

  await mesh.handleSignal("b", "offer", description("offer"))

  assert.equal(created[0].remoteDescriptions.length, 1)
  assert.ok(signals.some((s) => s.type === "answer"))
})

await test("se aplicar a resposta falhar, a colisão seguinte continua sendo reconhecida", async () => {
  const { mesh } = newMesh("a")
  mesh.addPeer("b")
  created[0].signalingState = "have-local-offer"

  failNextRemoteDescription = true
  await mesh.handleSignal("b", "answer", description("answer")).catch(() => {})
  await mesh.handleSignal("b", "offer", description("offer"))

  assert.equal(created[0].remoteDescriptions.length, 1, "a oferta em colisão não deveria ter sido aplicada")
})

console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
