# BUG-07: CORS e URL da API fixos no código

- **Severidade:** Média
- **Área:** backend e frontend
- **Onde:**
  - `src/main/java/com/higor/React_java/usuario/api/controllers/UsuarioController.java:21`
  - `meu-projeto/src/features/auth/api/usuarioApi.tsx:4`
- **Evidência:**
  - O 403 foi reproduzido no probe HTTP da revisão de 2026-10-27 para a origem `127.0.0.1:5173` e para a porta `5174`.
  - A URL embutida no bundle foi confirmada pela revisão no `vite build`.

## Sintoma

- Nestes três casos o backend responde **403 "Invalid CORS request"**, e a tela só loga "Falhou":
  - o front é aberto por `http://127.0.0.1:5173`;
  - o Vite cai para a porta 5174 porque a 5173 está ocupada;
  - o front roda por `npm run preview` (porta 4173).
- O build de produção embute `http://localhost:8080/Usuario`. Em qualquer deploy, o navegador do visitante vai chamar o localhost **dele**.
- Todo controller novo (o de login, por exemplo) vai precisar repetir a anotação.

## Como reproduzir

1. Suba o backend e rode `npm run dev` no `meu-projeto`.
2. Abra `http://127.0.0.1:5173` em vez de `localhost` e envie o cadastro.
3. A aba Network mostra o 403.

## Causa-raiz

- `@CrossOrigin(origins = "http://localhost:5173")` em `UsuarioController.java:21` libera uma única origem exata, e só neste controller.
- `static baseUrl = "http://localhost:8080/Usuario"` em `usuarioApi.tsx:4` deixa a URL da API fixa no código.

## Direção

Há duas saídas comuns no mercado. Escolha uma:

- **Proxy do Vite em dev e URL relativa no front.** Elimina o CORS em dev e combina com um deploy em que front e API ficam na mesma origem.
- **CORS global e URL por variável de ambiente.** No backend, pesquise `WebMvcConfigurer#addCorsMappings`, com as origens vindas de uma propriedade por ambiente. No front, a URL da API vem de `import.meta.env`.

## Critério de pronto

- O front funciona aberto tanto por `localhost` quanto por `127.0.0.1`.
- A configuração de CORS fica num único lugar, e nenhum controller tem `@CrossOrigin`.
- Depois de `npm run build`, o bundle não contém `localhost:8080` fixo.
