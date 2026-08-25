-- Novo roadmap predefinido "Mobile", completando o conjunto de 5 trilhas
-- (Backend, Frontend, Full-Stack Web, DevOps, Mobile). Cobertura de
-- assuntos inspirada no roadmap.sh (Android/iOS/React Native), textos e
-- links próprios deste projeto.

INSERT INTO roadmap (id, owner_id, title, source) VALUES
    ('00000000-0000-0000-0000-000000000050', NULL, 'Mobile', 'roadmap.sh');

-- ===== Etapas principais =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('00000000-0000-0000-0000-000000000051', '00000000-0000-0000-0000-000000000050', NULL, 'Fundamentos mobile', 1, 'Conceitos que valem pra qualquer app de celular, antes de escolher framework: ciclo de vida, UI declarativa e as diferenças entre plataformas.'),
    ('00000000-0000-0000-0000-000000000052', '00000000-0000-0000-0000-000000000050', NULL, 'Escolha de stack', 2, 'Nativo (uma linguagem por plataforma) ou multiplataforma (um código pra ambas) — a decisão que mais afeta o resto do projeto.'),
    ('00000000-0000-0000-0000-000000000053', '00000000-0000-0000-0000-000000000050', NULL, 'Interface e navegação', 3, 'Construir telas que se adaptam a tamanhos de tela diferentes e permitir que o usuário se mova entre elas.'),
    ('00000000-0000-0000-0000-000000000054', '00000000-0000-0000-0000-000000000050', NULL, 'Dados e estado', 4, 'Guardar dados no dispositivo, gerenciar estado da aplicação e lidar com o usuário ficando sem internet no meio do caminho.'),
    ('00000000-0000-0000-0000-000000000055', '00000000-0000-0000-0000-000000000050', NULL, 'Conectividade', 5, 'Como o app conversa com o mundo exterior: APIs, notificações push e conexões em tempo real.'),
    ('00000000-0000-0000-0000-000000000056', '00000000-0000-0000-0000-000000000050', NULL, 'Publicação e distribuição', 6, 'O caminho entre "funciona no meu emulador" e o app estar instalado no celular de um usuário de verdade.'),
    ('00000000-0000-0000-0000-000000000057', '00000000-0000-0000-0000-000000000050', NULL, 'Qualidade', 7, 'Garantir que o app funciona bem em dispositivos reais, não só no ambiente controlado de desenvolvimento.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('00000000-0000-0000-0000-000000000051', 'Android Developers — App Fundamentals', 'https://developer.android.com/guide/components/fundamentals', 0),
    ('00000000-0000-0000-0000-000000000052', 'React Native vs Native (documentação oficial)', 'https://reactnative.dev/architecture/overview', 0),
    ('00000000-0000-0000-0000-000000000053', 'Material Design 3', 'https://m3.material.io/', 0),
    ('00000000-0000-0000-0000-000000000054', 'React Native — AsyncStorage', 'https://reactnative.dev/docs/asyncstorage', 0),
    ('00000000-0000-0000-0000-000000000055', 'Firebase Cloud Messaging Docs', 'https://firebase.google.com/docs/cloud-messaging', 0),
    ('00000000-0000-0000-0000-000000000056', 'Play Console — Publish your app', 'https://developer.android.com/distribute/best-practices/launch', 0),
    ('00000000-0000-0000-0000-000000000057', 'Firebase Crashlytics Docs', 'https://firebase.google.com/docs/crashlytics', 0);

-- ---- Subetapas de Fundamentos mobile ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000051', 'Ciclo de vida de app', 1, 'Estados que um app assume (em primeiro plano, em segundo plano, encerrado) e os callbacks que o sistema operacional dispara em cada transição.'),
    ('e1000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000051', 'UI declarativa vs imperativa', 2, 'Descrever como a tela deve parecer dado um estado (SwiftUI, Jetpack Compose, React Native) em vez de comandar manualmente cada mudança visual.'),
    ('e1000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000051', 'Diferenças Android/iOS', 3, 'Convenções de navegação, permissões e ciclo de revisão de loja que mudam de uma plataforma pra outra, mesmo usando o mesmo framework.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'Android — Activity Lifecycle', 'https://developer.android.com/guide/components/activities/activity-lifecycle', 0),
    ('e1000000-0000-0000-0000-000000000002', 'Jetpack Compose Docs', 'https://developer.android.com/jetpack/compose/documentation', 0),
    ('e1000000-0000-0000-0000-000000000003', 'Apple Human Interface Guidelines', 'https://developer.apple.com/design/human-interface-guidelines', 0);

-- ---- Subetapas de Escolha de stack ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000052', 'Nativo (Kotlin/Swift)', 1, 'Uma linguagem por plataforma, acesso total às APIs do sistema operacional, melhor performance — ao custo de manter duas bases de código.'),
    ('e1000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000052', 'Multiplataforma (React Native/Flutter)', 2, 'Um código-fonte compartilhado entre Android e iOS — mais velocidade de desenvolvimento, com camadas de abstração a mais entre você e o SO.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000004', 'Kotlin for Android', 'https://developer.android.com/kotlin', 0),
    ('e1000000-0000-0000-0000-000000000005', 'Flutter Documentation', 'https://docs.flutter.dev/', 0);

-- ---- Subetapas de Interface e navegação ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000053', 'Layouts responsivos', 1, 'Adaptar a interface a telas de tamanhos e densidades de pixel muito diferentes, de um celular pequeno a um tablet.'),
    ('e1000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000053', 'Navegação entre telas', 2, 'Pilhas de navegação, abas e modais — os padrões que o usuário já espera de qualquer app mobile.'),
    ('e1000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000053', 'Listas e performance de scroll', 3, 'Renderizar listas longas sem travar: virtualização, pra só desenhar os itens visíveis na tela.'),
    ('e1000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000053', 'Design system mobile (Material/Human Interface)', 4, 'Seguir as diretrizes visuais de cada plataforma (Material Design no Android, Human Interface no iOS) faz o app parecer nativo de verdade.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000006', 'Android — Support Different Screen Sizes', 'https://developer.android.com/training/multiscreen/screensizes', 0),
    ('e1000000-0000-0000-0000-000000000007', 'React Navigation Docs', 'https://reactnavigation.org/docs/getting-started', 0),
    ('e1000000-0000-0000-0000-000000000008', 'React Native — FlatList Performance', 'https://reactnative.dev/docs/optimizing-flatlist-configuration', 0),
    ('e1000000-0000-0000-0000-000000000009', 'Material Design 3', 'https://m3.material.io/', 0);

-- ---- Subetapas de Dados e estado ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000054', 'Armazenamento local (SQLite/AsyncStorage)', 1, 'Persistir dados direto no dispositivo pra funcionar sem depender de conexão de rede o tempo todo.'),
    ('e1000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000054', 'Gerenciamento de estado', 2, 'Compartilhar dados entre telas diferentes do app (usuário logado, carrinho de compras) de forma previsível.'),
    ('e1000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000054', 'Sincronização offline-first', 3, 'Deixar o usuário continuar usando o app sem internet, e sincronizar as mudanças com o servidor quando a conexão voltar.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000010', 'Android — Room Persistence Library', 'https://developer.android.com/training/data-storage/room', 0),
    ('e1000000-0000-0000-0000-000000000011', 'Zustand (GitHub)', 'https://github.com/pmndrs/zustand', 0),
    ('e1000000-0000-0000-0000-000000000012', 'WatermelonDB (offline-first)', 'https://watermelondb.dev/', 0);

-- ---- Subetapas de Conectividade ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000055', 'Consumo de APIs REST', 1, 'Buscar e enviar dados pro backend, tratando perda de conexão e timeout — mais comum em mobile do que em web.'),
    ('e1000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000055', 'Push notifications', 2, 'Alertar o usuário mesmo com o app fechado, via um serviço de push do sistema operacional (FCM no Android, APNs no iOS).'),
    ('e1000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000055', 'WebSockets em mobile', 3, 'Conexões em tempo real (chat, localização ao vivo) precisam lidar com o app indo pra background e a rede caindo com frequência.'),
    ('e1000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000055', 'Deep linking', 4, 'Abrir o app direto numa tela específica a partir de um link externo (e-mail, outra rede social, notificação).');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000013', 'MDN — Fetch API', 'https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API', 0),
    ('e1000000-0000-0000-0000-000000000014', 'Firebase Cloud Messaging Docs', 'https://firebase.google.com/docs/cloud-messaging', 0),
    ('e1000000-0000-0000-0000-000000000015', 'MDN — WebSockets API', 'https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API', 0),
    ('e1000000-0000-0000-0000-000000000016', 'Android — Deep Links', 'https://developer.android.com/training/app-links/deep-linking', 0);

-- ---- Subetapas de Publicação e distribuição ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000056', 'Assinatura de app', 1, 'Certificado que identifica o desenvolvedor e garante que atualizações futuras vêm de quem publicou o app originalmente.'),
    ('e1000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000056', 'Google Play / App Store', 2, 'Processo de submissão, revisão e as políticas de cada loja — nem sempre técnico, mas sempre parte do trabalho de lançar um app.'),
    ('e1000000-0000-0000-0000-000000000019', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000056', 'Versionamento e releases', 3, 'Version code/name, rollout gradual e como reverter rápido se uma versão nova quebrar algo em produção.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000017', 'Android — Sign your app', 'https://developer.android.com/studio/publish/app-signing', 0),
    ('e1000000-0000-0000-0000-000000000018', 'Play Console — Launch checklist', 'https://developer.android.com/distribute/best-practices/launch', 0),
    ('e1000000-0000-0000-0000-000000000019', 'Android — Staged rollouts', 'https://support.google.com/googleplay/android-developer/answer/6346149', 0);

-- ---- Subetapas de Qualidade ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('e1000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000057', 'Testes de UI mobile', 1, 'Automatizar interações reais na tela (tocar, deslizar, digitar) pra pegar regressões visuais e de fluxo antes do usuário.'),
    ('e1000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000057', 'Crash reporting', 2, 'Saber que o app quebrou no celular de um usuário real, com stack trace e contexto, sem depender dele reportar manualmente.'),
    ('e1000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000050', '00000000-0000-0000-0000-000000000057', 'Performance e battery usage', 3, 'Um app que esquenta o celular ou consome bateria rápido demais é desinstalado, não importa quão boa seja a funcionalidade.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('e1000000-0000-0000-0000-000000000020', 'Detox (E2E testing for React Native)', 'https://wix.github.io/Detox/', 0),
    ('e1000000-0000-0000-0000-000000000021', 'Firebase Crashlytics Docs', 'https://firebase.google.com/docs/crashlytics', 0),
    ('e1000000-0000-0000-0000-000000000022', 'Android — App Performance', 'https://developer.android.com/topic/performance', 0);
