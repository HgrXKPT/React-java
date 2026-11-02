# US-05: Regras de alerta (limite, criticidade e vigência)

- **Fase:** C. O núcleo
- **Status:** pendente
- **Depende de:** US-02 (a regra se aplica a algo que já está cadastrado)
- **Área:** backend e frontend
- **Rota da tela:** `/admin/regras-alerta`
- **Endpoints:** `GET /regras-alerta`, `GET /regras-alerta/{id}`, `POST /regras-alerta`, `PUT /regras-alerta/{id}`

## A dor

O sensor só mede. Ele não sabe que -18 °C é normal para congelados e que -5 °C é um desastre. **Alguém precisa dizer ao sistema o que é aceitável.** E isso muda: no verão, a loja aperta o limite, e uma regra de teste não pode valer para sempre.

Sem regras, a US-06 não tem com o que comparar a leitura, e nenhum alerta existe.

## O que entrega

> Como administrador, quero cadastrar e editar regras com limite, criticidade e período de vigência, para definir quando uma leitura é problema.

- A tela lista as regras: a que se aplica, o limite, a criticidade, a vigência e se ela está vigente **hoje**.
- O formulário cria uma regra e edita uma regra existente.
- O formulário valida antes de enviar, e o back valida de novo.
- Uma regra com o fim da vigência antes do início é rejeitada.

## Onde resolver

**Back (novo):**
- migration `Vn__criar_tabela_regras_alerta.sql`
- `model/domain/RegraAlerta.java`, mais o tipo da criticidade
- `repository/RegraAlertaRepository.java`, `service/RegraAlertaService.java`, `controller/RegraAlertaController.java`
- `model/dto/input/RegraAlertaInputDTO.java`, `model/dto/output/RegraAlertaOutputDTO.java`
- `src/test/.../service/RegraAlertaServiceTest.java`

**Front (novo):**
- `models/regra-alerta.ts`: a criticidade como union, com rótulos
- `services/regra-alerta.service.ts`
- `pages/Admin/RegrasAlerta/index.tsx` e `pages/Admin/RegrasAlerta/hooks/use-regras-alerta-page.ts`
- rota em `routes.tsx` e link na `Sidebar.tsx`

## Como resolver

1. **Responda as decisões 1 e 2 antes de criar a tabela.** Elas definem as colunas.
2. **Back: criar e listar**, no padrão das US anteriores.
3. **Back: buscar por id e editar.** É a primeira edição do projeto. Pense: o `PUT` com um id que não existe responde o quê? Quem impede que a edição mude o id?
4. **Back: validação da vigência.** Comparar duas datas do mesmo objeto não cabe numa anotação de um campo só. Onde fica essa regra: no service ou numa validação customizada? Siga o padrão que o projeto já usa (`validarNome` no `LocalService`).
5. **Front: editar reaproveita o formulário de criar.** Pesquise como o `react-hook-form` preenche o formulário com dados que vieram da API (`reset` ou `defaultValues`). Pense: como a tela sabe se está criando ou editando?
6. **Front: validação.** As regras do formulário espelham as do back. Repare quantas regras agora estão escritas duas vezes (front e back), e se isso incomoda.

## O que você vai aprender

- **Back:** edição (`PUT`), buscar por id, validação que envolve mais de um campo e datas de vigência.
- **Front:** um formulário de verdade (criar e editar no mesmo componente, preencher com dados da API) e validação entre campos.

## Decisões suas

1. **A regra se aplica a quê?** A um sensor específico? A todos os sensores de um local? A um tipo de sensor? Isso muda a tabela e muda a US-06.
2. **O que é "limite"?** Só um máximo? Mínimo e máximo (uma faixa)? Para câmara fria, temperatura baixa demais também é problema?
3. **Quais são as criticidades?** Quantos níveis? Com quais nomes?
4. **Duas regras vigentes para o mesmo sensor ao mesmo tempo** podem existir? Se sim, a US-06 vai ter que decidir o que fazer quando as duas forem violadas.
5. **Vigência sem data de fim** significa "vale para sempre"?
6. **Validação de schema no front** (ex.: uma biblioteca que descreve o formulário num objeto e gera os tipos) ou continuar com as regras do `react-hook-form`? Biblioteca nova é decisão sua.

## Bugs e pendências que aparecem aqui

- [BUG-04](BUG-04-erros-fora-do-contrato-de-resposta.md): com um formulário grande, o erro de validação **precisa** dizer o campo, ou a tela não consegue mostrar a mensagem no lugar certo. Se a US-01 não resolveu, aqui ele dói.
- [BUG-05](BUG-05-tela-cadastro-sem-feedback-e-envio-duplo.md): bloquear o envio duplo também vale para a edição.

## Fora de escopo

- Excluir regra. Pense se uma regra que já gerou alertas pode ser apagada. Talvez "encerrar a vigência" resolva.
- Histórico de alterações da regra (quem mudou o limite e quando).
- Aplicar a regra às leituras (US-06).

## Critério de pronto

- Criar, listar, buscar por id e editar funcionam pelo Swagger e pela tela.
- Vigência inválida responde `400` com mensagem, e a tela mostra a mensagem junto do campo.
- `PUT` num id inexistente responde o status que você decidiu no passo 3, no formato de erro da API.
- A lista mostra se cada regra está vigente hoje.
- `RegraAlertaServiceTest` cobre vigência inválida, id inexistente na edição e criação válida.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
