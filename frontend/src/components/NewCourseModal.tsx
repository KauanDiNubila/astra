import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { api } from "@/lib/api"
import type { CourseSummary } from "@/lib/types"
import { FormModal } from "@/components/FormModal"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Stepper } from "@/components/ui/stepper"

const MAX_INITIAL_MODULES = 30

export function NewCourseModal({
  open,
  onClose,
  onCreated,
}: {
  open: boolean
  onClose: () => void
  onCreated: () => void | Promise<void>
}) {
  const [title, setTitle] = useState("")
  const [platform, setPlatform] = useState("")
  const [moduleCount, setModuleCount] = useState(0)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setTitle("")
    setPlatform("")
    setModuleCount(0)
    setError(null)
  }, [open])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!title.trim()) {
      setError("Dê um título ao curso.")
      return
    }
    setError(null)
    setSaving(true)
    try {
      await api.post<CourseSummary>("/courses", {
        title: title.trim(),
        platform: platform.trim() || null,
        moduleCount: moduleCount || null,
      })
      await onCreated()
      onClose()
    } catch {
      setError("Não foi possível criar o curso.")
    } finally {
      setSaving(false)
    }
  }

  return (
    <FormModal
      open={open}
      title="Novo curso"
      submitLabel="Criar curso"
      savingLabel="Criando..."
      saving={saving}
      error={error}
      onClose={onClose}
      onSubmit={onSubmit}
    >
      <div className="flex flex-col gap-2">
        <Label htmlFor="course-title">Título</Label>
        <Input
          id="course-title"
          autoFocus
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Spring Boot na Prática"
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="course-platform">Plataforma</Label>
        <Input
          id="course-platform"
          value={platform}
          onChange={(e) => setPlatform(e.target.value)}
          placeholder="Udemy, Alura, YouTube…"
        />
      </div>
      <div className="flex items-center justify-between gap-4">
        <div>
          <Label>Módulos</Label>
          <p className="mt-1 text-xs text-muted-foreground">Você pode adicionar mais depois.</p>
        </div>
        <Stepper
          value={moduleCount}
          onChange={setModuleCount}
          min={0}
          max={MAX_INITIAL_MODULES}
          aria-label="Quantidade de módulos"
        />
      </div>
    </FormModal>
  )
}
