import { useEffect, useState } from "react"
import { useReducedMotion } from "motion/react"
import { X } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { Message } from "@/lib/types"
import { cn } from "@/lib/utils"

type ReplyBarProps = {
  replyingTo: Message | null
  targetLabel: (message: Message) => string
  onCancel: () => void
}

export function ReplyBar({ replyingTo, targetLabel, onCancel }: ReplyBarProps) {
  const reducedMotion = useReducedMotion()
  const [displayed, setDisplayed] = useState<Message | null>(null)

  useEffect(() => {
    if (replyingTo) setDisplayed(replyingTo)
  }, [replyingTo])

  return (
    <div
      className={cn(
        "grid ease-out",
        replyingTo ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
        reducedMotion ? "duration-0" : "duration-200",
      )}
      style={{ transitionProperty: "grid-template-rows" }}
    >
      <div className="overflow-hidden">
        <div className="flex items-center justify-between gap-2 border-t bg-muted/40 px-4 py-2">
          <div className="min-w-0 flex-1">
            <p className="text-xs font-medium text-muted-foreground">
              Respondendo a {displayed ? targetLabel(displayed) : ""}
            </p>
            <p className="truncate text-sm">{displayed?.content ?? "📷 Foto"}</p>
          </div>
          <Button type="button" variant="ghost" size="icon" tabIndex={replyingTo ? 0 : -1} onClick={onCancel}>
            <X className="size-4" />
          </Button>
        </div>
      </div>
    </div>
  )
}
