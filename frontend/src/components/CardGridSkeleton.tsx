import { Skeleton } from "@/components/ui/skeleton"

export function CardGridSkeleton({ items = 4 }: { items?: number }) {
  return (
    <div className="flex flex-col gap-8">
      <Skeleton className="h-8 w-32" />
      <Skeleton className="h-32 w-full rounded-xl" />
      <div className="grid gap-4 sm:grid-cols-2">
        {Array.from({ length: items }).map((_, i) => (
          <Skeleton key={i} className="h-24 w-full rounded-xl" />
        ))}
      </div>
    </div>
  )
}
