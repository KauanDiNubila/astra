import { Card, CardContent } from "@/components/ui/card"
import { useCountUp } from "@/hooks/use-count-up"
import { cn, INTERACTIVE_CARD_CLASS } from "@/lib/utils"

export function StatCard({
  label,
  value,
  format = (n: number) => String(Math.round(n)),
}: {
  label: string
  value: number
  format?: (value: number) => string
}) {
  const animated = useCountUp(value)
  return (
    <Card className={cn(INTERACTIVE_CARD_CLASS)}>
      <CardContent className="flex flex-col gap-1">
        <span className="text-sm text-muted-foreground">{label}</span>
        <span className="text-2xl font-semibold tabular-nums">{format(animated)}</span>
      </CardContent>
    </Card>
  )
}
