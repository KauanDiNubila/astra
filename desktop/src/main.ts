import path from "node:path"
import { app, BrowserWindow, ipcMain, powerSaveBlocker, shell } from "electron"
import type { IpcMainEvent, IpcMainInvokeEvent } from "electron"

const SITE = process.env.ASTRA_URL ?? "https://astra-app.dev"
const SITE_ORIGIN = new URL(SITE).origin

app.commandLine.appendSwitch("disable-features", "CalculateNativeWinOcclusion")

let sleepBlockerId: number | null = null

function setInCall(inCall: boolean) {
  if (inCall && sleepBlockerId === null) {
    sleepBlockerId = powerSaveBlocker.start("prevent-app-suspension")
  } else if (!inCall && sleepBlockerId !== null) {
    powerSaveBlocker.stop(sleepBlockerId)
    sleepBlockerId = null
  }
}

function isFromAstra(event: IpcMainEvent | IpcMainInvokeEvent) {
  const url = event.senderFrame?.url
  return !!url && new URL(url).origin === SITE_ORIGIN
}

ipcMain.on("astra:set-in-call", (event, inCall: unknown) => {
  if (isFromAstra(event)) setInCall(inCall === true)
})

ipcMain.handle("astra:get-version", (event) => (isFromAstra(event) ? app.getVersion() : null))

function openExternal(url: string) {
  if (url.startsWith("https://")) void shell.openExternal(url)
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 900,
    minHeight: 600,
    show: false,
    title: "Astra",
    backgroundColor: "#0a0a0a",
    autoHideMenuBar: true,
    webPreferences: {
      contextIsolation: true,
      sandbox: true,
      nodeIntegration: false,
      backgroundThrottling: false,
      preload: path.join(__dirname, "preload.js"),
    },
  })

  win.once("ready-to-show", () => win.show())
  win.on("closed", () => setInCall(false))

  win.webContents.setWindowOpenHandler(({ url }) => {
    openExternal(url)
    return { action: "deny" }
  })

  win.webContents.on("will-navigate", (event, url) => {
    if (new URL(url).origin !== SITE_ORIGIN) {
      event.preventDefault()
      openExternal(url)
    }
  })

  void win.loadURL(SITE)
}

if (!app.requestSingleInstanceLock()) {
  app.quit()
} else {
  app.on("second-instance", () => {
    const win = BrowserWindow.getAllWindows()[0]
    if (!win) return
    if (win.isMinimized()) win.restore()
    win.focus()
  })
  app.on("window-all-closed", () => app.quit())
  void app.whenReady().then(createWindow)
}
