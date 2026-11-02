# US-02: Cadastro de sensores (cada um num local, com tipo e status)

- **Fase:** A. Cadastros
- **Status:** pendente
- **Depende de:** US-01 (o formulário escolhe o local a partir do `GET /locais`)
- **Área:** backend e frontend
- **Rota da tela:** `/admin/sensores`
- **Endpoints:** `GET /sensores`, `POST /sensores`

## A dor

Um local sozinho não mede nada. O sistema precisa saber **quais sensores existem e onde cada um está**. Sem isso, quando uma leitura chegar na US-03, não há como dizer de qual câmara ela é.

O status também importa: um sensor em manutenção pode mandar valores absurdos, e ninguém quer ser acordado por um alerta de um sensor que está sendo consertado.

## O que entrega

> Como administrador, quero cadastrar sensores em cada local, com tipo e status, para que o sistema saiba de onde vem cada leitura.

- A tela `/admin/sensores` lista os sensores com o nome do local, o tipo e o status escritos por extenso ("Em manutenção", não `MANUTENCAO`).
- O formulário cria um sensor escolhendo o local num `<select>` preenchido pela API.
- Status possíveis: `ATIVO`, `INATIVO` e `MANUTENCAO`.
- Criar um sensor num local que não existe é rejeitado com uma mensagem clara.

## Onde resolver

**Back (novo, no padrão do `Local`):**
- migration `Vn__criar_tabela_sensores.sql`, com chave estrangeira para `locais`
- `model/domain/Sensor.java`, mais o tipo do status
- `repository/SensorRepository.java`, `service/SensorService.java`, `controller/SensorController.java`
- `model/dto/input/SensorInputDTO.java`, `model/dto/output/SensorOutputDTO.java`
- `src/test/.../service/SensorServiceTest.java`

**Front (novo):**
- `models/sensor.ts`: o tipo do sensor, o status como union e os rótulos de cada status
- `services/sensor.service.ts`
- `pages/Admin/Sensores/index.tsx` e `pages/Admin/Sensores/hooks/use-sensores-page.ts`
- rota em `routes.tsx` e link em `components/layout/Sidebar.tsx`

**Reaproveita:** `services/local.service.ts` (US-01), para carregar o `<select>`.

## Como resolver

1. **Migration.** A tabela `sensores` tem uma coluna que aponta para `locais.id`. Pesquise `FOREIGN KEY ... REFERENCES` em T-SQL. Pense: o que o banco deve fazer se alguém tentar apagar um local que tem sensor?
2. **Entidade e relação.** O sensor pertence a um local. Pesquise `@ManyToOne` e `@JoinColumn`. Para o status, pesquise `@Enumerated(EnumType.STRING)` e por que `ORDINAL` é perigoso.
3. **Service.** Ao criar, o service recebe o **id** do local, não o local inteiro. Ele precisa buscar o local e decidir o que fazer se o local não existir.
4. **Saída.** O `SensorOutputDTO` precisa do nome do local para a lista. Atenção: buscar o local de cada sensor um por um, ao montar a lista, gera uma consulta por sensor. Pesquise "problema N+1", a diferença entre `FetchType.EAGER` e `LAZY` (e qual é o padrão do `@ManyToOne`), e veja as consultas no console (o `show-sql` está ligado no dev).
5. **Front: model.** O status vira um union de strings, e os rótulos ficam num objeto que mapeia cada status para o texto exibido. É o primeiro `model` com rótulos.
6. **Front: formulário.** O `<select>` de local é preenchido ao abrir a tela, com o mesmo `useEffect` da US-01. Pense: a tela agora espera duas chamadas (locais e sensores). O que ela mostra enquanto uma terminou e a outra não?

## O que você vai aprender

- **Back:** relação entre tabelas (FK no banco, `@ManyToOne` no Java), enum persistido como texto, validar a existência de uma referência e o problema N+1.
- **Front:** union de strings + rótulos, `<select>` com opções vindas da API e uma tela que depende de duas buscas.

## Decisões suas

1. **Quais tipos de sensor existem?** Só temperatura? Temperatura e umidade? O tipo é um valor fixo no código (enum) ou um cadastro?
2. **Como o sensor é identificado?** Um nome ("Sensor porta 1")? Um código que o equipamento envia? Isso importa na US-03: o simulador precisa dizer de qual sensor é a leitura.
3. **O sensor nasce com qual status?** O status vem no formulário ou é sempre `ATIVO` na criação?
4. **O nome do sensor é único no sistema inteiro ou só dentro do mesmo local?**
5. **Local inexistente responde 400 ou 404?** O recurso que você está criando é o sensor, e quem não existe é outro recurso.

## Bugs e pendências que aparecem aqui

- [BUG-11](BUG-11-open-in-view-segura-conexao.md): o `open-in-view` está ligado (é o padrão, e nada nos `application*.yaml` desliga). Com ele, uma relação `LAZY` carrega mesmo fora do service, por exemplo no `OutputDTO.of(...)` chamado no controller. Se você usar `LAZY` e desligar o `open-in-view`, esse mesmo código passa a lançar `LazyInitializationException`. Vale entender os dois lados antes de decidir.
- Se o BUG-01 e o BUG-04 foram resolvidos na US-01, o sensor já nasce com 409 e com o formato de erro certo. Se não, ele herda os mesmos problemas.

## Fora de escopo

- Editar sensor e mudar o status depois de criado. Pode entrar quando uma US precisar (ex.: colocar em manutenção).
- Receber leituras (US-03).
- Filtrar a lista de sensores por local.

## Critério de pronto

- `POST /sensores` com um local existente responde `201`. Com um local inexistente, responde erro com mensagem clara, no formato de erro da API.
- `GET /sensores` traz o nome do local de cada sensor.
- A tela mostra o status por extenso, e o `<select>` lista os locais reais.
- O console mostra quantas consultas a listagem faz, e você sabe explicar esse número.
- `SensorServiceTest` cobre local inexistente e criação válida.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
