import { Client } from "@stomp/stompjs"
import { getToken } from "@/lib/api"

const apiBaseURL = import.meta.env.VITE_API_URL || "http://localhost:8080"
const brokerURL = apiBaseURL.replace(/^http/, "ws") + "/ws"

export function createChatClient() {
  return new Client({
    brokerURL,
    connectHeaders: {
      Authorization: `Bearer ${getToken()}`,
    },
    reconnectDelay: 5000,
  })
}
