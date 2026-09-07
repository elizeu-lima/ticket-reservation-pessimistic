# 10 — Segurança

> Estado honesto: este é um projeto de portfólio focado em concorrência de banco de dados. Segurança de autenticação/autorização foi conscientemente reduzida de escopo no MVP, mas os pontos abaixo já foram considerados desde o início, como pede o processo do projeto — não deixados só para o final.

## Já implementado

| Prática | Onde | Descrição |
|---|---|---|
| Validação de entrada | DTOs (`@NotBlank`, `@NotNull`, `@Positive`, `@Future`) | Previne payloads malformados de chegarem à camada de negócio |
| Prevenção de mass assignment | DTOs de request restritos | Cliente não pode enviar campos como `id`, `availableQuantity` na criação |
| Defesa em profundidade | Bean Validation + `CHECK` constraints no banco | RN-005 reforçada em duas camadas independentes |
| Gerenciamento de credenciais locais | `.env` + `.env.example`, `.gitignore` configurado | Senha do Postgres local não versionada no Git |
| Mensagens de erro não vazam detalhes internos | `GlobalExceptionHandler` | Respostas de erro padronizadas, sem stack trace exposto ao cliente |

## Débito técnico consciente (registrado, não esquecido)

- **Credenciais em `application.properties` sem criptografia/vault**: aceitável para ambiente local (`postgres`/`postgres`), mas **precisa ser resolvido antes de qualquer deploy real** — via Spring profiles + variáveis de ambiente de verdade, ou um secrets manager caso vá para cloud.
- **Sem autenticação/autorização**: não há verificação de identidade em nenhum endpoint. Fora de escopo do MVP (ver `docs/01-contexto.md`), mas **precisaria** existir antes de qualquer uso real (ex.: JWT simples), já que hoje qualquer requisição pode criar eventos e tipos de ingresso livremente.
- **Sem rate limiting**: não há proteção contra abuso do endpoint de criação. Relevante especialmente quando o endpoint de reserva (Épico 2) existir — rate limiting ali não é só segurança, é proteção contra teste de carga acidental afetando dados reais.

## Planejado para quando o escopo justificar

- Rate limiting no endpoint de reserva.
- Cabeçalhos de segurança HTTP padrão (CSP, HSTS, etc.) caso o projeto ganhe um consumidor real além dos próprios testes.
- Revisão de dependências (ex. `mvn dependency-check` ou similar) antes de qualquer deploy.
