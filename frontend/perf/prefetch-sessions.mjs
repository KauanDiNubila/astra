import assert from "node:assert/strict"
import { api, createUser, launch, loginInBrowser } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
const DELAY_MS = 1500

const user = await createUser("Prefetch", "pf")
const category = await api("/categories", user.token, "POST", { name: "Categoria do teste", color: "#10b981" })
assert.equal(category.status, 201)
const session = await api("/sessions", user.token, "POST", {
  categoryId: category.json.id,
  focusedMinutes: 25,
  startedAt: new Date().toISOString(),
  note: "Nota única do teste",
})
assert.equal(session.status, 201)

const browser = await launch()

async function openSessionsFrom(page) {
  await page.getByRole("link", { name: "Sessões" }).first().click()
  const list = page.getByText("Nota única do teste").first()
  const skeleton = page.locator("[data-slot=skeleton]")
  const deadline = Date.now() + 8000
  while (Date.now() < deadline) {
    if (await list.isVisible()) return { firstPaint: "lista" }
    if ((await skeleton.count()) > 0) return { firstPaint: "esqueleto" }
    await page.waitForTimeout(20)
  }
  throw new Error("a página de sessões não apareceu")
}

async function scenario(name, startFocusFirst) {
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
  const requests = []
  await page.route("**/sessions?*", async (route) => {
    requests.push(Date.now())
    await new Promise((resolve) => setTimeout(resolve, DELAY_MS))
    await route.continue()
  })
  await loginInBrowser(page, APP, user.email)
  await page.waitForTimeout(1500)
  requests.length = 0
  if (startFocusFirst) {
    await page.getByRole("button", { name: "Iniciar foco" }).first().click()
    await page.waitForTimeout(DELAY_MS + 1200)
  }
  const result = await openSessionsFrom(page)
  console.log(`${name}: primeira imagem=${result.firstPaint}`)
  await page.close()
  return result
}

const cold = await scenario("sem iniciar foco", false)
assert.equal(cold.firstPaint, "esqueleto", "controle: sem foco a página carrega do zero e mostra o esqueleto")

const warm = await scenario("iniciando foco antes", true)
assert.equal(warm.firstPaint, "lista", "com o foco iniciado as sessões já estão em cache, sem esqueleto")

await browser.close()
console.log("prefetch-sessions: ok")
