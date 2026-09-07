# 01 — Contexto

## Visão geral

Sistema de reserva de ingressos para eventos, cujo objetivo técnico central é garantir **zero overselling** sob alta concorrência — o cenário clássico de N usuários tentando reservar simultaneamente o último ingresso disponível.

Este projeto é desenvolvido em duas frentes paralelas:
1. **Portfólio técnico**: demonstrar domínio de controle de concorrência em banco de dados, arquitetura em camadas, testes automatizados e boas práticas de engenharia.
2. **Comparação empírica**: implementar e comparar, com dados reais de teste de carga, duas estratégias de controle de concorrência — **locking pessimista** (`SELECT FOR UPDATE`, este repositório) e **locking otimista** (`@Version`, repositório irmão).

## Problema que o projeto resolve

Sistemas ingênuos de reserva sofrem de *race conditions*: ler o estoque, verificar disponibilidade e escrever a reserva como passos não atômicos permite que múltiplas requisições concorrentes "vejam" a mesma unidade disponível e todas a reservem — gerando overselling. Este é um problema real e recorrente em sistemas de venda de ingressos, passagens e produtos com estoque limitado.

## Estrutura do projeto (multi-repositório)

Decisão registrada em [ADR-000](./adr/ADR-000-estrutura-repositorios.md):

- `ticket-reservation-pessimistic` (este repositório) — implementação com lock pessimista, fechada e imutável após conclusão.
- `ticket-reservation-optimistic` — implementação com lock otimista (Épico futuro).
- `ticket-reservation-benchmark` — script de carga (k6) e infraestrutura padronizada, compartilhados entre os dois repositórios para garantir comparação justa.

## Personas / Atores

- **Organizador**: cria eventos e define tipos de ingresso com quantidade limitada.
- **Comprador**: consulta disponibilidade e reserva ingressos.
- **Sistema de carga** (ator não humano): k6 simulando concorrência real para validação de RNF-001.

## Escopo desta fase (Épico 1)

Modelagem de domínio, CRUD básico de evento e tipo de ingresso, consulta de disponibilidade. **Não inclui** ainda a reserva propriamente dita nem o controle de concorrência — isso é o Épico 2.

## Fora de escopo (todo o projeto)

- Pagamento real (reserva confirmada é tratada como venda concluída, sem gateway de pagamento).
- Fila de espera (waitlist) quando esgotado.
- Multi-tenancy entre organizadores.
- Notificações (e-mail/SMS).
- Frontend — projeto é uma API REST pura.
- Workflow de publicação de evento (todo evento criado já é considerado disponível).

## Stack tecnológica

- **Linguagem/Framework**: Java 21, Spring Boot 4.1.1
- **Persistência**: PostgreSQL 17 (produção/dev), migrations via Flyway
- **Testes**: JUnit 5, Mockito (unitários), Testcontainers + Postgres real (integração)
- **Infraestrutura local**: Docker Compose
- **CI/CD**: GitHub Actions (planejado, Épico 6)
- **Cache de leitura**: Redis (planejado, Épico 4 — fora do caminho de escrita/decisão de concorrência)
