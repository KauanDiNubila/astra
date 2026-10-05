import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import { ThemeProvider } from '@/context/ThemeContext'
import './index.css'
import App from './App.tsx'

if (window.astraDesktop?.customTitleBar) {
  document.documentElement.classList.add('has-titlebar')
  const height = window.astraDesktop.titleBarHeight
  if (height) document.documentElement.style.setProperty('--titlebar-h', `${height}px`)
}

window.addEventListener('vite:preloadError', () => {
  const key = 'astra:preload-reload'
  if (sessionStorage.getItem(key)) return
  sessionStorage.setItem(key, '1')
  window.location.reload()
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>,
)
