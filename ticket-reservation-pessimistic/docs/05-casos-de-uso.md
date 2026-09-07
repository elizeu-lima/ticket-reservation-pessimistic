# 05 — Casos de Uso

## UC-01 — Cadastro completo de evento com ingressos (Épico 1)

**Ator principal**: Organizador

**Fluxo**:
1. Organizador cria um evento (`POST /events`).
2. Organizador cadastra um ou mais tipos de ingresso para esse evento (`POST /events/{eventId}/ticket-types`), cada um com sua quantidade total.
3. Sistema garante que a quantidade disponível nasce igual à quantidade total (RN-005).

**Pós-condição**: evento pronto para consulta pública de disponibilidade.

**RFs envolvidos**: RF-001, RF-008.

---

## UC-02 — Consulta de disponibilidade (Épico 1)

**Ator principal**: Comprador (ou anônimo)

**Fluxo**:
1. Comprador consulta os tipos de ingresso disponíveis de um evento (`GET /events/{eventId}/ticket-types`).
2. Sistema retorna a lista com quantidades atuais.

**Fluxo alternativo**: evento não existe → 404.

**RFs envolvidos**: RF-002.

---

## UC-03 — Reserva concorrente disputada (Épico 2 — planejado)

**Ator principal**: Comprador (múltiplas instâncias simultâneas)

**Descrição**: N compradores tentam reservar simultaneamente o último ingresso disponível de um `TicketType`. Exatamente `min(N, availableQuantity)` reservas devem ter sucesso — nenhuma reserva além do estoque real, mesmo sob concorrência real.

**Este é o caso de uso central do projeto** — a razão de existir da comparação entre locking pessimista e otimista, e o alvo do teste de carga do Épico 3.

**RNF envolvido**: RNF-001.

---

## UC-04 — Cancelamento de reserva (Épico 2 — planejado)

**Ator principal**: Comprador

**Descrição**: Comprador cancela uma reserva already confirmada, devolvendo a unidade ao estoque disponível (`availableQuantity` incrementado).

**RF envolvido**: RF-005 (a ser detalhado no Épico 2).
