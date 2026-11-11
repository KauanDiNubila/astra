import { useAttachmentUrl } from "@/context/ChatContext"

type AttachmentImageProps = {
  messageId: string
  onOpen: () => void
  onLoaded: () => void
}

export function AttachmentImage({ messageId, onOpen, onLoaded }: AttachmentImageProps) {
  const url = useAttachmentUrl(messageId)
  if (!url) {
    return <div className="h-40 w-52 animate-pulse rounded-md bg-foreground/10" />
  }
  return (
    <button type="button" onClick={onOpen} className="block cursor-zoom-in">
      <img src={url} alt="Imagem enviada" onLoad={onLoaded} className="max-h-64 max-w-full rounded-md object-cover" />
    </button>
  )
}
