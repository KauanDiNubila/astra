import { useEffect } from "react"
import { ASTRA_LOGO_PATH, ASTRA_LOGO_VIEWBOX } from "@/components/AstraLogo"

const APP_NAME = "Astra"
const PLAIN_FAVICON = "/favicon.svg"

// O SVG é montado aqui em vez de desenhar o favicon original num canvas: o
// arquivo original não tem width/height (só viewBox), e nesse caso o
// drawImage falha em alguns navegadores. Se mexer em public/favicon.svg,
// mexer aqui junto.
const [VX, VY, VW, VH] = ASTRA_LOGO_VIEWBOX.split(" ").map(Number)
const MARK_SCALE = (32 * 0.7) / VW
const MARK_X = (32 - VW * MARK_SCALE) / 2 - VX * MARK_SCALE
const MARK_Y = (32 - VH * MARK_SCALE) / 2 - VY * MARK_SCALE

const BADGE_FAVICON =
  "data:image/svg+xml," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">` +
      `<rect width="32" height="32" rx="7" fill="#0a0a0a"/>` +
      `<path transform="translate(${MARK_X} ${MARK_Y}) scale(${MARK_SCALE})" d="${ASTRA_LOGO_PATH}" fill="#fafafa" fill-rule="evenodd"/>` +
      `<circle cx="23.5" cy="8.5" r="8" fill="#0a0a0a"/>` +
      `<circle cx="23.5" cy="8.5" r="6" fill="#ef4444"/>` +
      `</svg>`,
  )

export function useUnreadTabIndicator(unread: number, pageTitle?: string) {
  useEffect(() => {
    const baseTitle = pageTitle ? `${pageTitle} · ${APP_NAME}` : APP_NAME
    document.title = unread > 0 ? `(${unread}) ${baseTitle}` : baseTitle
    const link = document.querySelector<HTMLLinkElement>('link[rel="icon"]')
    if (link) link.href = unread > 0 ? BADGE_FAVICON : PLAIN_FAVICON

    return () => {
      document.title = APP_NAME
    }
  }, [unread, pageTitle])
}
