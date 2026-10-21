import type { RoadmapStep } from "@/lib/types"

export function roadmapProgress(steps: RoadmapStep[]) {
  const counted = steps.filter((s) => s.status !== "SKIPPED")
  const done = counted.filter((s) => s.status === "DONE").length
  const milestones = steps.filter((s) => !s.parentStepId).length
  return {
    milestones,
    topics: steps.length - milestones,
    done,
    total: counted.length,
    pct: counted.length > 0 ? Math.round((done / counted.length) * 100) : 0,
    started: steps.some((s) => s.status === "DONE" || s.status === "LEARNING"),
  }
}

export function formatStructure(milestones: number, topics: number) {
  return `${milestones} ${milestones === 1 ? "marco" : "marcos"} · ${topics} ${topics === 1 ? "tópico" : "tópicos"}`
}
