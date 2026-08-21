import path from "path"
import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

function cspPlugin(): Plugin {
  return {
    name: "astra-csp",
    apply: "build",
    transformIndexHtml(html) {
      const apiOrigin = process.env.VITE_API_URL || "http://localhost:8080"
      const wsOrigin = apiOrigin.replace(/^http/, "ws")
      const csp = [
        "default-src 'self'",
        "script-src 'self'",
        "style-src 'self' 'unsafe-inline'",
        `img-src 'self' data: blob: ${apiOrigin}`,
        "font-src 'self'",
        `connect-src 'self' ${apiOrigin} ${wsOrigin}`,
        "base-uri 'self'",
        "form-action 'self'",
        "object-src 'none'",
        "upgrade-insecure-requests",
      ].join("; ")
      return html.replace(
        "<title>Astra</title>",
        `<title>Astra</title>\n    <meta http-equiv="Content-Security-Policy" content="${csp}" />`,
      )
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), cspPlugin()],
  resolve: {
    alias: {
      "@": path.resolve(import.meta.dirname, "./src"),
    },
  },
  optimizeDeps: {
    include: ["motion/react"],
  },
})
