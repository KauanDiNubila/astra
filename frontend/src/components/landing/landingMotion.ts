import { gsap, ScrollTrigger } from "@/lib/gsap"
import { formatHours } from "@/lib/format"
import { NAV_STEP } from "@/components/landing/AppFrame"

const DESKTOP = "(min-width: 1024px) and (prefers-reduced-motion: no-preference)"
const MOBILE = "(max-width: 1023px) and (prefers-reduced-motion: no-preference)"

type Format = (value: number) => string

const FORMATS: Record<string, Format> = {
  int: (v) => String(Math.round(v)),
  hours: formatHours,
  days: (v) => {
    const n = Math.max(1, Math.round(v))
    return n === 1 ? "1 dia" : `${n} dias`
  },
  clock: (v) => {
    const s = Math.max(0, Math.round(v))
    return `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`
  },
}

function all<T extends Element = HTMLElement>(scope: Element, selector: string) {
  return Array.from(scope.querySelectorAll<T & HTMLElement>(selector))
}

function counter(tl: gsap.core.Timeline, el: HTMLElement, from: number, to: number, format: Format, at: number, duration: number) {
  el.dataset.final ??= el.textContent ?? ""
  const state = { value: from }
  el.textContent = format(from)
  tl.fromTo(
    state,
    { value: from },
    { value: to, duration, ease: "power1.inOut", onUpdate: () => void (el.textContent = format(state.value)) },
    at,
  )
}

function counters(tl: gsap.core.Timeline, screen: Element, at: number, duration: number) {
  all(screen, "[data-counter]").forEach((el) => {
    const format = FORMATS[el.dataset.format ?? "int"]
    counter(tl, el, Number(el.dataset.from ?? 0), Number(el.dataset.counter), format, at, duration)
  })
}

function restoreCounters(root: Element) {
  all(root, "[data-final]").forEach((el) => {
    el.textContent = el.dataset.final ?? ""
  })
}

function pomodoroMotion(screen: Element) {
  const tl = gsap.timeline({ defaults: { ease: "none" } })
  const time = screen.querySelector<HTMLElement>("[data-time]")!
  tl.fromTo(screen.querySelector("[data-ring]"), { strokeDashoffset: 1 }, { strokeDashoffset: 0, duration: 0.8 }, 0)
  counter(tl, time, 25 * 60, 0, FORMATS.clock, 0, 0.8)
  tl.fromTo(
    screen.querySelector("[data-done]"),
    { autoAlpha: 0, y: 10, scale: 0.96 },
    { autoAlpha: 1, y: 0, scale: 1, duration: 0.18, ease: "power2.out" },
    0.78,
  )
  return tl
}

function heatmapMotion(screen: Element) {
  const tl = gsap.timeline({ defaults: { ease: "none" } })
  tl.from(all(screen, "[data-cell]"), { autoAlpha: 0, scale: 0.4, duration: 0.12, ease: "power2.out", stagger: { amount: 0.7 } }, 0)
  counters(tl, screen, 0, 0.82)
  return tl
}

function rankingMotion(screen: Element) {
  const tl = gsap.timeline({ defaults: { ease: "none" } })
  all(screen, "[data-row]").forEach((row) => {
    const shift = Number(row.dataset.shift)
    if (shift !== 0) tl.from(row, { y: shift, duration: 0.55, ease: "power2.inOut" }, 0.2)
  })
  tl.from(screen.querySelector("[data-bar]"), { scaleX: 0.56, duration: 0.6, ease: "power1.inOut" }, 0.15)
  counters(tl, screen, 0.1, 0.5)
  tl.fromTo(screen.querySelector("[data-badge]"), { autoAlpha: 0, scale: 0.6 }, { autoAlpha: 1, scale: 1, duration: 0.15, ease: "back.out(2)" }, 0.75)
  return tl
}

