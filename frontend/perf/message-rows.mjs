import assert from "node:assert/strict"
import { buildMessageRows } from "../src/lib/messageRows.ts"

const minute = 60 * 1000
const at = (base, offset) => new Date(base + offset).toISOString()
const message = (id, senderId, createdAt) => ({ id, senderId, createdAt })
const base = new Date("2026-11-10T12:00:00").getTime()

let failures = 0
function test(label, fn) {
  try {
    fn()
    console.log("OK  ", label)
  } catch (error) {
    failures++
    console.log("FAIL", label, error.message.split("\n")[0])
  }
}

test("lista vazia não gera linhas", () => {
  assert.deepEqual(buildMessageRows([]), [])
})

test("a primeira mensagem abre um dia novo e não agrupa com anterior", () => {
  const [row] = buildMessageRows([message("a", "ana", at(base, 0))])
  assert.equal(row.newDay, true)
  assert.equal(row.groupedWithPrev, false)
  assert.equal(row.groupedWithNext, false)
})

test("mensagens seguidas da mesma pessoa em menos de 5 minutos se agrupam nos dois sentidos", () => {
  const rows = buildMessageRows([
    message("a", "ana", at(base, 0)),
    message("b", "ana", at(base, 2 * minute)),
    message("c", "ana", at(base, 4 * minute)),
  ])
  assert.deepEqual(rows.map((r) => r.groupedWithPrev), [false, true, true])
  assert.deepEqual(rows.map((r) => r.groupedWithNext), [true, true, false])
  assert.deepEqual(rows.map((r) => r.newDay), [true, false, false])
})

test("passou de 5 minutos ou mudou de pessoa, não agrupa", () => {
  const rows = buildMessageRows([
    message("a", "ana", at(base, 0)),
    message("b", "ana", at(base, 5 * minute)),
    message("c", "beto", at(base, 6 * minute)),
  ])
  assert.deepEqual(rows.map((r) => r.groupedWithPrev), [false, false, false])
  assert.deepEqual(rows.map((r) => r.groupedWithNext), [false, false, false])
})

test("virou o dia: nova divisória e sem agrupamento", () => {
  const rows = buildMessageRows([
    message("a", "ana", new Date("2026-11-10T23:58:00").toISOString()),
    message("b", "ana", new Date("2026-11-11T00:01:00").toISOString()),
  ])
  assert.equal(rows[1].newDay, true)
  assert.equal(rows[1].groupedWithPrev, true)
})

console.log(failures === 0 ? "\nTudo certo" : `\n${failures} falha(s)`)
process.exit(failures === 0 ? 0 : 1)
