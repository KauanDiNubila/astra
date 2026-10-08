# Astra para Windows

App de Windows (Electron) que abre o Astra numa janela própria. O app só carrega `https://astra-app.dev`; o site continua sendo atualizado pela Vercel, sem novo instalador.

## Desenvolver

```bash
cd desktop
npm install
npm start
```

Para apontar para o front local: `ASTRA_URL=http://localhost:5173 npm start`.

## Gerar o instalador

```bash
npm run dist
```

O instalador sai em `release/Astra-Setup-<versão>.exe`. Ele não é assinado, então o Windows mostra o aviso "O Windows protegeu seu PC" na primeira instalação (Mais informações, depois Executar assim mesmo).

## Publicar uma versão

1. Suba o número em `package.json` (`version`).
2. Faça o commit e crie a tag com o mesmo número, com `v` na frente:

```bash
git tag v0.1.1
git push origin v0.1.1
```

O workflow `release-desktop.yml` gera o instalador e publica o Release no GitHub. Os apps já instalados verificam novas versões ao abrir e a cada 4 horas, baixam sozinhos e instalam quando o app for fechado.

A tag precisa bater com a versão do `package.json`, senão o workflow falha de propósito.

## Microsoft Store

O app está publicado na [Microsoft Store](https://apps.microsoft.com/detail/9NRB7QNCJGSP?hl=pt-br&gl=BR). A versão da Store é um pacote `.appx` separado do instalador do GitHub. Ela não atualiza sozinha (a Store faz isso) e usa a identidade que a Microsoft atribui ao app no Partner Center.

```bash
export ASTRA_STORE_IDENTITY_NAME="<Package/Identity/Name>"
export ASTRA_STORE_PUBLISHER="<Package/Identity/Publisher>"
export ASTRA_STORE_PUBLISHER_DISPLAY_NAME="<Package/Properties/PublisherDisplayName>"
ELECTRON_BUILDER_CACHE="C:/Users/<você>/eb-cache" npm run dist:store
```

O pacote sai em `release-store/Astra-Store-<versão>.appx`. Se o build falhar com `spawn UNKNOWN` ao rodar dentro do app do Claude no Windows, é a pasta `AppData` virtualizada: aponte `ELECTRON_BUILDER_CACHE` para uma pasta fora dela, como acima.

Os ícones da Store ficam em `build/appx/`. Os textos da ficha, as capturas de tela e o passo a passo do envio estão em `store/LISTING.md`.

## Segurança

- A página não acessa o Node.js (`contextIsolation`, `sandbox`, sem `nodeIntegration`).
- Só o domínio do Astra é carregado na janela; qualquer outro endereço abre no navegador do sistema.
- Os pedidos da página ao app passam por uma ponte pequena (`preload.ts`) e só são aceitos se vierem do domínio do Astra.
- O Electron traz o próprio Chromium. Mantenha a dependência `electron` atualizada.
