# Validação de formulário com `react-hook-form`

- **Data:** 2026-10-27
- **Para quê:** validar o cadastro (`RegisterPage.tsx`) e mostrar uma mensagem embaixo de cada campo, sem escrever um `if` por campo.
- **Biblioteca:** `react-hook-form` 7.91.0, já instalada no `meu-projeto/package.json`.
- **Formato:** segue o `GEMINI.md`. Primeiro o conceito, depois passos curtos. Os trechos de código são esqueletos de poucas linhas, e quem escreve o componente é você.

---

## Como ler este guia

1. Leia a **Parte 1** inteira antes de mexer no código. Ela é curta e explica o que o resto faz.
2. Na **Parte 2**, faça **um passo por vez**. No fim de cada passo, confira o critério de pronto antes de seguir.
3. Se travar, olhe a **Parte 3** (erros comuns) e depois as dicas fechadas do passo.
4. Se aparecer uma palavra desconhecida, ela está no **Glossário**, no final.

---

## Parte 1: o que é

### 1.1 A ideia em uma frase

Você descreve **as regras** de cada campo, e a biblioteca faz **o resto**: lê os valores, roda as regras, guarda as mensagens de erro e só chama a sua função de envio se estiver tudo certo.

O `if` por campo sai do seu código. Ele continua existindo, mas dentro da biblioteca, escrito uma vez só para todos os campos.

### 1.2 Hoje e com `react-hook-form`

Esta tabela resume o guia inteiro. Cada coisa da coluna da esquerda é substituída pela da direita.

| O que você tem hoje | Com `react-hook-form` |
|---|---|
| Um `useState` por campo (`RegisterPage.tsx:5-7`) | Um `useForm()` para o formulário inteiro |
| `value={login}` e `onChange={...}` em cada input | `{...register("login")}` em cada input |
| A função de envio recebe o evento e chama `e.preventDefault()` | A função de envio recebe **os dados prontos**, e o `preventDefault` fica com a biblioteca |
| Um `if` por campo para validar | As regras ficam no `register("login", { ...regras })` |
| Um único `erro` em texto (`:8`) | `errors.login`, `errors.email` e `errors.senha` (um por campo), mais `errors.root` (erro geral) |

### 1.3 Input controlado e input não controlado (por que o `useState` sai)

O jeito que você usa hoje é o **controlado**: o valor do input fica guardado no estado do React.

```
tecla → onChange → setLogin("a") → React re-renderiza o componente → input mostra "a"
```

A cada tecla, o componente inteiro é desenhado de novo.

O `react-hook-form` usa o **não controlado**: o valor fica no próprio input, no DOM. A biblioteca só guarda **onde** cada input está (a `ref`) e vai lá ler o valor quando precisa.

```
tecla → o input guarda o valor sozinho (nada re-renderiza)
...
clique em Enviar → a biblioteca lê o valor de cada input pela ref
```

Uma analogia: no controlado, você anota cada letra num caderno e o input só copia o que está no caderno. No não controlado, o input tem o próprio caderno, e a biblioteca só sabe onde ele está e o abre quando precisa.

É por isso que você não precisa mais de `useState` para os campos, e também por isso que um formulário grande feito com `react-hook-form` continua leve.

### 1.4 As peças

Você vai usar 5 coisas, todas vindas do `useForm`:

```tsx
const { register, handleSubmit, setError, formState } = useForm<CriarUsuarioRequest>();
const { errors, isSubmitting } = formState;
```

**`useForm<CriarUsuarioRequest>()`** cria o formulário. O `<CriarUsuarioRequest>` diz ao TypeScript quais campos existem (`login`, `email`, `senha`). Com isso, um erro de digitação como `register("logn")` nem compila.

**`register("login", regras)`** conecta um input à biblioteca. As regras são opcionais. Ele devolve um objeto com 4 propriedades:

```
register("login")  →  { name: "login", ref, onChange, onBlur }
```

O `{...register("login")}` dentro do `<input>` é um **spread**: ele aplica essas 4 propriedades no input, como se você escrevesse `name`, `ref`, `onChange` e `onBlur` à mão. É assim que a biblioteca fica sabendo de cada tecla e de onde o input está.

**`handleSubmit(enviar)`** é a parte que mais confunde. O `handleSubmit` **não chama** a `enviar` na hora. Ele **devolve uma função nova**, e é essa função que o `<form>` chama no envio. Ela faz o seguinte:

