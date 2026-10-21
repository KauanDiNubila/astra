import { useReducedMotion } from "motion/react"
import { Bar, BarChart, XAxis } from "recharts"
import type { GitHubDailyPoint } from "@/lib/types"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart"

function formatShortDate(dayKey: string): string {
  const [, month, day] = dayKey.split("-")
  return `${day}/${month}`
}

const chartConfig = {
  contributionCount: {
    label: "Atividade",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

export function GitHubCommitChart({ data }: { data: GitHubDailyPoint[] }) {
  const reducedMotion = useReducedMotion()
  const hasData = data.some((p) => p.contributionCount > 0)

  if (!hasData) {
    return (
      <div className="flex h-[200px] items-center justify-center text-sm text-muted-foreground">
        Sem atividade nesse período.
      </div>
    )
  }

  const tickIndexes = data.length > 1 ? [0, Math.floor((data.length - 1) / 2), data.length - 1] : [0]
  const ticks = tickIndexes.map((i) => data[i].date)

  return (
    <ChartContainer config={chartConfig} className="h-[200px] w-full">
      <BarChart data={data} margin={{ left: 14, right: 14, top: 8, bottom: 0 }} barCategoryGap="18%">
        <XAxis
          dataKey="date"
          tickLine={false}
          axisLine={false}
          ticks={ticks}
          interval={0}
          tickFormatter={formatShortDate}
          tickMargin={8}
          fontSize={10}
        />
        <ChartTooltip
          cursor={{ fill: "var(--muted)", opacity: 0.6, radius: 4 }}
          content={
            <ChartTooltipContent
              hideIndicator
              labelFormatter={(value) => formatShortDate(value as string)}
              formatter={(value) => (
                <span className="font-medium text-foreground tabular-nums">
                  {value as number} {(value as number) === 1 ? "contribuição" : "contribuições"}
                </span>
              )}
            />
          }
        />
        <Bar
          dataKey="contributionCount"
          name="Atividade"
          fill="var(--color-contributionCount)"
          radius={[3, 3, 1, 1]}
          maxBarSize={22}
          isAnimationActive={!reducedMotion}
          animationDuration={700}
        />
      </BarChart>
    </ChartContainer>
  )
}
