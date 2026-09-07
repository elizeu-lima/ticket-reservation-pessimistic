# 03 — Requisitos Não Funcionais

> A maioria dos RNFs mensuráveis (performance, throughput) pertence ao Épico 3 (testes de carga), quando haverá dados reais para fixar números. Nesta fase, registram-se os RNFs já aplicáveis ao Épico 1 e os que permanecem em aberto.

## RNF-001 — Consistência sob concorrência

**Categoria**: Confiabilidade / Concorrência
**Descrição**: Sob N requisições concorrentes disputando o mesmo `TicketType` com estoque insuficiente para todas, exatamente `min(N, availableQuantity)` devem ter sucesso — nenhuma reserva além do estoque real.
**Status**: A DEFINIR o valor de N — depende do teste de carga do Épico 3. Este é o requisito não funcional **central** de todo o projeto.
**Estratégia planejada**: `SELECT FOR UPDATE` (locking pessimista) no Épico 2, comparado empiricamente contra locking otimista (`@Version`) no repositório irmão.

## RNF-002 — Performance

**Categoria**: Performance
**Descrição**: Latência do endpoint de reserva sob carga X.
**Status**: A DEFINIR — depende de baseline a ser medido no Épico 3 com k6. Não é possível fixar um número (ex.: "P95 < 200ms") sem antes medir o comportamento real da aplicação sob o hardware de teste disponível.

## RNF-003 — Observabilidade

**Categoria**: Observabilidade
**Descrição**: Toda decisão de aceitar/rejeitar uma requisição de reserva deve ser rastreável via log estruturado com correlation ID.
**Status**: Planejado para o Épico 6. Nesta fase, a aplicação usa apenas logging padrão do Spring Boot.

## RNF-004 — Testabilidade

**Categoria**: Testabilidade
**Descrição**: Lógica de negócio deve ser testável isoladamente, sem depender de infraestrutura externa.
**Status**: ✅ Atendido no Épico 1 — arquitetura em camadas (Controller/Service/Repository) permite testar Services com Mockito sem subir banco ou contexto Spring completo. Ver [11-testes.md](./11-testes.md).
**Decisão consciente**: não se adotou "% de cobertura mínima" como métrica formal — cobertura de cenários críticos (regra de negócio, casos de erro) é priorizada sobre cobertura numérica genérica, para evitar métrica de vaidade (seção de anti-overengineering do processo).

## RNF-005 — Reprodutibilidade de ambiente

**Categoria**: Manutenibilidade / DX
**Descrição**: O ambiente de desenvolvimento deve subir localmente via `docker compose up -d`, sem configuração manual além de variáveis de ambiente documentadas.
**Status**: ✅ Atendido — `docker-compose.yml` + `.env.example` documentam exatamente o necessário.

## RNF-006 — Consistência do schema com a camada de aplicação

**Categoria**: Manutenibilidade
**Descrição**: Regras de negócio críticas (ex.: `availableQuantity <= totalQuantity`) devem ser reforçadas em mais de uma camada — Bean Validation na entrada e `CHECK` constraint no banco — para evitar que um bug de aplicação futuro corrompa dados diretamente.
**Status**: ✅ Atendido e validado por teste de integração (`TicketTypeRepositoryTest`), que confirma que o banco rejeita a violação mesmo se a camada de aplicação falhar em barrá-la.

## RNF-007 — Compatibilidade de versão

**Categoria**: Compatibilidade
**Descrição**: O projeto deve rodar de forma consistente na versão de stack declarada (Spring Boot 4.1.1, Java 21).
**Status**: ✅ Atendido, após correção de inconsistência identificada durante o desenvolvimento (ver `docs/aprendizado.md` — mudança acidental de versão do `pom.xml` durante troubleshooting).
