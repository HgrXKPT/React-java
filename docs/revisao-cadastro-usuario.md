# Revisão de TL — `feature/cadastro-usuario`

- **Data:** 2026-10-27
- **Escopo:** commits `754c647` e `f161fab` (ainda não enviados ao remoto) + o que está alterado e não commitado no working tree (`@Valid` no controller e `@Size` na senha).
- **Formato:** segue o `GEMINI.md` do repositório. Cada problema vem com **Sintoma → Causa-raiz → Direção**, perguntas para você responder e um critério de pronto. Código, só assinatura.

---

## Como usar este guia

1. **Siga a ordem dos itens.** Eles têm dependência entre si: o item 3 (formato do erro HTTP) muda o que o front do item 7 consegue mostrar, por exemplo.
2. **Antes de corrigir, reproduza.** Escreva o teste ou faça a chamada no Swagger e veja o erro acontecer. Corrigir sem ter visto o erro é chute.
3. **Responda as perguntas antes de abrir as dicas.** As dicas ficam fechadas e vão em camadas:
   - **Pista:** para onde olhar.
   - **Conceito:** o nome do que você deve pesquisar.
   - **Direção:** o caminho concreto, ainda sem código pronto.

   Abra uma camada só quando a anterior não bastar.
4. **Quando discordar de algum ponto, discuta.** Isto aqui é uma revisão, não uma ordem.

Para testar a API na mão, o springdoc já está no projeto: suba a aplicação e abra `http://localhost:8080/swagger-ui/index.html`.

---

## Visão geral

| # | Problema | Onde | Severidade |
|---|---|---|---|
| 0 | Nenhum teste cobre a funcionalidade | `ReactJavaApplicationTests.java:10` | Alta (processo) |
| 1 | A senha é validada depois de virar hash | `UsuarioService.java:30`, `Usuario.java:45` | Alta |
| 2 | `@Valid` e tamanhos dos campos (você já começou) | `UsuarioController.java:31`, `CriarUsuarioRequest.java:9-11` | Alta |
| 3 | Toda regra de negócio vira 500 | `UsuarioService.java:27`, `Usuario.java:33-47` | Alta |
| 4 | Corrida no cadastro e login duplicado sem checagem | `UsuarioService.java:26-35`, `V1__criar_tabela.sql:5` | Alta |
| 5 | E-mail sem normalização | `Usuario.java:27`, `UsuarioRepository.java:14` | Média |
| 6 | Tamanhos da entidade divergem da migration | `UsuarioEntity.java:23-31`, `V1__criar_tabela.sql:3-5` | Média |
| 7 | Front engole o erro e permite envio duplo | `RegisterPage.tsx:9-16`, `usuarioApi.tsx:14-16` | Média |
| 8 | Lógica e apresentação no mesmo componente | `RegisterPage.tsx:4` | Baixa |
| 9 | CORS e URL da API fixos no código | `UsuarioController.java:21`, `usuarioApi.tsx:4` | Média |
| 10 | `save` faz `merge` (SELECT a mais) | `UsuarioService.java:35`, `Usuario.java:26` | Baixa |
| 11 | O commit `f161fab` mistura quatro mudanças | histórico | Processo |

No final há um checklist de ajustes pequenos e uma tabela de cenários que serve de definição de pronto da branch.

---

## 0. Rede de segurança: nenhum teste cobre a funcionalidade

**Sintoma.** O único teste é o `contextLoads` (`ReactJavaApplicationTests.java:10`). Por causa do `@SpringBootTest` na linha 6, ele precisa de um Postgres no ar para passar.

**Causa-raiz.** A funcionalidade foi escrita sem nenhum teste que diga o que ela deveria fazer. Por isso os bugs dos itens 1, 3 e 4 passaram sem ninguém ver.

**Direção.** Você já tem tudo no classpath. Rodei `mvnw dependency:tree` e apareceram JUnit Jupiter 6.0.3, Mockito 5.23 e AssertJ 3.27, que vêm pelos starters de teste do `pom.xml:83-102`. Comece pela camada mais barata:

