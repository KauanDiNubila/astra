import assert from "node:assert/strict"
import axios from "axios"
import { retryDelay, shouldRetry } from "../src/lib/queryRetry.ts"

const withResponse = (status) => new axios.AxiosError("falhou", "ERR_BAD_RESPONSE", undefined, undefined, { status, data: null, headers: {}, config: {}, statusText: "" })
const noResponse = () => new axios.AxiosError("Network Error", "ERR_NETWORK")

assert.equal(shouldRetry(0, noResponse()), true, "sem resposta: tenta de novo")
assert.equal(shouldRetry(3, noResponse()), true, "sem resposta: ainda tem tentativas")
assert.equal(shouldRetry(4, noResponse()), false, "sem resposta: desiste depois de 4 retentativas")

for (const status of [502, 503, 504]) {
  assert.equal(shouldRetry(0, withResponse(status)), true, `${status} é queda do servidor`)
}

for (const status of [400, 401, 403, 404, 409, 422, 429, 500]) {
  assert.equal(shouldRetry(0, withResponse(status)), false, `${status} não melhora tentando de novo`)
}

assert.equal(shouldRetry(0, new axios.CanceledError("cancelado")), false, "pedido cancelado não é falha de rede")
assert.equal(shouldRetry(0, new Error("bug")), true, "erro desconhecido: uma retentativa, como antes")
assert.equal(shouldRetry(1, new Error("bug")), false)

assert.deepEqual([0, 1, 2, 3].map(retryDelay), [1000, 2000, 4000, 8000])
assert.equal(retryDelay(10), 8000, "espera tem teto")

console.log("query-retry: ok")
