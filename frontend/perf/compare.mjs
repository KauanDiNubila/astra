import { execSync, spawn } from "node:child_process"
import fs from "node:fs"
import os from "node:os"
import path from "node:path"
import { fileURLToPath } from "node:url"
import { bench, PAGES } from "./bench.mjs"
import { seed } from "./seed.mjs"
import { sleep } from "./lib.mjs"

const frontendDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..")
const repoDir = path.resolve(frontendDir, "..")
const BEFORE = process.env.BEFORE
const AFTER = process.env.AFTER
const DELAYS = (process.env.DELAYS ?? "50,150").split(",").map(Number)
const ROUNDS = Number(process.env.ROUNDS ?? 10)
const PORT = Number(process.env.PORT ?? 3000)
const NAME = process.env.NAME ?? "comparacao"

if (!BEFORE) {
  console.error("Defina BEFORE com o commit da versão antiga, por exemplo: BEFORE=10bdb0d npm run perf:compare")
  process.exit(1)
}

const tmp = fs.mkdtempSync(path.join(os.tmpdir(), "astra-perf-"))
const worktrees = []

function run(command, cwd) {
  execSync(command, { cwd, stdio: "pipe" })
}

function build(ref, label) {
  const outDir = path.join(tmp, `dist-${label}`)
  if (!ref) {
    run(`npx vite build --outDir "${outDir}"`, frontendDir)
    return outDir
  }
  const tree = path.join(tmp, `src-${label}`)
  run(`git worktree add --detach "${tree}" ${ref}`, repoDir)
  worktrees.push(tree)
  fs.symlinkSync(path.join(frontendDir, "node_modules"), path.join(tree, "frontend", "node_modules"), "junction")
  run(`npx vite build --outDir "${outDir}"`, path.join(tree, "frontend"))
  return outDir
}

function stop(child) {
  if (process.platform === "win32") {
    try {
      execSync(`taskkill /T /F /PID ${child.pid}`, { stdio: "ignore" })
    } catch {
      return
    }
  } else {
    child.kill()
  }
}

async function serve(outDir) {
  const child = spawn(`npx vite preview --outDir "${outDir}" --port ${PORT} --strictPort`, {
    cwd: frontendDir,
    shell: true,
    stdio: "ignore",
  })
  for (let i = 0; i < 60; i++) {
    try {
      if ((await fetch(`http://localhost:${PORT}/`)).ok) return child
    } catch {
      await sleep(500)
    }
  }
  stop(child)
  throw new Error("vite preview não subiu")
}

function unlinkJunction(link) {
  if (!fs.existsSync(link) || !fs.lstatSync(link).isSymbolicLink()) return !fs.existsSync(link)
  try {
    fs.unlinkSync(link)
  } catch {
    fs.rmdirSync(link)
  }
  return !fs.existsSync(link)
}

function cleanup() {
  let safe = true
  for (const tree of worktrees) {
    if (!unlinkJunction(path.join(tree, "frontend", "node_modules"))) {
      safe = false
      continue
    }
    try {
      run(`git worktree remove --force "${tree}"`, repoDir)
    } catch {
      continue
    }
  }
  if (safe) fs.rmSync(tmp, { recursive: true, force: true })
  else console.error(`Remova manualmente a pasta temporária ${tmp} (ainda tem um link para node_modules).`)
}

try {
  console.log(`Compilando antes (${BEFORE}) e depois (${AFTER ?? "código atual"})...`)
  const versions = { antes: build(BEFORE, "antes"), depois: build(AFTER, "depois") }
  const email = await seed()
  const results = {}
  for (const [label, outDir] of Object.entries(versions)) {
    const server = await serve(outDir)
    try {
      for (const delay of DELAYS) {
        console.log(`Medindo ${label} com ${delay} ms de atraso na API...`)
        results[`${label}-${delay}`] = await bench({ app: `http://localhost:${PORT}`, email, delay, rounds: ROUNDS })
        await sleep(15000)
      }
    } finally {
      stop(server)
      await sleep(2000)
    }
  }

  const date = new Date().toISOString().slice(0, 10)
  const file = path.join(frontendDir, "perf", "results", `${date}-${NAME}.json`)
  fs.mkdirSync(path.dirname(file), { recursive: true })
  fs.writeFileSync(
    file,
    JSON.stringify({ date, before: BEFORE, after: AFTER ?? "HEAD", rounds: ROUNDS, delays: DELAYS, results }, null, 2) + "\n",
  )

  for (const delay of DELAYS) {
    const before = results[`antes-${delay}`].median
    const after = results[`depois-${delay}`].median
    console.log(`\nAtraso de ${delay} ms por chamada (mediana de ${ROUNDS} voltas, página já visitada)`)
    console.log("| Página | Antes | Depois | Redução |")
    console.log("|---|---|---|---|")
    let totalBefore = 0
    let totalAfter = 0
    for (const [page] of PAGES) {
      totalBefore += before[page]
      totalAfter += after[page]
      console.log(`| ${page} | ${before[page]} ms | ${after[page]} ms | −${Math.round((1 - after[page] / before[page]) * 100)}% |`)
    }
    console.log(`| Volta completa | ${totalBefore} ms | ${totalAfter} ms | −${Math.round((1 - totalAfter / totalBefore) * 100)}% |`)
  }
  console.log(`\nResultados salvos em ${path.relative(repoDir, file)}`)
} finally {
  cleanup()
}
