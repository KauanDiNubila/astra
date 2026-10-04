const STORAGE_KEY = "astra:call-devices"

export type DevicePrefs = {
  micId: string
  cameraId: string
  speakerId: string
}

export type DeviceLists = {
  mics: MediaDeviceInfo[]
  cameras: MediaDeviceInfo[]
  speakers: MediaDeviceInfo[]
}

export function loadDevicePrefs(): DevicePrefs {
  const empty = { micId: "", cameraId: "", speakerId: "" }
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? { ...empty, ...JSON.parse(raw) } : empty
  } catch {
    return empty
  }
}

export function saveDevicePrefs(prefs: DevicePrefs) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
  } catch {
    return
  }
}

export async function listDevices(): Promise<DeviceLists> {
  if (!navigator.mediaDevices?.enumerateDevices) return { mics: [], cameras: [], speakers: [] }
  const devices = await navigator.mediaDevices.enumerateDevices()
  const usable = (kind: MediaDeviceKind) =>
    devices.filter((d) => d.kind === kind && d.deviceId !== "communications" && d.deviceId !== "")
  return {
    mics: usable("audioinput"),
    cameras: usable("videoinput"),
    speakers: usable("audiooutput"),
  }
}

export function supportsSpeakerSelection() {
  return typeof HTMLMediaElement !== "undefined" && "setSinkId" in HTMLMediaElement.prototype
}

export function deviceLabel(device: MediaDeviceInfo, index: number, fallback: string) {
  return device.label || `${fallback} ${index + 1}`
}