```
handleSubmit(enviar)   ← devolve a função que o <form> vai chamar
  ├─ e.preventDefault()
  ├─ lê o valor de cada input
  ├─ roda as regras de cada campo
  ├─ alguma falhou? → preenche errors, redesenha a tela e NÃO chama enviar
  └─ tudo certo?    → chama enviar(dados)
```

Então a sua `enviar` só roda com dados válidos e recebe os dados já prontos, como `{ login, email, senha }`. Ela não recebe mais o evento.

**`formState.errors`** é um objeto com no máximo um erro por campo. Campo sem erro não aparece nele:

```
errors = {
  login: { type: "minLength", message: "O login precisa ter pelo menos 6 caracteres" },
  // email não aparece → o email está válido
  root:  { message: "Erro na API: 409" }   // erro geral (vindo do servidor)
}
```

Por isso a tela precisa perguntar "tem erro neste campo?" antes de mostrar a mensagem.

**`setError(campo, { message })`** registra um erro manualmente. Serve para o erro que só o servidor conhece. `"root"` é o nome reservado para o erro do formulário como um todo.

**`isSubmitting`** fica `true` enquanto a sua `enviar`, que é `async`, não termina. Serve para desabilitar o botão durante o envio.

### 1.5 Quando a validação roda

Por padrão, a validação roda só quando o usuário clica em Enviar. Depois do primeiro envio com erro, ela passa a rodar **a cada tecla**, e a mensagem some assim que o usuário corrige o campo. Não é preciso configurar nada para isso.

### 1.6 Dois tipos de erro

| Tipo | Exemplo | Quem descobre | Onde aparece |
|---|---|---|---|
| Erro de campo | login com 3 letras, e-mail vazio | o front, antes de enviar | embaixo do campo (`errors.login`) |
| Erro do servidor | e-mail já cadastrado, backend fora do ar | só o backend | numa mensagem geral (`errors.root`) |

A validação no front existe para o conforto do usuário: ele descobre o erro sem esperar a resposta da API. Quem **garante** a regra é o backend (`CriarUsuarioRequest.java:9-11`), porque alguém que chama a API pelo Swagger não passa pelo front.

---

## Parte 2: como fazer

### Onde você parou

Você já começou o Passo 1 no `RegisterPage.tsx`:

- [x] chamou `useForm<CriarUsuarioRequest>()` (`:10`)
- [x] renomeou a função para `enviar` (`:12`)
- [x] o form usa `handleSubmit(enviar)` (`:28`)
- [ ] o `useForm` ainda não foi importado (a linha `:1` só importa `useState`)
- [ ] o tipo `CriarUsuarioRequest` ainda não foi importado
- [ ] a `enviar` ainda recebe o evento (`e: React.FormEvent`) em vez dos dados
- [ ] os 3 `useState` dos campos continuam lá (`:5-7`)
- [ ] os inputs ainda usam `value` e `onChange` (`:36-37`, `:49-50`, `:62-63`)
- [ ] a API ainda é chamada com `{ login, email, senha }` vindo do estado (`:16`)

Enquanto os dois imports não forem feitos, o TypeScript não compila. Eu li o arquivo, mas não rodei o build.

### Passo 1: conectar o formulário, ainda sem regras

**Objetivo:** fazer os dados chegarem na `enviar` pela biblioteca, sem nenhum `useState` de campo.

**O que fazer:**

1. Importe `useForm` de `"react-hook-form"` e o tipo `CriarUsuarioRequest` de `"../types"`.
2. Apague os 3 `useState` dos campos. **Deixe o `erro` por enquanto**, ele sai no Passo 3.
3. Em cada input, tire o `value` e o `onChange` e coloque `{...register("nomeDoCampo")}`. O `type`, o `id` e o `className` continuam.
4. Troque o parâmetro da `enviar`: em vez do evento, ela recebe `dados: CriarUsuarioRequest`.
5. Chame a API passando `dados`, em vez de montar o objeto na mão.

**Esqueleto:**

```tsx
async function enviar(dados: CriarUsuarioRequest) { /* ... */ }

<input id="login" type="text" {...register("login")} />
```

**Pronto quando:**

- `npm run build` e `npm run lint` passam, rodando dentro de `meu-projeto/`;
- com um `console.log(dados)` temporário na `enviar`, o console do navegador mostra `{ login, email, senha }` com o que você digitou.

<details><summary>Pista</summary>

Se o TypeScript reclamar do `handleSubmit(enviar)`, olhe o parâmetro da `enviar`. O `handleSubmit` entrega os dados do formulário, não o evento.

</details>

<details><summary>Conceito</summary>

Pesquise "função que devolve função" (*higher-order function*). O `handleSubmit(enviar)` é executado quando o componente renderiza e devolve a função que vai no `onSubmit`.