| Camada | O que prova | Precisa de banco? |
|---|---|---|
| `Usuario.criar` (Java puro) | as regras do domínio | não |
| `UsuarioService` com dublês de repositório e de encoder | ordem das operações, duplicidade | não |
| Controller (`@WebMvcTest` + `@MockitoBean`) | status HTTP e corpo do erro (itens 2 e 3) | não |
| Integração com Postgres real | constraint UNIQUE, corrida | sim, deixe para depois |

**Regra de trabalho para os itens seguintes:** primeiro escreva o teste que **reproduz** o bug e veja ele falhar. Só depois corrija e veja ele passar. Teste que nunca falhou não prova nada.

**Perguntas para você:**
- Qual destes testes você escreveria primeiro para provar o bug do item 1? Em que camada ele fica?
- Para testar o `UsuarioService`, você precisa do `BCryptPasswordEncoder` real? O que você perde e o que ganha usando um dublê?

<details><summary>Pista</summary>

O `UsuarioService` recebe as dependências pelo construtor (`UsuarioService.java:19`). Então dá para instanciá-lo com `new` dentro do teste, sem subir o Spring.
</details>

<details><summary>Conceito</summary>

Pesquise **test doubles** (stub, mock, fake), **pirâmide de testes** e **test slices** do Spring Boot (`@WebMvcTest`). No Spring Boot 4, o `@MockBean` foi substituído por `@MockitoBean`.
</details>

**Pronto quando:** `mvnw test` roda pelo menos os testes de domínio e de service **sem banco**, e cada bug dos itens 1 a 4 tem um teste que falhava antes da correção.

> O `contextLoads` continua dependendo do banco. Se isso vai mudar (com Testcontainers, por exemplo) é uma decisão sua, porque traz dependência nova. Não precisa ser nesta branch.

---

## 1. A senha é validada depois de virar hash

**Sintoma.** Antes do seu `@Valid`, um `POST` com `"senha": ""` criava a conta. Com o `@Valid`, o controller passa a barrar esse caso. O service e o domínio, porém, continuam aceitando senha vazia: qualquer caminho que chame `usuarioService.criar` sem passar pelo controller (um teste, um import em lote, um outro endpoint) cria a conta.

**Causa-raiz.** É a ordem das operações em `UsuarioService.java:30-32`. O `encode` roda **antes** do `Usuario.criar`, e por isso a regra `senha.isBlank()` em `Usuario.java:45` está validando o **hash**, que nunca é vazio. De quebra, o BCrypt, que é a operação mais cara do método, roda antes das validações baratas.

**Perguntas para você:**
1. O campo `Usuario.senha` guarda a senha ou o hash? O nome do campo deixa isso claro?
2. "A senha digitada tem tamanho válido" e "o usuário tem um hash de senha" são a mesma regra? Qual camada é dona de cada uma?
3. Quanto tempo leva um `encode`? **Meça** com `System.nanoTime()` antes e depois. Com esse número, faz sentido o `encode` rodar antes de saber se o e-mail já existe?

<details><summary>Pista</summary>

Leia o `criar` de cima para baixo e escreva, em ordem, quando cada dado é validado. Depois reordene essa lista do mais barato para o mais caro.
</details>

<details><summary>Conceito</summary>

**Fail-fast / guard clauses**: valide o que é barato e local antes do que é caro ou consulta o banco. Para ler, não para aplicar agora: **Value Object**. Por exemplo, um tipo para a senha em texto puro e outro para o hash, de modo que o compilador não deixe confundir os dois. Pergunte-se se isso não seria complexidade demais para o tamanho atual do projeto.
</details>

<details><summary>Direção</summary>

A ordem fica: validar a senha pura → checar duplicidade no banco → gerar o hash → `Usuario.criar` → salvar. No domínio, a regra passa a ser "o hash existe". O nome do parâmetro deve contar a verdade, algo como `criar(String email, String senhaHash, String login)`.
</details>

**Pronto quando:** um teste do service, sem Spring, prova que senha vazia lança exceção **e** que o `encode` nunca é chamado nesse caso (verifique com o Mockito).

---

## 2. `@Valid` e tamanhos dos campos (você já começou)

**O que está certo.** O `@Valid` em `UsuarioController.java:31` liga o Bean Validation, e só funciona porque o `spring-boot-starter-validation` já estava no `pom.xml:45-48`. Bom diagnóstico.

