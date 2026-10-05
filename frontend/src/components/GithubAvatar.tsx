import { useState } from "react"
import { GitHubIcon } from "@/components/icons/GitHubIcon"
import { cn } from "@/lib/utils"

type Props = {
  login: string
  src?: string | null
  className?: string
}

export function GithubAvatar({ login, src, className }: Props) {
  const sources = [src, `https://github.com/${encodeURIComponent(login)}.png?size=96`].filter(
    (value, index, all): value is string => !!value && all.indexOf(value) === index,
  )
  const [failed, setFailed] = useState(0)

  if (failed >= sources.length) {
    return <GitHubIcon className={cn("rounded-full bg-foreground/10 p-1.5 text-foreground", className)} />
  }

  return (
    <img
      key={sources[failed]}
      src={sources[failed]}
      alt=""
      referrerPolicy="no-referrer"
      onError={() => setFailed((count) => count + 1)}
      className={cn("rounded-full object-cover", className)}
    />
  )
}
