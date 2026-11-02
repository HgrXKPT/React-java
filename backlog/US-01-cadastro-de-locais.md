# US-01: Cadastro de locais (listar e criar)

- **Fase:** A. Cadastros
- **Status:** em andamento. O commit `13a470a` entregou a parte de criar no back.
- **Depende de:** nada. É a raiz do modelo.
- **Área:** backend e frontend
- **Rota da tela:** `/admin/locais`
- **Endpoints:** `GET /locais` (falta), `POST /locais` (pronto)

## A dor

O sistema ainda não sabe **onde** monitorar. Todo o resto depende de um local: o sensor fica num local, a leitura vem de um sensor e o dashboard agrupa por local. Sem um cadastro de locais, não há onde pendurar nada.

E hoje dá para criar um local, mas não dá para **ver** os locais criados. O administrador não sabe o que já existe, e a US-02 não tem de onde carregar a lista para o formulário de sensor.

## O que entrega

> Como administrador, quero cadastrar e ver os locais monitorados, para depois instalar sensores neles.

- A tela `/admin/locais` lista os locais existentes: nome e descrição.
- Na mesma tela, um formulário cria um local novo, e a lista mostra o local criado sem precisar de F5.
- Com a lista vazia, a tela diz isso (não fica em branco).
- Com o back fora do ar, a tela mostra uma mensagem de erro visível.
- Nome duplicado mostra uma mensagem clara na tela.
- A sidebar tem um link para "Locais", e a navbar mostra o título da tela atual.

## Onde resolver

**Já existe (commit `13a470a`):**
- `src/main/resources/db/migration/V2__criar_tabela_locais.sql`: tabela `locais`
- `model/domain/Local.java`, `repository/LocalRepository.java` (`existsByNome`)
- `service/LocalService.java`: `criar` com validação de nome e descrição
- `controller/LocalController.java:25`: só o `@PostMapping`
- `model/dto/input/LocalInputDTO.java`, `model/dto/output/LocalOutputDTO.java`

**Back, falta:**
- `controller/LocalController.java`: o endpoint de listagem
- `service/LocalService.java`: o método que lista
- `src/test/java/com/higor/React_java/service/LocalServiceTest.java` (novo): teste do service

**Front, tudo novo:**
- `models/local.ts`: os tipos do local e do DTO
- `services/local.service.ts`: `listar` e `criar`, no padrão de `services/usuario.service.ts`
- `pages/Admin/Locais/index.tsx` e `pages/Admin/Locais/hooks/use-locais-page.ts`
- `routes.tsx:22-30`: a rota nova dentro dos `children` do `AppLayout`
- `components/layout/Sidebar.tsx:14-20`: o link para a tela
- `components/layout/AppLayout.tsx:18`: o título deixa de ser fixo

## Como resolver

1. **Back: listar.** Um `GET` no mesmo controller, que devolve uma lista de `LocalOutputDTO`. O `JpaRepository` já tem o método que busca tudo. Pense: quem transforma a lista de entidades em lista de DTO, o service ou o controller? Siga o que o `POST` já faz.
2. **Back: teste do service.** Cubra pelo menos: nome vazio, nome duplicado e criação válida. Use o `UsuarioServiceTest` como modelo (`@Mock` no repository, `@InjectMocks` no service).
3. **Teste no Swagger** (`http://localhost:8080/swagger-ui/index.html`): crie dois locais e liste.
4. **Front: model e service.** Primeiro o tipo, depois o service com `listar` e `criar`. O service traduz DTO → model, como o `toUsuario` faz.
5. **Front: a tela.** Esta é a primeira tela que **busca dado ao abrir**, e isso traz sintaxe nova: buscar dados quando o componente aparece na tela (pesquise `useEffect`) e guardar o resultado em estado. A tela tem quatro estados: carregando, erro, lista vazia e lista com itens. O hook da página é quem cuida deles. O componente só desenha.
6. **Front: criar e atualizar a lista.** O formulário usa `react-hook-form`, como no cadastro de usuário. Depois de criar, a lista precisa mostrar o local novo (ver "Decisões suas").
7. **Front: rota, sidebar e título.** A rota entra como filha do `AppLayout`. Com uma segunda página, o título fixo da navbar passa a mentir. Pesquise `handle` na rota + `useMatches()` do React Router.

