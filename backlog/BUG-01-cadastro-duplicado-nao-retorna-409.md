# BUG-01: Cadastro duplicado responde 400 ou 500 em vez de 409

- **Severidade:** Alta
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/application/services/UsuarioService.java:27-42`
  - `src/main/resources/db/migration/V1__criar_tabela.sql:3,5`
  - `src/main/java/com/higor/React_java/middleware/GlobalExceptionHandler.java:15,24`
- **Evidência:** reproduzido no probe HTTP da revisão de 2026-10-27, com repositório mockado e sem banco real.

## Sintoma

A API dá três respostas diferentes para o mesmo fato ("esse usuário já existe"):

| Cenário | Resposta hoje |
|---|---|
| E-mail já cadastrado | `400 {"status":400,"mensagem":"E-mail já cadastrado",...}` |
| Login já cadastrado, com e-mail novo | `500` no formato padrão do Spring Boot |
| Duplo clique ou duas requisições simultâneas com o mesmo e-mail | `500` em uma delas |

O 400 é o mesmo status de payload inválido. Por isso o front não consegue distinguir "corrija o formulário" de "essa conta já existe, vá para o login".

## Como reproduzir

1. `POST /Usuario/Criar` com `{"email":"a@x.com","login":"joaosilva","senha":"segredo123"}`. Resposta: **201**.
2. Repita com o mesmo e-mail. Resposta: **400**.
3. Envie `{"email":"b@x.com","login":"joaosilva","senha":"segredo123"}`. Resposta: **500**.

## Causa-raiz

- `UsuarioService.java:27` só checa `existsByEmail`. Não há checagem de login, e o `UNIQUE` de `login` (`V1__criar_tabela.sql:5`) só é descoberto na hora do INSERT.
- `UsuarioService.java:38-42`: o `catch (DataIntegrityViolationException ex)` apenas relança a exceção. Nenhum handler mapeia essa exceção, então ela vira 500.
- Há um *check-then-act* entre o `existsByEmail` (linha 27) e o `save` (linha 39): outra requisição pode inserir o mesmo e-mail nesse intervalo. A checagem prévia não substitui a constraint.
- `UsuarioService.java:28` lança `RegraDeNegocioException`, que `GlobalExceptionHandler.java:15` mapeia para 400. A `ConflitoDeDadosException`, que `GlobalExceptionHandler.java:24` mapeia para 409, não é lançada em lugar nenhum.

## Direção

- A fonte de verdade da unicidade é a constraint do banco. A checagem prévia serve só para dar uma mensagem amigável no caso comum.
- Traduza a violação de UNIQUE para conflito (409). Cuidado: `DataIntegrityViolationException` também cobre estouro de tamanho, NOT NULL e outras violações, e só UNIQUE é conflito. Pesquise como descobrir qual constraint foi violada: a causa raiz é a `ConstraintViolationException` do Hibernate, e o Postgres devolve o SQLSTATE `23505`.
- Se `criar` ganhar `@Transactional`, o INSERT pode acontecer só no commit, depois do seu `catch`. Pesquise `saveAndFlush`.
- Pergunta para você: e-mail duplicado e login duplicado devem ter mensagens diferentes? Mensagem específica ajuda o usuário, mas também confirma para qualquer um quais contas existem (enumeração de contas).

## Critério de pronto

- E-mail duplicado responde `409` no mesmo formato de erro da API.
- Login duplicado responde `409` no mesmo formato.
- Duas requisições simultâneas com o mesmo e-mail resultam em uma `201` e uma `409`, nunca em `500`.
- Coberto por teste (ver BUG-08).
