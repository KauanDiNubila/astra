import { api, befriend, createUser } from "./lib.mjs"

export async function seed() {
  const main = await createUser("Bench", "bench")
  const friend = await createUser("Amigo", "bench")
  await befriend(main, friend)
  for (const [user, count] of [[main, 6], [friend, 4]]) {
    const categories = (await api("/categories", user.token)).json
    for (let i = 0; i < count; i++) {
      await api("/sessions", user.token, "POST", {
        categoryId: categories[i % categories.length].id,
        focusedMinutes: 25 + i * 5,
        startedAt: new Date(Date.now() - ((i + 1) * 86400000) / 3).toISOString(),
        note: null,
      })
    }
  }
  for (const title of ["Spring Boot", "React avançado", "Algoritmos"]) {
    await api("/courses", main.token, "POST", { title, platform: "Udemy", moduleCount: 6 })
  }
  for (const title of ["Backend Java", "Frontend"]) {
    await api("/roadmaps", main.token, "POST", { title, source: null })
  }
  return main.email
}
