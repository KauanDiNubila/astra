import { useEffect, useRef, useState } from "react"
import type { ChangeEvent, ClipboardEvent, FormEvent, KeyboardEvent, RefObject } from "react"
import { ArrowUp, ImagePlus, X } from "lucide-react"
import { toast } from "sonner"
import { useChat } from "@/context/ChatContext"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import type { ConversationRef } from "@/lib/conversation"

type PendingImage = {
  file: File
  previewUrl: string
}

type ComposerProps = {
  conversation: ConversationRef
  placeholder: string
  replyToMessageId: string | undefined
  textareaRef: RefObject<HTMLTextAreaElement | null>
  onSent: () => void
}

export function Composer({ conversation, placeholder, replyToMessageId, textareaRef, onSent }: ComposerProps) {
  const { sendMessage, sendImageMessage } = useChat()
  const [draft, setDraft] = useState("")
  const [pendingImage, setPendingImage] = useState<PendingImage | null>(null)
  const [sendingImage, setSendingImage] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const pendingImageRef = useRef<PendingImage | null>(null)

  useEffect(() => {
    pendingImageRef.current = pendingImage
  }, [pendingImage])

  useEffect(() => {
    return () => {
      if (pendingImageRef.current) URL.revokeObjectURL(pendingImageRef.current.previewUrl)
    }
  }, [])

  function submitDraft() {
    if (pendingImage) {
      if (!sendingImage) void submitImage()
      return
    }
    if (!draft.trim()) return
    if (!sendMessage(conversation, draft.trim(), replyToMessageId)) {
      toast.error("Sem conexão com o chat. Sua mensagem continua no campo, tente de novo em instantes.")
      return
    }
    setDraft("")
    onSent()
  }

  async function submitImage() {
    if (!pendingImage) return
    setSendingImage(true)
    try {
      await sendImageMessage(conversation, pendingImage.file, draft.trim() || undefined, replyToMessageId)
      URL.revokeObjectURL(pendingImage.previewUrl)
      setPendingImage(null)
      setDraft("")
      onSent()
    } finally {
      setSendingImage(false)
    }
  }

  function cancelImage() {
    if (pendingImage) URL.revokeObjectURL(pendingImage.previewUrl)
    setPendingImage(null)
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    submitDraft()
  }

  function onKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      submitDraft()
    }
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) setPendingImage({ file, previewUrl: URL.createObjectURL(file) })
    event.target.value = ""
  }

  function handlePaste(event: ClipboardEvent<HTMLTextAreaElement>) {
    const item = Array.from(event.clipboardData.items).find((i) => i.type.startsWith("image/"))
    if (!item) return
    const file = item.getAsFile()
    if (!file) return
    event.preventDefault()
    setPendingImage({ file, previewUrl: URL.createObjectURL(file) })
  }

  return (
    <>
      {pendingImage && (
        <div className="border-t bg-muted/40 px-4 py-2">
          <div className="relative inline-block">
            <img src={pendingImage.previewUrl} alt="Pré-visualização" className="size-16 rounded-md object-cover" />
            <button
              type="button"
              onClick={cancelImage}
              disabled={sendingImage}
              title="Remover anexo"
              className="absolute -top-1.5 -right-1.5 flex size-5 items-center justify-center rounded-full bg-background text-muted-foreground shadow ring-1 ring-border hover:text-foreground disabled:pointer-events-none disabled:opacity-50"
            >
              <X className="size-3" />
            </button>
          </div>
        </div>
      )}

      <form onSubmit={onSubmit} className="px-3 pt-1 pb-3 sm:px-4 sm:pb-4">
        <div className="flex items-end gap-1 rounded-2xl border bg-card p-1.5 pl-2 transition-colors focus-within:border-foreground/30">
          <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={handleFileChange} />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            title="Anexar imagem"
            className="size-8 shrink-0 rounded-xl text-muted-foreground"
            onClick={() => fileInputRef.current?.click()}
          >
            <ImagePlus className="size-4" />
          </Button>
          <div className="min-w-0 flex-1">
            <Textarea
              ref={textareaRef}
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={onKeyDown}
              onPaste={handlePaste}
              placeholder={placeholder}
              className="max-h-40 min-h-8 w-full resize-none border-0 bg-transparent px-1 py-1.5 shadow-none focus-visible:ring-0 dark:bg-transparent"
            />
          </div>
          <Button
            type="submit"
            size="icon"
            aria-label="Enviar"
            className="size-8 shrink-0 rounded-xl"
            disabled={pendingImage ? sendingImage : !draft.trim()}
          >
            <ArrowUp className="size-4" />
          </Button>
        </div>
      </form>
    </>
  )
}
