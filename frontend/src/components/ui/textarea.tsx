import * as React from "react"
import { motion, useMotionValue, useReducedMotion, useSpring } from "motion/react"

import { cn } from "@/lib/utils"

const CARET_SPRING = { stiffness: 500, damping: 30, mass: 0.5 }
const CARET_SPRING_REDUCED = { stiffness: 10000, damping: 100, mass: 0.1 }

// Propriedades que afetam onde o texto quebra/posiciona — precisam ser
// idênticas entre o textarea real e o espelho invisível, senão a medição
// da posição do caret fica errada.
const MIRRORED_STYLE_PROPS = [
  "boxSizing",
  "width",
  "paddingTop",
  "paddingRight",
  "paddingBottom",
  "paddingLeft",
  "borderTopWidth",
  "borderRightWidth",
  "borderBottomWidth",
  "borderLeftWidth",
  "borderStyle",
  "fontStyle",
  "fontVariant",
  "fontWeight",
  "fontSize",
  "lineHeight",
  "fontFamily",
  "textAlign",
  "textTransform",
  "textIndent",
  "letterSpacing",
  "wordSpacing",
  "tabSize",
] as const

const TEXTAREA_CLASS =
  "flex field-sizing-content min-h-16 w-full rounded-lg border border-input bg-transparent px-2.5 py-2 text-base transition-colors outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:bg-input/50 disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 md:text-sm dark:bg-input/30 dark:disabled:bg-input/80 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40"

function Textarea({ className, style, value, defaultValue, onChange, onBlur, ...props }: React.ComponentProps<"textarea">) {
  const [internalValue, setInternalValue] = React.useState(defaultValue ?? "")
  const isControlled = value !== undefined
  const textValue = isControlled ? value : internalValue

  const containerRef = React.useRef<HTMLDivElement>(null)
  const textareaRef = React.useRef<HTMLTextAreaElement>(null)
  const mirrorRef = React.useRef<HTMLDivElement>(null)
  const markerRef = React.useRef<HTMLSpanElement>(null)

  const caretX = useMotionValue(0)
  const caretY = useMotionValue(0)
  const caretOpacity = useMotionValue(0)
  const prefersReducedMotion = useReducedMotion()
  const springOptions = prefersReducedMotion ? CARET_SPRING_REDUCED : CARET_SPRING
  const springCaretX = useSpring(caretX, springOptions)
  const springCaretY = useSpring(caretY, springOptions)

  const syncMirrorStyle = () => {
    const textarea = textareaRef.current
    const mirror = mirrorRef.current
    if (!textarea || !mirror) return
    const styles = window.getComputedStyle(textarea)
    for (const prop of MIRRORED_STYLE_PROPS) {
      mirror.style[prop] = styles[prop]
    }
  }

  const updateCaret = (target: HTMLTextAreaElement) => {
    const mirror = mirrorRef.current
    const marker = markerRef.current
    if (!mirror || !marker) return

    const selectionStart = target.selectionStart ?? 0
    const selectionEnd = target.selectionEnd ?? 0
    const hasSelection = selectionStart !== selectionEnd
    const caretIndex =
      selectionStart === selectionEnd
        ? selectionStart
        : target.selectionDirection === "backward"
          ? selectionStart
          : selectionEnd

    syncMirrorStyle()
    mirror.textContent = ""
    mirror.appendChild(document.createTextNode(target.value.slice(0, caretIndex)))
    marker.textContent = "​"
    mirror.appendChild(marker)
    mirror.appendChild(document.createTextNode(target.value.slice(caretIndex) || "​"))

    const x = marker.offsetLeft - target.scrollLeft
    const y = marker.offsetTop - target.scrollTop
    const isVisible =
      x >= -1 && x <= target.clientWidth + 1 && y >= -1 && y <= target.clientHeight + 1

    caretX.set(x)
    caretY.set(y)

    if (!isVisible || hasSelection) {
      caretOpacity.set(0)
      return
    }
    caretOpacity.set(1)
  }

  const updateCaretRef = React.useRef(updateCaret)
  updateCaretRef.current = updateCaret

  React.useEffect(() => {
    const textarea = textareaRef.current
    if (textarea && document.activeElement === textarea) updateCaretRef.current(textarea)
  }, [textValue])

  React.useEffect(() => {
    const textarea = textareaRef.current
    const container = containerRef.current
    if (!textarea || !container) return

    const updateIfFocused = () => {
      if (document.activeElement === textarea) updateCaretRef.current(textarea)
    }
    const handleSelectionChange = () => {
      if (document.activeElement !== textarea) return
      requestAnimationFrame(() => {
        if (document.activeElement === textarea) updateCaretRef.current(textarea)
      })
    }

    document.addEventListener("selectionchange", handleSelectionChange)
    document.fonts.addEventListener("loadingdone", updateIfFocused)
    void document.fonts.ready.then(updateIfFocused)
    textarea.addEventListener("scroll", updateIfFocused)

    const resizeObserver = new ResizeObserver(updateIfFocused)
    resizeObserver.observe(textarea)

    return () => {
      document.removeEventListener("selectionchange", handleSelectionChange)
      document.fonts.removeEventListener("loadingdone", updateIfFocused)
      textarea.removeEventListener("scroll", updateIfFocused)
      resizeObserver.disconnect()
    }
  }, [])

  return (
    <div ref={containerRef} className="relative grid grid-cols-1" style={{ caretColor: "transparent" }}>
      <textarea
        {...props}
        ref={textareaRef}
        data-slot="textarea"
        className={cn(TEXTAREA_CLASS, "col-start-1 col-end-2 row-start-1 row-end-2", className)}
        style={style}
        value={textValue}
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
      <div
        ref={mirrorRef}
        aria-hidden
        className="pointer-events-none invisible absolute left-0 top-0 overflow-hidden whitespace-pre-wrap break-words"
      >
        <span ref={markerRef} />
      </div>
      <motion.div
        aria-hidden
        className="pointer-events-none absolute top-0 left-0 h-[1.1em] w-px bg-foreground"
        style={{ x: springCaretX, y: springCaretY, opacity: caretOpacity }}
      />
    </div>
  )
}

export { Textarea }
