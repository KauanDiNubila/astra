-- "Backend Java" era específico demais para quem não usa Java. Generaliza
-- o roadmap para "Backend": a maior parte do conteúdo (Banco de Dados, APIs,
-- Cache, Mensageria, Observabilidade, Containers, Design de sistemas,
-- Segurança) já é agnóstica de linguagem. Só os dois primeiros marcos citam
-- Java/Spring diretamente — passam a ser apresentados como "exemplo de
-- caminho", não o único caminho.

UPDATE roadmap SET title = 'Backend' WHERE id = '00000000-0000-0000-0000-000000000010';

UPDATE roadmap_step
SET title = 'Fundamentos da linguagem (ex.: Java)',
    description = 'Sintaxe, tipos, orientação a objetos, coleções, generics, concorrência e tratamento de exceções — os mesmos conceitos existem em Java, C#, Kotlin ou Python, só a sintaxe muda.'
WHERE id = '00000000-0000-0000-0000-000000000011';

UPDATE roadmap_step
SET title = 'Framework web (ex.: Spring Boot)',
    description = 'Um framework que cuida de injeção de dependência, roteamento HTTP e acesso a dados — Spring Boot em Java, mas o mesmo papel é cumprido por Django, Express ou ASP.NET em outras linguagens.'
WHERE id = '00000000-0000-0000-0000-000000000012';
