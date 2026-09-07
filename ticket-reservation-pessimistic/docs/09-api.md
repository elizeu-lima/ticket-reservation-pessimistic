# 09 — API

Base URL (dev local): `http://localhost:8080`

Todas as respostas de erro seguem o formato padronizado:
```json
{
  "status": 404,
  "message": "Evento não encontrado com o ID: 99999",
  "timestamp": "2026-09-06T09:45:43.94Z"
}
```

---

## `POST /events`

Cria um evento (RF-001).

**Request body:**
```json
{
  "name": "Show de Rock",
  "date": "2027-01-15T20:00:00-03:00"
}
```

**Respostas:**
- `201 Created` — header `Location: /events/{id}`, corpo com `id`, `name`, `date`.
- `400 Bad Request` — validação falhou (nome vazio, data ausente ou no passado).

---

## `POST /events/{eventId}/ticket-types`

Cria um tipo de ingresso vinculado a um evento (RF-008).

**Request body:**
```json
{
  "name": "Pista",
  "price": 150.00,
  "totalQuantity": 100
}
```
> Nota: `eventId` vem exclusivamente da URL — não é aceito no corpo (evita duplicidade/ambiguidade).

**Respostas:**
- `201 Created` — header `Location`, corpo com `id`, `eventId`, `name`, `price`, `totalQuantity`, `availableQuantity` (sempre igual a `totalQuantity` na criação — RN-005).
- `400 Bad Request` — validação falhou (ex.: `totalQuantity <= 0`).
- `404 Not Found` — evento não existe.

---

## `GET /events/{eventId}/ticket-types`

Consulta disponibilidade de ingressos de um evento (RF-002).

**Respostas:**
- `200 OK` — array de tipos de ingresso.
- `404 Not Found` — evento não existe (decisão de design: não retorna lista vazia para evento inexistente).

**Exemplo de resposta:**
```json
[
  {
    "id": 1,
    "eventId": 1,
    "name": "Pista",
    "price": 150,
    "totalQuantity": 100,
    "availableQuantity": 100
  }
]
```

---

## Endpoints planejados (Épico 2+)

- `POST /events/{eventId}/ticket-types/{ticketTypeId}/reservations` — criar reserva (núcleo do projeto).
- `DELETE /reservations/{id}` — cancelar reserva.