**O que ainda falta:**

**a) Qual é o corpo do 400?** Mande `"senha": ""` pelo Swagger e leia a resposta. O front consegue saber **qual campo** falhou? Anote o que veio, porque vamos voltar a isso no item 3.

**b) `@Size(min = 3, max = 12)` na senha (`CriarUsuarioRequest.java:11`).**
- Por que 12? Um gerenciador de senhas costuma gerar senhas de 20 caracteres ou mais. Pesquise a **NIST SP 800-63B** na parte de comprimento de senha. Ela recomenda aceitar senhas longas, de pelo menos 64 caracteres.
- E o mínimo de 3, protege contra o quê?
- **Pergunta-chave:** o `@Size` conta caracteres ou bytes? O BCrypt do Spring Security 7 **lança exceção acima de 72 bytes**. Se você aumentar o `max`, quantos bytes ocupa uma senha de 64 caracteres acentuados em UTF-8?

<details><summary>Pista</summary>

`"ã".length()` em Java devolve 1. `"ã".getBytes(StandardCharsets.UTF_8).length` devolve 2. Um emoji ocupa 2 `char`s e 4 bytes. Compare os dois números.
</details>

<details><summary>Direção</summary>

Há duas saídas e as duas são válidas, cabe a você escolher:
- **Manter o `max` baixo o bastante** para que o pior caso caiba em 72 bytes. Se o pior caso é 3 bytes por `char`, o `max` fica em torno de 24.
- **Aceitar senha longa** e checar o tamanho em bytes antes do `encode`, devolvendo um 400 com uma mensagem clara em vez do 500 do BCrypt.

O critério que importa é que nenhuma entrada do usuário consiga chegar ao BCrypt e virar 500.
</details>

**c) `login` e `email` sem `@Size`.** O banco limita esses campos (`V1__criar_tabela.sql:3` e `:5`). Hoje um login de 101 caracteres passa pela validação e estoura no Postgres. Qual dos dois é a fonte da verdade do tamanho, o DTO ou a migration? Veja também o item 6.

**d) Import sem uso.** `jakarta.validation.constraints.Min` (`CriarUsuarioRequest.java:4`) não é usado.

