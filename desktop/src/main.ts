import path from "node:path"
import { app, BrowserWindow, desktopCapturer, ipcMain, powerSaveBlocker, session, shell } from "electron"
import type { IpcMainEvent, IpcMainInvokeEvent } from "electron"
import { findDeepLink, handleDeepLink, startSocialLogin } from "./desktop-login"
import { startAutoUpdate } from "./updater"

const SITE = process.env.ASTRA_URL ?? "https://astra-app.dev"
const SITE_ORIGIN = new URL(SITE).origin

app.setAppUserModelId("dev.astra.app")
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

ipcMain.handle("astra:social-login", (event, provider: unknown) => isFromAstra(event) && startSocialLogin(provider))

ipcMain.handle("astra:get-version", (event) => (isFromAstra(event) ? app.getVersion() : null))

interface ShareChoice {
  sourceId: string
  withAudio: boolean
}

let pendingShare: ShareChoice | null = null

ipcMain.handle("astra:list-screen-sources", async (event) => {
  if (!isFromAstra(event)) return []
  const sources = await desktopCapturer.getSources({
    types: ["screen", "window"],
    thumbnailSize: { width: 320, height: 180 },
    fetchWindowIcons: false,
  })
  return sources.map((source) => ({
    id: source.id,
    name: source.name,
    kind: source.id.startsWith("screen:") ? "screen" : "window",
    thumbnail: source.thumbnail.toDataURL(),
  }))
})

ipcMain.handle("astra:select-screen-source", (event, sourceId: unknown, withAudio: unknown) => {
  if (!isFromAstra(event) || typeof sourceId !== "string") return false
  pendingShare = { sourceId, withAudio: withAudio === true }
  return true
})

function installDisplayMediaHandler() {
  session.defaultSession.setDisplayMediaRequestHandler(
    async (request, callback) => {
      const choice = pendingShare
      pendingShare = null
      const frameUrl = request.frame?.url
      if (!choice || !frameUrl || new URL(frameUrl).origin !== SITE_ORIGIN) {
        callback({})
        return
      }
      const sources = await desktopCapturer.getSources({ types: ["screen", "window"] })
      const video = sources.find((source) => source.id === choice.sourceId)
      if (!video) {
        callback({})
        return
      }
      callback(choice.withAudio ? { video, audio: "loopback" } : { video })
    },
    { useSystemPicker: false },
  )
}

const TITLE_BAR_HEIGHT = 32

const TITLE_BAR_THEMES = {
  dark: { color: "#171717", symbolColor: "#fafafa" },
  light: { color: "#fafafa", symbolColor: "#0a0a0a" },
} as const

type TitleBarTheme = keyof typeof TITLE_BAR_THEMES

function isTitleBarTheme(value: unknown): value is TitleBarTheme {
  return value === "dark" || value === "light"
}

ipcMain.on("astra:set-title-bar-theme", (event, theme: unknown) => {
  if (!isFromAstra(event) || !isTitleBarTheme(theme)) return
  const win = BrowserWindow.fromWebContents(event.sender)
  win?.setTitleBarOverlay({ ...TITLE_BAR_THEMES[theme], height: TITLE_BAR_HEIGHT })
})

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
    titleBarStyle: "hidden",
    titleBarOverlay: { ...TITLE_BAR_THEMES.dark, height: TITLE_BAR_HEIGHT },
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
  app.on("second-instance", (_event, commandLine) => {
    const win = BrowserWindow.getAllWindows()[0]
    if (!win) return
    if (win.isMinimized()) win.restore()
    win.focus()
    const link = findDeepLink(commandLine)
    if (link) void handleDeepLink(link, win, SITE)
  })
  app.on("window-all-closed", () => app.quit())
  void app.whenReady().then(() => {
    if (app.isPackaged) app.setAsDefaultProtocolClient("astra")
    installDisplayMediaHandler()
    createWindow()
    startAutoUpdate()
  })
}
