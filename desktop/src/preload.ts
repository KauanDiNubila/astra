import { contextBridge, ipcRenderer } from "electron"

contextBridge.exposeInMainWorld("astraDesktop", {
  isDesktop: true,
  getVersion: (): Promise<string | null> => ipcRenderer.invoke("astra:get-version"),
  listScreenSources: (): Promise<unknown[]> => ipcRenderer.invoke("astra:list-screen-sources"),
  selectScreenSource: (sourceId: string, withAudio: boolean): Promise<boolean> =>
    ipcRenderer.invoke("astra:select-screen-source", sourceId, withAudio === true),
  setInCall: (inCall: boolean): void => ipcRenderer.send("astra:set-in-call", inCall === true),
})
