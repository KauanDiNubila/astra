import type { ReactNode } from "react"
import {
  HeadphoneOff,
  Headphones,
  Mic,
  MicOff,
  MonitorOff,
  MonitorUp,
  PhoneOff,
  Settings,
  Video,
  VideoOff,
} from "lucide-react"
import { useCall } from "@/context/CallContext"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Switch } from "@/components/ui/switch"
import { deviceLabel, supportsSpeakerSelection } from "@/lib/callDevices"
import { MIC_LABELS, micStateOf } from "@/lib/callView"
import { cn } from "@/lib/utils"

type ToggleButtonProps = {
  pressed: boolean
  pressedVariant: "default" | "destructive"
  label: string
  icon: ReactNode
  onClick: () => void
  className: string
}

function ToggleButton({ pressed, pressedVariant, label, icon, onClick, className }: ToggleButtonProps) {
  return (
    <Button
      type="button"
      size="icon"
      variant={pressed ? pressedVariant : "secondary"}
      className={className}
      onClick={onClick}
      title={label}
      aria-label={label}
      aria-pressed={pressed}
    >
      {icon}
    </Button>
  )
}

function DeviceSelect({
  label,
  value,
  devices,
  fallback,
  onChange,
}: {
  label: string
  value: string
  devices: MediaDeviceInfo[]
  fallback: string
  onChange: (deviceId: string) => void
}) {
  const known = devices.some((d) => d.deviceId === value)
  return (
    <label className="flex flex-col gap-1 text-xs font-medium text-muted-foreground">
      {label}
      <select
        value={known ? value : ""}
        onChange={(e) => onChange(e.target.value)}
        className="h-9 w-full rounded-md border bg-background px-2 text-sm font-normal text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring max-sm:h-11"
      >
        <option value="">Padrão do sistema</option>
        {devices.map((d, i) => (
          <option key={d.deviceId} value={d.deviceId}>
            {deviceLabel(d, i, fallback)}
          </option>
        ))}
      </select>
    </label>
  )
}

function ProcessingToggle({
  id,
  label,
  description,
  checked,
  onChange,
}: {
  id: string
  label: string
  description: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-start gap-3">
      <div className="min-w-0 flex-1">
        <label htmlFor={id} className="text-sm font-medium">
          {label}
        </label>
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
      <Switch id={id} checked={checked} onCheckedChange={onChange} className="mt-0.5" />
    </div>
  )
}

function DeviceSettings({ buttonClass }: { buttonClass: string }) {
  const { devices, devicePrefs, refreshDevices, selectMic, selectCamera, selectSpeaker, setMicProcessing } = useCall()
  const speakerSupported = supportsSpeakerSelection()

  return (
    <Popover onOpenChange={(open) => open && void refreshDevices()}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          size="icon"
          variant="secondary"
          className={buttonClass}
          title="Configurações de dispositivos"
          aria-label="Configurações de dispositivos"
        >
          <Settings className="size-5" />
        </Button>
      </PopoverTrigger>
      <PopoverContent side="top" align="center" className="w-80 max-w-[calc(100vw-2rem)] gap-3 p-3">
        <p className="font-medium">Dispositivos</p>
        <DeviceSelect
          label="Microfone"
          value={devicePrefs.micId}
          devices={devices.mics}
          fallback="Microfone"
          onChange={(id) => void selectMic(id)}
        />
        {speakerSupported && (
          <DeviceSelect
            label="Saída de áudio"
            value={devicePrefs.speakerId}
            devices={devices.speakers}
            fallback="Alto-falante"
            onChange={selectSpeaker}
          />
        )}
        <DeviceSelect
          label="Câmera"
          value={devicePrefs.cameraId}
          devices={devices.cameras}
          fallback="Câmera"
          onChange={(id) => void selectCamera(id)}
        />
        {devices.cameras.length > 0 && devices.cameras.every((d) => !d.label) && (
          <p className="text-xs text-muted-foreground">Ligue a câmera uma vez para ver os nomes dos dispositivos.</p>
        )}
        <div className="flex flex-col gap-3 border-t pt-3">
          <ProcessingToggle
            id="call-echo-cancellation"
            label="Cancelamento de eco"
            description="Evita que os outros ouçam a própria voz pela sua caixa de som. Com fone de ouvido, pode desligar."
            checked={devicePrefs.echoCancellation}
            onChange={(checked) => void setMicProcessing({ echoCancellation: checked })}
          />
          <ProcessingToggle
            id="call-noise-suppression"
            label="Supressão de ruído"
            description="Reduz ruídos de fundo, como ventilador e teclado."
            checked={devicePrefs.noiseSuppression}
            onChange={(checked) => void setMicProcessing({ noiseSuppression: checked })}
          />
        </div>
      </PopoverContent>
    </Popover>
  )
}

export function CallControls() {
  const {
    muted,
    micMissing,
    deafened,
    cameraOn,
    screenSharing,
    screenShareSupported,
    toggleMute,
    toggleDeafen,
    toggleCamera,
    toggleScreenShare,
    leave,
  } = useCall()

  const mic = micStateOf(muted, micMissing)
  const base = "size-12 rounded-full"

  return (
    <div className="flex flex-wrap items-center justify-center gap-2 px-3 pt-3 pb-[max(0.75rem,env(safe-area-inset-bottom))]">
      <ToggleButton
        pressed={mic !== "on"}
        pressedVariant="destructive"
        label={MIC_LABELS[mic]}
        icon={mic !== "on" ? <MicOff className="size-5" /> : <Mic className="size-5" />}
        onClick={toggleMute}
        className={base}
      />
      <ToggleButton
        pressed={deafened}
        pressedVariant="destructive"
        label={deafened ? "Ativar áudio" : "Desativar áudio da call"}
        icon={deafened ? <HeadphoneOff className="size-5" /> : <Headphones className="size-5" />}
        onClick={toggleDeafen}
        className={base}
      />
      <ToggleButton
        pressed={cameraOn}
        pressedVariant="default"
        label={cameraOn ? "Desligar câmera" : "Ligar câmera"}
        icon={cameraOn ? <Video className="size-5" /> : <VideoOff className="size-5" />}
        onClick={() => void toggleCamera()}
        className={base}
      />
      {screenShareSupported && (
        <ToggleButton
          pressed={screenSharing}
          pressedVariant="default"
          label={screenSharing ? "Parar de compartilhar a tela" : "Compartilhar tela"}
          icon={screenSharing ? <MonitorOff className="size-5" /> : <MonitorUp className="size-5" />}
          onClick={() => void toggleScreenShare()}
          className={base}
        />
      )}
      <DeviceSettings buttonClass={base} />
      <Button
        type="button"
        size="icon"
        variant="destructive"
        className={cn(base, "ml-2")}
        onClick={leave}
        title="Sair da chamada"
        aria-label="Sair da chamada"
      >
        <PhoneOff className="size-5" />
      </Button>
    </div>
  )
}
