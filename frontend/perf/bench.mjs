import { API, launch, loginInBrowser, sleep } from "./lib.mjs"

export const PAGES = [
  ["Ranking", "Ranking"],
  ["Cursos", "Cursos"],
  ["Roadmaps", "Roadmaps"],
  ["Dashboard", "Dashboard"],
]

const median = (values) => {
  const sorted = [...values].sort((a, b) => a - b)
  return sorted[Math.floor(sorted.length / 2)]
}

export async function bench({ app, email, delay, rounds }) {
  const browser = await launch()
  const page = await (await browser.newContext({ viewport: { width: 1280, height: 800 } })).newPage()
  await page.route(`${API}/**`, async (route) => {
    await sleep(delay)
    await route.continue()
  })
  await loginInBrowser(page, app, email)
  await page.getByRole("heading", { name: "Dashboard", exact: true }).waitFor({ timeout: 20000 })

  const visit = (label, heading) =>
    page.evaluate(
      async ({ label, heading }) => {
        const link = [...document.querySelectorAll("a")].find((a) => a.textContent.trim() === label && a.offsetParent)
        const start = performance.now()
        link.click()
        await new Promise((resolve) => {
          const check = () =>
            [...document.querySelectorAll("h1")].some((h) => h.textContent.trim() === heading)
              ? resolve()
              : requestAnimationFrame(check)
          check()
        })
        return performance.now() - start
      },
      { label, heading },
    )

  const first = {}
  for (const [label, heading] of PAGES) {
    first[label] = Math.round(await visit(label, heading))
    await sleep(1500)
  }
  const raw = Object.fromEntries(PAGES.map(([label]) => [label, []]))
  for (let round = 0; round < rounds; round++) {
    for (const [label, heading] of PAGES) {
      raw[label].push(Math.round(await visit(label, heading)))
      await sleep(1200)
    }
  }
  await browser.close()
  return {
    first,
    median: Object.fromEntries(Object.entries(raw).map(([label, values]) => [label, median(values)])),
    raw,
  }
}
