import { useEffect, useRef, useState } from "react"
import type { RefObject } from "react"
import { createPortal } from "react-dom"

const FINE_POINTER = "(hover: hover) and (pointer: fine)"
const TRACK_WIDTH = 12
const EDGE_PADDING = 8
const EDGE_REACH = 20
const MIN_THUMB = 32
const HIDE_DELAY = 900

interface OverlayScrollbarProps {
  target?: RefObject<HTMLElement | null>
  zIndex?: number
}

interface Area {
  top: number
  bottom: number
  right: number
  scrollTop: number
  scrollHeight: number
  clientHeight: number
}

function titleBarBottom() {
  return document.querySelector('[data-slot="desktop-title-bar"]')?.getBoundingClientRect().bottom ?? 0
}

function readArea(scroller: HTMLElement | null): Area | null {
  if (!scroller) {
    if (document.body.style.overflow === "hidden") return null
    const doc = document.documentElement
    return {
      top: 0,
      bottom: window.innerHeight,
      right: window.innerWidth,
      scrollTop: window.scrollY,
      scrollHeight: doc.scrollHeight,
      clientHeight: doc.clientHeight,
    }
  }
  const rect = scroller.getBoundingClientRect()
  return {
    top: rect.top,
    bottom: rect.bottom,
    right: rect.right,
    scrollTop: scroller.scrollTop,
    scrollHeight: scroller.scrollHeight,
    clientHeight: scroller.clientHeight,
  }
}

