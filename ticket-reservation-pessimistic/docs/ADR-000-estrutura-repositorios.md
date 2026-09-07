# ADR-000 — Estrutura de repositórios (multi-repo com harness compartilhado)

## Status
Aceito

## Problema
O projeto precisa comparar duas estratégias de controle de concorrência (locking pessimista vs. otimista) de forma empírica e justa, mantendo cada implementação como um artefato de portfólio independente e imutável após concluída — sem risco de uma implementação "vazar" decisões para a outra durante o desenvolvimento.

## Alternativas consideradas

| Alternativa | Descrição |
|---|---|
| A — Repos totalmente isolados | Dois repositórios Git independentes, cada um com seu próprio setup de teste de carga |
| B — Repos isolados + harness compartilhado | Dois repositórios independentes + um terceiro repositório só com script de carga (k6) e infraestrutura padronizada (mesma versão de Postgres, mesmos recursos de container) |
| C — Monorepo | Um único repositório com múltiplos módulos |

## Decisão
Alternativa B.

## Justificativa
Garante isolamento total entre as duas implementações (requisito explícito do autor: não alterar a implementação pessimista após concluída) **e** garante que a comparação de performance entre as duas seja cientificamente válida — controlando as variáveis de ambiente (versão do banco, recursos de container, script de carga idêntico) fora do escopo de cada implementação.

## Trade-offs
- **Ganho**: isolamento real; comparação de throughput não contaminada por diferenças acidentais de ambiente.
- **Perda**: mais um repositório para manter; exige disciplina para manter o harness compartilhado sincronizado com o contrato de API de ambos os projetos (mitigado por manter uma assinatura de endpoint estável entre os dois repositórios).

## Estrutura resultante

```
ticket-reservation-pessimistic/     (este repositório)
ticket-reservation-optimistic/      (futuro)
ticket-reservation-benchmark/       (futuro — k6 + infra padronizada + resultados)
```
