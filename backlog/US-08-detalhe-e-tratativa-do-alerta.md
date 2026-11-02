# US-08: Detalhe do alerta + tratativa (desconsiderar ou concluir)

- **Fase:** D. Operação
- **Status:** pendente
- **Depende de:** US-07 (a fila leva ao detalhe)
- **Área:** backend e frontend
- **Rota da tela:** `/alertas/:id`
- **Endpoints:** `GET /alertas/{id}` + a ação de tratativa (formato a decidir)

## A dor

O operador vê o alerta na fila. E agora? Ele precisa de **contexto** para decidir: qual regra disparou, qual valor foi lido, o que aconteceu antes. E precisa **registrar o que decidiu**: "chamei a manutenção" ou "era a porta aberta durante a reposição, falso alarme".

Sem isso, voltamos à dor número 3: no dia seguinte ninguém sabe se alguém viu o problema, quem viu e o que foi feito. O alerta também nunca sai da fila, e uma fila que só cresce deixa de ser olhada.

## O que entrega

> Como operador, quero abrir um alerta, ver o contexto e registrar se ele foi concluído ou desconsiderado, para que a fila mostre só o que ainda precisa de atenção.

- A tela de detalhe mostra o alerta: local, sensor, valor lido, regra violada (limite e criticidade) e horário.
- A tela mostra as leituras do sensor em volta do horário do alerta (reaproveitando a US-04).
- O operador **conclui** ou **desconsidera** o alerta, com uma observação.
- Um alerta tratado não pode ser tratado de novo.
- Depois de tratar, a fila não mostra mais o alerta como pendente.

## Onde resolver

**Back:**
- `controller/AlertaController.java`: o `GET` por id e a ação de tratativa
- `service/AlertaService.java`: as regras da tratativa
- uma migration para guardar a tratativa (numa tabela própria ou em colunas do alerta, ver "Decisões suas")
- `model/dto/input/TratativaInputDTO.java` (ou o nome que você escolher)

**Front:**
- `pages/Alertas/Detalhe/index.tsx` e `pages/Alertas/Detalhe/hooks/use-detalhe-alerta-page.ts` (novos)
- `services/alerta.service.ts`: `buscarPorId` e a ação de tratar
- `routes.tsx`: a rota com `:id`
- o gráfico da US-04, se der para reaproveitar como componente

## Como resolver

1. **Back: detalhe.** `GET /alertas/{id}`. Pesquise `@PathVariable`. Um id inexistente responde o quê?
2. **Back: qual é o formato da ação?** "Tratar" não é bem um campo sendo editado, é uma ação. Pesquise as opções: `PATCH /alertas/{id}`, `POST /alertas/{id}/tratativas`, ou `POST /alertas/{id}/concluir` e `/desconsiderar`. O BUG-12 discutiu verbo no path: aqui essa discussão volta, com outra resposta possível.
3. **Back: as transições.** Quais mudanças de estado são permitidas? Só um alerta pendente pode ser tratado. Escreva as transições permitidas antes de codar. Pense: onde fica essa regra?
4. **Back: dois operadores ao mesmo tempo.** Os dois abrem o mesmo alerta. Um conclui e o outro desconsidera, com um segundo de diferença. O que fica gravado? O segundo deveria receber um erro? Pesquise *optimistic locking* e `@Version`.
5. **Front: rota com parâmetro.** Pesquise `useParams` do React Router.
6. **Front: depois de tratar.** A tela volta para a fila? Fica no detalhe mostrando "tratado"? Lembre que a fila guarda a página e os filtros na URL (US-07): voltar não pode perder isso.

## O que você vai aprender

- **Back:** rota com id, ação que muda o estado, transições de estado e concorrência entre dois usuários.
- **Front:** rota com parâmetro, uma ação que altera dado e a navegação de volta mantendo o estado da tela anterior.

## Decisões suas

1. **A tratativa é uma tabela própria ou colunas no alerta?** Um alerta pode ter mais de uma tratativa (ex.: reaberto e tratado de novo)? Se nunca, colunas bastam.
2. **O formato da ação** (passo 2).
3. **A observação é obrigatória?** Sempre, ou só para desconsiderar?
4. **Quem tratou?** Até a US-10, o back não sabe quem está logado de verdade (o front guarda o usuário no `localStorage` e qualquer um pode editar). Por enquanto, o front envia o usuário? Fica vazio? Anote a escolha, porque a US-10 vai mudar isso.
5. **Um alerta desconsiderado pode ser reaberto?**

## Bugs e pendências que aparecem aqui

- [BUG-12](BUG-12-rota-fora-do-padrao-rest-sem-location.md): a discussão sobre verbo no path volta no passo 2.
- [BUG-05](BUG-05-tela-cadastro-sem-feedback-e-envio-duplo.md): o botão de tratar precisa bloquear o duplo clique, ou o segundo clique cai no passo 4.

## Fora de escopo

- Encaminhar o alerta para outra equipe ou abrir uma ordem de manutenção.
- Tratar vários alertas de uma vez.
- Restringir quem pode tratar (US-10).

## Critério de pronto

- O detalhe mostra o alerta, a regra e o gráfico em volta do horário.
- Concluir e desconsiderar funcionam, e o alerta sai da fila padrão.
- Tratar um alerta já tratado responde erro com mensagem clara, no formato de erro da API.
- O cenário dos dois operadores (passo 4) tem a resposta que você decidiu, demonstrada por um teste ou por um roteiro manual escrito no PR. Provar isso com Mockito não basta, porque quem detecta o conflito é o banco.
- `AlertaServiceTest` cobre: tratar um pendente, tratar um já tratado e tratar um id inexistente.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
