# BUG-12: Rota fora do padrão REST e 201 sem `Location`

- **Severidade:** Baixa
- **Área:** backend e frontend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/api/controllers/UsuarioController.java:20`
  - `src/main/java/com/higor/React_java/usuario/api/controllers/UsuarioController.java:30-35`
  - `meu-projeto/src/features/auth/api/usuarioApi.tsx:4,9`
- **Evidência:** lido no código.

## Sintoma

- A rota `POST /Usuario/Criar` tem verbo no path, está em PascalCase e no singular. Os próximos endpoints tendem a copiar o padrão: `/Usuario/Atualizar`, `/Usuario/Deletar`.
- O 201 não traz o cabeçalho `Location` com o URI do recurso criado. A RFC 9110, §9.3.3, diz que um POST que cria recurso SHOULD responder 201 com `Location`.

## Como reproduzir

1. `POST /Usuario/Criar` com um payload válido.
2. Os cabeçalhos da resposta não incluem `Location`.

## Causa-raiz

- `@RequestMapping("/Usuario")` (linha 20) com `@PostMapping("/Criar")` (linha 30) põe a ação no path, mas o método HTTP já diz qual é a ação.
- `ResponseEntity.status(HttpStatus.CREATED)` (linha 35) não define o `Location`.

## Direção

- A convenção de mercado é recurso no plural e em minúsculas, com a ação dada pelo método HTTP: `POST /usuarios`, `GET /usuarios/{id}`, `PUT /usuarios/{id}`.
- Pesquise `ResponseEntity.created(URI)`.
- Mudar a rota quebra o front, então altere os dois juntos. Quanto mais tarde for feito, mais endpoints vão precisar migrar.

## Critério de pronto

- `POST /usuarios` responde `201` com `Location: /usuarios/{id}`.
- O front usa a nova rota.
