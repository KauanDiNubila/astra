import { Skeleton } from "@/components/ui/skeleton"

export function PageSkeleton({ rows = 3 }: { rows?: number }) {
  return (
    <div className="flex flex-col gap-8">
      <Skeleton className="h-8 w-40" />
      <div className="flex flex-col gap-3 rounded-xl p-4 ring-1 ring-foreground/10">
        {Array.from({ length: rows }).map((_, i) => (
          <Skeleton key={i} className="h-5 w-full" />
        ))}
      </div>
    </div>
  )
}