**Pronto quando:** as linhas de entrada inválida da [tabela de cenários](#definição-de-pronto-da-branch) devolvem 400, e nenhuma devolve 500.

---

## 3. Toda regra de negócio vira 500

**Sintoma.** Cadastrar um e-mail que já existe devolve **500 Internal Server Error**, e o corpo não traz a mensagem "E-mail já cadastrado". Para o front, um e-mail duplicado, um bug e um servidor fora do ar parecem a mesma coisa.

**Causa-raiz.** `UsuarioService.java:27` e `Usuario.java:33-47` lançam `IllegalArgumentException`, e o projeto não tem nenhum `@RestControllerAdvice`. Exceção que ninguém mapeia, o Spring trata como erro inesperado: status 500. Além disso, por padrão o Spring Boot não devolve a mensagem da exceção no corpo; veja a propriedade `server.error.include-message`.

**Perguntas para você:**
1. Qual status HTTP cada caso deveria ter: dado inválido, e-mail já existente e erro inesperado?
2. Mapear **toda** `IllegalArgumentException` para 400 parece a saída mais rápida. Mas o BCrypt lança `IllegalArgumentException` com senha acima de 72 bytes, e qualquer biblioteca pode lançar uma por bug. O que acontece com esses casos se tudo virar 400?
3. Que formato o corpo do erro precisa ter para o front mostrar a mensagem **ao lado do campo** certo?

<details><summary>Pista</summary>

Responda a pergunta 2 com calma: ela aponta que você precisa de uma exceção **sua**, que só o seu código lança, em vez de reaproveitar uma genérica do Java.
</details>

<details><summary>Conceito</summary>

Pesquise `@RestControllerAdvice` + `@ExceptionHandler`, a **RFC 9457 (Problem Details for HTTP APIs)** e a classe `ProblemDetail` do Spring. Pesquise também qual exceção o `@Valid` lança quando a validação falha (`MethodArgumentNotValidException`) e o que dá para extrair dela.
</details>

<details><summary>Direção</summary>

Um único `@RestControllerAdvice` com poucos handlers: um para a exceção de conflito (409), um para a falha de validação (400, com a lista de campos) e, se quiser, um genérico (500, **sem** stack trace no corpo). A assinatura tem esta cara:

```java
@ExceptionHandler(SuaExcecaoDeConflito.class)
ProblemDetail tratar(SuaExcecaoDeConflito ex)
```

Comece com uma ou duas exceções. Não monte uma hierarquia de dez classes antes de precisar.
</details>

**Pronto quando:** um `@WebMvcTest` prova que um e-mail duplicado devolve 409 com a mensagem no corpo e que um campo inválido devolve 400 com o nome do campo.

---

## 4. Corrida no cadastro e login duplicado sem checagem

**Sintoma.** Há dois problemas aqui:
- **Login duplicado:** cadastrar um e-mail novo com um login que já existe sempre devolve 500. Não precisa de concorrência para acontecer. A coluna `login` é `UNIQUE` no banco (`V1__criar_tabela.sql:5`), mas o service não verifica.
- **Corrida no e-mail:** duas requisições simultâneas com o mesmo e-mail (um duplo clique, por exemplo) passam as duas pelo `existsByEmail`. A segunda bate na constraint e devolve 500.

**Causa-raiz.** É o padrão **check-then-act**: entre o `existsByEmail` (linha 26) e o `save` (linha 35) existe uma janela de tempo em que outra requisição pode inserir o mesmo e-mail. Quem garante a unicidade de verdade é a constraint do banco. O `exists` serve só para dar uma mensagem amigável no caso comum.

**Antes de corrigir, sinta o problema:**
1. Coloque um breakpoint em `UsuarioService.java:30`.
2. Dispare dois `POST` iguais, um depois do outro, e os dois param no breakpoint.
3. Solte o primeiro e depois o segundo.
4. Veja qual exceção aparece no log e qual status volta.

**Perguntas para você:**
1. Quem é a fonte da verdade da unicidade, o `if` em Java ou o banco?
2. Colocar `@Transactional` no método resolve a corrida? O nível de isolamento padrão do Postgres é **READ COMMITTED**. O que uma transação enxerga de outra que ainda não fez commit?
3. Quando a constraint é violada, que exceção o Spring lança? E como você descobre se o problema foi o e-mail ou o login?
4. Se você tratar a violação da constraint, ainda precisa do `existsByEmail`? Pense no custo do BCrypt (item 1) e na clareza da mensagem.

<details><summary>Pista</summary>

O Postgres dá nome às constraints criadas com `UNIQUE` direto na coluna. Rode `\d usuarios` no `psql` e veja os nomes.
</details>

<details><summary>Conceito</summary>

Pesquise **check-then-act / race condition**, **níveis de isolamento de transação**, `DataIntegrityViolationException` e **flush** no JPA/Hibernate.

**Armadilha:** se você envolver o `save` num `try/catch` e depois colocar `@Transactional` no método, em que momento o INSERT vai de fato para o banco? Dentro do `try` ou no commit, depois que o método já retornou? Pesquise `saveAndFlush`.
</details>

<details><summary>Direção</summary>

Use as duas camadas juntas:
- `existsByEmail` e `existsByLogin` para o caminho comum, com mensagem clara e sem gastar BCrypt à toa;
- tradução da violação da constraint para 409, para cobrir a corrida.

Garanta que a exceção estoure **dentro** do trecho que você está tratando.
</details>

**Pronto quando:** um login duplicado devolve 409, o experimento do breakpoint devolve 201 para a primeira requisição e 409 para a segunda, e nenhum caso devolve 500.

---

## 5. E-mail sem normalização

**Sintoma.** `Joao@x.com`, `joao@x.com` e ` joao@x.com` (com espaço) viram três contas da mesma pessoa. Quando o login for implementado com o `findByEmail` (`UsuarioRepository.java:12`), ele vai falhar dependendo de como o usuário digitar.

**Causa-raiz.** O e-mail é gravado do jeito que chegou (`Usuario.java:27`). Tanto o `existsByEmail` quanto o `UNIQUE` do Postgres diferenciam maiúsculas de minúsculas.

**Perguntas para você:**
1. Em que **único** lugar a normalização deveria ficar para que seja impossível esquecê-la? Lembre que o cadastro **e** o login vão comparar e-mails.
2. Só normalizar no Java basta? Se alguém inserir direto no banco, o que acontece?
3. O login também deveria ignorar maiúsculas? Isso é uma decisão de produto, não técnica. Decida e registre.

<details><summary>Conceito</summary>

Para a garantia no banco: **índice único sobre uma expressão** (`lower(email)`) ou o tipo `citext` do Postgres. Para mudar o banco: **nunca edite a `V1` que já foi aplicada**. O Flyway guarda um checksum de cada migration e vai recusar subir a aplicação. Toda mudança entra numa `V2__...`.
</details>

**Pronto quando:** cadastrar `Joao@x.com` e depois ` joao@x.com ` devolve 409.

---

## 6. Tamanhos da entidade divergem da migration

**Sintoma.** A entidade diz uma coisa e o banco diz outra:

| Campo | `UsuarioEntity.java` | `V1__criar_tabela.sql` |
|---|---|---|
| email | `length = 200` (linha 23) | `VARCHAR(255)` (linha 3) |
| senha | `length = 100` (linha 27) | `VARCHAR(255)` (linha 4) |
| login | `length = 150`, sem `unique` (linha 30) | `VARCHAR(100) UNIQUE` (linha 5) |

**Causa-raiz.** Há duas fontes da verdade. Com o Flyway criando as tabelas e `ddl-auto=validate` (`application.properties:9`), os atributos `length` e `unique` do `@Column` **não fazem nada em runtime**: eles só são usados quando o Hibernate gera o DDL, o que este projeto não faz. Esses atributos só induzem quem lê o código ao erro.

**Perguntas para você:**
1. Se o `validate` comparasse o tamanho das colunas, a aplicação subiria hoje? Ela sobe? O que isso diz sobre o que o `validate` confere?
2. O que é melhor: alinhar esses atributos com a migration ou removê-los? Qual opção mente menos para quem ler o código daqui a seis meses?

**Pronto quando:** a entidade não contradiz a migration, e o `@Size` do DTO (item 2) usa os mesmos limites da migration.

---

## 7. O front engole o erro e permite envio duplo

**Sintoma.**
- Com erro (e-mail duplicado, backend fora do ar ou bloqueio de CORS), o usuário clica em Enviar e **nada acontece**. O `catch` em `RegisterPage.tsx:13-15` só escreve "Falhou" no console.
- No sucesso também nada acontece, e os campos continuam preenchidos, o que convida a reenviar.
- O botão (`RegisterPage.tsx:61-66`) continua habilitado durante o `await`, então um duplo clique dispara dois `POST`s e cai no item 4.

**Causa-raiz.** A tela não tem estado para representar "enviando", "deu certo" e "deu errado". Além disso, `usuarioApi.tsx:14-16` lança um `Error` só com o status e descarta o corpo da resposta, então a mensagem do backend (item 3) nunca chega à tela.

**Perguntas para você:**
1. Desenhe os estados possíveis deste formulário e as transições entre eles. Quais estados são mutuamente exclusivos?
2. Dá para representar isso com **um** estado em vez de três booleanos (`enviando`, `sucesso`, `erro`)? Que combinações impossíveis os três booleanos permitem?
3. O que o usuário deveria ver em cada caso: 400, 409 e servidor fora do ar? Dica: quando não há conexão, o `fetch` **lança uma exceção** em vez de devolver uma resposta.
4. `required` e `minLength` no `<input>` substituem a validação do backend?

<details><summary>Conceito</summary>

**Discriminated union** em TypeScript para o estado. O atributo `disabled` no botão. **Validação no front é UX; a garantia é sempre do backend.** Leitura opcional: o React 19 tem `useActionState` e `useFormStatus`, que tratam o estado de "enviando". Compare com um `useState` simples e escolha o que você consegue explicar.
</details>

**Faça depois do item 3.** O formato do erro definido lá é o que esta tela vai ler.

**Pronto quando:** na aba Network do navegador, um duplo clique gera uma única requisição; um 409 mostra a mensagem do backend; com o backend desligado, aparece uma mensagem de falha de conexão; e no sucesso o usuário recebe confirmação.

---

## 8. Lógica e apresentação no mesmo componente

**Sintoma.** `RegisterPage.tsx` guarda o estado, chama a API, trata o erro e renderiza o formulário. Para testar o fluxo de cadastro é preciso renderizar a tela inteira. Além disso, o arquivo se chama `RegisterPage.tsx` e exporta `CreatePage` (linha 4, importado em `App.tsx:2`).

**Causa-raiz.** Tudo foi escrito num lugar só. A sua regra de stack diz "separar lógica de estado dos componentes de apresentação".

**Perguntas para você:**
1. O que mudaria se amanhã houvesse um segundo lugar para cadastrar usuário (um modal de admin, por exemplo)? O que você conseguiria reaproveitar hoje?
2. Com **uma** tela só, separar agora é necessário ou é antecipação? Defenda a sua resposta.

<details><summary>Conceito</summary>

**Custom hook**: uma função `use...` que guarda o estado e as chamadas, enquanto o componente só renderiza. A forma seria algo como `function useCriarUsuario(): { criar, status, erro }`.
</details>

**Pronto quando:** o nome do arquivo e o nome do componente batem, e você tomou uma decisão consciente sobre a separação.

---

## 9. CORS e URL da API fixos no código

**Sintoma.** Se a porta 5173 estiver ocupada, o Vite sobe na 5174. Se você abrir a página por `127.0.0.1` em vez de `localhost`, ou fizer deploy em outro host, o navegador bloqueia por CORS e a tela mostra só "Falhou". E cada controller novo vai ter que repetir a anotação.

**Causa-raiz.** A origem está fixa num único controller (`UsuarioController.java:21`) e a URL absoluta está fixa no front (`usuarioApi.tsx:4`).

**Opções e trade-offs:**

| Opção | Como funciona | Prós | Contras |
|---|---|---|---|
| A. Proxy do Vite (`server.proxy` em `vite.config.ts`) | o front chama um caminho relativo e o Vite repassa ao backend | some o CORS em dev, sem mexer no backend | só vale em dev; em produção alguém precisa fazer esse papel |
| B. CORS global (`WebMvcConfigurer#addCorsMappings`) com a origem vinda de uma property | uma regra para todos os controllers, configurável por ambiente | funciona com front e back em domínios diferentes | ainda precisa da URL da API no front |
| C. `import.meta.env.VITE_API_URL` | a URL da API vem do `.env` | troca de ambiente sem mexer no código | não resolve o CORS sozinha, combina com B |

**Pergunta que decide:** em produção, front e back vão ficar no mesmo domínio?

**Minha recomendação, com o porquê:** hoje o projeto só roda local, então a **A** é a mais simples e elimina o problema inteiro. Quando houver deploy com domínios separados, B + C. A escolha final é sua.

---

## 10. `save` faz `merge` e gera um SELECT a mais

**Prioridade baixa.** O comportamento está correto; o custo é uma query extra. Está aqui para você entender o mecanismo, não para otimizar às cegas.

**Meça primeiro.** O `show-sql` já está ligado (`application.properties:12`). Cadastre um usuário e conte as queries no console. Você espera ver 2 (`exists` e `INSERT`).

**Causa-raiz.** O `SimpleJpaRepository.save` decide entre `persist` (entidade nova) e `merge` (entidade existente) olhando o id. Como o domínio já gera o UUID (`Usuario.java:26`), o id nunca é nulo, a entidade parece existente e o `merge` faz um SELECT antes do INSERT. O retorno do `save` (`UsuarioService.java:35`) também é ignorado.

**Perguntas para você:**
1. Hoje existem **dois** geradores de id: `UUID.randomUUID()` no domínio e `DEFAULT gen_random_uuid()` no banco (`V1__criar_tabela.sql:2`). Qual dos dois de fato roda? O outro é código morto?
2. Que vantagem tem o domínio conhecer o id antes de salvar?

<details><summary>Conceito</summary>

`EntityManager.persist` contra `merge`, `Persistable<ID>#isNew()` e `@GeneratedValue(strategy = GenerationType.UUID)`. Cada um muda **quem** gera o id. Escolha conscientemente.
</details>

---

## 11. O commit `f161fab` mistura quatro mudanças

**Sintoma.** São 24 arquivos e cerca de 2.300 linhas num commit `feat(...)`. Ele junta o scaffold do Vite, a tela de cadastro, o ajuste de CORS e a **correção** do tipo do id no `UsuarioRepository` (de `Long` para `UUID`). Não dá para reverter ou fazer cherry-pick da correção sem levar o frontend inteiro junto.

**A boa notícia:** o `git status` mostra a branch `ahead 2` do remoto, ou seja, esses commits **ainda não foram enviados**. Reescrevê-los agora é seguro.

**Direção, se quiser corrigir:**
1. Guarde o trabalho em andamento primeiro (`git stash`), para não misturar com o que vai ser refeito.
2. Desfaça só o último commit mantendo as alterações (pesquise a diferença entre `git reset --soft` e `--mixed`).
3. Refaça em commits separados, adicionando os arquivos por grupo, por exemplo:
   - `chore(front): cria projeto Vite com React e Tailwind`
   - `fix(usuario): corrige tipo do id no UsuarioRepository`
   - `feat(usuario): integra tela de cadastro com o backend`
4. Rode `git stash pop`.

**Pergunta para você:** vale a pena reescrever, ou basta fazer certo daqui em diante? As duas respostas são aceitáveis. O importante é decidir sabendo o custo de cada uma.

---

## Ajustes pequenos

**O que remover:**
- [ ] Construtor público sem argumentos em `Usuario.java:21-23`. Procurei e **ninguém usa** `new Usuario()`. Ele permite criar um `Usuario` sem passar pelas validações de `criar`. Que motivo existe para ele continuar?
- [ ] Import `Min` sem uso em `CriarUsuarioRequest.java:4`.
- [ ] Import `URI` sem uso em `UsuarioController.java:3`. Ou use-o: veja o item sobre o `Location` abaixo.
- [ ] Sobras do template do Vite. Procurei e nada referencia `hero.png`, `react.svg`, `vite.svg` nem `public/icons.svg`. O `App.css` está vazio, mas é importado em `App.tsx:1`. O `meu-projeto/README.md` é o README padrão do template.

**O que ajustar:**
- [ ] `React.FormEvent` (`RegisterPage.tsx:9`) está marcado `@deprecated` no `@types/react` instalado (`index.d.ts:2173`). O próprio tipo indica `SubmitEvent`.
- [ ] `usuarioApi.tsx` não tem JSX, então deveria ser `.ts`.
- [ ] Rota `/Usuario/Criar` (`UsuarioController.java:20,30`). Em REST, o verbo já é o `POST`, e o recurso costuma ser plural e minúsculo. Um `201 Created` normalmente vem com o cabeçalho `Location` apontando o recurso criado. **Atenção:** mudar a rota quebra o contrato com o front, então mude os dois juntos.
- [ ] Credenciais `postgres/postgres` em `application.properties:4-5`. Pesquise placeholders com valor padrão, como `${DB_PASSWORD:postgres}`, para tirar o segredo do arquivo sem quebrar o ambiente local.

---

## Definição de pronto da branch

**Comandos que precisam passar:**
- `./mvnw test`
- `npm run build` (dentro de `meu-projeto`)
- `npm run lint` (dentro de `meu-projeto`)

**Cenários, testados pelo Swagger ou por teste automatizado:**

| Cenário | Esperado |
|---|---|
| Cadastro válido | 201 com `id` |
| Senha vazia | 400, erro no campo `senha` |
| Senha que passa de 72 bytes | 400, **nunca** 500 |
| E-mail `a@` | 400, erro no campo `email` |
| Corpo sem `login` | 400, erro no campo `login` |
| Login com 101 caracteres | 400 |
| E-mail que já existe | 409 com mensagem |
| Mesmo e-mail com maiúsculas ou espaços | 409 |
| Login que já existe, com e-mail novo | 409 com mensagem |
| Duas requisições iguais ao mesmo tempo | uma 201 e uma 409, nenhum 500 |
| Front: duplo clique | uma requisição só |
| Front: backend desligado | mensagem de falha de conexão na tela |

**Antes de abrir o PR, escreva 3 linhas:** o que você mudou, o que **decidiu não fazer** e por quê.
