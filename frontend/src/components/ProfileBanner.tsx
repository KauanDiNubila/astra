import { useEffect, useState } from "react"
import type { ReactNode } from "react"
import type { ProfileEffect } from "@/lib/types"
import { cn } from "@/lib/utils"
import { ProfileEffectOverlay } from "@/components/ProfileEffect"

export function ProfileBanner({
  bannerUrl,
  accentColor,
  effect,
  className,
  children,
}: {
  bannerUrl: string | null
  accentColor: string | null
  effect: ProfileEffect | null
  className?: string
  children?: ReactNode
}) {
  const [imgError, setImgError] = useState(false)
  useEffect(() => setImgError(false), [bannerUrl])
  const showImg = bannerUrl && !imgError

  return (
    <div
      className={cn("relative h-24 w-full overflow-hidden bg-muted", className)}
      style={accentColor ? { backgroundColor: accentColor } : undefined}
    >
      {showImg && (
        <img src={bannerUrl} alt="" onError={() => setImgError(true)} className="size-full object-cover" />
      )}
      <ProfileEffectOverlay effect={effect} />
      {children}
    </div>
  )
}
