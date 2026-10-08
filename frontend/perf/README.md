# Testes de desempenho e de cache

Scripts para medir o tempo de navegação entre páginas e conferir as regras do cache no navegador (TanStack Query). Rodam contra o backend local, com dados criados na hora.

## Pré-requisitos

- Backend local rodando em `http://localhost:8080` (`./mvnw spring-boot:run`, com o Docker Desktop aberto).
- Um Chromium para o Playwright: aponte `CHROME_PATH` para o executável (por exemplo o `chrome-headless-shell.exe` instalado pelo Playwright) ou instale com `npx playwright-core install chromium-headless-shell`.
- O backend limita a 5 logins por minuto por IP; cada script faz poucos logins, mas espere um minuto entre execuções seguidas.

## Comparar duas versões (antes e depois)

```bash
BEFORE=10bdb0d npm run perf:compare
```

O script:

1. compila a versão `BEFORE` (numa cópia temporária do repositório) e o código atual (ou `AFTER=<commit>`);
2. cria um usuário com sessões, um amigo, 3 cursos e 2 roadmaps;
3. serve cada versão em `http://localhost:3000` (`vite preview`, igual à produção);
4. faz login e percorre Ranking → Cursos → Roadmaps → Dashboard, `ROUNDS` vezes, medindo do clique no menu até o título da página aparecer com os dados;
5. atrasa cada chamada à API em cada valor de `DELAYS` (ms), simulando a rede até o servidor;
6. imprime a tabela de medianas e salva tudo em `perf/results/<data>-<NAME>.json`.

Variáveis: `BEFORE` (obrigatória), `AFTER`, `DELAYS` (padrão `50,150`), `ROUNDS` (padrão `10`), `NAME`, `PORT` (padrão `3000`, precisa estar liberada no CORS do backend).

## Conferir as regras do cache

```bash
npm run test:cache
```

Com o frontend de desenvolvimento em `http://localhost:5173` (ou `APP=...`), cria duas contas amigas e verifica que:

- voltar a uma página mostra os dados guardados sem esqueleto e confere com o servidor;
- uma sessão salva em outro aparelho aparece ao voltar para a aba;
- uma sessão salva no app já aparece na primeira exibição do Dashboard e no Ranking;
- a sessão de um amigo aparece no Ranking ao voltar para a aba;
- um roadmap criado aparece na lista;
- outra conta entrando na mesma aba nunca vê os dados da anterior.

## Resultado: cache no navegador (2026-10-08)

Antes: `10bdb0d` (sem cache). Depois: `a93deca` (TanStack Query). Mediana de 10 voltas, página já visitada. Dados brutos em [`results/2026-10-08-cache-navegador.json`](results/2026-10-08-cache-navegador.json).

**50 ms por chamada** (latência medida da API de produção com a conexão aberta):

| Página | Antes | Depois | Redução |
|---|---|---|---|
| Ranking | 103 ms | 14 ms | −86% |
| Cursos | 84 ms | 12 ms | −86% |
| Roadmaps | 90 ms | 9 ms | −90% |
| Dashboard | 179 ms | 89 ms | −50% |
| Volta completa | 456 ms | 124 ms | −73% |

**150 ms por chamada** (rede mais lenta, como 4G):

| Página | Antes | Depois | Redução |
|---|---|---|---|
| Ranking | 197 ms | 19 ms | −90% |
| Cursos | 195 ms | 11 ms | −94% |
| Roadmaps | 190 ms | 10 ms | −95% |
| Dashboard | 288 ms | 86 ms | −70% |
| Volta completa | 870 ms | 126 ms | −86% |

Leitura:

- Depois do cache o tempo não depende mais da rede (mesmo resultado com 0, 50, 150 ou 400 ms de atraso).
- Os ~85 ms que sobram no Dashboard são o tempo de desenhar o heatmap de um ano e os gráficos, não espera de rede.
- A primeira visita a cada página não muda; o ganho aparece ao voltar a uma página.
- A simulação não inclui o tempo de consulta ao banco de produção, que o "antes" esperava e o "depois" não espera mais; os percentuais são uma estimativa conservadora.
