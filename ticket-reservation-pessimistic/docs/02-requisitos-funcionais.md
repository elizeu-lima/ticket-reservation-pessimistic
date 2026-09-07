# 02 — Requisitos Funcionais

> Nesta fase (Épico 1), foram implementados e validados os requisitos abaixo. Requisitos de reserva/concorrência pertencem ao Épico 2 e serão adicionados neste mesmo documento quando implementados.

---

## RF-001 — Criar evento

- **Descrição**: Permite que um organizador cadastre um novo evento.
- **Objetivo**: Estabelecer a entidade raiz sobre a qual os tipos de ingresso são vinculados.
- **Ator**: Organizador
- **Pré-condições**: Nenhuma.
- **Endpoint**: `POST /events`
- **Fluxo principal**:
  1. Organizador envia `name` e `date`.
  2. Sistema valida os dados (RN-004: data deve ser futura).
  3. Sistema persiste o evento.
  4. Sistema retorna o evento criado (201, header `Location`).
- **Exceções**:
  - Nome vazio → 400 (`@NotBlank`).
  - Data no passado ou ausente → 400 (`@Future`, `@NotNull`).
- **Regras de negócio relacionadas**: RN-004.
- **Critérios de aceitação**:
  - Payload válido → 201 com evento persistido.
  - Data no passado → 400 com mensagem clara.
- **Prioridade**: Alta
- **Status**: ✅ Implementado e testado (unitário + manual)
- **Decisão de escopo**: sem workflow de publicação — todo evento criado já está apto a receber tipos de ingresso.

---

## RF-008 — Cadastrar tipo de ingresso vinculado a um evento

- **Descrição**: Permite ao organizador definir um tipo de ingresso (ex.: "Pista", "VIP") com quantidade total disponível, vinculado a um evento existente.
- **Objetivo**: Estabelecer o "estoque" que será disputado no Épico 2.
- **Ator**: Organizador
- **Pré-condições**: Evento já existe.
- **Endpoint**: `POST /events/{eventId}/ticket-types`
- **Fluxo principal**:
  1. Organizador informa `name`, `price`, `totalQuantity` no corpo; `eventId` vem da URL (não duplicado no corpo — decisão de design REST).
  2. Sistema valida existência do evento (senão, 404).
  3. Sistema persiste o `TicketType` com `availableQuantity = totalQuantity` (RN-005).
- **Exceções**:
  - Evento inexistente → 404 (`EventNotFoundException`).
  - `totalQuantity ≤ 0` → 400 (`@Positive`).
  - Nome vazio, preço ausente/negativo → 400.
- **Regras de negócio relacionadas**: RN-005.
- **Critérios de aceitação**:
  - Evento existente + payload válido → 201, `availableQuantity == totalQuantity` na resposta.
  - Evento inexistente → 404 padronizado (`ErrorResponse`).
- **Prioridade**: Alta
- **Status**: ✅ Implementado e testado (unitário: caminho feliz + evento inexistente; integração: violação de constraint do banco)

---

## RF-002 — Consultar disponibilidade de ingressos

- **Descrição**: Permite consultar todos os tipos de ingresso de um evento e suas quantidades disponíveis.
- **Objetivo**: Fornecer visibilidade de estoque sem expor lógica de reserva.
- **Ator**: Comprador (ou anônimo)
- **Pré-condições**: Evento existe.
- **Endpoint**: `GET /events/{eventId}/ticket-types`
- **Fluxo principal**:
  1. Cliente informa `eventId` na URL.
  2. Sistema valida existência do evento (senão, 404 — decisão consciente, ver observação abaixo).
  3. Sistema retorna lista de tipos de ingresso com disponibilidade atual.
- **Exceções**: Evento inexistente → 404.
- **Decisão de design**: consultar ingressos de um evento inexistente retorna **404**, não 200 com lista vazia — escolhido para diferenciar "evento sem tipos cadastrados" de "evento não existe".
- **Observação de arquitetura**: este é o endpoint que futuramente usará cache Redis (Épico 4) — a resposta precisa ser barata de gerar/serializar.
- **Critérios de aceitação**:
  - Evento com tipos cadastrados → 200 com lista correta.
  - Evento inexistente → 404.
- **Prioridade**: Alta
- **Status**: ✅ Implementado e testado (manual)

---

## Débito de escopo identificado

Durante testes manuais, percebeu-se a ausência de um `GET /events/{id}` (consulta de evento único). Não fazia parte do escopo original do Épico 1 e não é bloqueante — o fluxo principal do sistema não depende dele —, mas está registrado aqui para avaliação futura como possível RF adicional.

## Observação de regra de negócio pendente de decisão

O sistema atualmente **permite** dois `TicketType` com o mesmo `name` no mesmo evento (ex.: dois lotes "Pista"). Não há constraint de unicidade. Isso pode ser intencional (lotes diferentes) ou pode virar uma nova regra de negócio (nome único por evento) — decisão deliberadamente adiada, não implementada.
