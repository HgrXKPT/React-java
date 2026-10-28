# BUG-06: E-mail sem normalização permite duas contas para a mesma caixa

- **Severidade:** Média
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/application/services/UsuarioService.java:27`
  - `src/main/java/com/higor/React_java/usuario/domain/Usuario.java:32-48`
  - `src/main/resources/db/migration/V1__criar_tabela.sql:3`
- **Evidência:**
  - E-mail: inferido da semântica do Postgres (comparação de `VARCHAR` com distinção de maiúsculas). Não executei contra um banco real.
  - Login com espaços: reproduzido no probe da revisão de 2026-10-27, com repositório mockado.

## Sintoma

- Cadastrar `Joao@Gmail.com` e depois `joao@gmail.com`, com outro login, resulta em **201** nos dois casos: duas contas para a mesma caixa de e-mail. O login por e-mail, quando existir, só vai achar a grafia exata.
- O login `"   joaosilva   "` recebe **201** e é gravado com os espaços.

## Como reproduzir

1. `POST /Usuario/Criar` com `"email":"Joao@Gmail.com"`. Resposta: 201.
2. `POST /Usuario/Criar` com `"email":"joao@gmail.com"` e outro login. Resposta: 201.
3. `SELECT email FROM usuarios` mostra as duas linhas.

## Causa-raiz

- O e-mail é gravado e comparado exatamente como chegou. Tanto o `existsByEmail` (`UsuarioService.java:27`) quanto o `UNIQUE` de `V1__criar_tabela.sql:3` diferenciam maiúsculas de minúsculas.
- Nem o domínio (`Usuario.criar`, linhas 32-48) nem o service normalizam os identificadores.

## Direção

- Defina a forma canônica do e-mail (sem espaços nas bordas e em minúsculas) e aplique-a num único lugar, antes de checar e de gravar. Pergunta para você: qual camada é dona dessa regra?
- Garanta a unicidade também no banco, sobre a forma canônica. Pesquise índice único sobre `lower(email)` numa nova migration.
- Remova os espaços das bordas do login e decida se o login também deve ignorar maiúsculas.
- Corrija cedo: depois que existirem duplicatas por maiúsculas, a migration de unicidade falha até alguém limpar os dados.

## Critério de pronto

- Cadastrar `Joao@Gmail.com` e depois `joao@gmail.com` resulta em `201` e depois `409`.
- Um login enviado com espaços nas bordas é gravado sem eles.
