// Só mantém um "pulso" de 1s vivo enquanto o pomodoro está rodando.
// Workers dedicados não sofrem o mesmo congelamento de abas em segundo
// plano que o thread principal sofre em navegadores baseados em Chromium
// depois de alguns minutos ocultos — por isso a contagem em si mora aqui,
// e não num setInterval direto no componente React.
let intervalId: ReturnType<typeof setInterval> | null = null

self.onmessage = (event: MessageEvent<{ type: "start" | "stop" }>) => {
  if (event.data.type === "start") {
    if (intervalId !== null) return
    intervalId = setInterval(() => self.postMessage({ type: "tick" }), 1000)
  } else if (event.data.type === "stop" && intervalId !== null) {
    clearInterval(intervalId)
    intervalId = null
  }
}
