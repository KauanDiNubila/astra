import { contextBridge, ipcRenderer } from "electron"

contextBridge.exposeInMainWorld("astraDesktop", {
  isDesktop: true,
  getVersion: (): Promise<string | null> => ipcRenderer.invoke("astra:get-version"),
  setInCall: (inCall: boolean): void => ipcRenderer.send("astra:set-in-call", inCall === true),
})
