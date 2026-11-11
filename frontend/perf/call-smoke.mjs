import { befriend, createUser, launch, loginInBrowser, sleep } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
let failures = 0

function check(ok, label, detail = "") {
  if (!ok) failures++
  console.log(ok ? "OK  " : "FAIL", label, detail)
}

const ana = await createUser("Ana", "callsmoke")
const beto = await createUser("Beto", "callsmoke")
await befriend(ana, beto)

const browser = await launch(["--use-fake-ui-for-media-stream", "--use-fake-device-for-media-stream"])
const newPage = async (user) => {
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } })
  await context.grantPermissions(["microphone", "camera"], { origin: APP })
  const page = await context.newPage()
  await loginInBrowser(page, APP, user.email)
  return page
}

const receivingAudio = (page) =>
  page.evaluate(() =>
    [...document.querySelectorAll("audio")].some((a) => {
      const stream = a.srcObject
      return stream instanceof MediaStream && stream.getAudioTracks().some((t) => t.readyState === "live")
    }),
  )

const untilTrue = async (fn, ms = 20000) => {
  const end = Date.now() + ms
  while (Date.now() < end) {
    if (await fn()) return true
    await sleep(300)
  }
  return false
}

try {
  const anaPage = await newPage(ana)
  const betoPage = await newPage(beto)

  await anaPage.goto(`${APP}/chat/${beto.id}`)
  await anaPage.getByRole("button", { name: "Ligar" }).waitFor()
  await sleep(1500)
  await anaPage.getByRole("button", { name: "Ligar" }).click()

  await betoPage.getByRole("button", { name: "Atender chamada" }).waitFor({ timeout: 15000 })
  check(true, "quem recebe vê o convite da chamada")
  await betoPage.getByRole("button", { name: "Atender chamada" }).click()

  const anaHears = await untilTrue(() => receivingAudio(anaPage))
  const betoHears = await untilTrue(() => receivingAudio(betoPage))
  check(anaHears && betoHears, "os dois lados passam a receber áudio um do outro", `(Ana: ${anaHears}, Beto: ${betoHears})`)

  check((await betoPage.getByText("Ana", { exact: true }).count()) > 0, "cada lado vê o nome do outro no tile")
  check((await anaPage.getByText("Você", { exact: true }).count()) > 0, "o próprio tile se chama Você")
  check((await anaPage.getByText("2 na call").count()) > 0, "o cabeçalho mostra quantos estão na call")

  const mute = anaPage.getByRole("button", { name: "Silenciar microfone" })
  await mute.click()
  const unmute = anaPage.getByRole("button", { name: "Ativar microfone" })
  await unmute.waitFor({ timeout: 5000 })
  check((await unmute.getAttribute("aria-pressed")) === "true", "silenciar troca o rótulo e marca o botão como pressionado")
  await unmute.click()
  await anaPage.getByRole("button", { name: "Silenciar microfone" }).waitFor({ timeout: 5000 })
  check(true, "ativar o microfone volta ao estado normal")

  await anaPage.getByRole("button", { name: "Ligar câmera" }).click()
  await anaPage.getByRole("button", { name: "Desligar câmera" }).waitFor({ timeout: 8000 })
  const remoteVideo = await untilTrue(() =>
    betoPage.evaluate(() =>
      [...document.querySelectorAll("video")].some(
        (v) => v.srcObject instanceof MediaStream && v.srcObject.getVideoTracks().some((t) => t.readyState === "live"),
      ),
    ),
  )
  check(remoteVideo, "ligar a câmera mostra o vídeo no outro lado")
  await anaPage.getByRole("button", { name: "Desligar câmera" }).click()
  await anaPage.getByRole("button", { name: "Ligar câmera" }).waitFor({ timeout: 5000 })

  await anaPage.getByRole("button", { name: "Minimizar chamada" }).click()
  await anaPage.getByTitle("Abrir chamada").waitFor({ timeout: 5000 })
  await anaPage.getByRole("dialog", { name: /Chamada com/ }).waitFor({ state: "detached", timeout: 5000 })
  check(true, "minimizar mostra a chamada em miniatura")
  await anaPage.getByRole("button", { name: "Silenciar microfone" }).click()
  await anaPage.getByRole("button", { name: "Ativar microfone" }).waitFor({ timeout: 5000 })
  await anaPage.getByRole("button", { name: "Ativar microfone" }).click()
  check(true, "dá para silenciar e reativar pela miniatura")
  await anaPage.getByRole("button", { name: "Expandir chamada" }).click()
  await anaPage.getByRole("button", { name: "Minimizar chamada" }).waitFor({ timeout: 5000 })
  check(true, "expandir volta para a chamada inteira")

  await anaPage.getByRole("button", { name: "Sair da chamada" }).first().click()
  const ended = await untilTrue(async () => !(await receivingAudio(betoPage)), 10000)
  check(ended, "desligar encerra a chamada dos dois lados")
} catch (error) {
  check(false, "chamada de ponta a ponta", error.message.split(String.fromCharCode(10))[0])
}

await browser.close()
console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
