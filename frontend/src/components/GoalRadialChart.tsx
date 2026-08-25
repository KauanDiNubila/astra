import { useReducedMotion } from "motion/react"
import { Label, PolarAngleAxis, PolarRadiusAxis, RadialBar, RadialBarChart } from "recharts"
import { ChartContainer, type ChartConfig } from "@/components/ui/chart"

type Props = {
  label: string
  achievedHours: number
  targetHours: number
}

const chartConfig = {
  value: { label: "Progresso" },
} satisfies ChartConfig

export function GoalRadialChart({ label, achievedHours, targetHours }: Props) {
  const reducedMotion = useReducedMotion()

  if (targetHours <= 0) {
    return (
      <div className="flex h-[180px] flex-col items-center justify-center gap-1 text-center">
        <p className="text-sm text-muted-foreground">Meta {label} não definida.</p>
      </div>
    )
  }

  const reached = achievedHours >= targetHours
  const clamped = Math.min(achievedHours, targetHours)

  return (
    <div className="flex flex-col items-center gap-2">
      <ChartContainer config={chartConfig} className="aspect-square w-[180px]">
        <RadialBarChart
          data={[{ value: clamped, fill: reached ? "var(--chart-1)" : "var(--chart-2)" }]}
          startAngle={90}
          endAngle={-270}
          innerRadius={62}
          outerRadius={80}
        >
          <PolarAngleAxis type="number" domain={[0, targetHours]} angleAxisId={0} tick={false} />
          <RadialBar
            dataKey="value"
            background={{ className: "fill-muted" }}
            cornerRadius={10}
            isAnimationActive={!reducedMotion}
          />
          <PolarRadiusAxis tick={false} tickLine={false} axisLine={false}>
            <Label
              content={({ viewBox }) => {
                if (viewBox && "cx" in viewBox && "cy" in viewBox) {
                  return (
                    <text x={viewBox.cx} y={viewBox.cy} textAnchor="middle" dominantBaseline="middle">
                      <tspan x={viewBox.cx} y={(viewBox.cy ?? 0) - 4} className="fill-foreground text-2xl font-bold">
                        {achievedHours.toFixed(1)}h
                      </tspan>
                      <tspan x={viewBox.cx} y={(viewBox.cy ?? 0) + 16} className="fill-muted-foreground text-xs">
                        de {targetHours}h
                      </tspan>
                    </text>
                  )
                }
              }}
            />
          </PolarRadiusAxis>
        </RadialBarChart>
      </ChartContainer>
      <p className="text-sm font-medium">Meta {label}</p>
    </div>
  )
}
