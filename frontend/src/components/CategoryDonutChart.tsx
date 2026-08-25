import { useReducedMotion } from "motion/react"
import { Cell, Pie, PieChart, Sector } from "recharts"
import type { PieLabelRenderProps, PieSectorDataItem } from "recharts"
import { formatMinutes } from "@/lib/format"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart"

type CategorySlice = { categoryId: string; name: string; minutes: number; color: string | null }

const PALETTE = [
  "var(--chart-1)",
  "var(--chart-2)",
  "var(--chart-3)",
  "var(--chart-4)",
  "var(--chart-5)",
  "var(--chart-6)",
  "var(--chart-7)",
  "var(--chart-8)",
]

const RADIAN = Math.PI / 180

function ExternalLabel(props: PieLabelRenderProps) {
  const { cx, cy, midAngle, outerRadius, name, value } = props as PieLabelRenderProps & {
    name: string
    value: number
  }
  const cos = Math.cos(-midAngle! * RADIAN)
  const sin = Math.sin(-midAngle! * RADIAN)
  const lineStart = Number(outerRadius) + 4
  const lineEnd = lineStart + 22
  const onRight = cos >= 0

  const x1 = Number(cx) + lineStart * cos
  const y1 = Number(cy) + lineStart * sin
  const x2 = Number(cx) + lineEnd * cos
  const y2 = Number(cy) + lineEnd * sin
  const labelX = Number(cx) + (lineEnd + (onRight ? 4 : -4)) * cos
  const anchor = onRight ? "start" : "end"

  return (
    <g>
      <line x1={x1} y1={y1} x2={x2} y2={y2} className="stroke-muted-foreground/40" strokeWidth={1} />
      <text x={labelX} y={y2 - 4} textAnchor={anchor} className="fill-foreground text-[11px] font-medium">
        {name}
      </text>
      <text x={labelX} y={y2 + 9} textAnchor={anchor} className="fill-muted-foreground text-[10px]">
        {formatMinutes(value)}
      </text>
    </g>
  )
}

function ActiveSlice(props: PieSectorDataItem) {
  return <Sector {...props} outerRadius={Number(props.outerRadius) + 6} />
}

function CenterTotal({ total }: { total: number }) {
  return (
    <text x="50%" y="50%" textAnchor="middle" dominantBaseline="middle">
      <tspan x="50%" dy="-0.3em" className="fill-foreground text-lg font-semibold">
        {formatMinutes(total)}
      </tspan>
      <tspan x="50%" dy="1.4em" className="fill-muted-foreground text-[10px]">
        total
      </tspan>
    </text>
  )
}

export function CategoryDonutChart({ data }: { data: CategorySlice[] }) {
  const reducedMotion = useReducedMotion()
  const total = data.reduce((sum, d) => sum + d.minutes, 0)

  if (total === 0) {
    return (
      <div className="flex h-[280px] items-center justify-center text-sm text-muted-foreground">
        Nenhuma sessão nesse período.
      </div>
    )
  }

  const sorted = data.filter((d) => d.minutes > 0).sort((a, b) => b.minutes - a.minutes)
  const chartConfig = Object.fromEntries(
    sorted.map((slice, i) => [slice.categoryId, { label: slice.name, color: slice.color || PALETTE[i % PALETTE.length] }]),
  ) satisfies ChartConfig

  return (
    <ChartContainer config={chartConfig} className="mx-auto aspect-square max-h-[320px]">
      <PieChart>
        <ChartTooltip
          content={
            <ChartTooltipContent
              hideLabel
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
        <Pie
          data={sorted}
          dataKey="minutes"
          nameKey="name"
          innerRadius={55}
          outerRadius={70}
          paddingAngle={2}
          isAnimationActive={!reducedMotion}
          label={ExternalLabel}
          labelLine={false}
          activeShape={ActiveSlice}
        >
          {sorted.map((slice, i) => (
            <Cell key={slice.categoryId} fill={slice.color || PALETTE[i % PALETTE.length]} stroke="none" />
          ))}
        </Pie>
        <CenterTotal total={total} />
      </PieChart>
    </ChartContainer>
  )
}
