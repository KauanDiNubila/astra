-- Expande o roadmap "Frontend" (de 15 pra ~43 etapas), na mesma linha do
-- que foi feito para "Backend": cobertura de assuntos inspirada no
-- roadmap.sh, textos e links próprios deste projeto.

-- ===== Mais filhos nas etapas principais existentes =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000021', 'APIs do navegador (DOM, Web APIs)', 4, 'Manipular a página via DOM, e usar APIs nativas do navegador (localStorage, IntersectionObserver, geolocalização) sem depender de biblioteca nenhuma.'),
    ('b1000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000022', 'Ciclo de vida de componentes', 4, 'Quando um componente monta, atualiza e desmonta — e como isso se conecta com efeitos colaterais (useEffect no React, watchers no Vue).'),
    ('b1000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000023', 'GraphQL no frontend', 3, 'Consumir uma API GraphQL do lado do cliente: queries, mutations e clientes como Apollo ou urql em vez de fetch cru.'),
    ('b1000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000024', 'Gerenciadores de pacote (npm/pnpm)', 4, 'Instalar, versionar e travar dependências (package-lock/pnpm-lock), e entender a diferença de performance entre npm, yarn e pnpm.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'MDN — Web APIs', 'https://developer.mozilla.org/en-US/docs/Web/API', 0),
    ('b1000000-0000-0000-0000-000000000002', 'React — Synchronizing with Effects', 'https://react.dev/learn/synchronizing-with-effects', 0),
    ('b1000000-0000-0000-0000-000000000003', 'GraphQL — Introduction', 'https://graphql.org/learn/', 0),
    ('b1000000-0000-0000-0000-000000000004', 'pnpm Motivation', 'https://pnpm.io/motivation', 0);

-- ===== Novas etapas principais =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000020', NULL, 'Acessibilidade (a11y)', 5, 'Construir interfaces que funcionam pra quem usa leitor de tela, navega só por teclado ou tem baixa visão — não é opcional, é parte do trabalho.'),
    ('b1000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000020', NULL, 'Performance web', 6, 'Por que uma página lenta perde usuário: métricas que medem percepção real de velocidade e como melhorá-las.'),
    ('b1000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000020', NULL, 'CSS avançado', 7, 'Formas de organizar e escrever CSS que escalam além de um arquivo único gigante de regras soltas.'),
    ('b1000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000020', NULL, 'Gerenciamento de estado avançado', 8, 'Quando useState local não basta: como compartilhar estado entre componentes distantes e sincronizar dados vindos do servidor.'),
    ('b1000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000020', NULL, 'Ferramentas de qualidade', 9, 'Automação que pega problemas antes de virarem bug em produção: formatação consistente, catálogo de componentes, checagens no CI.'),
    ('b1000000-0000-0000-0000-000000000060', '00000000-0000-0000-0000-000000000020', NULL, 'PWA e recursos modernos', 10, 'Recursos que aproximam uma aplicação web de um app nativo: funcionar offline, notificar e persistir dados no dispositivo.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000010', 'MDN — Accessibility', 'https://developer.mozilla.org/en-US/docs/Web/Accessibility', 0),
    ('b1000000-0000-0000-0000-000000000020', 'web.dev — Core Web Vitals', 'https://web.dev/articles/vitals', 0),
    ('b1000000-0000-0000-0000-000000000030', 'BEM Methodology', 'https://getbem.com/introduction/', 0),
    ('b1000000-0000-0000-0000-000000000040', 'TanStack Query (React Query)', 'https://tanstack.com/query/latest', 0),
    ('b1000000-0000-0000-0000-000000000050', 'Storybook Docs', 'https://storybook.js.org/docs', 0),
    ('b1000000-0000-0000-0000-000000000060', 'web.dev — Progressive Web Apps', 'https://web.dev/explore/progressive-web-apps', 0);

-- ---- Subetapas de Acessibilidade ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000010', 'ARIA e semântica', 1, 'Usar elementos HTML semânticos primeiro, e atributos ARIA só quando o HTML nativo não é suficiente pra descrever o papel de um elemento.'),
    ('b1000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000010', 'Navegação por teclado', 2, 'Garantir que toda ação clicável também funcione com Tab e Enter, com foco visível em cada elemento interativo.'),
    ('b1000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000010', 'Contraste e leitura de tela', 3, 'Contraste mínimo de cor pra baixa visão, e como testar a página de fato com um leitor de tela (VoiceOver, NVDA).');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000011', 'WAI-ARIA Authoring Practices', 'https://www.w3.org/WAI/ARIA/apg/', 0),
    ('b1000000-0000-0000-0000-000000000012', 'MDN — Keyboard-navigable JavaScript widgets', 'https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Authoring_Practices_Guide/', 0),
    ('b1000000-0000-0000-0000-000000000013', 'WebAIM — Contrast Checker', 'https://webaim.org/resources/contrastchecker/', 0);

-- ---- Subetapas de Performance web ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000020', 'Core Web Vitals', 1, 'LCP, INP e CLS: as três métricas que o Google usa pra medir se uma página carrega rápido, responde rápido e não pula o layout.'),
    ('b1000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000020', 'Lazy loading', 2, 'Carregar imagens, componentes e rotas só quando forem realmente necessários, em vez de tudo de uma vez no carregamento inicial.'),
    ('b1000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000020', 'Otimização de imagens', 3, 'Formatos modernos (WebP/AVIF), tamanhos responsivos (srcset) e compressão — normalmente o maior ganho de performance percebida.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000021', 'web.dev — Core Web Vitals', 'https://web.dev/articles/vitals', 0),
    ('b1000000-0000-0000-0000-000000000022', 'MDN — Lazy loading', 'https://developer.mozilla.org/en-US/docs/Web/Performance/Lazy_loading', 0),
    ('b1000000-0000-0000-0000-000000000023', 'web.dev — Image optimization', 'https://web.dev/explore/images', 0);

-- ---- Subetapas de CSS avançado ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000030', 'Metodologias (BEM)', 1, 'Convenção de nomenclatura de classes (Block__Element--Modifier) que evita colisão de estilos em projetos grandes.'),
    ('b1000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000030', 'Pré-processadores (Sass)', 2, 'Variáveis, aninhamento e mixins que o CSS puro não tinha até recentemente — ainda muito usado em bases de código maduras.'),
    ('b1000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000030', 'CSS-in-JS', 3, 'Escrever estilos dentro do próprio componente JavaScript (styled-components, Emotion) — vantagens de escopo automático, custo de runtime.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000031', 'BEM Methodology', 'https://getbem.com/introduction/', 0),
    ('b1000000-0000-0000-0000-000000000032', 'Sass Documentation', 'https://sass-lang.com/documentation/', 0),
    ('b1000000-0000-0000-0000-000000000033', 'styled-components Docs', 'https://styled-components.com/docs', 0);

-- ---- Subetapas de Gerenciamento de estado avançado ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000040', 'Context API', 1, 'Compartilhar dados (tema, usuário logado) entre componentes distantes sem passar props manualmente em cada nível.'),
    ('b1000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000040', 'Bibliotecas de estado global (Redux/Zustand)', 2, 'Quando Context não é suficiente: bibliotecas dedicadas a estado global complexo, com histórico de mudanças e ferramentas de debug.'),
    ('b1000000-0000-0000-0000-000000000043', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000040', 'Cache de dados do servidor (React Query)', 3, 'Estado que vem da API é diferente de estado local: bibliotecas como React Query cuidam de cache, revalidação e loading automaticamente.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000041', 'React — Passing Data Deeply with Context', 'https://react.dev/learn/passing-data-deeply-with-context', 0),
    ('b1000000-0000-0000-0000-000000000042', 'Zustand (GitHub)', 'https://github.com/pmndrs/zustand', 0),
    ('b1000000-0000-0000-0000-000000000043', 'TanStack Query Docs', 'https://tanstack.com/query/latest/docs/framework/react/overview', 0);

-- ---- Subetapas de Ferramentas de qualidade ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000050', 'Linters e formatters', 1, 'ESLint pra pegar erros e más práticas, Prettier pra formatação consistente — rodando automaticamente antes de cada commit.'),
    ('b1000000-0000-0000-0000-000000000052', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000050', 'Storybook', 2, 'Catálogo isolado de componentes de UI, testado e documentado fora do contexto da aplicação inteira.'),
    ('b1000000-0000-0000-0000-000000000053', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000050', 'CI para frontend', 3, 'Rodar lint, testes e build automaticamente a cada push, pegando quebras antes de chegar em produção.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000051', 'ESLint Getting Started', 'https://eslint.org/docs/latest/use/getting-started', 0),
    ('b1000000-0000-0000-0000-000000000052', 'Storybook Docs', 'https://storybook.js.org/docs', 0),
    ('b1000000-0000-0000-0000-000000000053', 'GitHub Actions Docs', 'https://docs.github.com/en/actions', 0);

-- ---- Subetapas de PWA e recursos modernos ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('b1000000-0000-0000-0000-000000000061', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000060', 'Service Workers', 1, 'Script que roda em background no navegador, intercepta requisições e permite cache offline e notificações push.'),
    ('b1000000-0000-0000-0000-000000000062', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000060', 'Web Storage', 2, 'localStorage, sessionStorage e IndexedDB — onde e por quanto tempo guardar dados direto no navegador do usuário.'),
    ('b1000000-0000-0000-0000-000000000063', '00000000-0000-0000-0000-000000000020', 'b1000000-0000-0000-0000-000000000060', 'WebSockets no cliente', 3, 'Conexão bidirecional persistente com o servidor, usada pra chat, notificações em tempo real e atualizações ao vivo.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('b1000000-0000-0000-0000-000000000061', 'MDN — Service Worker API', 'https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API', 0),
    ('b1000000-0000-0000-0000-000000000062', 'MDN — Web Storage API', 'https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API', 0),
    ('b1000000-0000-0000-0000-000000000063', 'MDN — WebSockets API', 'https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API', 0);