function githubMotion(screen: Element) {
  const tl = gsap.timeline({ defaults: { ease: "none" } })
  tl.from(all(screen, "[data-gbar]"), { scaleY: 0, duration: 0.3, ease: "power2.out", stagger: { amount: 0.45 } }, 0)
  tl.from(all(screen, "[data-lang]"), { scaleX: 0, duration: 0.3, ease: "power2.out", stagger: 0.08 }, 0.4)
  counters(tl, screen, 0, 0.75)
  return tl
}

const SCREEN_MOTIONS = [pomodoroMotion, heatmapMotion, rankingMotion, githubMotion]

function heroIntro(hero: Element, frame: Element | null) {
  const tl = gsap.timeline({ delay: 0.1 })
  tl.from(all(hero, "[data-hero-fade]"), { autoAlpha: 0, y: 24, duration: 0.9, ease: "power3.out", stagger: 0.08, clearProps: "transform,opacity,visibility" })
  if (frame) tl.from(frame, { autoAlpha: 0, y: 60, duration: 1.1, ease: "power3.out", clearProps: "transform,opacity,visibility" }, 0.35)
  return tl
}

function desktopShowcase(root: Element) {
  const stage = root.querySelector<HTMLElement>("[data-stage]")
  if (!stage) return
  const hero = stage.querySelector<HTMLElement>("[data-stage-hero]")!
  const heroEnd = stage.querySelector<HTMLElement>("[data-hero-end]")!
  const slot = stage.querySelector<HTMLElement>("[data-frame-slot]")!
  const frame = stage.querySelector<HTMLElement>("[data-frame]")!
  const screens = all(stage, "[data-screen]")
  const chapters = all(stage, "[data-chapter]")
  const bars = all(stage, "[data-progress-bar]")
  const icons = all(stage, "[data-nav-icon]")
  const indicator = stage.querySelector("[data-indicator]")

  heroIntro(hero, stage.querySelector("[data-frame-intro]"))

  const start = () => {
    const scale = Math.min(1.3, (stage.offsetWidth * 0.84) / slot.offsetWidth)
    return {
      x: stage.offsetWidth / 2 - (slot.offsetLeft + slot.offsetWidth / 2),
      y: hero.offsetTop + heroEnd.offsetTop + heroEnd.offsetHeight + 56 - slot.offsetTop,
      scale,
    }
  }

  gsap.set(frame, { transformOrigin: "50% 0%" })

  const tl = gsap.timeline({
    defaults: { ease: "none" },
    scrollTrigger: {
      trigger: stage,
      start: "top top",
      end: () => `+=${window.innerHeight * 4.8}`,
      pin: true,
      scrub: 0.8,
      invalidateOnRefresh: true,
    },
  })

  tl.set(screens.slice(1), { autoAlpha: 0 }, 0)
  tl.set(chapters.flatMap((c) => all(c, "[data-word]")), { yPercent: 110 }, 0)
  tl.set(chapters.flatMap((c) => all(c, "[data-chapter-fade]")), { autoAlpha: 0, y: 14 }, 0)
  tl.set(chapters, { autoAlpha: 0 }, 0)
  tl.set(bars, { scaleX: 0, transformOrigin: "0% 50%" }, 0)
  tl.set(stage.querySelector("[data-progress]"), { autoAlpha: 0 }, 0)

  tl.fromTo(
    frame,
    { x: () => start().x, y: () => start().y, scale: () => start().scale },
    { x: 0, y: 0, scale: 1, duration: 1, ease: "power2.inOut" },
    0,
  )
  tl.to(hero, { y: -90, autoAlpha: 0, duration: 0.5, ease: "power1.in", force3D: false }, 0)

  const chapterIn = (i: number, at: number) => {
    tl.set(chapters[i], { autoAlpha: 1 }, at)
    tl.to(all(chapters[i], "[data-word]"), { yPercent: 0, duration: 0.3, ease: "power3.out", stagger: 0.015 }, at)
    tl.to(all(chapters[i], "[data-chapter-fade]"), { autoAlpha: 1, y: 0, duration: 0.3, ease: "power2.out", stagger: 0.04 }, at + 0.06)
  }

  const chapterOut = (i: number, at: number) => {
    tl.to(all(chapters[i], "[data-word]"), { yPercent: -110, duration: 0.22, ease: "power2.in", stagger: 0.008 }, at)
    tl.to(all(chapters[i], "[data-chapter-fade]"), { autoAlpha: 0, y: -10, duration: 0.18, ease: "power2.in" }, at)
    tl.set(chapters[i], { autoAlpha: 0 }, at + 0.32)
  }

  const switchScreen = (from: number, to: number, at: number) => {
    tl.to(screens[from], { autoAlpha: 0, scale: 0.97, duration: 0.22, ease: "power2.in" }, at)
    tl.fromTo(screens[to], { autoAlpha: 0, scale: 1.03 }, { autoAlpha: 1, scale: 1, duration: 0.3, ease: "power2.out" }, at + 0.12)
    tl.to(indicator, { y: to * NAV_STEP, duration: 0.34, ease: "power2.inOut" }, at)
    tl.to(icons[from], { opacity: 0.4, duration: 0.2 }, at)
    tl.to(icons[to], { opacity: 1, duration: 0.2 }, at + 0.12)
  }

  chapterIn(0, 0.62)
  tl.to(stage.querySelector("[data-progress]"), { autoAlpha: 1, duration: 0.25 }, 0.7)
  switchScreen(0, 1, 0.72)

  SCREEN_MOTIONS.forEach((motion, i) => {
    const at = 1.05 + i * 1.15
    tl.add(motion(screens[i + 1]), at)
    tl.to(bars[i], { scaleX: 1, duration: 1.05 }, at - 0.05)
    if (i < SCREEN_MOTIONS.length - 1) {
      chapterOut(i, at + 0.9)
      switchScreen(i + 1, i + 2, at + 0.92)
      chapterIn(i + 1, at + 1.04)
    }
  })

  tl.to({}, { duration: 0.35 })
}

