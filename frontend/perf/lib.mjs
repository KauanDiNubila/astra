import { chromium } from "playwright-core"

export const API = process.env.API ?? "http://localhost:8080"
export const PASSWORD = process.env.PERF_PASSWORD ?? "Xk9$mQ2vN8pL4wR7"
export const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

export function launch(args = []) {
  return chromium.launch({
    executablePath: process.env.CHROME_PATH || undefined,
    headless: true,
    args,
  })
}

export async function api(path, token, method = "GET", body) {
  const res = await fetch(`${API}${path}`, {
    method,
    headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  })
  const text = await res.text()
  return { status: res.status, json: text ? JSON.parse(text) : null }
}

export async function createUser(name, prefix) {
  const email = `${prefix}-${name.toLowerCase()}-${Date.now()}@astra.local`
  const registered = await api("/auth/register", null, "POST", { name, email, password: PASSWORD, acceptTerms: true })
  if (registered.status !== 201) throw new Error(`register ${registered.status} ${JSON.stringify(registered.json)}`)
  const login = await apiLogin(email)
  return { name, email, id: registered.json.id, tag: registered.json.tag, token: login.accessToken }
}

async function apiLogin(email) {
  for (let attempt = 0; attempt < 12; attempt++) {
    const login = await api("/auth/login", null, "POST", { email, password: PASSWORD })
    if (login.status === 200) return login.json
    if (login.status !== 429) throw new Error(`login ${login.status}`)
    await sleep(10000)
  }
  throw new Error("login bloqueado pelo limite de tentativas")
}

export async function befriend(a, b) {
  const sent = await api("/friends", a.token, "POST", { handle: `${b.name}#${b.tag}` })
  if (sent.status !== 201) throw new Error(`friend request ${sent.status}`)
  await api(`/friends/${sent.json.id}/accept`, b.token, "POST")
}

export async function loginInBrowser(page, app, email) {
  for (let attempt = 0; attempt < 12; attempt++) {
    await page.goto(`${app}/login`)
    await page.locator("#email").fill(email)
    await page.locator("#password").fill(PASSWORD)
    const response = page.waitForResponse((r) => r.url().endsWith("/auth/login"), { timeout: 20000 })
    await page.locator('button[type="submit"]').click()
    if ((await response).status() !== 429) {
      await page.waitForURL(/dashboard/, { timeout: 20000 })
      return
    }
    await sleep(10000)
  }
  throw new Error("login bloqueado pelo limite de tentativas")
}
