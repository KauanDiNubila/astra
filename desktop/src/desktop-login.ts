import crypto from "node:crypto"
import { BrowserWindow, Notification, session, shell } from "electron"

const API_URL = process.env.ASTRA_API_URL ?? "https://api.astra-app.dev"
const LOGIN_TIMEOUT_MS = 10 * 60 * 1000
const PROVIDERS = new Set(["google", "github"])

interface PendingLogin {
  verifier: string
  startedAt: number
}

let pending: PendingLogin | null = null

export function startSocialLogin(provider: unknown): boolean {
  if (typeof provider !== "string" || !PROVIDERS.has(provider)) return false
  const verifier = crypto.randomBytes(32).toString("base64url")
  const challenge = crypto.createHash("sha256").update(verifier).digest("base64url")
  pending = { verifier, startedAt: Date.now() }
  void shell.openExternal(`${API_URL}/auth/desktop/login?provider=${provider}&challenge=${challenge}`)
  return true
}

export function findDeepLink(args: string[]): string | undefined {
  return args.find((arg) => arg.startsWith("astra://"))
}

function notifyFailure() {
  new Notification({ title: "Astra", body: "Não foi possível entrar. Tente de novo." }).show()
}

export async function handleDeepLink(link: string, window: BrowserWindow | undefined, siteUrl: string) {
  let url: URL
  try {
    url = new URL(link)
  } catch {
    return
  }
  if (url.protocol !== "astra:" || url.hostname !== "auth") return

  const login = pending
  pending = null
  const code = url.searchParams.get("code")
  if (!code || !login || Date.now() - login.startedAt > LOGIN_TIMEOUT_MS) return

  try {
    const response = await session.defaultSession.fetch(`${API_URL}/auth/desktop/exchange`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, verifier: login.verifier }),
      credentials: "include",
    })
    if (!response.ok) {
      notifyFailure()
      return
    }
  } catch {
    notifyFailure()
    return
  }

  if (!window || window.isDestroyed()) return
  void window.loadURL(`${siteUrl}/dashboard`)
  if (window.isMinimized()) window.restore()
  window.focus()
}
