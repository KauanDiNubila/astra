import assert from "node:assert/strict"
import { api, createUser, launch, loginInBrowser } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
const DELAY_MS = 3000

const other = await createUser("Outra", "rl")
const categories = await api("/categories", other.token)
const saved = await api("/sessions", other.token, "POST", {
  categoryId: categories.json[0].id,
  focusedMinutes: 40,
  startedAt: new Date().toISOString(),
  note: null,
})
assert.equal(saved.status, 201)
const me = await createUser("Eu", "rl")

const browser = await launch()
const page = await browser.newPage({ viewport: { width: 1100, height: 700 } })
await page.route("**/ranking?*", async (route) => {
  const url = route.request().url()
  if (url.includes("GLOBAL") || url.includes("WEEKLY")) await new Promise((r) => setTimeout(r, DELAY_MS))
  await route.continue()
})
await loginInBrowser(page, APP, me.email)
await page.getByRole("link", { name: "Ranking" }).first().click()
await page.getByRole("heading", { name: "Ranking" }).waitFor()
await page.getByText("Ninguém do seu grupo de amigos").waitFor()

const box = page.locator("[aria-busy]").first()
const indicator = page.getByRole("progressbar", { name: "Carregando ranking" })

await page.getByRole("button", { name: "Global", exact: true }).click()
await page.waitForTimeout(800)
const emptyBefore = await box.innerText()
console.log(`trocando de Amigos (vazio) para Global: texto=${JSON.stringify(emptyBefore)} indicador=${await indicator.isVisible()}`)
assert.ok(!emptyBefore.includes("Ninguém registrou"), "enquanto carrega não afirma que ninguém registrou tempo")
assert.ok(emptyBefore.includes("Carregando ranking"), "sem lista anterior, diz que está carregando")
assert.equal(await indicator.isVisible(), true, "a linha de carregamento aparece")

await box.getByText("Outra", { exact: true }).first().waitFor({ timeout: 10000 })
assert.equal(await indicator.isVisible(), false, "a linha some quando a resposta chega")

await page.getByRole("button", { name: "Semana", exact: true }).click()
await page.waitForTimeout(800)
const withPrevious = await box.innerText()
console.log(`trocando de Hoje (com lista) para Semana: texto=${JSON.stringify(withPrevious)} indicador=${await indicator.isVisible()}`)
assert.ok(withPrevious.includes("Outra"), "com lista anterior, ela continua na tela")
assert.equal(await indicator.isVisible(), true, "e a linha mostra que está atualizando")

await box.getByText("Outra", { exact: true }).first().waitFor()
await page.waitForTimeout(DELAY_MS)
assert.equal(await indicator.isVisible(), false)

await browser.close()
console.log("ranking-loading: ok")
