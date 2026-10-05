/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string
}

interface AstraDesktopBridge {
  isDesktop: true
  getVersion: () => Promise<string | null>
  setInCall: (inCall: boolean) => void
}

interface Window {
  astraDesktop?: AstraDesktopBridge
}
