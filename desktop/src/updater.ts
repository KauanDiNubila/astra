import { app, Notification } from "electron"
import { autoUpdater } from "electron-updater"

const CHECK_INTERVAL_MS = 4 * 60 * 60 * 1000

function checkNow() {
  autoUpdater.checkForUpdates().catch((error: Error) => console.error("[updater] falhou:", error.message))
}

export function startAutoUpdate() {
  if (process.windowsStore) return
  const testFeed = process.env.ASTRA_UPDATE_URL
  if (!app.isPackaged && !testFeed) return

  if (testFeed) autoUpdater.setFeedURL({ provider: "generic", url: testFeed })
  autoUpdater.autoDownload = true
  autoUpdater.autoInstallOnAppQuit = !testFeed

  autoUpdater.on("update-available", (info) => console.log("[updater] versão disponível:", info.version))
  autoUpdater.on("update-not-available", () => console.log("[updater] já está na versão mais recente"))
  autoUpdater.on("error", (error) => console.error("[updater] erro:", error.message))
  autoUpdater.on("update-downloaded", (info) => {
    console.log("[updater] baixada:", info.version)
    new Notification({
      title: "Astra atualizado",
      body: `A versão ${info.version} será instalada quando você fechar o app.`,
    }).show()
  })

  checkNow()
  setInterval(checkNow, CHECK_INTERVAL_MS)
}
