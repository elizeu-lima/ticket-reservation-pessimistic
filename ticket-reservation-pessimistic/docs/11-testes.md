# 11 — Testes

## Estratégia

Duas camadas de teste automatizado, com propósitos distintos e complementares:

| Camada | Ferramenta | O que valida | O que NÃO valida |
|---|---|---|---|
| **Unitário** | JUnit 5 + Mockito | Lógica de negócio isolada (Service) | Comportamento real do banco, mapeamento JPA, constraints SQL |
| **Integração** | JUnit 5 + Testcontainers (Postgres real) | Mapeamento JPA, migrations Flyway, constraints do banco, comportamento real do dialect Postgres | — |

**Decisão explícita: Testcontainers com Postgres real, não H2.** H2 não reproduz fielmente tipos, constraints e comportamento de locking específicos do Postgres — e o objetivo central do projeto é validar comportamento **especificamente do Postgres** sob concorrência (Épico 2). Testar contra H2 daria falsa confiança.

## Cobertura atual (Épico 1)

### `EventServiceTest` (unitário, Mockito)
- Criação de evento com sucesso → `EventResponse` correto.
- Busca por ID existente → retorna entidade.
- Busca por ID inexistente → lança `EventNotFoundException`.

### `TicketTypeServiceTest` (unitário, Mockito)
- Criação de tipo de ingresso → `availableQuantity == totalQuantity` (RN-005), com verificação de que `save()` foi chamado.
- Evento inexistente → lança `EventNotFoundException` **e** `save()` nunca é chamado (`verify(..., never())`) — confirma que a validação ocorre antes de qualquer tentativa de persistência.

### `TicketTypeRepositoryTest` (integração, `@DataJpaTest` + Testcontainers)
- Violação da constraint `chk_available_lte_total` (`available_quantity > total_quantity`) → `DataIntegrityViolationException`, usando `saveAndFlush()` para forçar o envio imediato do SQL ao banco (evitando falso negativo por causa do buffering padrão do Hibernate).

## Decisões técnicas relevantes

- **`@DataJpaTest` + `@AutoConfigureTestDatabase(replace = Replace.NONE)`**, não `@SpringBootTest`: o teste de integração valida especificamente a camada de persistência, não a aplicação inteira (Controllers, tratamento de exceção HTTP). `@SpringBootTest` traria overhead desnecessário e não seria fiel ao escopo do risco sendo testado. Sem `Replace.NONE`, o Spring substituiria automaticamente o datasource por um banco em memória, anulando o propósito de usar Testcontainers.
- **`saveAndFlush()` em vez de `save()`** no teste de constraint: o Hibernate bufferiza escritas até o *flush* (fim da transação); sem forçar o flush, a exceção de violação de constraint só apareceria fora do método de teste, tarde demais para ser capturada.
- **Container `static` + `@Testcontainers`**: garante que o container Postgres suba uma única vez por classe de teste, não uma vez por método — relevante para performance.

## Testes manuais (Postman)

Realizados antes da automação, cobrindo os três fluxos principais e casos de erro (evento inexistente, validação de Bean Validation em múltiplos campos simultaneamente). Serviram também para descobrir lacunas reais (ex.: ausência de handler para `MethodArgumentTypeMismatchException` ao enviar um `eventId` não numérico) — registradas como débito conhecido, não bloqueante.

## Pendente para Épico 2+

- Testes de concorrência real (múltiplas threads/conexões disputando o mesmo `TicketType`) — validação empírica de RNF-001.
- Testes de carga com k6 (Épico 3).
