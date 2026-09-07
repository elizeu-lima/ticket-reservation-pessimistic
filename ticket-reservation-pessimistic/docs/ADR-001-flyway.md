# ADR-001 — Flyway como ferramenta de migração de banco

## Status
Aceito

## Problema
O projeto precisa versionar e aplicar mudanças de schema do PostgreSQL de forma controlada e reproduzível.

## Alternativas consideradas

| Critério | Flyway | Liquibase |
|---|---|---|
| Formato de migração | SQL puro | XML/YAML/JSON/SQL — abstrai o SQL |
| Filosofia | Você escreve o SQL, a ferramenta versiona e aplica | Você descreve a mudança de forma abstrata, a ferramenta gera o SQL |
| Rollback declarativo | Limitado na versão open-source | Suportado nativamente |
| Portabilidade entre bancos diferentes | Baixa (SQL específico do dialect) | Maior |

## Decisão
Flyway.

## Justificativa
O problema central deste projeto é controle de concorrência **em SQL** (`SELECT FOR UPDATE`, isolamento de transação no Postgres). Faz sentido que o desenvolvedor escreva e leia SQL puro desde as migrations, reforçando exatamente a habilidade que o projeto pretende demonstrar. Usar Liquibase introduziria uma camada de abstração sobre o SQL justamente no ponto em que o SQL é protagonista.

Além disso, o projeto não tem os requisitos que justificariam Liquibase: não há necessidade de portabilidade entre múltiplos SGBDs-alvo, nem de rollback declarativo complexo.

## Trade-offs
- **Ganho**: alinhamento direto com o objetivo de aprendizado do projeto; simplicidade.
- **Perda**: rollback de migration é manual (escrever novo script de correção), não declarativo — aceitável, já que o projeto não versiona schema em múltiplos bancos-alvo.

## Consequências
Migrations vivem em `src/main/resources/db/migration/`, nomeadas `V{n}__descricao.sql`, executadas automaticamente pelo Flyway na inicialização da aplicação (comportamento padrão do `spring-boot-starter-flyway`).
