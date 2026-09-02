const STORAGE_KEY = "astra:chat-sound"

// localStorage (e não sessionStorage): silenciar o som é uma preferência, não
// um estado de sessão — quem desligou espera continuar desligado depois.
export function loadChatSoundEnabled(): boolean {
  if (typeof window === "undefined") return true
  try {
    return localStorage.getItem(STORAGE_KEY) !== "off"
  } catch {
    return true
  }
}

export function saveChatSoundEnabled(enabled: boolean) {
  try {
    localStorage.setItem(STORAGE_KEY, enabled ? "on" : "off")
  } catch {
    // storage indisponível (aba anônima restrita) não deve quebrar o chat
  }
}
