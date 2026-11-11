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

  await anaPage.getByRole("button", { name: /Desligar|Encerrar|Sair/ }).first().click()
  const ended = await untilTrue(async () => !(await receivingAudio(betoPage)), 10000)
  check(ended, "desligar encerra a chamada dos dois lados")
} catch (error) {
  check(false, "chamada de ponta a ponta", error.message.split(String.fromCharCode(10))[0])
}

await browser.close()
console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
