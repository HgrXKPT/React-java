# Formulário em React, do zero

- **Data:** 2026-10-27
- **Para quem:** quem programa em C# ou Java e nunca estudou React.
- **Objetivo:** entender o formulário de cadastro que você já tem e depois validá-lo com `react-hook-form`.
- **Por que este guia existe:** o anterior (`validacao-formulario-react-hook-form.md`) partia do princípio de que você já sabia React. Este começa do começo.

## Como ler

1. **Parte 1:** o mínimo de React, sempre comparando com C#. Não pule esta parte.
2. **Parte 2:** o seu formulário de hoje, explicado linha por linha.
3. **Parte 3:** um exemplo **completo** com `react-hook-form`, explicado bloco a bloco.
4. **Parte 4:** como levar o exemplo para o seu `RegisterPage.tsx`.
5. **Parte 5:** os erros mais comuns e como resolver cada um.

O exemplo da Parte 3 é de um formulário diferente do seu (contato) de propósito. Você aprende lendo um código que funciona e aplica no seu fazendo a tradução, e essa tradução é o seu exercício. Compilei o exemplo com as mesmas configurações de TypeScript do seu projeto e ele passa.

---

## Parte 1: o mínimo de React

### 1.1 Componente é uma função que devolve tela

```tsx
export function Saudacao() {
  return <h1>Olá</h1>;
}
```

- É uma função comum de TypeScript. O que ela devolve é o pedaço de tela que vai aparecer.
- Quem chama essa função é o React, não você. Você só a usa como se fosse uma tag: `<Saudacao />`. É o que o seu `App.tsx:9` faz com `<CreatePage />`.
- **Re-renderizar** é o React chamar a sua função de novo para desenhar a tela atualizada. Isso acontece muitas vezes, e vai ser importante no item 1.6.

Em C#, pense num método que monta e devolve o HTML de um pedaço da página e que o framework chama sempre que algo muda.

### 1.2 JSX: HTML dentro do TypeScript

O que vem depois do `return` parece HTML, mas é **JSX**. Se você já viu Razor (`.cshtml`), é a mesma ideia: HTML com código no meio.