function mobileShowcase(root: Element) {
  const hero = root.querySelector("[data-mobile-hero]")
  if (hero) heroIntro(hero, hero.querySelector("[data-frame-intro]"))

  all(root, "[data-mchapter]").forEach((block, i) => {
    const tl = gsap.timeline({ scrollTrigger: { trigger: block, start: "top 75%", toggleActions: "play none none none" } })
    tl.from(all(block, "[data-word]"), { yPercent: 110, duration: 0.9, ease: "expo.out", stagger: 0.04 })
    tl.from(all(block, "[data-chapter-fade]"), { autoAlpha: 0, y: 16, duration: 0.6, ease: "power3.out", stagger: 0.06 }, 0.15)
    tl.from(block.querySelector("[data-mframe]"), { autoAlpha: 0, y: 40, duration: 0.9, ease: "expo.out" }, 0.2)
    const screen = block.querySelector("[data-screen]")
    if (screen) tl.add(SCREEN_MOTIONS[i](screen).timeScale(0.6), 0.45)
  })
}

function sharedSections(root: Element) {
  const cards = all(root, "[data-feature-card], [data-cta-card]")
  gsap.set(cards, { autoAlpha: 0, y: 24 })
  ScrollTrigger.batch(cards, {
    start: "top 90%",
    once: true,
    onEnter: (batch) => gsap.to(batch, { autoAlpha: 1, y: 0, duration: 0.7, ease: "power3.out", stagger: 0.08 }),
  })
}

export function setupLandingMotion(root: HTMLElement) {
  const mm = gsap.matchMedia()

  mm.add({ desktop: DESKTOP, mobile: MOBILE }, (context) => {
    const { desktop, mobile } = context.conditions as { desktop: boolean; mobile: boolean }
    if (!desktop && !mobile) return
    if (desktop) desktopShowcase(root)
    else mobileShowcase(root)
    sharedSections(root)
    return () => restoreCounters(root)
  })

  let alive = true
  document.fonts?.ready.then(() => {
    if (alive) ScrollTrigger.refresh()
  })

  return () => {
    alive = false
    mm.revert()
  }
}
