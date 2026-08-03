# Astra

App web para organizar estudos e trabalho de forma prática, com um gancho social
(ranking entre amigos) para dar consistência. A unidade fundamental é a **sessão**:
todo tempo focado vira uma sessão registrada e tudo o mais — ranking, heatmap,
streak, estatísticas e progresso — é **calculado por agregação sobre as sessões**,
nunca armazenado como tabela.

> Projeto de portfólio. Interface em português, código em inglês. O primeiro
> usuário é o próprio autor (dogfooding).

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS) |
| Framework | Spring Boot 4.0.7 (sobre Spring Framework 7) |
| Build | Maven |
| Banco | PostgreSQL 17 |
| Migrations | Flyway |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security (JWT, a partir da fatia 2) |
| Validação | Bean Validation |
| Docs de API | SpringDoc OpenAPI (Swagger UI) |
| Testes | JUnit 5, Testcontainers (Postgres real) |
| Dev local | Docker Compose (Postgres) |
| Utilitário | Lombok |

## Como rodar (dev)

Pré-requisito: **Java 21** e **Docker Desktop** ligado.

```bash
./mvnw spring-boot:run
```

O `spring-boot-docker-compose` sobe o Postgres do [`compose.yaml`](compose.yaml)
automaticamente e conecta a aplicação — não é preciso configurar datasource à mão.

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Banco (dev): `localhost:5432`, db/user/senha `astra`

Para rodar os testes (também usam Docker, via Testcontainers):

```bash
./mvnw test
```

## Documentação

- [Como contribuir](CONTRIBUTING.md) — fluxo de branches, commits e issues (para quem forkar).
- O planejamento é acompanhado pelos **milestones e issues** do repositório.

## Status

Em desenvolvimento — **Fatia 1 (núcleo: registrar tempo)**. Ver os
[milestones e issues](../../milestones) do repositório.
