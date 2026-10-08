const required = ["ASTRA_STORE_IDENTITY_NAME", "ASTRA_STORE_PUBLISHER", "ASTRA_STORE_PUBLISHER_DISPLAY_NAME"]
const missing = required.filter((name) => !process.env[name])
if (missing.length > 0) {
  throw new Error(`Defina ${missing.join(", ")} (valores em Partner Center > Identidade do produto).`)
}

module.exports = {
  appId: "dev.astra.app",
  productName: "Astra",
  copyright: "Astra",
  directories: { output: "release-store", buildResources: "build" },
  files: ["dist/**/*", "package.json"],
  asar: true,
  win: { target: [{ target: "appx", arch: ["x64"] }] },
  appx: {
    applicationId: "Astra",
    displayName: "Astra: Ecossistema de Estudos",
    identityName: process.env.ASTRA_STORE_IDENTITY_NAME,
    publisher: process.env.ASTRA_STORE_PUBLISHER,
    publisherDisplayName: process.env.ASTRA_STORE_PUBLISHER_DISPLAY_NAME,
    languages: ["pt-BR"],
    backgroundColor: "#0a0a0a",
    showNameOnTiles: false,
    artifactName: "Astra-Store-${version}.${ext}",
  },
  protocols: [{ name: "Astra", schemes: ["astra"] }],
}
