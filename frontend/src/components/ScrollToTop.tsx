import { useLayoutEffect } from "react"
import { useLocation, useNavigationType } from "react-router-dom"

export function ScrollToTop() {
  const { pathname } = useLocation()
  const navigationType = useNavigationType()

  useLayoutEffect(() => {
    if (navigationType !== "POP") window.scrollTo(0, 0)
  }, [pathname, navigationType])

  return null
}