## O que você vai aprender

- **Back:** o caminho completo de um recurso (migration → entidade → repository → service → controller → DTO), `GET` de coleção e teste de service com Mockito.
- **Front:** buscar dados ao abrir a tela, os estados de uma tela que depende da API, renderizar uma lista (`.map` com `key`), uma rota nova no layout e um título que vem da rota.

## Decisões suas

1. **Depois de criar, como a lista se atualiza?** Buscar a lista de novo na API, ou acrescentar o item que voltou do `POST` ao estado que já está na tela? Qual das duas pode mostrar algo diferente do que está no banco?
2. **Formulário e lista na mesma tela, ou criar numa tela separada?** O mais simples é na mesma tela.
3. **"Câmara Fria 1" e "câmara fria 1" são o mesmo local?** Descubra a collation do banco: o `UNIQUE` da coluna `nome` diferencia maiúsculas? E o `existsByNome`?
4. **A lista precisa de ordem?** Por nome? Por data de criação? Sem `ORDER BY`, o banco não garante ordem nenhuma.

## Bugs e pendências que aparecem aqui

- [BUG-01](BUG-01-cadastro-duplicado-nao-retorna-409.md): o mesmo padrão se repetiu no local. Nome duplicado responde 400 (`LocalService.java:22-24`), o `catch` só relança (`LocalService.java:34-38`) e a `ConflitoDeDadosException` (409) continua sem uso.
- [BUG-04](BUG-04-erros-fora-do-contrato-de-resposta.md): o handler de validação continua comentado (`GlobalExceptionHandler.java:33-36`). Um `POST /locais` com `"nome": ""` responde fora do formato de erro da API, e a tela não consegue mostrar a mensagem. Ao resolver, cuidado com o [BUG-03](BUG-03-senha-em-texto-puro-no-log.md): o handler não pode devolver o valor rejeitado.
- [BUG-05](BUG-05-tela-cadastro-sem-feedback-e-envio-duplo.md): o formulário de locais herda dois problemas do cadastro de usuário. O botão não bloqueia durante o envio (`pages/Auth/Cadastro/index.tsx:78-83`), e o service descarta a mensagem do back (`services/usuario.service.ts:26-28`).
- [BUG-12](BUG-12-rota-fora-do-padrao-rest-sem-location.md): o `201` do `POST /locais` não traz `Location` (`LocalController.java:30`).
- Pendências da navbar e da sidebar: título fixo (`AppLayout.tsx:18`) e links provisórios (`Sidebar.tsx:14-20`, com `/Importacoes` e `/Configuracoes` apontando para rotas que não existem).
- Ao criar o `models/local.ts`, tipe o `id` como a API devolve (número). O `models/usuario.ts:2,8` tipa como `string`, e isso não bate com a API.

## Fora de escopo

- Editar e excluir local. Entra se e quando uma US pedir.
- Paginação da lista (o volume de locais é pequeno; a paginação entra na US-07).
- Controle de quem pode cadastrar (US-10).
- CSS além do mínimo para enxergar a estrutura, a menos que você peça.

## Critério de pronto

- `GET /locais` devolve os locais cadastrados.
- Nome duplicado responde `409` no formato de erro da API, e a tela mostra a mensagem.
- `POST /locais` com nome vazio responde `400` dizendo qual campo falhou.
- A tela mostra os estados carregando, erro, vazio e lista. O local criado aparece sem F5, e um duplo clique gera um único `POST` (confira na aba Network).
- A navbar mostra "Locais" em `/admin/locais` e "Dashboard" em `/`.
- `LocalServiceTest` cobre nome vazio, nome duplicado e criação válida.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
