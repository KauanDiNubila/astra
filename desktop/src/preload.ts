import { contextBridge, ipcRenderer } from "electron"

contextBridge.exposeInMainWorld("astraDesktop", {
  isDesktop: true,
  customTitleBar: true,
  setTitleBarTheme: (theme: "light" | "dark"): void => ipcRenderer.send("astra:set-title-bar-theme", theme),
  getVersion: (): Promise<string | null> => ipcRenderer.invoke("astra:get-version"),
  listScreenSources: (): Promise<unknown[]> => ipcRenderer.invoke("astra:list-screen-sources"),
  selectScreenSource: (sourceId: string, withAudio: boolean): Promise<boolean> =>
    ipcRenderer.invoke("astra:select-screen-source", sourceId, withAudio === true),
  loginWithProvider: (provider: "google" | "github"): Promise<boolean> =>
    ipcRenderer.invoke("astra:social-login", provider),
  setInCall: (inCall: boolean): void => ipcRenderer.send("astra:set-in-call", inCall === true),
})
