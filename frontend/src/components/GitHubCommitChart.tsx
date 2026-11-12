import { useReducedMotion } from "motion/react"
import { Bar, BarChart, Cell, CartesianGrid, XAxis } from "recharts"
import type { BucketUnit, ChartBucket } from "@/lib/githubSeries"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart"

const MONTHS = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"]

function shortDate(dayKey: string) {
  const [, month, day] = dayKey.split("-")
  return `${day}/${month}`
}

function axisLabel(bucket: ChartBucket, unit: BucketUnit, currentYear: string) {
  if (unit !== "month") return shortDate(bucket.from)
  const [year, month] = bucket.to.split("-")
  const name = MONTHS[Number(month) - 1]
  return year === currentYear ? name : `${name}/${year.slice(2)}`
}

function tooltipLabel(bucket: ChartBucket, unit: BucketUnit) {
  if (unit === "day" || bucket.from === bucket.to) return shortDate(bucket.from)
  return `${shortDate(bucket.from)} – ${shortDate(bucket.to)}`
}

const chartConfig = {
  count: {
    label: "Atividade",
    color: "var(--chart-1)",
  },
} satisfies ChartConfig

export function GitHubCommitChart({ buckets, unit }: { buckets: ChartBucket[]; unit: BucketUnit }) {
  const reducedMotion = useReducedMotion()
  const currentYear = buckets.at(-1)?.to.slice(0, 4) ?? ""
  const data = buckets.map((bucket) => ({
    ...bucket,
    label: axisLabel(bucket, unit, currentYear),
    tooltip: tooltipLabel(bucket, unit),
  }))
  const hasData = data.some((p) => p.count > 0)

  if (!hasData) {
    return (
      <div className="flex h-[220px] items-center justify-center text-sm text-muted-foreground">
        Sem atividade nesse período.
      </div>
    )
  }

  const lastIndex = data.length - 1

  return (
    <ChartContainer config={chartConfig} className="h-[220px] w-full">
      <BarChart data={data} margin={{ left: 4, right: 4, top: 8, bottom: 0 }} barCategoryGap={unit === "day" && data.length > 14 ? "22%" : "32%"}>
        <CartesianGrid vertical={false} strokeDasharray="3 4" stroke="var(--border)" />
        <XAxis
          dataKey="label"
          tickLine={false}
          axisLine={false}
          interval={unit === "day" && data.length > 14 ? "preserveStartEnd" : 0}
          minTickGap={18}
          tickMargin={10}
          fontSize={11}
        />
        <ChartTooltip
          cursor={{ fill: "var(--muted)", opacity: 0.4 }}
          content={
            <ChartTooltipContent
              hideIndicator
              labelFormatter={(_, payload) => payload?.[0]?.payload.tooltip}
              formatter={(value) => (
                <span className="font-medium text-foreground tabular-nums">
                  {value as number} {(value as number) === 1 ? "contribuição" : "contribuições"}
                </span>
              )}
            />
          }
        />
        <Bar
          dataKey="count"
          name="Atividade"
          radius={[4, 4, 1, 1]}
          maxBarSize={28}
          minPointSize={2}
          isAnimationActive={!reducedMotion}
          animationBegin={0}
          animationDuration={600}
          animationEasing="ease-out"
        >
          {data.map((bucket, index) => (
            <Cell
              key={bucket.from}
              fill="var(--color-count)"
              fillOpacity={index === lastIndex ? 1 : bucket.count === 0 ? 0.2 : 0.6}
            />
          ))}
        </Bar>
      </BarChart>
    </ChartContainer>
  )
}
