import * as React from "react"
import { motion, useMotionValue, useReducedMotion, useSpring } from "motion/react"

import { cn } from "@/lib/utils"

const PASSWORD_CHAR =
  typeof navigator !== "undefined" && navigator.userAgent.match(/firefox|fxios/i) ? "●" : "•"

const CARET_SPRING = { stiffness: 500, damping: 30, mass: 0.5 }
const CARET_SPRING_REDUCED = { stiffness: 10000, damping: 100, mass: 0.1 }

// Só esses tipos suportam a Selection API (selectionStart/selectionEnd) — em
// qualquer outro (email, number, date...) o navegador sempre devolve null,
// então o caret customizado ficaria preso no início. Nesses, cai pro nativo.
const SELECTION_SUPPORTED_TYPES = new Set(["text", "search", "tel", "password", "url", undefined])

const INPUT_CLASS =
  "h-8 w-full min-w-0 rounded-lg border border-input bg-transparent px-2.5 py-1 text-base transition-colors outline-none file:inline-flex file:h-6 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-input/50 disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm dark:bg-input/30 dark:disabled:bg-input/80 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40"

function Input({ className, type, style, value, defaultValue, onChange, onBlur, ...props }: React.ComponentProps<"input">) {
  const smoothCaret = SELECTION_SUPPORTED_TYPES.has(type)

  const [internalValue, setInternalValue] = React.useState(defaultValue ?? "")
  const isControlled = value !== undefined
  const inputValue = isControlled ? value : internalValue

  const containerRef = React.useRef<HTMLDivElement>(null)
  const inputRef = React.useRef<HTMLInputElement>(null)
  const measureRef = React.useRef<HTMLSpanElement>(null)

  const caretX = useMotionValue(0)
  const caretOpacity = useMotionValue(0)
  const prefersReducedMotion = useReducedMotion()
  const springCaretX = useSpring(caretX, prefersReducedMotion ? CARET_SPRING_REDUCED : CARET_SPRING)

  const syncMeasureSpan = () => {
    const input = inputRef.current
    const measureSpan = measureRef.current
    if (!input || !measureSpan) return
    const styles = window.getComputedStyle(input)
    measureSpan.style.font = `${styles.fontStyle} ${styles.fontWeight} ${styles.fontSize} ${styles.fontFamily}`
    measureSpan.style.letterSpacing = styles.letterSpacing
  }

  const measurePrefixWidth = (text: string) => {
    const input = inputRef.current
    const measureSpan = measureRef.current
    if (!input || !measureSpan) return null
    syncMeasureSpan()
    measureSpan.textContent = text
    const paddingLeft = parseFloat(window.getComputedStyle(input).paddingLeft) || 0
    return text.length > 0 ? measureSpan.offsetWidth + paddingLeft : paddingLeft - 1
  }

  const scrollCaretIntoView = (target: HTMLInputElement, absoluteWidth: number) => {
    const styles = window.getComputedStyle(target)
    const paddingLeft = parseFloat(styles.paddingLeft) || 0
    const paddingRight = parseFloat(styles.paddingRight) || 0
    const maxScroll = Math.max(0, target.scrollWidth - target.clientWidth)
    const visibleRight = target.scrollLeft + target.clientWidth - paddingRight
    const visibleLeft = target.scrollLeft + paddingLeft

    if (absoluteWidth > visibleRight) {
      target.scrollLeft = Math.min(absoluteWidth - target.clientWidth + paddingRight, maxScroll)
      return
    }
    if (absoluteWidth < visibleLeft) {
      target.scrollLeft = Math.max(0, absoluteWidth - paddingLeft)
    }
  }

  const getCaretIndex = (target: HTMLInputElement) => {
    const selectionStart = target.selectionStart ?? 0
    const selectionEnd = target.selectionEnd ?? 0
    if (selectionStart === selectionEnd) return selectionStart
    return target.selectionDirection === "backward" ? selectionStart : selectionEnd
  }

  const updateCaret = (target: HTMLInputElement) => {
    const selectionStart = target.selectionStart ?? 0
    const selectionEnd = target.selectionEnd ?? 0
    const hasSelection = selectionStart !== selectionEnd
    const caretIndex = getCaretIndex(target)
    const isPassword = target.type === "password"
    const textBeforeCaret = isPassword ? PASSWORD_CHAR.repeat(caretIndex) : target.value.slice(0, caretIndex)

    const absoluteWidth = measurePrefixWidth(textBeforeCaret)
    if (absoluteWidth === null) return

    scrollCaretIntoView(target, absoluteWidth)

    const styles = window.getComputedStyle(target)
    const paddingLeft = parseFloat(styles.paddingLeft) || 0
    const paddingRight = parseFloat(styles.paddingRight) || 0
    const caretPosition = absoluteWidth - target.scrollLeft
    const minX = paddingLeft - 1
    const maxX = target.clientWidth - paddingRight
    const isCaretVisible = caretPosition >= minX && caretPosition <= maxX + 1

    caretX.set(Math.min(caretPosition, maxX))

    if (!isCaretVisible || hasSelection) {
      caretOpacity.set(0)
      return
    }
    caretOpacity.set(1)
  }

  const updateCaretRef = React.useRef(updateCaret)
  updateCaretRef.current = updateCaret

  React.useEffect(() => {
    if (!smoothCaret) return
    const input = inputRef.current
    if (input && document.activeElement === input) updateCaretRef.current(input)
  }, [inputValue, smoothCaret])

  React.useEffect(() => {
    if (!smoothCaret) return
    const input = inputRef.current
    const container = containerRef.current
    if (!input || !container) return

    const updateIfFocused = () => {
      if (document.activeElement === input) updateCaretRef.current(input)
    }
    const handleSelectionChange = () => {
      if (document.activeElement !== input) return
      requestAnimationFrame(() => {
        if (document.activeElement === input) updateCaretRef.current(input)
      })
    }

    document.addEventListener("selectionchange", handleSelectionChange)
    document.fonts.addEventListener("loadingdone", updateIfFocused)
    void document.fonts.ready.then(updateIfFocused)
    input.addEventListener("scroll", updateIfFocused)

    const resizeObserver = new ResizeObserver(updateIfFocused)
    resizeObserver.observe(container)

    return () => {
      document.removeEventListener("selectionchange", handleSelectionChange)
      document.fonts.removeEventListener("loadingdone", updateIfFocused)
      input.removeEventListener("scroll", updateIfFocused)
      resizeObserver.disconnect()
    }
  }, [smoothCaret])

  if (!smoothCaret) {
    return (
      <input
        type={type}
        data-slot="input"
        className={cn(INPUT_CLASS, className)}
        style={style}
        value={value}
        defaultValue={defaultValue}
        onChange={onChange}
        onBlur={onBlur}
        {...props}
      />
    )
  }

  return (
    <div ref={containerRef} className="relative grid grid-cols-1" style={{ caretColor: "transparent" }}>
      <input
        {...props}
        ref={inputRef}
        type={type}
        data-slot="input"
        className={cn(INPUT_CLASS, "col-start-1 col-end-2 row-start-1 row-end-2", className)}
        style={style}
        value={inputValue}
        onChange={(e) => {
          if (!isControlled) setInternalValue(e.target.value)
          onChange?.(e)
          requestAnimationFrame(() => updateCaretRef.current(e.target))
        }}
        onBlur={(e) => {
          caretOpacity.set(0)
          onBlur?.(e)
        }}
      />
      <span ref={measureRef} aria-hidden className="pointer-events-none invisible absolute left-0 top-0 whitespace-pre" />
      <motion.div
        aria-hidden
        className="pointer-events-none col-start-1 col-end-2 row-start-1 row-end-2 my-auto h-[1.1em] w-px justify-self-start bg-foreground"
        style={{ x: springCaretX, opacity: caretOpacity }}
      />
    </div>
  )
}

export { Input }
