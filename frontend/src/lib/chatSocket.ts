import { Client } from "@stomp/stompjs"
import { getAccessToken } from "@/lib/api"

const apiBaseURL = import.meta.env.VITE_API_URL || "http://localhost:8080"
const brokerURL = apiBaseURL.replace(/^http/, "ws") + "/ws"

export function createChatClient() {
  const client = new Client({
    brokerURL,
    reconnectDelay: 5000,
  })
  client.beforeConnect = () => {
    client.connectHeaders = { Authorization: `Bearer ${getAccessToken()}` }
  }
  return client
}
