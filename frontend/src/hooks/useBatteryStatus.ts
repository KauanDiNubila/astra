import { useEffect, useState } from "react"

type BatteryStatus = { level: number; charging: boolean }

type BatteryManager = {
  level: number
  charging: boolean
  addEventListener: (type: "levelchange" | "chargingchange", listener: () => void) => void
  removeEventListener: (type: "levelchange" | "chargingchange", listener: () => void) => void
}

type NavigatorWithBattery = Navigator & { getBattery?: () => Promise<BatteryManager> }

// Battery Status API: só existe em navegadores baseados em Chromium (Firefox e
// Safari nunca implementaram ou removeram por privacidade) — sem suporte,
// o hook fica em `null` pra sempre, e quem consome esconde o indicador.
export function useBatteryStatus(): BatteryStatus | null {
  const [status, setStatus] = useState<BatteryStatus | null>(null)

  useEffect(() => {
    const nav = navigator as NavigatorWithBattery
    if (!nav.getBattery) return

    let battery: BatteryManager | null = null
    function updateStatus() {
      if (battery) setStatus({ level: battery.level, charging: battery.charging })
    }

    let cancelled = false
    nav.getBattery().then((b) => {
      if (cancelled) return
      battery = b
      updateStatus()
      b.addEventListener("levelchange", updateStatus)
      b.addEventListener("chargingchange", updateStatus)
    })

    return () => {
      cancelled = true
      battery?.removeEventListener("levelchange", updateStatus)
      battery?.removeEventListener("chargingchange", updateStatus)
    }
  }, [])

  return status
}
