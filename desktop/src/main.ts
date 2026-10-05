import { app, BrowserWindow, shell } from "electron"

const SITE = process.env.ASTRA_URL ?? "https://astra-app.dev"
const SITE_ORIGIN = new URL(SITE).origin

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
    },
  })

  win.once("ready-to-show", () => win.show())

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
