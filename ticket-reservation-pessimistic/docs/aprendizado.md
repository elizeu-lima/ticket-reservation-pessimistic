# Registro de Aprendizado

> Registrado ao longo do Épico 1, enquanto os detalhes ainda estavam frescos. Objetivo: evidência concreta de evolução técnica, não só um changelog do que foi feito.

---

## Arquitetura em camadas

**Conceito**: Controller deve ser "agnóstico a transporte" — não conhecer regra de negócio, não decidir dados, apenas traduzir HTTP ↔ chamada de Service.

**Por que importa na prática, não só em teoria**: se uma regra (ex. RN-005) estivesse no Controller, qualquer novo ponto de entrada (endpoint de importação em lote, comando administrativo, consumidor de fila) precisaria duplicá-la. Com a regra no Service, qualquer novo ponto de entrada reaproveita de graça. Também é o motivo de testar Service com Mockito ser simples e rápido — nenhuma dependência de HTTP real.

**Evidência concreta no projeto**: `TicketTypeService.createTicketType` recebe `Long eventId` e um DTO puro — nunca `HttpServletRequest` nem anotações web. Poderia ser chamado de qualquer lugar sem mudança.

---

## N+1 queries, LAZY vs. EAGER, e `open-in-view`

**O problema**: `@ManyToOne` é `EAGER` por padrão no JPA — busca o relacionamento mesmo quando não vai ser usado. Sob volume (ex. listar 50 `TicketType` acessando `.getEvent()` de cada um), isso vira 1 query inicial + N queries adicionais (uma por item) — o clássico "problema N+1", que destrói throughput sob carga por aumentar round-trips ao banco linearmente com o tamanho da lista.

**Decisão tomada**: `TicketType.event` mapeado como `LAZY`, porque nenhum fluxo atual (RF-002 especificamente) precisa navegar de `TicketType` até os dados completos de `Event` — o `eventId` já vem pela URL da requisição.

**Detalhe técnico capturado**: mesmo com `LAZY`, `ticketType.getEvent().getId()` funciona sem exigir sessão ativa — porque o Hibernate responde direto do proxy usando a FK já presente na linha, sem query adicional. Já `.getEvent().getName()` exigiria buscar o `Event` de verdade, e falharia fora de uma transação.

**`spring.jpa.open-in-view=false`**: desligado deliberadamente. Por padrão (`true`), a conexão com o banco fica aberta durante toda a renderização da resposta HTTP, permitindo que relacionamentos LAZY sejam acessados "por acidente" durante a serialização — mascarando N+1 silenciosamente. Desligar força o erro (`LazyInitializationException`) a aparecer no lugar certo (dentro do Service, dentro da transação), em vez de mascarado.

---

## Mass assignment e DTOs

**Conceito**: nunca expor a entidade JPA diretamente como corpo de request/response.

**Motivos concretos, não genéricos**:
1. Mass assignment — sem DTO de entrada restrito, nada impediria um cliente de enviar `availableQuantity` manualmente na criação, violando RN-005.
2. Acoplamento schema↔contrato — mudar uma coluna no banco não deveria quebrar o contrato público da API.
3. Serialização de LAZY fora de transação pode gerar `LazyInitializationException` se a entidade completa for exposta.

**Aplicado no projeto**: `CreateTicketTypeRequest` não tem campo `id` nem `availableQuantity` — só o que o cliente deveria poder controlar.

---

## Lost update (race condition clássica) — a base conceitual do projeto

Com isolamento `READ_COMMITTED` (padrão do Postgres), duas transações concorrentes podem ambas ler o mesmo `availableQuantity`, ambas decidirem "posso vender" em memória, e ambas escreverem com sucesso — resultando em overselling. O lock do `UPDATE` protege a escrita física da linha, mas não protege a *decisão de negócio* já tomada antes dele existir com base numa leitura obsoleta.

