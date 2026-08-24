import { Volume2 } from "lucide-react"
import type { PomodoroSettings } from "@/lib/pomodoroSettings"
import { CHIME_OPTIONS, playChime } from "@/lib/sound"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"

type Props = {
  settings: PomodoroSettings
  onChange: (settings: PomodoroSettings) => void
}

function range(start: number, end: number, step: number) {
  const values: number[] = []
  for (let n = start; n <= end; n += step) values.push(n)
  return values
}

const FOCUS_MINUTE_OPTIONS = range(5, 90, 5)
const BREAK_MINUTE_OPTIONS = range(5, 30, 5)
const POMODORO_COUNT_OPTIONS = [2, 3, 4, 5, 6, 8]

function SettingSelect({
  label,
  value,
  options,
  formatOption,
  onChange,
}: {
  label: string
  value: number
  options: number[]
  formatOption: (n: number) => string
  onChange: (value: number) => void
}) {
  return (
    <div className="flex flex-col gap-2">
      <Label>{label}</Label>
      <Select value={String(value)} onValueChange={(v) => onChange(Number(v))}>
        <SelectTrigger>
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {options.map((n) => (
            <SelectItem key={n} value={String(n)}>
              {formatOption(n)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}

function SettingToggle({
  label,
  checked,
  onChange,
}: {
  label: string
  checked: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-center justify-between gap-4 px-4 py-3">
      <Label className="font-normal">{label}</Label>
      <Switch checked={checked} onCheckedChange={onChange} />
    </div>
  )
}

export function PomodoroSettingsPanel({ settings, onChange }: Props) {
  function update<K extends keyof PomodoroSettings>(key: K, value: PomodoroSettings[K]) {
    onChange({ ...settings, [key]: value })
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="grid gap-4 sm:grid-cols-2">
        <SettingSelect
          label="Duração do pomodoro"
          value={settings.focusMinutes}
          options={FOCUS_MINUTE_OPTIONS}
          formatOption={(n) => `${n}min`}
          onChange={(v) => update("focusMinutes", v)}
        />
        <SettingSelect
          label="Duração da pausa curta"
          value={settings.shortBreakMinutes}
          options={BREAK_MINUTE_OPTIONS}
          formatOption={(n) => `${n}min`}
          onChange={(v) => update("shortBreakMinutes", v)}
        />
        <SettingSelect
          label="Duração da pausa longa"
          value={settings.longBreakMinutes}
          options={BREAK_MINUTE_OPTIONS}
          formatOption={(n) => `${n}min`}
          onChange={(v) => update("longBreakMinutes", v)}
        />
        <SettingSelect
          label="Pomodoros até pausa longa"
          value={settings.pomodorosUntilLongBreak}
          options={POMODORO_COUNT_OPTIONS}
          formatOption={(n) => `${n} Pomodoro${n === 1 ? "" : "s"}`}
          onChange={(v) => update("pomodorosUntilLongBreak", v)}
        />
      </div>

      <div className="flex flex-col divide-y rounded-lg border">
        <SettingToggle
          label="Iniciar próximo pomodoro automaticamente"
          checked={settings.autoStartNextPomodoro}
          onChange={(v) => update("autoStartNextPomodoro", v)}
        />
        <SettingToggle
          label="Iniciar pausa automaticamente"
          checked={settings.autoStartBreak}
          onChange={(v) => update("autoStartBreak", v)}
        />
        <SettingToggle
          label="Desabilitar pausas"
          checked={settings.disableBreaks}
          onChange={(v) => update("disableBreaks", v)}
        />
        <SettingToggle
          label="Som ao terminar o ciclo"
          checked={settings.soundEnabled}
          onChange={(v) => update("soundEnabled", v)}
        />
        {settings.soundEnabled && (
          <div className="flex items-center justify-between gap-4 px-4 py-3">
            <Label className="font-normal">Som</Label>
            <div className="flex items-center gap-1.5">
              <Select value={settings.soundId} onValueChange={(v) => update("soundId", v as PomodoroSettings["soundId"])}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {CHIME_OPTIONS.map((c) => (
                    <SelectItem key={c.id} value={c.id}>
                      {c.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="size-8 shrink-0"
                title="Ouvir"
                onClick={() => playChime(settings.soundId)}
              >
                <Volume2 className="size-4" />
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
