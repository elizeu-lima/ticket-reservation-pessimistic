# 04 — Regras de Negócio

> Diferenciação importante: regra de negócio é uma restrição do **domínio** (o que é válido ou não, independentemente de como o sistema é implementado). Requisito funcional é o **comportamento observável** do sistema. Detalhe de implementação é **como** a regra é tecnicamente aplicada. As duas últimas colunas de cada regra abaixo mostram como a regra se manifesta na prática, sem confundir os três conceitos.

---

## RN-004 — Data do evento deve ser futura

**Enunciado**: A data de um evento deve ser estritamente posterior ao momento de sua criação.

**Onde se aplica**: RF-001 (criar evento).

**Como é reforçada**: Bean Validation (`@Future`) no DTO `CreateEventRequest`, camada de entrada da API.

**Por que só uma camada aqui**: diferente de RN-005, esta é uma validação de **formato/valor de entrada**, não uma decisão de atribuição — não há necessidade de reforço adicional no banco (não existe forma simples de expressar "maior que o `NOW()` no momento da inserção" como `CHECK` constraint portável sem complexidade desproporcional ao risco).

---

## RN-005 — Quantidade disponível nasce igual à quantidade total

**Enunciado**: No momento da criação de um `TicketType`, `availableQuantity` é sempre inicializada com o mesmo valor de `totalQuantity`. Nenhum outro flufo do Épico 1 altera esse valor — a alteração de `availableQuantity` por reserva pertence ao Épico 2.

**Onde se aplica**: RF-008 (criar tipo de ingresso).

**Como é reforçada**:
1. **Camada de aplicação**: o `TicketTypeService` decide o valor de `availableQuantity` — o campo **não existe** no DTO de entrada (`CreateTicketTypeRequest`), prevenindo mass assignment (o cliente não pode enviar esse valor).
2. **Camada de banco**: `CHECK (available_quantity <= total_quantity)` — reforço defensivo, validado por teste de integração real contra Postgres.

**Por que duas camadas**: RN-005 é regra de negócio crítica o suficiente (base para RNF-001, a garantia central do projeto) para justificar defesa em profundidade — se um bug futuro na aplicação corrompesse a lógica, o banco ainda rejeitaria o estado inválido.

---

## RN-006 (proposta, não implementada) — Unicidade de nome de tipo de ingresso por evento

**Status**: Identificada durante testes manuais, **não implementada** — decisão deliberadamente adiada.

**Contexto**: atualmente é possível criar dois `TicketType` com o mesmo `name` no mesmo evento. Pode ser aceitável (ex.: lotes diferentes de venda com o mesmo nome comercial) ou pode ser uma lacuna de regra de negócio. Requer decisão de produto antes de virar regra formal.

---

## Regras a formalizar no Épico 2

- Regra de decremento atômico de `availableQuantity` na reserva (núcleo de RNF-001).
- Regra de rejeição de reserva quando estoque insuficiente.
- Regra de cancelamento e devolução de estoque (RF-005, ainda não implementado).
