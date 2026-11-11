import { api, befriend, createUser, launch, loginInBrowser, sleep, stompSession } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
let failures = 0

function check(ok, label, detail = "") {
  if (!ok) failures++
  console.log(ok ? "OK  " : "FAIL", label, detail)
}

const ana = await createUser("Ana", "chatbugs")
const beto = await createUser("Beto", "chatbugs")
await befriend(ana, beto)

const browser = await launch()
const context = await browser.newContext({ viewport: { width: 1280, height: 800 } })
const page = await context.newPage()
await loginInBrowser(page, APP, ana.email)

const betoSocket = await stompSession(beto.token)
const sendToAna = (content) => betoSocket.send("/app/chat.send", { recipientId: ana.id, content, replyToMessageId: null })

try {
  const offline = await context.newPage()
  await offline.routeWebSocket(/\/ws$/, (ws) => ws.close())
  await offline.goto(`${APP}/chat/${beto.id}`)
  const composer = offline.locator("textarea")
  await composer.waitFor()
  await composer.fill("mensagem que não pode sumir")
  await composer.press("Enter")
  await sleep(500)
  check((await composer.inputValue()) === "mensagem que não pode sumir", "sem conexão, o rascunho continua no campo")
  await offline.close()
} catch (error) {
  check(false, "cenário abortou", error.message.split(String.fromCharCode(10))[0])
}

try {
  for (let i = 1; i <= 40; i++) sendToAna(`mensagem ${i}`)
  await page.goto(`${APP}/chat/${beto.id}`)
  await page.locator(".overlay-scroll p", { hasText: /^mensagem 40$/ }).waitFor()
  const container = page.locator(".overlay-scroll").filter({ has: page.locator("p", { hasText: /^mensagem 40$/ }) })
  await container.hover()
  await page.mouse.wheel(0, -900)
  await container.evaluate(
    (el) =>
      new Promise((resolve) => {
        const deadline = performance.now() + 3000
        const waitForScroll = () => {
          if (el.scrollHeight - el.scrollTop - el.clientHeight > 200 || performance.now() > deadline) resolve()
          else requestAnimationFrame(waitForScroll)
        }
        waitForScroll()
      }),
  )
  const distance = await container.evaluate(
    (el) =>
      new Promise((resolve) => {
        const end = performance.now() + 700
        let closest = Infinity
        const sample = () => {
          closest = Math.min(closest, el.scrollHeight - el.scrollTop - el.clientHeight)
          if (performance.now() < end) requestAnimationFrame(sample)
          else resolve(closest)
        }
        sample()
      }),
  )
  check(distance > 200, "subir a conversa logo depois de abrir não volta sozinho para o fim", `(menor distância do fim: ${Math.round(distance)}px)`)
} catch (error) {
  check(false, "cenário abortou", error.message.split(String.fromCharCode(10))[0])
}

try {
  await page.goto(`${APP}/chat`)
  const item = page.getByRole("link", { name: /Beto/ })
  await item.getByText("mensagem 40").waitFor()
  const unread = async () => {
    const label = await item.getByLabel(/não lida/).getAttribute("aria-label", { timeout: 500 }).catch(() => null)
    return label ? Number(label.split(" ")[0]) : 0
  }
  const before = await unread()
  for (let i = 1; i <= 6; i++) sendToAna(`rajada ${i}`)
  await item.getByText("rajada 6").waitFor()
  await sleep(300)
  const counted = (await unread()) - before
  check(counted === 6, "6 mensagens em rajada contam 6 não lidas", `(contou ${counted})`)
} catch (error) {
  check(false, "cenário abortou", error.message.split(String.fromCharCode(10))[0])
}

