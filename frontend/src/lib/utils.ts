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
