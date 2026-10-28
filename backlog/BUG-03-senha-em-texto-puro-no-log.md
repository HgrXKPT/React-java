# BUG-03: Senha reprovada na validação aparece em texto puro no log

- **Severidade:** Alta (segurança)
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/application/dto/CriarUsuarioRequest.java:11`
  - `src/main/java/com/higor/React_java/middleware/GlobalExceptionHandler.java:33-36`
- **Evidência:** reproduzido pela revisão de 2026-10-27 num probe sem devtools. Não reproduzi pessoalmente.

## Sintoma

Quando a senha falha no `@Valid` (por exemplo, por passar do limite), o log grava algo como:

```
WARN ... DefaultHandlerExceptionResolver : Resolved [MethodArgumentNotValidException ... on field 'senha': rejected value [<senha real>]]
```

Isso acontece na configuração padrão, ou seja, também em produção. Pelo `run.cmd`, com o `spring-boot-devtools` ativo (`pom.xml:69`), a senha também volta no corpo do 400, em `errors[].rejectedValue` e no `trace`.

Um caso comum: o usuário cola a senha do gerenciador, ela passa do limite, e a senha real dele fica gravada no log.

## Como reproduzir

1. `POST /Usuario/Criar` com uma `"senha"` de 70 caracteres.
2. Procure o valor enviado no console da aplicação.

## Causa-raiz

- Não há handler para `MethodArgumentNotValidException`: o de `GlobalExceptionHandler.java:33-36` está comentado. Quem trata a exceção é o `DefaultHandlerExceptionResolver` do Spring, que loga em WARN a mensagem da exceção, e essa mensagem inclui o `rejectedValue`.
- O record `CriarUsuarioRequest` gera um `toString()` com todos os campos. Qualquer log do objeto imprime até senha válida (por exemplo, com `logging.level.web=DEBUG`).

## Direção

- Regra de mercado: nunca logar senha (OWASP Logging Cheat Sheet).
- Ao implementar o handler de validação (BUG-04), não devolva `ex.getMessage()` nem o `rejectedValue` do campo de senha.
- Pesquise como sobrescrever o `toString()` de um record para mascarar um campo. Vale também para o `LoginRequest`.

## Critério de pronto

- Com uma senha inválida, nenhuma linha de log e nenhum corpo de resposta contém o valor enviado, com ou sem devtools.
