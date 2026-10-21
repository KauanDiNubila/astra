import type { RoadmapStep } from "@/lib/types"

export type MilestoneState = "done" | "active" | "todo"

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

export function milestoneStates(steps: RoadmapStep[]): MilestoneState[] {
  return steps
    .filter((s) => !s.parentStepId)
    .sort((a, b) => a.position - b.position)
    .map((main) => {
      const children = steps.filter((s) => s.parentStepId === main.id && s.status !== "SKIPPED")
      const allDone = children.length > 0 && children.every((c) => c.status === "DONE")
      if (main.status === "DONE" || allDone) return "done"
      if (main.status === "LEARNING" || children.some((c) => c.status === "DONE" || c.status === "LEARNING")) return "active"
      return "todo"
    })
}
