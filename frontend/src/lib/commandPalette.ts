export const COMMAND_PALETTE_EVENT = "astra:command-palette"

export function openCommandPalette() {
  window.dispatchEvent(new Event(COMMAND_PALETTE_EVENT))
}
