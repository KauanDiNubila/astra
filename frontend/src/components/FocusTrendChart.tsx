import { useReducedMotion } from "motion/react"
import { Area, AreaChart, XAxis } from "recharts"
import { formatMinutes } from "@/lib/format"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart"

type Point = { day: string; minutes: number }

function formatShortDate(dayKey: string): string {
  const [, month, day] = dayKey.split("-")
  return `${day}/${month}`
}

const chartConfig = {
  minutes: {
    label: "Foco",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

export function FocusTrendChart({ data }: { data: Point[] }) {
  const reducedMotion = useReducedMotion()
  const hasData = data.some((p) => p.minutes > 0)

  if (!hasData) {
    return (
      <div className="flex h-[200px] items-center justify-center text-sm text-muted-foreground">
        Nenhuma sessão nesse período.
      </div>
    )
  }

  const tickIndexes = data.length > 1 ? [0, Math.floor((data.length - 1) / 2), data.length - 1] : [0]
  const ticks = tickIndexes.map((i) => data[i].day)

  return (
    <ChartContainer config={chartConfig} className="h-[200px] w-full">
      <AreaChart data={data} margin={{ left: 0, right: 0, top: 8, bottom: 0 }}>
        <defs>
          <linearGradient id="focus-trend-gradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--color-minutes)" stopOpacity={0.35} />
            <stop offset="100%" stopColor="var(--color-minutes)" stopOpacity={0} />
          </linearGradient>
        </defs>
        <XAxis
          dataKey="day"
          tickLine={false}
          axisLine={false}
          ticks={ticks}
          interval={0}
          tickFormatter={formatShortDate}
          tickMargin={8}
          fontSize={10}
        />
        <ChartTooltip
          cursor={false}
          content={
            <ChartTooltipContent
              labelFormatter={(value) => formatShortDate(value as string)}
              formatter={(value, name) => (
                <div className="flex w-full items-center justify-between gap-4">
                  <span className="text-muted-foreground">{name}</span>
                  <span className="font-mono font-medium text-foreground">
                    {formatMinutes(value as number)}
                  </span>
                </div>
              )}
            />
          }
        />
        <Area
          dataKey="minutes"
          name="Foco"
          type="monotone"
          fill="url(#focus-trend-gradient)"
          stroke="var(--color-minutes)"
          strokeWidth={2.5}
          isAnimationActive={!reducedMotion}
        />
      </AreaChart>
    </ChartContainer>
  )
}