export function OverlayScrollbar({ target, zIndex = 20 }: OverlayScrollbarProps) {
  const track = useRef<HTMLDivElement>(null)
  const thumb = useRef<HTMLDivElement>(null)
  const [enabled] = useState(() => window.matchMedia(FINE_POINTER).matches)

  useEffect(() => {
    if (!enabled) return
    const trackEl = track.current
    const thumbEl = thumb.current
    if (!trackEl || !thumbEl) return
    const scroller = target ? (target.current ?? null) : null
    if (target && !scroller) return
    const eventTarget: HTMLElement | Window = scroller ?? window

    let trackTop = 0
    let trackHeight = 0
    let thumbHeight = 0
    let dragOffset = 0
    let dragging = false
    let hovering = false
    let hideTimer: ReturnType<typeof setTimeout> | undefined

    function layout() {
      const area = readArea(scroller)
      const overflow = area ? area.scrollHeight - area.clientHeight : 0
      if (!area || overflow <= 1) {
        trackEl!.style.display = "none"
        return null
      }
      trackEl!.style.display = ""
      trackTop = Math.max(area.top, titleBarBottom()) + EDGE_PADDING
      trackHeight = Math.max(0, area.bottom - EDGE_PADDING - trackTop)
      thumbHeight = Math.min(trackHeight, Math.max(MIN_THUMB, trackHeight * (area.clientHeight / area.scrollHeight)))
      trackEl!.style.top = `${trackTop}px`
      trackEl!.style.left = `${area.right - TRACK_WIDTH - 2}px`
      trackEl!.style.height = `${trackHeight}px`
      thumbEl!.style.height = `${thumbHeight}px`
      const fraction = Math.min(1, Math.max(0, area.scrollTop / overflow))
      thumbEl!.style.transform = `translateY(${fraction * (trackHeight - thumbHeight)}px)`
      return area
    }

    function setVisible(visible: boolean) {
      trackEl!.style.opacity = visible ? "1" : "0"
      trackEl!.style.pointerEvents = visible ? "auto" : "none"
    }

    function scheduleHide() {
      clearTimeout(hideTimer)
      hideTimer = setTimeout(() => {
        if (!dragging && !hovering) setVisible(false)
      }, HIDE_DELAY)
    }

    function reveal() {
      if (!layout()) return
      setVisible(true)
      scheduleHide()
    }

    function scrollToFraction(fraction: number, smooth: boolean) {
      const area = readArea(scroller)
      if (!area) return
      const top = Math.min(1, Math.max(0, fraction)) * (area.scrollHeight - area.clientHeight)
      if (scroller) scroller.scrollTo({ top, behavior: smooth ? "smooth" : "instant" })
      else window.scrollTo({ top, behavior: smooth ? "smooth" : "instant" })
    }

    function fractionFor(clientY: number) {
      const span = trackHeight - thumbHeight
      return span > 0 ? (clientY - trackTop - dragOffset) / span : 0
    }

    function onPointerDown(event: PointerEvent) {
      event.preventDefault()
      dragging = true
      trackEl!.dataset.dragging = "true"
      trackEl!.setPointerCapture(event.pointerId)
      const thumbRect = thumbEl!.getBoundingClientRect()
      if (event.target === thumbEl) {
        dragOffset = event.clientY - thumbRect.top
      } else {
        dragOffset = thumbHeight / 2
        scrollToFraction(fractionFor(event.clientY), true)
      }
    }

    function onPointerMove(event: PointerEvent) {
      if (!dragging) return
      scrollToFraction(fractionFor(event.clientY), false)
    }

    function onPointerUp() {
      dragging = false
      delete trackEl!.dataset.dragging
      scheduleHide()
    }

    function onEnter() {
      hovering = true
      clearTimeout(hideTimer)
    }

    function onLeave() {
      hovering = false
      scheduleHide()
    }

    function onNearEdge(event: Event) {
      const area = readArea(scroller)
      if (area && (event as PointerEvent).clientX >= area.right - EDGE_REACH) reveal()
    }

    let frame = 0
    function refresh() {
      cancelAnimationFrame(frame)
      frame = requestAnimationFrame(() => {
        if (trackEl!.style.opacity === "1") layout()
        else if (!layout()) setVisible(false)
      })
    }

    const resizeObserver = new ResizeObserver(refresh)
    const watched = new Set<Element>()
    function watchChildren() {
      const host = scroller ?? document.body
      resizeObserver.observe(scroller ?? document.documentElement)
      for (const child of Array.from(host.children)) {
        if (!watched.has(child)) {
          watched.add(child)
          resizeObserver.observe(child)
        }
      }
    }
    watchChildren()
    const mutationObserver = new MutationObserver(() => {
      watchChildren()
      refresh()
    })
    mutationObserver.observe(scroller ?? document.body, { childList: true })

    eventTarget.addEventListener("scroll", reveal, { passive: true })
    eventTarget.addEventListener("pointermove", onNearEdge, { passive: true })
    window.addEventListener("resize", refresh)
    trackEl.addEventListener("pointerdown", onPointerDown)
    trackEl.addEventListener("pointermove", onPointerMove)
    trackEl.addEventListener("pointerup", onPointerUp)
    trackEl.addEventListener("pointercancel", onPointerUp)
    trackEl.addEventListener("pointerenter", onEnter)
    trackEl.addEventListener("pointerleave", onLeave)

    return () => {
      cancelAnimationFrame(frame)
      clearTimeout(hideTimer)
      resizeObserver.disconnect()
      mutationObserver.disconnect()
      eventTarget.removeEventListener("scroll", reveal)
      eventTarget.removeEventListener("pointermove", onNearEdge)
      window.removeEventListener("resize", refresh)
      trackEl.removeEventListener("pointerdown", onPointerDown)
      trackEl.removeEventListener("pointermove", onPointerMove)
      trackEl.removeEventListener("pointerup", onPointerUp)
      trackEl.removeEventListener("pointercancel", onPointerUp)
      trackEl.removeEventListener("pointerenter", onEnter)
      trackEl.removeEventListener("pointerleave", onLeave)
    }
  }, [enabled, target])

  if (!enabled) return null

  return createPortal(
    <div
      ref={track}
      aria-hidden
      data-slot="overlay-scrollbar"
      style={{ zIndex, width: TRACK_WIDTH, display: "none", opacity: 0, pointerEvents: "none" }}
      className="group fixed cursor-pointer touch-none transition-opacity duration-500"
    >
      <div className="absolute inset-y-0 right-1 w-[3px] rounded-full bg-foreground/[0.06]" />
      <div
        ref={thumb}
        className="absolute right-1 w-[3px] cursor-grab rounded-full bg-foreground/35 transition-[width,background-color] duration-150 group-hover:w-[6px] group-hover:bg-foreground/55 group-data-[dragging=true]:w-[6px] group-data-[dragging=true]:cursor-grabbing group-data-[dragging=true]:bg-foreground/60"
      />
    </div>,
    document.body,
  )
}
