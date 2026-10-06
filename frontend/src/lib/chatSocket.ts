import { Client } from "@stomp/stompjs"
import { getAccessToken, refreshSession } from "@/lib/api"

const apiBaseURL = import.meta.env.VITE_API_URL || "http://localhost:8080"
const brokerURL = apiBaseURL.replace(/^http/, "ws") + "/ws"

const REFRESH_MARGIN_MS = 60_000

function expiresAt(token: string) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")))
    return typeof payload.exp === "number" ? payload.exp * 1000 : 0
  } catch {
    return 0
  }
}

async function freshAccessToken() {
  const token = getAccessToken()
  if (token && expiresAt(token) - Date.now() > REFRESH_MARGIN_MS) return token
  try {
    return (await refreshSession()).accessToken
  } catch {
    return token
  }
}

export function createChatClient() {
  const client = new Client({
    brokerURL,
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  })
  client.beforeConnect = async () => {
    client.connectHeaders = { Authorization: `Bearer ${await freshAccessToken()}` }
  }
  return client
}
