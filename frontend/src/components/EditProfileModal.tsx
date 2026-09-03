import { useEffect, useRef, useState } from "react"
import type { ChangeEvent, FormEvent } from "react"
import { createPortal } from "react-dom"
import { motion, useReducedMotion } from "motion/react"
import { Check, ChevronDown, Copy, KeyRound, Pencil, X } from "lucide-react"
import { AdminBadge } from "@/components/AdminBadge"
import { useAuth } from "@/context/AuthContext"
import { api, baseURL, getErrorMessage } from "@/lib/api"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

type Props = {
  open: boolean
  onClose: () => void
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ""
  const last = parts.length > 1 ? parts[parts.length - 1][0] : ""
  return (first + last).toUpperCase()
}

export function EditProfileModal({ open, onClose }: Props) {
  const { user, refreshUser } = useAuth()
  const reducedMotion = useReducedMotion()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [name, setName] = useState("")
  const [bio, setBio] = useState("")
  const [avatarFile, setAvatarFile] = useState<File | null>(null)
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null)
  const [imgError, setImgError] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [rendered, setRendered] = useState(open)
  const [copied, setCopied] = useState(false)

  async function copyHandle() {
    if (!user) return
    try {
      await navigator.clipboard.writeText(`${user.name}#${user.tag}`)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      // permissão de clipboard negada pelo navegador — sem feedback, mas não quebra a tela
    }
  }

  const [changingPassword, setChangingPassword] = useState(false)
  const [currentPassword, setCurrentPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [passwordSaving, setPasswordSaving] = useState(false)
  const [passwordError, setPasswordError] = useState<string | null>(null)
  const [passwordSuccess, setPasswordSuccess] = useState(false)

  useEffect(() => {
    if (open) {
      setRendered(true)
      return
    }
    const timeout = setTimeout(() => setRendered(false), 300)
    return () => clearTimeout(timeout)
  }, [open])

  useEffect(() => {
    if (!open || !user) return
    setName(user.name)
    setBio(user.bio ?? "")
    setAvatarFile(null)
    setAvatarPreview(null)
    setImgError(false)
    setError(null)
    setChangingPassword(false)
    setCurrentPassword("")
    setNewPassword("")
    setPasswordError(null)
    setPasswordSuccess(false)
  }, [open, user])

  useEffect(() => {
    return () => {
      if (avatarPreview) URL.revokeObjectURL(avatarPreview)
    }
  }, [avatarPreview])

  useEffect(() => {
    if (!rendered) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [rendered])

  useEffect(() => {
    if (!rendered) return
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }
    document.addEventListener("keydown", handleKeyDown)
    return () => document.removeEventListener("keydown", handleKeyDown)
  }, [rendered, onClose])

  if (!user || !rendered) return null

  function onPickFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) return
    if (avatarPreview) URL.revokeObjectURL(avatarPreview)
    setAvatarFile(file)
    setAvatarPreview(URL.createObjectURL(file))
    setImgError(false)
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSaving(true)
    try {
      await api.put("/me", { name, bio: bio.trim() || null })
      if (avatarFile) {
        const formData = new FormData()
        formData.append("file", avatarFile)
        await api.post("/me/avatar", formData)
      }
      await refreshUser()
      onClose()
    } catch {
      setError("Não foi possível salvar as alterações.")
    } finally {
      setSaving(false)
    }
  }

  async function submitPasswordChange() {
    setPasswordError(null)
    setPasswordSaving(true)
    try {
      await api.put("/me/password", { currentPassword, newPassword })
      setCurrentPassword("")
      setNewPassword("")
      setPasswordSuccess(true)
      setChangingPassword(false)
    } catch (err) {
      setPasswordError(getErrorMessage(err, "Não foi possível trocar a senha."))
    } finally {
      setPasswordSaving(false)
    }
  }

  const existingAvatarUrl = user ? `${baseURL}/users/${user.id}/avatar` : null
  const avatarSrc = avatarPreview ?? existingAvatarUrl
  const showImg = avatarSrc && !imgError

  return createPortal(
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: open ? 1 : 0 }}
      transition={{ duration: 0.2 }}
      className="fixed inset-0 z-100 flex items-center justify-center overflow-y-auto p-4"
    >
      <div onClick={onClose} className="fixed inset-0 bg-black/20 backdrop-blur-[1px] dark:bg-black/60" />

      <div className="pointer-events-none relative z-101 my-auto w-full max-w-2xl">
        <motion.div
          initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94, y: 16 }}
          animate={
            reducedMotion
              ? { opacity: open ? 1 : 0 }
              : { opacity: open ? 1 : 0, scale: open ? 1 : 0.96, y: open ? 0 : 16 }
          }
          transition={{ type: "spring", damping: 22, stiffness: 320, mass: 0.8 }}
          className="pointer-events-auto w-full overflow-hidden rounded-2xl border border-border bg-popover shadow-lg"
        >
              <form onSubmit={onSubmit}>
                <div className="flex items-center justify-between px-6 py-4">
                  <h2 className="text-lg font-semibold text-popover-foreground">Editar perfil</h2>
                  <button
                    type="button"
                    title="Fechar"
                    onClick={onClose}
                    className="p-1 text-muted-foreground transition-colors hover:text-foreground"
                  >
                    <X size={20} />
                  </button>
                </div>

                <div className="flex flex-col rounded-xl border border-border bg-card md:flex-row">
                  <div className="flex-1 space-y-4 p-6">
                    <div className="space-y-1.5">
                      <Label htmlFor="profile-name">Nome</Label>
                      <Input
                        id="profile-name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        pattern="[^#]*"
                        title="Nome não pode conter '#'"
                        required
                        maxLength={120}
                      />
                    </div>

                    <div className="space-y-1.5">
                      <Label htmlFor="profile-email">E-mail</Label>
                      <Input id="profile-email" value={user.email} disabled />
                    </div>

                    <div className="space-y-1.5">
                      <Label htmlFor="profile-handle">Seu identificador</Label>
                      <div className="flex items-center gap-2">
                        <Input id="profile-handle" value={`${user.name}#${user.tag}`} disabled />
                        <Button
                          type="button"
                          variant="outline"
                          size="icon"
                          onClick={copyHandle}
                          title="Copiar"
                        >
                          {copied ? <Check size={16} /> : <Copy size={16} />}
                        </Button>
                      </div>
                    </div>

                    <div className="space-y-1.5">
                      <Label htmlFor="profile-bio">O que você faz</Label>
                      <Input
                        id="profile-bio"
                        value={bio}
                        onChange={(e) => setBio(e.target.value)}
                        maxLength={80}
                      />
                    </div>
                  </div>

                  <div className="w-full border-t border-dashed border-border md:w-px md:border-t-0 md:border-l" />

                  <div className="flex flex-1 flex-col items-center justify-center p-6">
                    <span className="mb-4 text-sm font-medium text-muted-foreground">Preview</span>
                    <div className="relative mb-4">
                      {showImg ? (
                        <img
                          src={avatarSrc}
                          alt="Avatar"
                          onError={() => setImgError(true)}
                          className="size-32 rounded-full object-cover ring-1 ring-border"
                        />
                      ) : (
                        <div className="flex size-32 items-center justify-center rounded-full bg-muted text-3xl text-muted-foreground ring-1 ring-border">
                          {initials(name || user.name)}
                        </div>
                      )}
                      <button
                        type="button"
                        title="Trocar foto"
                        onClick={() => fileInputRef.current?.click()}
                        className="absolute bottom-0 right-0 rounded-full border border-border bg-card p-2 text-muted-foreground shadow-md transition-colors hover:text-foreground"
                      >
                        <Pencil size={18} />
                      </button>
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        onChange={onPickFile}
                        className="hidden"
                      />
                    </div>
                    <h3 className="flex items-center justify-center gap-1.5 text-center text-lg font-bold text-foreground">
                      {name || user.name}
                      {user.role === "ADMIN" && <AdminBadge />}
                    </h3>
                    {bio && <p className="text-center text-sm text-muted-foreground">{bio}</p>}
                  </div>
                </div>

                <div className="border-t border-border px-6 py-4">
                  <div className="overflow-hidden rounded-xl border border-border bg-card">
                    <button
                      type="button"
                      onClick={() =>
                        setChangingPassword((v) => {
                          if (v) {
                            setPasswordError(null)
                            setCurrentPassword("")
                            setNewPassword("")
                          }
                          return !v
                        })
                      }
                      aria-expanded={changingPassword}
                      className="flex w-full items-center justify-between px-4 py-3 text-left transition-colors hover:bg-muted/50"
                    >
                      <span className="flex items-center gap-2 text-sm font-medium text-foreground">
                        <KeyRound size={16} className="text-muted-foreground" />
                        Senha
                      </span>
                      <span className="flex items-center gap-2">
                        {passwordSuccess && !changingPassword && (
                          <span className="flex items-center gap-1 text-xs text-green-600 dark:text-green-500">
                            <Check size={14} />
                            Alterada
                          </span>
                        )}
                        <ChevronDown
                          size={16}
                          className={cn(
                            "text-muted-foreground transition-transform duration-200",
                            changingPassword && "rotate-180",
                          )}
                        />
                      </span>
                    </button>

                    <div
                      className={cn(
                        "grid ease-out",
                        changingPassword ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
                        reducedMotion ? "duration-0" : "duration-300",
                      )}
                      style={{ transitionProperty: "grid-template-rows" }}
                    >
                      <div className="overflow-hidden">
                        <div className="space-y-3 border-t border-border px-4 pb-4 pt-3">
                          <div className="space-y-1.5">
                            <Label htmlFor="current-password">Senha atual</Label>
                            <Input
                              id="current-password"
                              type="password"
                              value={currentPassword}
                              onChange={(e) => setCurrentPassword(e.target.value)}
                              autoComplete="current-password"
                              tabIndex={changingPassword ? 0 : -1}
                            />
                          </div>
                          <div className="space-y-1.5">
                            <Label htmlFor="new-password">Senha nova</Label>
                            <Input
                              id="new-password"
                              type="password"
                              value={newPassword}
                              onChange={(e) => setNewPassword(e.target.value)}
                              minLength={8}
                              autoComplete="new-password"
                              tabIndex={changingPassword ? 0 : -1}
                            />
                          </div>
                          {passwordError && <p className="text-sm text-destructive">{passwordError}</p>}
                          <Button
                            type="button"
                            size="sm"
                            disabled={passwordSaving || !currentPassword || newPassword.length < 8}
                            onClick={submitPasswordChange}
                            tabIndex={changingPassword ? 0 : -1}
                          >
                            {passwordSaving ? "Trocando..." : "Salvar senha nova"}
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex flex-col-reverse items-center justify-end gap-3 px-6 py-5 sm:flex-row">
                  {error && <p className="mr-auto text-sm text-destructive">{error}</p>}
                  <Button type="button" variant="outline" onClick={onClose} className="w-full sm:w-auto">
                    Cancelar
                  </Button>
                  <Button type="submit" disabled={saving} className="w-full sm:w-auto">
                    {saving ? "Salvando..." : "Salvar alterações"}
                  </Button>
                </div>
              </form>
            </motion.div>
          </div>
    </motion.div>,
    document.body,
  )
}
