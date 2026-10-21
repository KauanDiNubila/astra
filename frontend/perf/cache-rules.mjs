import { api, befriend, createUser, launch, loginInBrowser, PASSWORD, sleep } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
let failures = 0

function check(ok, label) {
  if (!ok) failures++
  console.log(ok ? "OK  " : "FAIL", label)
}

async function until(fn, ms = 10000) {
  const end = Date.now() + ms
  while (Date.now() < end) {
    try {
      if (await fn()) return true
    } catch {
      await sleep(150)
      continue
    }
    await sleep(150)
  }
  return false
}

const today = (page) =>
  page.evaluate(() => {
    const label = [...document.querySelectorAll("main p")].find((e) => e.textContent.trim() === "Hoje")
    return label?.nextElementSibling?.textContent.replace(/\s+/g, "") ?? null
  })

const dashboardShown = (page) => page.locator("main h1", { hasText: /Bom dia|Boa tarde|Boa noite/ }).waitFor({ timeout: 2000 })

const focusTab = (page) =>
  page.evaluate(() => {
    window.dispatchEvent(new Event("visibilitychange"))
    document.dispatchEvent(new Event("visibilitychange"))
    window.dispatchEvent(new Event("focus"))
  })

const nav = (page, label) => page.getByRole("link", { name: label, exact: true }).first().click()

function session(user, categoryId, minutes) {
  return api("/sessions", user.token, "POST", {
    categoryId,
    focusedMinutes: minutes,
    startedAt: new Date(Date.now() - 3600000).toISOString(),
    note: null,
  })
}

const alice = await createUser("Alice", "cache")
const bruno = await createUser("Bruno", "cache")
await befriend(alice, bruno)
const aliceCategory = (await api("/categories", alice.token)).json.find((c) => c.name === "Estudo")
const brunoCategory = (await api("/categories", bruno.token)).json[0]

const browser = await launch()
const p = await (await browser.newContext({ viewport: { width: 1280, height: 800 }, reducedMotion: "reduce" })).newPage()
let dashboardRequests = 0
let sessionsRequests = 0
p.on("request", (r) => {
  const url = new URL(r.url())
  if (url.port === "8080" && url.pathname === "/dashboard") dashboardRequests++
  if (url.port === "8080" && url.pathname === "/sessions" && r.method() === "GET") sessionsRequests++
})
await loginInBrowser(p, APP, alice.email)

check(await until(async () => (await today(p)) === "0min"), "dashboard carregou")

await nav(p, "Cursos")
await p.getByRole("heading", { name: "Cursos" }).waitFor()
const requestsBefore = dashboardRequests
await nav(p, "Dashboard")
await dashboardShown(p)
check((await p.locator("[data-slot=skeleton]").count()) === 0, "volta ao dashboard sem esqueleto de carregamento")
check(await until(async () => dashboardRequests > requestsBefore), "a volta confere com o servidor (nova requisição /dashboard)")

await session(alice, aliceCategory.id, 30)
await sleep(500)
check((await today(p)) === "0min", "sessão salva em outro aparelho ainda não aparece antes do foco")
await focusTab(p)
check(await until(async () => (await today(p)) === "30min"), "voltar para a aba atualiza sozinho")

await nav(p, "Sessões")
await p.getByRole("heading", { name: "Sessões" }).waitFor()
await nav(p, "Dashboard")
await dashboardShown(p)
const sessionsBefore = sessionsRequests
await nav(p, "Sessões")
await p.getByRole("heading", { name: "Sessões" }).waitFor({ timeout: 2000 })
check((await p.locator("[data-slot=skeleton]").count()) === 0, "volta a Sessões sem esqueleto de carregamento")
check(await until(async () => sessionsRequests > sessionsBefore), "a volta a Sessões confere com o servidor")

await p.getByRole("button", { name: "Manual" }).click()
await p.getByRole("button", { name: /Categoria/ }).first().click()
await p.getByText("Estudo", { exact: true }).last().click()
await p.getByRole("button", { name: /^Registrar/ }).click()
await sleep(700)
await nav(p, "Dashboard")
await dashboardShown(p)
await sleep(80)
const firstPaint = await today(p)
check(firstPaint === "55min", `sessão salva já aparece na primeira exibição do dashboard (${firstPaint})`)

await nav(p, "Ranking")
check(await until(async () => (await p.locator("main").textContent()).includes("55 min")), "ranking mostra os minutos novos")
await session(bruno, brunoCategory.id, 40)
await focusTab(p)
check(await until(async () => (await p.locator("main").textContent()).includes("40 min")), "sessão do amigo aparece no ranking ao voltar para a aba")

await nav(p, "Roadmaps")
await p.getByRole("button", { name: "Novo roadmap" }).click()
await p.getByPlaceholder("Minha trilha de backend").fill("Roadmap do teste de cache")
await p.getByRole("button", { name: "Criar roadmap" }).click()
check(await until(async () => (await p.getByText("Roadmap do teste de cache").count()) > 0), "roadmap criado aparece na lista")

await p.getByRole("button", { name: /Alice/ }).last().click()
await p.getByRole("button", { name: "Sair" }).click()
await p.getByRole("button", { name: "Sair" }).click()
await sleep(1500)
await p.evaluate(() => {
  history.pushState({}, "", "/login")
  dispatchEvent(new PopStateEvent("popstate"))
})
await sleep(800)
await p.locator("#email").fill(bruno.email)
await p.locator("#password").fill(PASSWORD)
await p.locator('button[type="submit"]').click()
await p.waitForURL(/dashboard/, { timeout: 15000 })
const seen = []
for (let i = 0; i < 30; i++) {
  seen.push(await today(p))
  await sleep(50)
}
check(!seen.some((text) => text?.includes("55")), "outra conta na mesma aba nunca vê os dados da anterior")
check(await until(async () => (await today(p)) === "40min"), "a outra conta vê os próprios dados")

await browser.close()
console.log(failures ? `${failures} falha(s)` : "tudo certo")
process.exit(failures ? 1 : 0)
