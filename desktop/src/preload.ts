import { contextBridge, ipcRenderer } from "electron"

contextBridge.exposeInMainWorld("astraDesktop", {
  isDesktop: true,
  customTitleBar: true,
  titleBarHeight: 24,
  windowControls: {
    minimize: (): void => ipcRenderer.send("astra:window-action", "minimize"),
    toggleMaximize: (): void => ipcRenderer.send("astra:window-action", "toggle-maximize"),
    close: (): void => ipcRenderer.send("astra:window-action", "close"),
    isMaximized: (): Promise<boolean> => ipcRenderer.invoke("astra:window-is-maximized"),
    onMaximizedChange: (callback: (maximized: boolean) => void): (() => void) => {
      const listener = (_event: unknown, maximized: boolean) => callback(maximized)
      ipcRenderer.on("astra:window-maximized", listener)
      return () => ipcRenderer.removeListener("astra:window-maximized", listener)
    },
  },
  getVersion: (): Promise<string | null> => ipcRenderer.invoke("astra:get-version"),
  listScreenSources: (): Promise<unknown[]> => ipcRenderer.invoke("astra:list-screen-sources"),
  selectScreenSource: (sourceId: string, withAudio: boolean): Promise<boolean> =>
    ipcRenderer.invoke("astra:select-screen-source", sourceId, withAudio === true),
  loginWithProvider: (provider: "google" | "github"): Promise<boolean> =>
    ipcRenderer.invoke("astra:social-login", provider),
  setInCall: (inCall: boolean): void => ipcRenderer.send("astra:set-in-call", inCall === true),
})
