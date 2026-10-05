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

## Segurança

- A página não acessa o Node.js (`contextIsolation`, `sandbox`, sem `nodeIntegration`).
- Só o domínio do Astra é carregado na janela; qualquer outro endereço abre no navegador do sistema.
- Os pedidos da página ao app passam por uma ponte pequena (`preload.ts`) e só são aceitos se vierem do domínio do Astra.
- O Electron traz o próprio Chromium. Mantenha a dependência `electron` atualizada.
