/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string
}

interface AstraScreenSource {
  id: string
  name: string
  kind: "screen" | "window"
  thumbnail: string
}

interface AstraDesktopBridge {
  isDesktop: true
  getVersion: () => Promise<string | null>
  listScreenSources: () => Promise<AstraScreenSource[]>
  selectScreenSource: (sourceId: string, withAudio: boolean) => Promise<boolean>
  loginWithProvider: (provider: "google" | "github") => Promise<boolean>
  setInCall: (inCall: boolean) => void
}

interface Window {
  astraDesktop?: AstraDesktopBridge
}
