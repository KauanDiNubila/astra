import { useEffect, useRef, useState } from "react"
import type { KeyboardEvent } from "react"
import { Check, ChevronDown, Plus, Trash2 } from "lucide-react"
import { api } from "@/lib/api"
import type { Category } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

const CONFIRM_TIMEOUT_MS = 3000

type Props = {
  categories: Category[]
  value: string
  onChange: (id: string) => void
  onRefresh: () => Promise<void>
}

export function CategoryPicker({ categories, value, onChange, onRefresh }: Props) {
  const [open, setOpen] = useState(false)
  const [creating, setCreating] = useState(false)
  const [newName, setNewName] = useState("")
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [confirmId, setConfirmId] = useState<string | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const confirmTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (confirmTimeoutRef.current) clearTimeout(confirmTimeoutRef.current)
    }
  }, [])

  const selected = categories.find((c) => c.id === value)

  async function createCategory() {
    const name = newName.trim()
    if (!name) return
    const res = await api.post<Category>("/categories", { name })
    setNewName("")
    setCreating(false)
    await onRefresh()
    onChange(res.data.id)
  }

  function onNewNameKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter") {
      event.preventDefault()
      createCategory()
    }
    if (event.key === "Escape") {
      setCreating(false)
      setNewName("")
    }
  }

  function armDelete(id: string, event: React.MouseEvent) {
    event.stopPropagation()
    setDeleteError(null)
    setConfirmId(id)
    if (confirmTimeoutRef.current) clearTimeout(confirmTimeoutRef.current)
    confirmTimeoutRef.current = setTimeout(() => setConfirmId(null), CONFIRM_TIMEOUT_MS)
  }

  async function confirmDelete(id: string, event: React.MouseEvent) {
    event.stopPropagation()
    if (confirmTimeoutRef.current) clearTimeout(confirmTimeoutRef.current)
    setConfirmId(null)
    setDeletingId(id)
    setDeleteError(null)
    try {
      await api.delete(`/categories/${id}`)
      if (value === id) onChange("")
      await onRefresh()
    } catch {
      setDeleteError("Essa categoria tem sessões registradas e não pode ser excluída.")
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <Popover
      open={open}
      onOpenChange={(o) => {
        setOpen(o)
        if (!o) {
          setCreating(false)
          setNewName("")
          setConfirmId(null)
          setDeleteError(null)
        }
      }}
    >
      <PopoverTrigger asChild>
        <Button type="button" variant="outline" className="w-full justify-between font-normal">
          {selected ? selected.name : "Escolha uma categoria"}
          <ChevronDown className="size-4 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-64 p-1" align="start">
        <div className="flex flex-col">
          {categories.length === 0 && (
            <p className="px-2 py-1.5 text-sm text-muted-foreground">Nenhuma categoria ainda.</p>
          )}
          {categories.map((c) => (
            <div
              key={c.id}
              className={`group flex items-center justify-between rounded-md px-2 py-1.5 text-sm hover:bg-accent ${
                c.id === value ? "bg-accent" : ""
              }`}
            >
              <button
                type="button"
                className="flex flex-1 items-center gap-2 text-left"
                onClick={() => {
                  onChange(c.id)
                  setOpen(false)
                }}
              >
                {c.id === value ? (
                  <Check className="size-3.5 shrink-0" />
                ) : (
                  <span className="size-3.5 shrink-0" />
                )}
                <span>{c.name}</span>
              </button>
              <button
                type="button"
                className={`shrink-0 rounded px-1 text-xs font-medium ${
                  confirmId === c.id
                    ? "bg-destructive text-white opacity-100"
                    : "text-muted-foreground opacity-0 hover:text-destructive group-hover:opacity-100"
                }`}
                onClick={(e) => (confirmId === c.id ? confirmDelete(c.id, e) : armDelete(c.id, e))}
                disabled={deletingId === c.id}
                title={confirmId === c.id ? "Clique para confirmar" : "Excluir categoria"}
              >
                {confirmId === c.id ? "Confirmar?" : <Trash2 className="size-3.5" />}
              </button>
            </div>
          ))}

          {deleteError && <p className="px-2 py-1 text-xs text-destructive">{deleteError}</p>}

          {categories.length > 0 && <div className="my-1 h-px bg-border" />}

          {creating ? (
            <div className="flex items-center gap-1 px-1 py-1">
              <Input
                autoFocus
                placeholder="Nome da categoria"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={onNewNameKeyDown}
                className="h-8"
              />
              <Button type="button" size="sm" className="h-8" onClick={createCategory}>
                OK
              </Button>
            </div>
          ) : (
            <button
              type="button"
              className="flex items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm text-muted-foreground hover:bg-accent hover:text-foreground"
              onClick={() => setCreating(true)}
            >
              <Plus className="size-3.5" />
              Nova categoria
            </button>
          )}
        </div>
      </PopoverContent>
    </Popover>
  )
}
