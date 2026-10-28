# BUG-09: Credenciais e configuração de dev valem para todos os ambientes

- **Severidade:** Média
- **Área:** infra
- **Onde:**
  - `src/main/resources/application.properties:3-13`
  - `.gitignore`
- **Evidência:** lido no código. `git check-ignore .env` não casa com nenhuma regra.

## Sintoma

- O usuário e a senha do banco (`postgres/postgres`, linhas 4-5) estão versionados. Para qualquer outro ambiente é preciso editar e commitar o arquivo, e a senha real acaba no histórico do git.
- `show-sql` e `format_sql` (linhas 12-13) estão ligados em todos os ambientes. Em produção, cada SQL vai direto para o stdout, fora do sistema de log.
- O Swagger UI (springdoc, `pom.xml:62-64`) fica exposto em todos os ambientes.
- O `.env` não está no `.gitignore`. Se as credenciais forem para um `.env`, um `git add -A` versiona o arquivo.

## Como reproduzir

1. `git check-ignore -v .env` não imprime nada.
2. Suba a aplicação sem nenhum profile: o SQL aparece no console e `/swagger-ui/index.html` responde.

## Causa-raiz

- Existe um único `application.properties`, sem placeholders `${...}` e sem profiles.

## Direção

- Pesquise placeholders de variável de ambiente no Spring (`${DB_PASSWORD}`) e profiles (`application-dev.properties` e `spring.profiles.active`).
- No profile de dev, deixe `show-sql` e Swagger ligados. Na configuração padrão, deixe os dois desligados.
- Adicione o `.env` ao `.gitignore` **antes** de criar o arquivo.

## Critério de pronto

- Nenhuma senha em arquivo versionado.
- A aplicação sobe em dev com o profile `dev`, e sem `DB_PASSWORD` definida ela falha com uma mensagem clara.
- `git check-ignore .env` casa com uma regra.
