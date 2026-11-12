import assert from "node:assert/strict"
import { chartRange } from "../src/lib/githubSeries.ts"

const TODAY = "2026-11-12"

function dayBefore(key, n) {
  const [y, m, d] = key.split("-").map(Number)
  return new Date(Date.UTC(y, m - 1, d - n)).toISOString().slice(0, 10)
}

function series(counts) {
  return counts.map((contributionCount, i) => ({ date: dayBefore(TODAY, counts.length - 1 - i), contributionCount }))
}

const year = series(Array.from({ length: 365 }, (_, i) => (i % 7 === 0 ? 2 : 1)))
const total = (range) => range.buckets.reduce((sum, b) => sum + b.count, 0)

{
  const range = chartRange("WEEK", year, TODAY)
  assert.equal(range.unit, "day")
  assert.equal(range.buckets.length, 7)
  assert.equal(range.buckets.at(-1).to, TODAY)
  assert.equal(range.buckets[0].from, dayBefore(TODAY, 6))
}

{
  const range = chartRange("MONTH", year, TODAY)
  assert.equal(range.buckets.length, 30)
  assert.equal(range.buckets[0].from, dayBefore(TODAY, 29))
}

{
  const range = chartRange("QUARTER", year, TODAY)
  assert.equal(range.unit, "week")
  assert.equal(range.buckets.length, 13)
  assert.equal(range.buckets.at(-1).to, TODAY)
  assert.equal(range.buckets.at(-1).from, dayBefore(TODAY, 6))
  assert.equal(range.buckets[0].from, dayBefore(TODAY, 89))
  assert.equal(range.buckets[0].to, dayBefore(TODAY, 84))
}

{
  const range = chartRange("YEAR", year, TODAY)
  assert.equal(range.unit, "month")
  assert.equal(range.buckets.length, 13)
  assert.equal(range.buckets.at(-1).from, "2026-11-01")
  assert.equal(range.buckets.at(-1).to, TODAY)
  assert.equal(range.buckets[0].from, dayBefore(TODAY, 364))
  assert.equal(range.buckets[1].from, "2025-12-01")
}

{
  for (const period of ["WEEK", "MONTH", "QUARTER", "YEAR"]) {
    const range = chartRange(period, year, TODAY)
    const expected = year.slice(-range.days).reduce((sum, p) => sum + p.contributionCount, 0)
    assert.equal(total(range), expected, `${period}: nenhum dia perdido nem contado duas vezes`)
  }
}

{
  const sparse = [{ date: dayBefore(TODAY, 2), contributionCount: 5 }]
  const range = chartRange("WEEK", sparse, TODAY)
  assert.equal(range.buckets.length, 7)
  assert.deepEqual(range.buckets.map((b) => b.count), [0, 0, 0, 0, 5, 0, 0])
}

{
  const range = chartRange("YEAR", [], TODAY)
  assert.equal(total(range), 0)
  assert.equal(range.buckets.length, 13)
}

console.log("github-series: ok")
