# ADR-002 — Locking pessimista (`SELECT FOR UPDATE`) como estratégia de concorrência deste repositório

## Status
Aceito (âmbito: este repositório especificamente — o repositório irmão `ticket-reservation-optimistic` implementa a alternativa)

## Problema
Sob alta concorrência, múltiplas requisições podem ler o mesmo valor de `availableQuantity` antes que qualquer uma escreva sua decrementação, resultando em **lost update** (atualização perdida) e overselling — venda de mais ingressos do que o estoque real permite.

### Mecânica do problema (anomalia "lost update")

Com isolamento padrão do Postgres (`READ_COMMITTED`), duas transações concorrentes podem:
1. Ambas ler `availableQuantity = 1`.
2. Ambas decidirem, em memória, que a reserva é válida (`1 > 0`).
3. Ambas escreverem `availableQuantity = 0` e commitarem com sucesso.

Resultado: 2 reservas confirmadas para 1 unidade real de estoque. O nível `READ_COMMITTED` só impede leitura de dados não commitados ("dirty read") — não impede que duas transações leiam o mesmo estado "limpo, porém obsoleto" antes de qualquer uma escrever.

## Alternativas consideradas

| Estratégia | Onde vive a garantia | Comportamento sob contenção | Complexidade operacional |
|---|---|---|---|
| **Locking pessimista** (`SELECT ... FOR UPDATE`) | Postgres (nativo) | Serializa acesso à linha disputada — segunda transação espera na fila até a primeira commitar/dar rollback | Baixa |
| **Locking otimista** (`@Version`) | Postgres (nativo) | Não bloqueia leitura; conflito só é detectado na escrita, exigindo retry na aplicação | Baixa-média (precisa de lógica de retry) |
| **Lock distribuído** (Redis/Redlock) | Sistema externo | Depende da implementação; risco de bugs sutis (expiração do lock antes da operação terminar) | Alta |

## Decisão
Locking pessimista via `SELECT ... FOR UPDATE`, neste repositório.

## Justificativa
O recurso disputado (`availableQuantity`) já vive inteiramente dentro de uma transação ACID única no Postgres — não há motivo para mover a fonte de verdade da consistência para um sistema externo (Redis), que resolveria um problema que o próprio banco já resolve nativamente e de forma mais simples.

`SELECT FOR UPDATE` adquire um lock de linha no momento da leitura — não apenas no momento da escrita — impedindo que uma segunda transação leia (e decida com base em) um valor que já está prestes a ser alterado por outra transação em andamento. O lock é liberado automaticamente no `COMMIT` ou `ROLLBACK`.

## Trade-offs
- **Ganho**: implementação simples, correção fácil de raciocinar e demonstrar; garantia forte e nativa do banco.
- **Perda**: sob altíssima contenção (muitas requisições disputando exatamente a mesma linha), o acesso é inteiramente serializado — throughput máximo para aquele recurso específico é limitado pela "fila" de espera do lock. Esse é exatamente o trade-off que será medido empiricamente contra o locking otimista no Épico 3, com dados reais de teste de carga (k6), documentados em `ticket-reservation-benchmark`.

## Consequências
- Leituras **sem** `FOR UPDATE` (ex.: `GET /events/{id}/ticket-types`) não são bloqueadas pelo lock — o MVCC do Postgres permite consultas concorrentes sem espera, mesmo durante uma reserva em andamento.
- Toda exceção lançada dentro do método `@Transactional` de reserva provoca rollback automático (comportamento padrão do Spring para `RuntimeException`), liberando o lock imediatamente para a próxima transação da fila.