**Solução adotada (ADR-002)**: `SELECT ... FOR UPDATE` — adquire lock **na leitura**, não só na escrita, forçando serialização de acesso à linha disputada. Detalhe importante: leituras comuns (sem `FOR UPDATE`) não são bloqueadas — o MVCC do Postgres permite consultas concorrentes sem espera.

---

## Ferramentas e decisões de dependência

### Flyway vs. Liquibase
Flyway escolhido por manter SQL explícito (alinhado ao foco do projeto em comportamento específico de Postgres). Ver ADR-001.

### Spring Boot 4 — reestruturação de pacotes (armadilha recorrente)
O Spring Boot 4 renomeou artifacts e **moveu pacotes** de classes que antes eram estáveis há anos:
- `spring-boot-starter-web` → `spring-boot-starter-webmvc`
- Starters de teste ganharam versões "slice" específicas (`spring-boot-starter-data-jpa-test`, etc.) além do genérico `spring-boot-starter-test`.
- `@MockBean` foi removido, substituído por `@MockitoBean`.
- `@DataJpaTest`: `org.springframework.boot.test.autoconfigure.orm.jpa` → `org.springframework.boot.data.jpa.test.autoconfigure`
- `@AutoConfigureTestDatabase`: `org.springframework.boot.test.autoconfigure.jdbc` → `org.springframework.boot.jdbc.test.autoconfigure`

**Lição principal**: tutoriais e conhecimento prévio (inclusive de LLMs com corte de treinamento anterior a essas mudanças) podem estar desatualizados para versões recentes — vale sempre verificar contra documentação oficial/javadoc da versão exata em uso antes de assumir um caminho de pacote.

### Testcontainers 2.x — renomeação de artifacts
`org.testcontainers:junit-jupiter` e `org.testcontainers:postgresql` (nomes usados até a série 1.x) foram renomeados para `testcontainers-junit-jupiter` e `testcontainers-postgresql` na série 2.x. Gerenciamento de versão via `testcontainers-bom`, importado dentro de `<dependencyManagement>` (não em `<dependencies>` — erro cometido e corrigido no processo).

---

## Erro processual real: mudança de versão não intencional

Durante troubleshooting sob frustração acumulada, o `<parent>` do `pom.xml` foi revertido de Spring Boot `4.1.1` para `3.3.4` sem decisão consciente — como efeito colateral de tentativas sucessivas de corrigir erros do Testcontainers. Isso gerou uma cadeia de sintomas confusos (versões de Hibernate diferentes entre execuções, artifacts com nomes antigos) até a causa raiz ser identificada comparando o `<parent><version>` do arquivo com os logs.

**Lição**: sob muitos erros seguidos, é fácil perder rastreabilidade de qual mudança causou qual sintoma. Vale, nesses momentos, parar e comparar o arquivo de configuração inteiro contra a última versão conhecida como funcional, em vez de seguir editando reativamente.

---

## Ferramentas de diagnóstico úteis descobertas no processo

- `./mvnw dependency:tree | Select-String "spring-boot-starter"` — para confirmar qual versão real está sendo resolvida pelo Maven, independente do que o `pom.xml` "parece" dizer.
- `docker exec -it <container> psql -U <user> -d <db> -c "<query>"` — consultar dados diretamente no Postgres sem sair do terminal, útil quando não existe endpoint de consulta correspondente ainda.

---

## Testes: distinção entre `@DataJpaTest` e `@SpringBootTest`

`@DataJpaTest` sobe apenas a fatia de persistência (EntityManager, Repositories, DataSource) — mais rápido, escopo mais preciso para validar mapeamento/constraints. `@SpringBootTest` sobe o contexto inteiro (Controllers, Handlers, possivelmente Tomcat) — apropriado para testes ponta a ponta, não para validar só a camada de dados. Usar o primeiro quando o risco real está na persistência evita overhead desnecessário e mantém o teste fiel ao que está realmente sendo validado.

Detalhe técnico: `@DataJpaTest`, por padrão, tenta substituir o datasource configurado por um banco em memória — `@AutoConfigureTestDatabase(replace = Replace.NONE)` é necessário para forçar o uso do Postgres real do Testcontainers.