</details>

### Passo 2: regras e mensagem embaixo de cada campo

**Objetivo:** ao enviar com um campo inválido, a mensagem aparece embaixo dele e nenhum POST é feito.

**As regras que existem** (vão no segundo argumento do `register`):

| Regra | O que checa | Formato |
|---|---|---|
| `required` | campo vazio | `required: "mensagem"` |
| `minLength` | tamanho mínimo | `minLength: { value: 6, message: "..." }` |
| `maxLength` | tamanho máximo | `maxLength: { value: 100, message: "..." }` |
| `pattern` | formato (regex) | `pattern: { value: /regex/, message: "..." }` |
| `validate` | qualquer regra sua | uma função que devolve `true` ou o texto da mensagem |

**As regras do seu backend** (`CriarUsuarioRequest.java:9-11`), que o front deve repetir:

| Campo | Backend | No front |
|---|---|---|
| login | `@NotBlank @Size(min = 6, max = 100)` | obrigatório, de 6 a 100 caracteres |
| email | `@NotBlank @Email @Size(min = 3, max = 255)` | obrigatório, formato de e-mail, de 3 a 255 caracteres |
| senha | `@NotBlank @Size(min = 3, max = 64)` | obrigatório, de 3 a 64 caracteres |

**O que fazer:**

1. Pegue `errors` de `formState` no `useForm`.
2. Passe as regras no segundo argumento de cada `register`.
3. Embaixo de cada input, mostre `errors.<campo>.message`, mas só quando o erro existir.
4. Coloque `noValidate` no `<form>` (a primeira pegadinha abaixo explica por quê).

**Esqueleto (uma regra, um campo):**

```tsx
{...register("login", { minLength: { value: 6, message: "..." } })}

{errors.login && <span role="alert">{errors.login.message}</span>}
```

**Duas pegadinhas:**

- **`type="email"` e o navegador.** O input de e-mail tem `type="email"` (`:47`). Com um e-mail mal formatado, o **navegador** valida antes, mostra o balão dele e cancela o envio, e a biblioteca nem chega a rodar. O `noValidate` no `<form>` desliga a validação do navegador e deixa só a sua.
- **`required` não reprova espaço em branco.** Ele só reprova texto exatamente vazio. Então `"   "` passa no front, mas o `@NotBlank` do backend reprova. Para o front se comportar igual ao backend, use `validate` com `trim()`.

**Pronto quando:**

- build e lint passam;
- ao enviar com tudo vazio, aparecem as 3 mensagens e a aba **Network** do navegador não mostra nenhum POST;
- em seguida, ao corrigir o login, a mensagem dele some enquanto você digita.

<details><summary>Pista</summary>

A mensagem não aparece, mas o POST também não sai? Confira se a regra tem `message`. Sem ela, o erro existe, mas o texto vem vazio.

</details>

<details><summary>Conceito</summary>

Pesquise *short-circuit* com `&&` no JSX e *optional chaining* (`?.`): mostrar algo só quando o valor existe.

</details>

### Passo 3: erro do servidor

**Objetivo:** o erro que vem da API aparece na tela através da biblioteca, e o `useState` de `erro` sai do componente.

**O que fazer:**

1. **Primeiro**, apague o `const [erro, setError] = useState("")` (`:8`). O `useForm` também tem um `setError`, e com os dois no mesmo componente o nome colide.
2. Pegue o `setError` do `useForm`.
3. No `catch`, troque o `setError(mensagem)` antigo por `setError("root", { message: ... })`.
4. No JSX, onde hoje está o `{erro && ...}` (`:69-73`), mostre `errors.root.message` quando ele existir.
5. Apague o `setError("")` do começo da `enviar` (`:13`). A biblioteca já limpa o `root` a cada novo envio.
6. Se o `useState` não for mais usado, apague também o import dele.

**Esqueleto:**

```tsx
setError("root", { message: "..." });
```

**O que esperar por enquanto:** a mensagem vai ser algo como `Erro na API: 409`, porque o `usuarioApi.tsx:15` só repassa o status. É o comportamento esperado. Uma mensagem melhor depende do BUG-04: o backend precisa devolver o erro num formato que diga qual campo falhou. Quando isso existir, basta trocar o nome: `setError("email", { ... })` faz a mensagem aparecer embaixo do campo de e-mail.

**Pronto quando:**

- build e lint passam;
- com o backend **desligado**, aparece a mensagem geral;
- ao cadastrar o **mesmo e-mail** duas vezes, a segunda tentativa mostra a mensagem;
- ao enviar de novo, a mensagem antiga some antes de aparecer a nova.

