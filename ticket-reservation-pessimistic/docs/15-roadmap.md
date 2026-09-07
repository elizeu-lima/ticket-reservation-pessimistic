# 15 — Roadmap

## Épico 1 — Modelagem de domínio ✅ Concluído

Evento, tipo de ingresso, consulta de disponibilidade. Base arquitetural (camadas, DTOs, tratamento de exceção, testes unitários + integração) estabelecida.

## Épico 2 — Reserva com controle de concorrência (lock pessimista) — Próximo

- Modelagem da entidade `Reservation`.
- Endpoint de reserva com `SELECT FOR UPDATE` (ADR-002).
- Revisão do `CascadeType`/`ON DELETE CASCADE` entre `Event` → `TicketType` (e agora `Reservation`), evitando exclusão em cascata de dados de compradores reais.
- Testes de concorrência real (múltiplas threads/conexões).
- RF-005 (cancelamento de reserva).

## Épico 3 — Testes de carga e validação de RNF-001

- Script k6 no repositório `ticket-reservation-benchmark`.
- Medição de "zero overselling" sob N requisições concorrentes documentada.
- Baseline de throughput/latência (fixando os valores hoje marcados como "A DEFINIR" em `docs/03-requisitos-nao-funcionais.md`).

## Épico 4 — Cache de leitura (Redis)

- Cache **apenas** no caminho de leitura (`GET /events/{id}/ticket-types`) — fora da decisão de concorrência (ADR sobre o papel do Redis).
- Comparação de throughput de leitura com/sem cache.

## Épico 5 — Implementação alternativa (locking otimista)

- Repositório irmão `ticket-reservation-optimistic`, com `@Version`.
- Comparação empírica de throughput entre as duas estratégias, usando o mesmo harness de `ticket-reservation-benchmark`.

## Épico 6 — CI/CD, observabilidade

- GitHub Actions rodando testes a cada push.
- Logs estruturados com correlation ID.
- Cobertura via JaCoCo (métrica complementar, não substituta de cobertura de cenários críticos).

## Épico 7 — Documentação final e apresentação de portfólio

- README consolidado com problema, arquitetura, decisões, desafios, aprendizados.
- Revisão final de todos os documentos em `/docs`.
