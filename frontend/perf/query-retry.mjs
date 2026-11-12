import assert from "node:assert/strict"
import axios from "axios"
import { backoff, retryDelay, shouldRetry } from "../src/lib/queryRetry.ts"

const withResponse = (status) => new axios.AxiosError("falhou", "ERR_BAD_RESPONSE", undefined, undefined, { status, data: null, headers: {}, config: {}, statusText: "" })
const noResponse = () => new axios.AxiosError("Network Error", "ERR_NETWORK")

assert.equal(shouldRetry(0, noResponse()), true, "sem resposta: tenta de novo")
assert.equal(shouldRetry(3, noResponse()), true, "sem resposta: ainda tem tentativas")
assert.equal(shouldRetry(4, noResponse()), false, "sem resposta: desiste depois de 4 retentativas")

for (const status of [502, 503, 504]) {
  assert.equal(shouldRetry(0, withResponse(status)), true, `${status} é queda do servidor`)
}

for (const status of [400, 401, 403, 404, 409, 422, 429]) {
  assert.equal(shouldRetry(0, withResponse(status)), false, `${status} não melhora tentando de novo`)
}

assert.equal(shouldRetry(0, withResponse(500)), true, "500 pode ser conexão velha do banco: uma retentativa")
assert.equal(shouldRetry(1, withResponse(500)), false, "500 repetido é bug de verdade, não insiste")
assert.equal(shouldRetry(0, new axios.CanceledError("cancelado")), false, "pedido cancelado não é falha de rede")
assert.equal(shouldRetry(0, new Error("bug")), true, "erro desconhecido: uma retentativa, como antes")
assert.equal(shouldRetry(1, new Error("bug")), false)

assert.deepEqual([0, 1, 2, 3].map(backoff), [1000, 2000, 4000, 8000])
assert.equal(backoff(10), 8000, "espera tem teto")

for (let attempt = 0; attempt < 5; attempt++) {
  for (let i = 0; i < 200; i++) {
    const delay = retryDelay(attempt)
    assert.ok(delay >= backoff(attempt) * 0.5 && delay <= backoff(attempt) * 1.5, `jitter dentro de 50% a 150% (${delay})`)
  }
}
const spread = new Set(Array.from({ length: 50 }, () => retryDelay(2)))
assert.ok(spread.size > 1, "consultas simultâneas não repetem no mesmo instante")

console.log("query-retry: ok")
