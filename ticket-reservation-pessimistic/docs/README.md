# Ticket Reservation — Pessimistic Locking

Sistema de reserva de ingressos construído para resolver, de forma comprovada e mensurável, o problema clássico de **overselling sob concorrência**: garantir que N compradores disputando simultaneamente o último ingresso disponível nunca resultem em mais vendas do que o estoque real permite.

Este é o repositório da implementação com **locking pessimista** (`SELECT FOR UPDATE`). Ele faz parte de um projeto maior de três repositórios, comparando empiricamente duas estratégias de controle de concorrência — ver [ADR-000](./docs/adr/ADR-000-estrutura-repositorios.md).

## Status atual

**Épico 1 (modelagem de domínio) concluído.** Épico 2 (reserva com lock pessimista) em andamento — ver [roadmap completo](./docs/15-roadmap.md).

## Stack

Java 21 · Spring Boot 4.1.1 · PostgreSQL 17 · Flyway · JUnit 5 + Mockito · Testcontainers · Docker Compose

## Como executar localmente

```bash
# 1. Configurar variáveis de ambiente
cp .env.example .env

# 2. Subir o banco
docker compose up -d

# 3. Rodar a aplicação
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Ver endpoints disponíveis em [docs/09-api.md](./docs/09-api.md).

## Como rodar os testes

```bash
./mvnw test
```

Requer Docker ativo (os testes de integração usam Testcontainers para subir um Postgres real e descartável).

## Documentação

| Documento | Conteúdo |
|---|---|
| [01-contexto.md](./docs/01-contexto.md) | Problema, objetivos, escopo |
| [02-requisitos-funcionais.md](./docs/02-requisitos-funcionais.md) | RFs detalhados |
| [03-requisitos-nao-funcionais.md](./docs/03-requisitos-nao-funcionais.md) | RNFs, incluindo os ainda "A DEFINIR" |
| [04-regras-de-negocio.md](./docs/04-regras-de-negocio.md) | RNs e como são reforçadas |
| [05-casos-de-uso.md](./docs/05-casos-de-uso.md) | Casos de uso, incluindo o cenário central de concorrência |
| [06-arquitetura.md](./docs/06-arquitetura.md) | Camadas, responsabilidades, diagrama |
| [adr/](./docs/adr/) | Decisões arquiteturais registradas (Flyway, estrutura de repositórios, estratégia de lock) |
| [08-modelagem-de-dados.md](./docs/08-modelagem-de-dados.md) | Schema, diagrama ER |
| [09-api.md](./docs/09-api.md) | Endpoints, payloads, respostas |
| [10-seguranca.md](./docs/10-seguranca.md) | O que já existe e débitos conscientes |
| [11-testes.md](./docs/11-testes.md) | Estratégia e cobertura de testes |
| [15-roadmap.md](./docs/15-roadmap.md) | Próximos épicos |
| [aprendizado.md](./docs/aprendizado.md) | Registro de aprendizado técnico ao longo do desenvolvimento |

## O problema central, resumido

Com o nível de isolamento padrão do Postgres (`READ_COMMITTED`), duas transações concorrentes podem ambas ler o mesmo estoque disponível, ambas decidirem que a venda é válida, e ambas confirmarem — resultando em overselling (anomalia conhecida como *lost update*). Este projeto resolve isso adquirindo lock **na leitura** (`SELECT ... FOR UPDATE`), não apenas na escrita, serializando o acesso à linha disputada. Detalhes completos em [ADR-002](./docs/adr/ADR-002-estrategia-lock-pessimista.md).
