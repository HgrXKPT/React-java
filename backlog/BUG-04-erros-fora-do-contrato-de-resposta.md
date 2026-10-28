# BUG-04: Erros de validação e erros inesperados saem fora do contrato de resposta

- **Severidade:** Alta
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/middleware/GlobalExceptionHandler.java:33-36`
  - `src/main/java/com/higor/React_java/middleware/ErroResponseDto.java`
- **Evidência:** reproduzido no probe HTTP da revisão de 2026-10-27.

## Sintoma

A API devolve erros em dois formatos diferentes:

| Cenário | Corpo da resposta |
|---|---|
| E-mail duplicado | `{"status","mensagem","timestamp"}` (`ErroResponseDto`) |
| `"senha": ""`, ou login com 5 caracteres | `{"timestamp","status":400,"error":"Bad Request","path"}`, sem dizer qual campo falhou |
| JSON malformado, 415, 405, qualquer 500 | formato do `/error` do Spring Boot |

Consequências:

- O front precisa de dois parsers e não consegue apontar o campo errado.
- Em dev, com devtools ativo, o mesmo erro traz `message`, `errors[]` e `trace`. Em produção, com o jar sem devtools, esses campos não vêm. Um front escrito contra o dev quebra em produção.

## Como reproduzir

1. `POST /Usuario/Criar` com `"senha": ""`.
2. Compare o corpo da resposta com o do caso de e-mail duplicado.

## Causa-raiz

- O handler de `MethodArgumentNotValidException` está comentado (`GlobalExceptionHandler.java:33-36`).
- Não existe handler catch-all para `Exception`.
- As exceções do próprio Spring MVC (`HttpMessageNotReadableException`, `HttpMediaTypeNotSupportedException` e outras) não passam pelo advice atual.

## Direção

- Defina **um** contrato de erro para a API inteira, com uma lista de campos inválidos.
- O padrão de mercado é o `ProblemDetail` (RFC 9457), que o Spring suporta nativamente. Pesquise `ResponseEntityExceptionHandler`: estender essa classe faz o advice cobrir também as exceções do MVC.
- Crie um catch-all que responda 500 com mensagem genérica, sem stack trace no corpo, e que logue a exceção no servidor.
- Pergunta para você: manter o `ErroResponseDto` ou migrar para o `ProblemDetail`? A resposta afeta o BUG-01 e o front no BUG-05.

## Critério de pronto

- Validação, JSON malformado, conflito e 500 respondem no mesmo formato.
- O erro de validação lista o campo e a mensagem de cada violação.
- Nenhuma resposta contém stack trace, com ou sem devtools.
