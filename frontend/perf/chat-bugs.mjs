import { befriend, createUser, launch, loginInBrowser, sleep, stompSession } from "./lib.mjs"

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

{
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
}

{
  for (let i = 1; i <= 40; i++) sendToAna(`mensagem ${i}`)
  await sleep(1500)
  await page.goto(`${APP}/chat/${beto.id}`)
  await page.locator(".overlay-scroll p", { hasText: /^mensagem 40$/ }).waitFor()
  const container = page.locator(".overlay-scroll").filter({ has: page.locator("p", { hasText: /^mensagem 40$/ }) })
  await container.hover()
  await page.mouse.wheel(0, -900)
  await sleep(700)
  const distance = await container.evaluate((el) => el.scrollHeight - el.scrollTop - el.clientHeight)
  check(distance > 200, "subir a conversa logo depois de abrir não volta sozinho para o fim", `(distância do fim: ${Math.round(distance)}px)`)
}

{
  await page.goto(`${APP}/chat`)
  await page.getByText("mensagem 40").first().waitFor()
  await sleep(500)
  const before = Number((await page.locator("span.rounded-full.bg-primary").first().textContent().catch(() => "0")) ?? 0)
  for (let i = 1; i <= 6; i++) sendToAna(`rajada ${i}`)
  await sleep(2000)
  const badge = Number((await page.locator("span.rounded-full.bg-primary").first().textContent()) ?? 0)
  check(badge - before === 6, "6 mensagens em rajada contam 6 não lidas", `(contou ${badge - before})`)
}

betoSocket.close()
await browser.close()
console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 1 - 1 : 1)
