// Varredura de tela inteira (esquerda -> direita) ao trocar de tema, via
// View Transitions API. Cai de volta pra troca instantânea em navegadores
// sem suporte (ex.: Firefox) — ver uso em ThemeContext.tsx.

const STYLE_ID = "theme-transition-styles"

const TRANSITION_CSS = `
::view-transition-new(root) {
  animation-name: astra-theme-reveal;
  animation-duration: 0.95s;
  animation-timing-function: cubic-bezier(0.65, 0, 0.35, 1);
}

::view-transition-old(root) {
  animation: none;
  z-index: -1;
}

@keyframes astra-theme-reveal {
  from {
    clip-path: polygon(0% 0%, 0% 0%, 0% 100%, 0% 100%);
  }
  to {
    clip-path: polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%);
  }
}
`

function ensureTransitionStyles() {
  let styleEl = document.getElementById(STYLE_ID) as HTMLStyleElement | null
  if (!styleEl) {
    styleEl = document.createElement("style")
    styleEl.id = STYLE_ID
    document.head.appendChild(styleEl)
  }
  styleEl.textContent = TRANSITION_CSS
}

export function runThemeTransition(applyTheme: () => void) {
  if (!document.startViewTransition || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    applyTheme()
    return
  }
  ensureTransitionStyles()
  // A transição pode legitimamente abortar (aba oculta, cliques rápidos em
  // sequência); o tema já foi trocado pelo applyTheme, então isso nunca deve
  // travar a UI — só evita o rejection sem handler no console.
  document.startViewTransition(applyTheme).ready.catch(() => {})
}
