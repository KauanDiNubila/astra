import assert from "node:assert/strict"
import { createUser, launch, loginInBrowser } from "./lib.mjs"

const APP = process.env.APP ?? "http://localhost:5173"
const DELAY_MS = 1500
const ROUTES = [
  { link: "Roadmaps", heading: "Roadmaps", api: "/roadmaps", chunk: "RoadmapsPage" },
  { link: "Ranking", heading: "Ranking", api: "/ranking", chunk: "RankingPage" },
]

const user = await createUser("Warmup", "wu")
const browser = await launch()

async function openPage({ saveData }) {
  const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
  if (saveData) {
    await page.addInitScript(() => {
      Object.defineProperty(navigator, "connection", { value: { saveData: true, effectiveType: "4g" } })
    })
  }
  const seen = []
  page.on("request", (request) => seen.push(request.url()))
  await page.route("**/*", async (route) => {
    const url = route.request().url()
    const { pathname } = new URL(url)
    if (url.includes(":8080") && ROUTES.some((r) => pathname === r.api)) {
      await new Promise((resolve) => setTimeout(resolve, DELAY_MS))
    }
    await route.continue()
  })
  await loginInBrowser(page, APP, user.email)
  await page.waitForTimeout(1500)
  seen.length = 0
  return { page, seen }
}

async function firstPaintAfterClick(page, route) {
  await page.getByRole("link", { name: route.link }).first().click()
  const heading = page.getByRole("heading", { name: route.heading, exact: true }).first()
  const skeleton = page.locator("[data-slot=skeleton]")
  const deadline = Date.now() + 10000
  while (Date.now() < deadline) {
    if ((await skeleton.count()) > 0) return "esqueleto"
    if (await heading.isVisible()) return "pagina"
    await page.waitForTimeout(20)
  }
  throw new Error(`${route.link} não apareceu`)
}

const requested = (seen, route) => ({
  chunk: seen.some((url) => url.includes(route.chunk)),
  api: seen.some((url) => url.includes(":8080") && url.includes(route.api)),
})

{
  const { page, seen } = await openPage({ saveData: true })
  await page.waitForTimeout(7000)
  for (const route of ROUTES) {
    assert.deepEqual(requested(seen, route), { chunk: false, api: false }, "Save-Data: nada é aquecido sozinho")
  }
  const paint = await firstPaintAfterClick(page, ROUTES[0])
  console.log(`controle sem aquecimento (${ROUTES[0].link}): primeira imagem=${paint}`)
  assert.equal(paint, "esqueleto", "controle: sem aquecer a página começa pelo esqueleto")
  await page.close()
}

{
  const { page } = await openPage({ saveData: true })
  const route = ROUTES[1]
  await page.getByRole("link", { name: route.link }).first().hover()
  await page.waitForTimeout(DELAY_MS + 1500)
  const paint = await firstPaintAfterClick(page, route)
  console.log(`hover antes do clique (${route.link}): primeira imagem=${paint}`)
  assert.equal(paint, "pagina", "depois do hover os dados já estão em cache")
  await page.close()
}

{
  const { page } = await openPage({ saveData: false })
  await page.waitForTimeout(DELAY_MS + 9000)
  const paints = []
  for (const route of ROUTES) {
    paints.push(`${route.link}=${await firstPaintAfterClick(page, route)}`)
  }
  console.log(`aquecimento ocioso, sem hover: ${paints.join(" ")}`)
  assert.ok(paints.every((p) => p.endsWith("pagina")), "parado no dashboard, os menus já abrem com dados")
  await page.close()
}

await browser.close()
console.log("route-warmup: ok")
