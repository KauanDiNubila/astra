import { cn } from "@/lib/utils"
import { AstraMark } from "@/components/landing/AstraMark"

export function OrbitArt({ className }: { className?: string }) {
  return (
    <div className={cn("relative aspect-square", className)} aria-hidden="true">
      <svg viewBox="0 0 200 200" className="absolute inset-0 size-full" fill="none" stroke="currentColor">
        <ellipse cx="100" cy="100" rx="92" ry="34" strokeOpacity="0.35" transform="rotate(-20 100 100)" />
        <ellipse cx="100" cy="100" rx="70" ry="24" strokeOpacity="0.25" transform="rotate(25 100 100)" />
        <circle cx="100" cy="100" r="96" strokeOpacity="0.12" />
        <circle cx="186" cy="70" r="4" fill="currentColor" stroke="none" />
        <circle cx="42" cy="146" r="3" fill="currentColor" stroke="none" fillOpacity="0.6" />
      </svg>
      <AstraMark className="absolute inset-[28%] size-[44%]" />
      <AstraMark className="absolute right-[6%] top-[8%] size-[12%] opacity-60" />
    </div>
  )
}
