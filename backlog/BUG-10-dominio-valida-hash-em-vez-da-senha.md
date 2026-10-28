# BUG-10: O domínio valida o hash em vez da senha, e as exceções dele viram 500

- **Severidade:** Média
- **Área:** backend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/application/services/UsuarioService.java:31-34`
  - `src/main/java/com/higor/React_java/usuario/domain/Usuario.java:21-23`
  - `src/main/java/com/higor/React_java/usuario/domain/Usuario.java:32-48`
- **Evidência:** lido no código. Hoje não é alcançável via HTTP, porque o `@Valid` barra antes. Quebra para qualquer chamador que não passe pelo controller.

## Sintoma

- `Usuario.criar` recebe a senha já em hash, com 60 caracteres. Por isso a regra "senha obrigatória" (linhas 45-47) nunca reprova. Qualquer chamador fora do controller com senha `""` cria uma conta com senha vazia: o `atualizar` que está em andamento, um seed, um import, um teste.
- As invariantes lançam `IllegalArgumentException`, que nenhum handler mapeia, então viram 500.
- O construtor público sem argumentos (linhas 21-23) permite criar um `Usuario` sem passar por validação nenhuma. Ele não é usado em lugar nenhum hoje.
- A regra de e-mail do domínio (`contains("@")`, linha 41) aceita `a@` e `@b`.

## Como reproduzir

1. Num teste de unidade, chame `UsuarioService.criar` com `senha = ""`, sem passar pelo controller.
2. O usuário é criado com o hash da string vazia.

## Causa-raiz

- A ordem das operações no service está invertida: `passwordEncoder.encode` (linha 33) roda antes da validação do domínio.
- O domínio usa uma exceção genérica da JDK, em vez de uma exceção de negócio que o advice conheça.

## Direção

- Pergunta para você: o domínio deve validar a senha em texto puro ou o hash? E onde fica a regra de tamanho e força da senha? A resposta define a ordem certa: validar, depois gerar o hash, depois criar.
- As invariantes do domínio devem lançar uma exceção que o `GlobalExceptionHandler` traduza para 400. A `RegraDeNegocioException` já existe.
- Avalie se o construtor público precisa existir. Se nenhum framework exige esse construtor no domínio, ele deve sair.

## Critério de pronto

- `UsuarioService.criar` com senha vazia é rejeitado mesmo quando chamado fora do controller, coberto por teste de unidade.
- Uma violação de invariante do domínio responde 400, não 500.