### Passo 4 (opcional): bloquear o envio duplo

O `isSubmitting` fica `true` desde o clique até a `enviar` terminar. Use-o no `disabled` do botão.

Isso só funciona porque a `enviar` é `async` e faz `await` na chamada da API: é essa promise que a biblioteca espera terminar.

**Pronto quando:** um duplo clique rápido gera **um** único POST na aba Network.

---

## Parte 3: erros comuns

| Sintoma | Causa provável | Direção |
|---|---|---|
| `Cannot find name 'useForm'` | faltou o import | importe de `"react-hook-form"` |
| O TypeScript reclama do `handleSubmit(enviar)` | a `enviar` ainda recebe o evento | o parâmetro é `dados: CriarUsuarioRequest` |
| `Cannot redeclare block-scoped variable 'setError'` | o `useState` de `erro` ainda existe junto com o `setError` do `useForm` | apague o `useState` antigo |
| Com um e-mail mal formatado, clico em Enviar e nada acontece | o navegador está validando o `type="email"` | `noValidate` no `<form>` |
| A mensagem de erro não aparece | faltou `message` na regra, ou faltou pegar `errors` de `formState` | confira as duas coisas |
| `"   "` passa como login válido | o `required` só reprova texto vazio | `validate` com `trim()` |
| O botão não desabilita durante o envio | a `enviar` não espera a API | `await` na chamada |
| `dados` chega com um campo `undefined` | o input daquele campo não tem `register` com esse nome | confira o nome no `register("...")` |

---

## Parte 4: para pensar

1. Se amanhã o mínimo do login mudar de 6 para 8 no backend, quantos lugares você vai precisar alterar? O que acontece se você esquecer o front?
2. Por que a biblioteca não redesenha o formulário a cada tecla? Em que tipo de tela isso faria diferença de verdade?
3. Se a validação do front existe só para o conforto do usuário, por que não deixar tudo com o backend?

---

## O que ficou de fora

Estes assuntos ficaram fora deste guia de propósito:

- **Schema (Zod, Yup, Valibot) com `@hookform/resolvers`:** tira as regras do JSX e gera o tipo a partir delas. Compensa quando há muitos campos ou regras entre campos, como "confirmar senha". São duas dependências novas.
- **Limpar o formulário depois do sucesso** (`reset`) e mostrar uma confirmação. As duas coisas fazem parte do BUG-05.
- **Contrato de erro do backend** com a lista de campos inválidos (BUG-04).

---

## Glossário

- **DOM:** a árvore de elementos que o navegador desenha (`<form>`, `<input>` e outros).
- **Re-renderizar:** o React executar a função do componente de novo e atualizar a tela.
- **Input controlado:** o valor fica no estado do React (`value` com `onChange`).
- **Input não controlado:** o valor fica no próprio input, e o código vai lá ler quando precisa.
- **`ref`:** uma referência direta a um elemento do DOM, o "endereço" do input.
- **Spread (`{...obj}`):** aplica as propriedades de um objeto como props do elemento.
- **Genérico (`useForm<T>()`):** informa ao TypeScript o tipo dos dados, para ele checar nomes e tipos.
- **Promise e `async`:** uma promise representa uma operação que termina depois, como uma chamada à API. `async` marca a função que trabalha com ela, e `await` espera a operação terminar.

---

## Onde conferi

Comportamentos verificados no código da versão instalada (`meu-projeto/node_modules/react-hook-form/dist/`):

- A validação roda no envio e, depois do primeiro erro, a cada tecla: `index.esm.mjs:2077-2078` (`mode: onSubmit`, `reValidateMode: onChange`).
- O `handleSubmit` chama o `preventDefault` e liga o `isSubmitting`: `index.esm.mjs:3411-3421`.
- O `handleSubmit` limpa o erro `root` a cada envio: `index.esm.mjs:3459`.
- O `required` só reprova texto exatamente vazio: `index.esm.mjs:873-878`.
- Uma regra sem `message` gera erro com texto vazio: `index.esm.mjs:842-847`.
- O que o `register` devolve (`name`, `ref`, `onChange`, `onBlur`): `types/form.d.ts:132-144`.
- As regras aceitas pelo `register`: `types/validator.d.ts:25-41`.
- Os nomes aceitos pelo `setError` (`"root"`, `"root.algo"`): `types/form.d.ts:272`.

Não testei no navegador:

- o bloqueio do envio feito pelo `type="email"`, que é o comportamento padrão de validação do HTML;
- nenhum dos critérios de pronto acima.
