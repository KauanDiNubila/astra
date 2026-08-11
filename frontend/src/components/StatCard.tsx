import { Card, CardContent } from "@/components/ui/card"

export function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <Card>
      <CardContent className="flex flex-col gap-1">
        <span className="text-sm text-muted-foreground">{label}</span>
        <span className="text-2xl font-semibold">{value}</span>
      </CardContent>
    </Card>
  )
}
