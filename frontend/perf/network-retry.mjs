import assert from "node:assert/strict"
import { createUser, launch, loginInBrowser } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
const FAILURES = Number(process.env.FAILURES ?? 3)

const user = await createUser("Retry", "rt")
const browser = await launch()
const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
await loginInBrowser(page, APP, user.email)
await page.waitForTimeout(1000)

let attempts = 0
await page.route("**/ranking?*", async (route) => {
  attempts++
  if (attempts <= FAILURES) return route.abort("failed")
  return route.continue()
})

const started = Date.now()
await page.getByRole("link", { name: "Ranking" }).first().click()
const outcome = await Promise.race([
  page.getByText("Não foi possível carregar o ranking").waitFor({ timeout: 30000 }).then(() => "erro"),
  page.getByRole("button", { name: "Global" }).waitFor({ timeout: 30000 }).then(async () => {
    await page.getByText("Não foi possível carregar o ranking").waitFor({ state: "hidden", timeout: 30000 })
    return "ranking"
  }),
])
console.log(`${FAILURES} quedas seguidas: resultado=${outcome} tentativas=${attempts} em ${Math.round((Date.now() - started) / 1000)}s`)
await browser.close()
assert.equal(outcome, "ranking", "a tela se recupera sozinha depois de quedas curtas")
console.log("network-retry: ok")
