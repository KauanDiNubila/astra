import { useReducedMotion } from "motion/react"
import { Bar, BarChart, XAxis } from "recharts"
import { formatMinutes } from "@/lib/format"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart"

type Point = { day: string; minutes: number }

function formatShortDate(dayKey: string): string {
  const [, month, day] = dayKey.split("-")
  return `${day}/${month}`
}

function formatLongDate(dayKey: string): string {
  const [year, month, day] = dayKey.split("-").map(Number)
  return new Date(year, month - 1, day).toLocaleDateString("pt-BR", { weekday: "short", day: "numeric", month: "short" })
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
      <BarChart data={data} margin={{ left: 14, right: 14, top: 8, bottom: 0 }} barCategoryGap="18%">
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
              hideIndicator
              labelFormatter={(value) => formatLongDate(value as string)}
              formatter={(value) => (
                <span className="font-medium text-foreground tabular-nums">{formatMinutes(value as number)}</span>
              )}
            />
          }
        />
        <Bar
          dataKey="minutes"
          name="Foco"
          fill="var(--color-minutes)"
          radius={[3, 3, 1, 1]}
          maxBarSize={22}
          activeBar={{ fill: "var(--color-minutes)", fillOpacity: 1, stroke: "none" }}
          fillOpacity={0.7}
          isAnimationActive={!reducedMotion}
          animationBegin={550}
          animationDuration={900}
          animationEasing="ease-out"
        />
      </BarChart>
    </ChartContainer>
  )
}
