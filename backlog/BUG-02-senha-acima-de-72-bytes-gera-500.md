# BUG-02: Senha acima de 72 bytes passa na validação e gera 500, e o mínimo de 3 aceita senha trivial

- **Severidade:** Alta
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/application/dto/CriarUsuarioRequest.java:11`
  - `src/main/java/com/higor/React_java/usuario/application/services/UsuarioService.java:33`
- **Evidência:** reproduzido no probe HTTP da revisão de 2026-10-27. A versão `spring-security-crypto` 7.1.2 foi conferida em `~/.m2`.

## Sintoma

- Uma senha com 37 × `ã` (37 caracteres, 74 bytes em UTF-8) passa no `@Valid`, e o cadastro responde **500**.
- Uma frase-senha de 64 caracteres com alguns acentos ou emojis também derruba o cadastro.
- No outro extremo, `"senha":"abc"` responde **201**.

## Como reproduzir

1. `POST /Usuario/Criar` com `"senha"` igual a `ã` repetido 37 vezes.
2. A resposta é 500, e o log mostra `IllegalArgumentException: password cannot be more than 72 bytes`.

## Causa-raiz

- `@Size(min = 3, max = 64)` (linha 11) conta caracteres (unidades UTF-16), não bytes.
- O BCrypt só considera 72 bytes. No `spring-security-crypto` 7.x ele lança exceção acima desse limite em vez de truncar. O `encode` em `UsuarioService.java:33` não trata essa exceção, e nenhum handler mapeia `IllegalArgumentException`.
- O mínimo de 3 está abaixo da referência de mercado: a NIST SP 800-63B pede no mínimo 8.

## Direção

- O limite real é em bytes, então a validação precisa medir os bytes em UTF-8 **antes** do encode e responder 400 apontando o campo. Pesquise constraint customizada no Bean Validation (`@Constraint` + `ConstraintValidator<A, T>`).
- Suba o mínimo para 8.
- O front deve espelhar as mesmas regras (ver BUG-05).

## Critério de pronto

- Senha de 74 bytes responde `400`, indicando o campo `senha`.
- Senha de 64 caracteres ASCII responde `201`.
- `"abc"` responde `400`.
