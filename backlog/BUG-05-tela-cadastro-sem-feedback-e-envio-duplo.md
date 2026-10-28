# BUG-05: Tela de cadastro não mostra erro nem sucesso e permite envio duplo

- **Severidade:** Alta
- **Área:** frontend
- **Onde:**
  - `meu-projeto/src/features/auth/pages/RegisterPage.tsx:9-16`
  - `meu-projeto/src/features/auth/pages/RegisterPage.tsx:61-66`
  - `meu-projeto/src/features/auth/api/usuarioApi.tsx:14-18`
- **Evidência:** lido no código.

## Sintoma

- Toda falha termina em `console.log("Falhou")` e a tela não muda. Isso vale para e-mail duplicado, erro de validação, 500, backend fora do ar e CORS bloqueado.
- No sucesso (201) a tela também não muda, e os campos continuam preenchidos. O usuário tende a reenviar.
- O botão continua habilitado durante o envio, então um duplo clique dispara dois POSTs e cai na corrida do BUG-01.
- Os inputs não têm `required`, `minLength` nem `maxLength`. O backend exige login com pelo menos 6 caracteres, e o usuário só descobre isso depois de enviar, sem nenhuma indicação na tela.

## Como reproduzir

1. Suba o front e o backend e cadastre um usuário.
2. Cadastre de novo com o mesmo e-mail. Nada acontece na tela.
3. Derrube o backend e envie o formulário. Também nada acontece na tela.

## Causa-raiz

- `usuarioApi.tsx:14-16` lança um `Error` só com o status e descarta o corpo de erro que a API mandou.
- `usuarioApi.tsx:18` chama `response.json()` sem nenhuma condição.
- `RegisterPage.tsx:13-15` engole todos os erros, e `RegisterPage.tsx:12` ignora o resultado do sucesso.
- Não existe estado de "enviando" para desabilitar o botão (`RegisterPage.tsx:61-66`).

## Direção

- O componente precisa representar os estados do envio (ocioso, enviando, sucesso e erro) e mostrar cada um na tela.
- A camada de API precisa entregar ao componente o erro estruturado do backend, para que a tela mostre a mensagem certa por campo. Isso depende do contrato definido no BUG-04.
- Bloqueie novos envios enquanto houver uma requisição em andamento.
- Espelhe no HTML as regras do backend com os atributos de validação nativos. Isso não substitui a validação do servidor.

## Critério de pronto

- E-mail duplicado mostra uma mensagem visível na tela.
- Erro de validação mostra a mensagem junto do campo correspondente.
- Com o backend fora do ar, aparece uma mensagem genérica visível.
- O sucesso mostra uma confirmação visível (ou redireciona) e limpa o formulário.
- Um duplo clique gera um único POST (confira na aba Network do navegador).
