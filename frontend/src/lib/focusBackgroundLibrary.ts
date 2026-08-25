export type LibraryBackground = {
  id: string
  label: string
  image: string
}

export type LibraryCategory = {
  id: string
  label: string
  backgrounds: LibraryBackground[]
}

export const FOCUS_BACKGROUND_LIBRARY: LibraryCategory[] = [
  {
    id: "paisagens",
    label: "Paisagens",
    backgrounds: [
      { id: "lua-sobre-o-lago", label: "Lua sobre o lago", image: "/focus-library/paisagens/lua-sobre-o-lago.jpg" },
      { id: "caverna-submersa", label: "Caverna submersa", image: "/focus-library/paisagens/caverna-submersa.jpg" },
      {
        id: "por-do-sol-no-campo",
        label: "Pôr do sol no campo",
        image: "/focus-library/paisagens/por-do-sol-no-campo.jpg",
      },
      { id: "ceu-de-nuvens", label: "Céu de nuvens", image: "/focus-library/paisagens/ceu-de-nuvens.jpg" },
      { id: "farol-na-costa", label: "Farol na costa", image: "/focus-library/paisagens/farol-na-costa.jpg" },
      {
        id: "estacao-entre-cerejeiras",
        label: "Estação entre cerejeiras",
        image: "/focus-library/paisagens/estacao-entre-cerejeiras.jpg",
      },
    ],
  },
  {
    id: "pixel-art",
    label: "Pixel Art",
    backgrounds: [
      { id: "mercado-noturno", label: "Mercado noturno", image: "/focus-library/pixel-art/mercado-noturno.jpg" },
      {
        id: "lanchonete-cyberpunk",
        label: "Lanchonete cyberpunk",
        image: "/focus-library/pixel-art/lanchonete-cyberpunk.jpg",
      },
      {
        id: "escritorio-na-cidade",
        label: "Escritório na cidade",
        image: "/focus-library/pixel-art/escritorio-na-cidade.jpg",
      },
      { id: "galaxia", label: "Galáxia", image: "/focus-library/pixel-art/galaxia.jpg" },
      { id: "telhados-na-chuva", label: "Telhados na chuva", image: "/focus-library/pixel-art/telhados-na-chuva.jpg" },
      { id: "planeta-com-aneis", label: "Planeta com anéis", image: "/focus-library/pixel-art/planeta-com-aneis.jpg" },
    ],
  },
]

export function findLibraryBackground(id: string): LibraryBackground | null {
  for (const category of FOCUS_BACKGROUND_LIBRARY) {
    const found = category.backgrounds.find((bg) => bg.id === id)
    if (found) return found
  }
  return null
}