try {
  await page.goto(`${APP}/chat/${beto.id}`)
  const composer = page.locator("textarea")
  await composer.fill("oi amigo")
  await composer.press("Enter")
  await page.locator(".overlay-scroll p", { hasText: /^oi amigo$/ }).waitFor({ timeout: 5000 })
  check(true, "a própria mensagem aparece na conversa com o amigo")
} catch (error) {
  check(false, "a própria mensagem aparece na conversa com o amigo", error.message.split(String.fromCharCode(10))[0])
}

try {
  const created = await api("/chat/groups", ana.token, "POST", { name: "Estudo em grupo", memberIds: [beto.id] })
  const groupId = created.json.groupId
  await page.goto(`${APP}/chat`)
  const item = page.getByRole("link", { name: /Estudo em grupo/ })
  await item.waitFor()
  for (let i = 1; i <= 3; i++) betoSocket.send("/app/chat.sendGroup", { groupId, content: `grupo ${i}`, replyToMessageId: null })
  await item.getByText("grupo 3").waitFor()
  await sleep(300)
  const label = await item.getByLabel(/não lida/).getAttribute("aria-label", { timeout: 500 }).catch(() => null)
  check(label === "3 não lidas", "mensagens de grupo contam como não lidas na lista", `(rótulo: ${label})`)

  await item.click()
  await page.locator(".overlay-scroll p", { hasText: /^grupo 3$/ }).waitFor({ timeout: 5000 })
  await sleep(500)
  const afterOpen = await item.getByLabel(/não lida/).count()
  check(afterOpen === 0, "abrir o grupo marca como lido")
  await page.locator(".overlay-scroll").getByText("Beto", { exact: true }).first().waitFor({ timeout: 5000 })
  check(true, "o nome de quem escreveu aparece nas mensagens do grupo")

  const composer = page.locator("textarea")
  await composer.fill("oi grupo")
  await composer.press("Enter")
  await page.locator(".overlay-scroll p", { hasText: /^oi grupo$/ }).waitFor({ timeout: 5000 })
  check(true, "a própria mensagem aparece no grupo")
} catch (error) {
  check(false, "cenário do grupo", error.message.split(String.fromCharCode(10))[0])
}

try {
  const history = await api(`/chat/${beto.id}/messages`, ana.token)
  const target = history.json.find((m) => m.content === "mensagem 10")
  await page.goto(`${APP}/chat/${beto.id}`)
  await page.locator(".overlay-scroll p", { hasText: /^oi amigo$/ }).waitFor({ timeout: 5000 })
  await api(`/chat/messages/${target.id}/pin`, beto.token, "PUT")
  await page.getByText("Mensagem fixada").waitFor({ timeout: 5000 })
  check((await page.getByText("mensagem 10").count()) > 0, "mensagem fixada pelo amigo aparece na barra de fixadas")
  await api(`/chat/messages/${target.id}/pin`, beto.token, "DELETE")
  await page.getByText("Mensagem fixada").waitFor({ state: "detached", timeout: 5000 })
  check(true, "desafixar remove a barra")
} catch (error) {
  check(false, "fixadas", error.message.split(String.fromCharCode(10))[0])
}

try {
  const slow = await context.newPage()
  const historyUrl = new RegExp(`/chat/${beto.id}/messages$`)
  await slow.route(historyUrl, async (route) => {
    const response = await route.fetch()
    await sleep(4000)
    await route.fulfill({ response })
  })
  const socketOpened = slow.waitForEvent("websocket")
  await slow.goto(`${APP}/chat/${beto.id}`)
  await socketOpened
  await sleep(1200)
  sendToAna("chegou durante o carregamento")
  const arrived = slow.locator("p", { hasText: /^chegou durante o carregamento$/ })
  await arrived.waitFor({ timeout: 3000 })
  await sleep(4500)
  check((await arrived.count()) > 0, "mensagem que chega enquanto o histórico carrega não some quando ele termina")
  await slow.close()
} catch (error) {
  check(false, "mensagem durante o carregamento do histórico", error.message.split(String.fromCharCode(10))[0])
}

betoSocket.close()
await browser.close()
console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
