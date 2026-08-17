import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const INTERACTIVE_CARD_CLASS =
  "transition-[transform,box-shadow,ring-color] duration-200 ease-out " +
  "hover:-translate-y-0.5 hover:shadow-lg hover:shadow-black/10 hover:ring-foreground/20 " +
  "active:translate-y-0 active:shadow-md " +
  "motion-reduce:transition-none motion-reduce:hover:translate-y-0"

export const SPOTLIGHT_CLASS =
  "relative overflow-hidden " +
  "before:pointer-events-none before:absolute before:inset-0 before:opacity-0 before:transition-opacity before:duration-300 before:content-[''] " +
  "before:bg-[radial-gradient(400px_circle_at_var(--x)_var(--y),color-mix(in_oklch,var(--foreground)_10%,transparent),transparent_70%)] " +
  "hover:before:opacity-100 motion-reduce:before:hidden"

export const BUTTON_REVEAL_CLASS =
  "relative overflow-hidden isolate " +
  "before:pointer-events-none before:absolute before:inset-0 before:content-[''] " +
  "before:bg-foreground/10 before:[clip-path:circle(0%_at_var(--x,50%)_var(--y,50%))] " +
  "before:transition-[clip-path] before:duration-500 before:ease-out " +
  "hover:before:[clip-path:circle(150%_at_var(--x,50%)_var(--y,50%))] " +
  "motion-reduce:before:hidden"

export const SCROLLBAR_HIDE_CLASS =
  "[scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"

export const gridStagger = {
  hidden: {},
  show: { transition: { staggerChildren: 0.06 } },
}

export const gridItem = {
  hidden: { opacity: 0, y: 16 },
  show: { opacity: 1, y: 0, transition: { duration: 0.32, ease: [0.22, 1, 0.36, 1] as const } },
}
