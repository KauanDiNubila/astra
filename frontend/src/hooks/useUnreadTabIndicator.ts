import { useEffect } from "react"

const BASE_TITLE = "Astra"
const PLAIN_FAVICON = "/favicon.svg"

// O SVG é montado aqui em vez de desenhar o favicon original num canvas: o
// arquivo original não tem width/height (só viewBox), e nesse caso o
// drawImage falha em alguns navegadores. Se mexer em public/favicon.svg,
// mexer aqui junto.
const BADGE_FAVICON =
  "data:image/svg+xml," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <rect width="32" height="32" rx="7" fill="#0a0a0a"/>
      <path d="M16 3C16.8 11 21 15.2 29 16C21 16.8 16.8 21 16 29C15.2 21 11 16.8 3 16C11 15.2 15.2 11 16 3Z" fill="#fafafa"/>
      <circle cx="23.5" cy="8.5" r="8" fill="#0a0a0a"/>
      <circle cx="23.5" cy="8.5" r="6" fill="#ef4444"/>
    </svg>`.replace(/\s+/g, " "),
  )

export function useUnreadTabIndicator(unread: number) {
  useEffect(() => {
    document.title = unread > 0 ? `(${unread}) ${BASE_TITLE}` : BASE_TITLE
    const link = document.querySelector<HTMLLinkElement>('link[rel="icon"]')
    if (link) link.href = unread > 0 ? BADGE_FAVICON : PLAIN_FAVICON

    return () => {
      document.title = BASE_TITLE
    }
  }, [unread])
}