| O que você quer | Razor (C#) | JSX (React) |
|---|---|---|
| Mostrar uma variável | `@nome` | `{nome}` |
| Classe CSS | `class="..."` | `className="..."` |
| Ligar o label ao campo | `for="login"` | `htmlFor="login"` |
| Mostrar só se tiver erro | `@if (erro != "") { <div>@erro</div> }` | `{erro && <div>{erro}</div>}` |

A regra geral: **dentro de `{ }` é código TypeScript**, fora é tag.

O `&&` da última linha é estranho no começo. Em TypeScript, `a && b` devolve `b` quando `a` tem valor e devolve o próprio `a` quando ele está vazio (`""`, `false`, `null`, `undefined`). O React não desenha nada visível para esses valores vazios. Então `{erro && <div>{erro}</div>}` se lê assim: "se `erro` tiver texto, desenha a div; se não tiver, não desenha nada".

### 1.3 Atributos são parâmetros (props)

```tsx
<input type="text" id="login" value={login} />
```

Cada atributo é uma **prop**, como um parâmetro nomeado em C#: `new Input(type: "text", id: "login", value: login)`. Texto fixo vai entre aspas, e código vai entre `{ }`.

### 1.4 Arrow function é lambda

```tsx
(e) => setLogin(e.target.value)
```

É exatamente uma lambda do C#, como `e => SetLogin(e.Target.Value)`: uma função sem nome, escrita no lugar onde é usada.

### 1.5 Eventos

```tsx
<input onChange={(e) => setLogin(e.target.value)} />
```

- `onChange` quer dizer "quando o texto mudar, chame esta função". Em C#, seria `textBox.TextChanged += (s, e) => ...`.
- `e` é o evento, `e.target` é o elemento que disparou o evento (o input) e `e.target.value` é o texto que está dentro dele.

```tsx
<form onSubmit={handleSubmit}>
```

- `onSubmit` dispara quando o usuário clica num botão `type="submit"` dentro do form ou aperta Enter num campo.
- Por padrão, o navegador **recarrega a página** quando um form é enviado. Isso vem do HTML antigo, que mandava o form direto para o servidor. O `e.preventDefault()` cancela esse comportamento. Sem ele, a página pisca e o que estava digitado se perde.

### 1.6 Estado: o `useState`

O problema: um componente é uma função, e o React chama essa função de novo a cada re-renderização. Uma variável comum, como `let login = ""`, nasceria de novo, vazia, a cada chamada.

O `useState` pede ao React para **guardar um valor entre uma chamada e outra**:

```tsx
const [login, setLogin] = useState("");
```

- `useState("")` cria o valor, começando com `""`.
- `login` é o valor atual, só para leitura.
- `setLogin("abc")` é a única forma de mudar o valor. Ela guarda o novo valor **e faz o React re-renderizar** o componente, para a tela mostrar o que mudou.

A sintaxe `const [a, b] = ...` é uma desestruturação, igual à desconstrução de tupla do C#: `var (login, setLogin) = UseState("");`.

Se você conhece WPF/MVVM, é como uma propriedade que dispara `PropertyChanged`: o valor mudou, a tela atualiza.

### 1.7 Hooks

Funções que começam com `use`, como `useState` (e, mais adiante, `useForm`), são **hooks**. Elas dão ao componente coisas que o React guarda para ele, como o estado. A regra é chamar hooks **no topo do componente**, nunca dentro de um `if`, de um laço ou de outra função.

### 1.8 Desestruturação de objeto

```tsx
const { register, handleSubmit } = useForm();
```

O `useForm()` devolve um objeto com várias propriedades, e as chaves `{ }` do lado esquerdo pegam algumas delas pelo nome. Em C#, seria:

```csharp
var form = UseForm();
var register = form.Register;
var handleSubmit = form.HandleSubmit;
```

Também dá para pegar algo de dentro de uma propriedade: `formState: { errors }` quer dizer "de dentro de `formState`, pegue `errors`".

### 1.9 Spread: `{...objeto}`

```tsx
const props = { type: "text", id: "login" };

<input {...props} />
// é o mesmo que:
<input type="text" id="login" />
```

Os `...` espalham cada propriedade do objeto como um atributo da tag. Não existe um equivalente direto em C#. Você vai usar isso com o `register`.

### 1.10 O que é igual ao C#

| TypeScript | C# |
|---|---|
| `type CriarUsuarioRequest = { email: string; ... }` | `record CriarUsuarioRequest(string Email, ...)`, que só descreve o formato dos dados |
| `useForm<CriarUsuarioRequest>()` | `UseForm<CriarUsuarioRequest>()`, um método genérico |
| `async function enviar() { await ... }` | `async Task Enviar() { await ... }` |
| `Promise<T>` | `Task<T>` |
| `try { } catch (error) { }` | `try { } catch (Exception ex) { }` |
| `condicao ? "a" : "b"` | `condicao ? "a" : "b"` (o ternário é igual) |
| `import { X } from "./arquivo"` | parecido com `using` |
| `import type { X } from "./arquivo"` | `using` só para um tipo, que some do código final |

---

## Parte 2: o seu formulário de hoje, linha por linha

Esta é a versão que você escreveu e que funcionava antes de começar a troca para o `react-hook-form`. Para caber, tirei o `className` e deixei só o input de login. Os outros dois são iguais.

### A parte de código

```tsx
import { useState } from "react";               // traz o hook de estado (1.6)
import { UsuarioApi } from "../api/usuarioApi"; // traz a sua classe que chama o backend

export function CreatePage() {                  // o componente (1.1)
  const [login, setLogin] = useState("");       // valor do campo login
  const [erro, setError] = useState("");        // texto do erro geral
  // (email e senha têm um useState cada, igual ao login)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();                         // não recarregar a página (1.5)
    setError("");                               // apaga o erro do envio anterior

    try {
      await UsuarioApi.criar({ login, email, senha }); // POST para o backend
    } catch (error: unknown) {
      if (error instanceof Error) {             // parecido com catch (Exception ex)
        setError(error.message);                // guarda a mensagem do erro
      } else {
        setError("Ocorreu um erro inesperado.");
      }
    }
  }
```

### A parte de tela (JSX)

```tsx
  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="login">Login:</label>
      <input
        type="text"
        id="login"
        value={login}
        onChange={(e) => setLogin(e.target.value)}
      />

      {erro && <div role="alert">{erro}</div>}

      <button type="submit">Enviar</button>
    </form>
  );
}
```

- `onSubmit={handleSubmit}`: ao enviar o form, chama a sua função `handleSubmit`.
- `value={login}`: o input mostra o que está guardado no estado.
- `onChange={...}`: cada tecla atualiza o estado.
- `{erro && ...}`: a div só aparece se `erro` tiver texto (1.2).
- `role="alert"`: faz o leitor de tela anunciar a mensagem.

### O que acontece quando o usuário digita "a" no login

1. O navegador dispara o `onChange` do input.
2. A sua lambda chama `setLogin("a")`.
3. O React guarda `"a"` e chama `CreatePage()` de novo, ou seja, re-renderiza.
4. Nessa nova chamada, `login` vale `"a"`, e o `value={login}` faz o input mostrar `"a"`.

### O que acontece quando o usuário clica em Enviar

1. O form dispara o `onSubmit`, que chama a sua `handleSubmit`.
2. O `preventDefault` impede o recarregamento da página.
3. O `setError("")` apaga o erro antigo.
4. A API é chamada. Se der erro, o `setError(mensagem)` guarda o texto e o React re-renderiza. Agora `erro` tem valor, e a div aparece.

### Onde entraria a validação feita na mão

Entre os passos 3 e 4: um `if (login === "")` para cada campo, cada um chamando `setError`. É esse trabalho que o `react-hook-form` vai tirar de você.

---

## Parte 3: `react-hook-form` com um exemplo completo

Este é um formulário de **contato**, com nome e mensagem. Não é o seu, mas usa todas as peças que o seu vai usar. Leia o código inteiro uma vez e depois leia a explicação bloco a bloco.

A `ContatoApi` foi inventada para o exemplo. Imagine uma classe igual à sua `UsuarioApi`, que lança um `Error` quando a resposta não é de sucesso.

```tsx
import { useForm } from "react-hook-form";
import { ContatoApi } from "../api/contatoApi";

type ContatoForm = {
  nome: string;
  mensagem: string;
};

export function ContatoPage() {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<ContatoForm>();

  async function enviar(dados: ContatoForm) {
    try {
      await ContatoApi.enviar(dados);
    } catch (error: unknown) {
      if (error instanceof Error) {
        setError("root", { message: error.message });
      } else {
        setError("root", { message: "Ocorreu um erro inesperado." });
      }
    }
  }

  return (
    <form onSubmit={handleSubmit(enviar)} noValidate>
      <label htmlFor="nome">Nome:</label>
      <input
        type="text"
        id="nome"
        {...register("nome", {
          required: "Informe o nome",
          minLength: { value: 2, message: "O nome precisa ter pelo menos 2 caracteres" },
          validate: (valor) => valor.trim() !== "" || "O nome não pode ser só espaços",
        })}
      />
      {errors.nome && <span role="alert">{errors.nome.message}</span>}

      <label htmlFor="mensagem">Mensagem:</label>
      <textarea
        id="mensagem"
        {...register("mensagem", {
          required: "Escreva a mensagem",
          maxLength: { value: 500, message: "A mensagem pode ter no máximo 500 caracteres" },
        })}
      />
      {errors.mensagem && <span role="alert">{errors.mensagem.message}</span>}

      {errors.root && <div role="alert">{errors.root.message}</div>}

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Enviando..." : "Enviar"}
      </button>
    </form>
  );
}
```

### Bloco 1: imports e o tipo

```tsx
import { useForm } from "react-hook-form";

type ContatoForm = {
  nome: string;
  mensagem: string;
};
```

- O `useForm` vem da biblioteca.
- `ContatoForm` descreve os campos do formulário, como um DTO. Os nomes daqui (`nome`, `mensagem`) são os mesmos que vão no `register("nome")`, e o TypeScript confere: `register("nomee")` não compila.

### Bloco 2: o `useForm`

```tsx
const {
  register,
  handleSubmit,
  setError,
  formState: { errors, isSubmitting },
} = useForm<ContatoForm>();
```

- É chamado uma vez, no topo do componente, porque é um hook (1.7).
- `<ContatoForm>` é o genérico (1.10).
- A desestruturação (1.8) pega 4 coisas:
  - `register`: conecta um input ao formulário;
  - `handleSubmit`: envolve a sua função de envio;
  - `setError`: registra um erro manualmente;
  - `formState: { errors, isSubmitting }`: os erros atuais e a informação de se o formulário está sendo enviado.
- Repare que **não há nenhum `useState`**. A biblioteca guarda os valores para você.

### Bloco 3: a função `enviar`

```tsx
async function enviar(dados: ContatoForm) {
  try {
    await ContatoApi.enviar(dados);
  } catch (error: unknown) {
    if (error instanceof Error) {
      setError("root", { message: error.message });
    } else {
      setError("root", { message: "Ocorreu um erro inesperado." });
    }
  }
}
```

- Ela recebe os `dados` já prontos e já validados, como `{ nome: "...", mensagem: "..." }`.
- Ela **não** recebe o evento, **não** chama `preventDefault` e **não** tem `if` de validação. Se ela está rodando, é porque os dados passaram em todas as regras.
- No `catch`, o `setError("root", { message })` guarda o erro geral. `"root"` é o nome reservado para o erro do formulário inteiro, e não de um campo. Ele substitui o seu `useState` de `erro`.
- Não existe um `setError("")` no começo, porque a biblioteca já limpa o `root` sozinha a cada novo envio.

### Bloco 4: o `<form>`

```tsx
<form onSubmit={handleSubmit(enviar)} noValidate>
```

O ponto mais importante: o `handleSubmit(enviar)` **não chama** a `enviar` nesse momento. Ele cria e devolve uma função nova, e é essa função nova que o form chama no envio. Ela faz o seguinte:

```
1. chama o preventDefault
2. lê o valor de cada input
3. roda as regras de cada campo
4. se alguma regra falhou: preenche o errors, re-renderiza e NÃO chama a enviar
5. se todas passaram:      chama enviar(dados)
```

Você já conhece esse papel: é o mesmo do `@Valid` no seu `UsuarioController.java:31`. A validação roda antes, e o seu método só é chamado se tudo passou.

O `noValidate` desliga a validação do próprio navegador. Sem ele, um input `type="email"` com um texto inválido faz o navegador mostrar um balão dele e cancelar o envio antes de a biblioteca rodar.

### Bloco 5: o input com `register`

```tsx
<input
  type="text"
  id="nome"
  {...register("nome", {
    required: "Informe o nome",
    minLength: { value: 2, message: "O nome precisa ter pelo menos 2 caracteres" },
    validate: (valor) => valor.trim() !== "" || "O nome não pode ser só espaços",
  })}
/>
```

- `register("nome", regras)` devolve `{ name, ref, onChange, onBlur }`, e o spread (1.9) coloca tudo isso no input.
- Com isso, a biblioteca escuta o `onChange` e sabe onde o input está (a `ref`, que é o "endereço" do elemento na tela). **É por isso que o `value` e o `onChange` somem**: o `register` já cuida deles.
- A diferença em relação ao seu jeito atual é que o valor fica no próprio input, e não num `useState`. A biblioteca lê o valor quando precisa. Como consequência, digitar não re-renderiza o componente.
- As regras do nome:
  - `required: "Informe o nome"` falha se o campo estiver vazio. O texto é a mensagem de erro.
  - `minLength: { value: 2, message: "..." }` falha se houver menos de 2 caracteres.
  - `validate: (valor) => ...` é uma regra sua, escrita como lambda. Ela é necessária porque o `required` só reprova texto exatamente vazio, então `"   "` passaria. Leia assim: "se o valor sem espaços não estiver vazio, devolve `true` (válido); senão, devolve a mensagem". O `||` devolve o lado direito quando o lado esquerdo é `false`.
- Se mais de uma regra falhar, aparece só uma mensagem por vez.

### Bloco 6: a mensagem embaixo do campo

```tsx
{errors.nome && <span role="alert">{errors.nome.message}</span>}
```

- É a mesma ideia do seu `{erro && ...}` (1.2): se `errors.nome` existir, desenha o span.
- `errors.nome` só existe enquanto o nome estiver inválido. Quando o usuário corrige o campo, ele some, e o span some junto.

### Bloco 7: o `<textarea>`

Ele está no exemplo só para mostrar que o `register` funciona do mesmo jeito em qualquer campo de texto.

### Bloco 8: erro geral e botão

```tsx
{errors.root && <div role="alert">{errors.root.message}</div>}

<button type="submit" disabled={isSubmitting}>
  {isSubmitting ? "Enviando..." : "Enviar"}
</button>
```

- `errors.root` é o que o `setError("root", ...)` do Bloco 3 guardou.
- `disabled={isSubmitting}`: enquanto a `enviar` não termina (o `await` da API), `isSubmitting` vale `true` e o botão fica desabilitado. Isso impede o clique duplo.
- `isSubmitting ? "Enviando..." : "Enviar"` é o ternário, igual ao do C#.

### Passo a passo de um envio

**O usuário deixa o nome vazio e clica em Enviar:**

1. O form chama a função que o `handleSubmit` criou.
2. Ela lê `nome = ""` e roda as regras. O `required` falha.
3. Ela grava `errors.nome = { message: "Informe o nome" }` e re-renderiza.
4. Na nova renderização, `errors.nome` existe, então o span aparece.
5. A `enviar` **não roda**, e nenhum POST é feito.
6. O usuário começa a digitar. Depois do primeiro envio com erro, a biblioteca valida a cada tecla. Quando o nome fica válido, `errors.nome` deixa de existir e a mensagem some.

**O usuário preenche tudo certo e clica em Enviar, mas a API falha:**

1. As regras passam, e a `enviar(dados)` roda.
2. `isSubmitting` vira `true`, e o botão é desabilitado e mostra "Enviando...".
3. A API lança um erro, e o `catch` chama `setError("root", ...)`.
4. A `enviar` termina e `isSubmitting` volta a `false`. A tela re-renderiza com a mensagem geral, e o botão volta ao normal.

### As regras disponíveis

| Regra | Falha quando | Como escrever |
|---|---|---|
| `required` | o campo está vazio | `required: "mensagem"` |
| `minLength` | o texto tem menos caracteres que o mínimo | `minLength: { value: 6, message: "mensagem" }` |
| `maxLength` | o texto tem mais caracteres que o máximo | `maxLength: { value: 100, message: "mensagem" }` |
| `pattern` | o texto não bate com a regex | `pattern: { value: /^\S+@\S+$/, message: "mensagem" }` |
| `validate` | a sua lambda devolve um texto | `validate: (valor) => condição \|\| "mensagem"` |

A regex `/^\S+@\S+$/` quer dizer: "um ou mais caracteres que não são espaço, depois um `@`, depois um ou mais caracteres que não são espaço". É uma checagem simples de formato de e-mail. A checagem completa continua sendo a do `@Email` no backend.

Sempre coloque `message`. Sem ela, o erro existe, mas o texto chega vazio e nada aparece na tela.

---

## Parte 4: levando o exemplo para o seu `RegisterPage.tsx`

O exemplo e o seu formulário seguem a mesma estrutura, só com outros nomes. Esta é a tradução:

| No exemplo | No seu `RegisterPage.tsx` |
|---|---|
| `type ContatoForm = { ... }`, criado no próprio arquivo | `CriarUsuarioRequest`, que já existe em `features/auth/types/index.ts`. Basta importar com `import type { CriarUsuarioRequest } from "../types/index";`. O `type` é obrigatório (veja a Parte 5) e já é usado no `usuarioApi.tsx:1`. |
| `useForm<ContatoForm>()` | `useForm<CriarUsuarioRequest>()`, que você já escreveu na `:10` |
| campos `nome` e `mensagem` | campos `login`, `email` e `senha` |
| `ContatoApi.enviar(dados)` | `UsuarioApi.criar(dados)` |
| regras do nome e da mensagem | as regras do seu backend (tabela abaixo) |
| `<textarea>` | `<input>`, mantendo o `type` (`text`, `email`, `password`), o `id` e o `className` de cada um |

### As regras do seu backend (`CriarUsuarioRequest.java:9-11`)

| Campo | No Java | Regras no `register` |
|---|---|---|
| `login` | `@NotBlank @Size(min = 6, max = 100)` | `required`, `minLength` 6, `maxLength` 100 e `validate` com `trim` |
| `email` | `@NotBlank @Email @Size(min = 3, max = 255)` | `required`, `minLength` 3, `maxLength` 255, `pattern` de e-mail e `validate` com `trim` |
| `senha` | `@NotBlank @Size(min = 3, max = 64)` | `required`, `minLength` 3, `maxLength` 64 e `validate` com `trim` |

O `validate` com `trim` existe porque o `@NotBlank` do Java reprova texto só com espaços, e o `required` do front não reprova.

### O que sai do seu arquivo atual

| Linha | O que é | Por que sai |
|---|---|---|
| `:1` | `import { useState } from "react";` | sem nenhum `useState` no arquivo, o import fica sem uso e o build quebra (o `tsconfig.app.json:20` liga `noUnusedLocals`). No lugar dele entra o `import { useForm } from "react-hook-form";` |
| `:5-7` | os 3 `useState` dos campos | o `register` passa a guardar os valores |
| `:8` | o `useState` de `erro` | vira `setError("root", ...)` e `errors.root`. Além disso, o nome `setError` colidiria com o do `useForm` |
| `:12` | o parâmetro `e: React.FormEvent` | a `enviar` passa a receber `dados: CriarUsuarioRequest` |
| `:13` | `setError("")` | a biblioteca limpa o `root` sozinha |
| `:16` | `{ login, email, senha }` | vira só `dados` |
| `:36-37`, `:49-50`, `:62-63` | `value` e `onChange` | o `{...register(...)}` substitui os dois |
| `:69-73` | `{erro && ...}` | vira `{errors.root && ...}` |

### O que entra

- Os dois imports: o do `useForm` e o do tipo `CriarUsuarioRequest`.
- O `noValidate` no `<form>`.
- Um `{...register("campo", { regras })}` em cada input.
- Um `{errors.campo && <span ...>}` embaixo de cada input.
- O `setError("root", ...)` no `catch` e o `{errors.root && ...}` na tela.
- O `disabled={isSubmitting}` no botão.

### Ordem sugerida

Faça um passo por vez e teste entre um passo e outro. Assim, quando algo quebrar, você sabe exatamente o que causou.

1. Imports, `useForm`, `register` nos 3 inputs **sem regras** e a `enviar(dados)` com um `console.log(dados)`.
2. Regras, span de erro em cada campo e `noValidate`.
3. `setError("root", ...)` no `catch` e `errors.root` no lugar do `erro`.
4. `isSubmitting` no botão.

### Como testar

Dentro da pasta `meu-projeto`:

```bash
npm run build   # o TypeScript confere os tipos; precisa terminar sem erro
npm run lint    # o oxlint confere o código; precisa terminar sem erro
npm run dev     # sobe o front; abra no navegador o endereço que ele mostrar
```

No navegador, aperte **F12** para abrir as ferramentas de desenvolvedor:

- a aba **Console** mostra o `console.log(dados)`;
- a aba **Network** lista as chamadas HTTP. Um envio válido aparece como `Criar`, e um envio bloqueado pela validação não aparece.

| Passo | Pronto quando |
|---|---|
| 1 | build e lint passam, e o Console mostra `{ login, email, senha }` com o que você digitou |
| 2 | enviar com tudo vazio mostra as 3 mensagens e nada aparece na Network; ao corrigir um campo, a mensagem dele some enquanto você digita |
| 3 | com o backend desligado aparece a mensagem geral (uma mensagem do navegador em inglês; no Chrome, `Failed to fetch`), e ao cadastrar o mesmo e-mail duas vezes aparece `Erro na API:` seguido do status, porque o `usuarioApi.tsx:15` só repassa o status |
| 4 | um duplo clique rápido gera uma única chamada na Network |

---

## Parte 5: erros comuns

As mensagens abaixo foram geradas de propósito com as configurações de TypeScript do seu projeto, então devem aparecer iguais no seu build.

| O que aparece | Causa | Como resolver |
|---|---|---|
| `Cannot find name 'useForm'.` | faltou o import | `import { useForm } from "react-hook-form";` |
| `'CriarUsuarioRequest' is a type and must be imported using a type-only import when 'verbatimModuleSyntax' is enabled.` | o tipo foi importado sem `type`. O `tsconfig.app.json:14` liga o `verbatimModuleSyntax`, que exige o `type` | `import type { CriarUsuarioRequest } from "../types/index";` |
| `'useState' is declared but its value is never read.` | sobrou o import do `useState` | apague a linha |
| `'e' is declared but its value is never read.` | sobrou o parâmetro do evento na `enviar` | troque por `dados: CriarUsuarioRequest` |
| erro de tipo no `handleSubmit(enviar)` | a `enviar` ainda recebe o evento | igual ao item acima |
| `Cannot redeclare block-scoped variable 'setError'.` | o `useState` de `erro` e o `useForm` criam um `setError` cada | apague o `useState` de `erro` |
| o input não deixa digitar, ou o dado chega vazio | sobrou um `value={...}` no input | apague o `value` e o `onChange` |
| com um e-mail mal formatado, nada acontece ao clicar em Enviar (ou aparece um balão do navegador) | o navegador está validando o `type="email"` antes da biblioteca | `noValidate` no `<form>` |
| o campo está inválido, mas nenhuma mensagem aparece | faltou `message` na regra, ou faltou o span com `errors.campo` | confira os dois |
| `"   "` passa como válido | o `required` só reprova texto vazio | `validate` com `trim()` |

---

## O que eu conferi e o que não testei

- **Conferi:** o exemplo da Parte 3 compila com as mesmas opções do seu `tsconfig.app.json` (`strict`, `verbatimModuleSyntax`, `noUnusedLocals`, `noUnusedParameters`). As mensagens da Parte 5 saíram de uma compilação feita de propósito com esses erros. O comportamento da biblioteca está conferido no código da versão instalada, com as linhas listadas no fim do guia anterior.
- **Não testei:** o exemplo rodando no navegador, nem nenhum dos critérios de pronto da Parte 4.
