# US-04: Histórico de leituras por sensor e período, com gráfico

- **Fase:** B. Dados entrando
- **Status:** pendente
- **Depende de:** US-03 (precisa da massa de ~500 mil leituras)
- **Área:** backend e frontend
- **Rota da tela:** `/historico-leituras`
- **Endpoints:** `GET /leituras?sensorId=...&inicio=...&fim=...`

## A dor

Na manhã seguinte à pane, a primeira pergunta é: **quando a temperatura começou a subir?** Com a planilha não dá para responder. Agora as leituras estão no banco, mas não há como vê-las. Um número numa tabela também não ajuda: ninguém enxerga uma subida lenta lendo 2 mil linhas. Precisa ser um gráfico.

## O que entrega

> Como operador, quero ver as leituras de um sensor num período, num gráfico, para entender quando e como um problema começou.

- A tela tem um filtro: sensor (`<select>`) e período (início e fim).
- Ao aplicar o filtro, a tela mostra um gráfico de valor × tempo.
- Período sem leitura mostra uma mensagem, não um gráfico vazio sem explicação.
- A consulta responde num tempo aceitável **com as ~500 mil leituras no banco**, e você mediu esse tempo.

## Onde resolver

**Back:**
- `controller/LeituraController.java`: o `GET` com parâmetros de filtro
- `service/LeituraService.java`: valida o filtro e consulta
- `repository/LeituraRepository.java`: a consulta por sensor e período
- uma migration nova **se** a medição mostrar que precisa de índice

**Front (novo):**
- `models/leitura.ts`, `services/leitura.service.ts`
- `pages/Historico/index.tsx` e `pages/Historico/hooks/use-historico-page.ts`
- rota em `routes.tsx` e link na `Sidebar.tsx`

**Reaproveita:** `services/sensor.service.ts` (US-02), para o `<select>`.

## Como resolver

1. **Back: parâmetros.** O filtro vem na query string. Pesquise `@RequestParam` e como o Spring converte texto em data. Pense: o que acontece se `fim` for antes de `inicio`? E se o período for de 5 anos?
2. **Back: consulta.** O Spring Data monta a consulta pelo nome do método. Pesquise *derived queries* com `Between` e `OrderBy`.
3. **Meça antes de otimizar.** Com as ~500 mil leituras, cronometre a consulta de um sensor num dia. Anote o número. Depois pesquise o plano de execução no SQL Server e veja se ele lê a tabela inteira.
4. **Só se a medição justificar:** uma migration com índice. Qual coluna vem primeiro no índice, o sensor ou a data? Por quê? Meça de novo e compare com o número de antes.
5. **Front: o filtro na URL.** Se o filtro ficar na URL, o operador pode mandar o link para alguém. Pesquise `useSearchParams` do React Router.
6. **Front: o gráfico.** Precisa de uma biblioteca de gráfico, que é uma dependência nova (ver "Decisões suas"). Comece pelo exemplo mais simples da documentação dela e encaixe os seus dados.
7. **Front: datas.** O `<input type="datetime-local">` não tem fuso horário. A API usa `DATETIMEOFFSET`. Pense no caminho da data, do input até o banco, e onde o fuso pode se perder.

## O que você vai aprender

- **Back:** filtro por query string, consulta por período, medir uma consulta em volume, plano de execução e índice.
- **Front:** estado do filtro na URL, a primeira biblioteca de terceiros com componente visual e datas/fuso horário entre front e back.

## Decisões suas

1. **Qual biblioteca de gráfico?** Compare o tamanho, se tem tipos TypeScript, se a documentação é boa e se ela é mantida. A decisão e o motivo são seus.
2. **Um sensor com leitura a cada minuto gera 43 mil pontos num mês. O gráfico mostra todos?** Limitar o tamanho do período? Agregar (média por hora) no back? Deixar a biblioteca lidar?
3. **O filtro é obrigatório?** Abrir a tela sem filtro mostra o quê?
4. **O período tem limite máximo?** Quem garante esse limite, o front, o back ou os dois?

## Bugs e pendências que aparecem aqui

- Nenhum bug aberto mexe no código desta US. Se a US-01 não resolveu o [BUG-04](BUG-04-erros-fora-do-contrato-de-resposta.md), um parâmetro de data inválido responde fora do formato de erro da API.

## Fora de escopo

- Vários sensores no mesmo gráfico.
- Exportar o histórico (CSV, PDF).
- Marcar no gráfico onde houve alerta (os alertas só existem a partir da US-06).

## Critério de pronto

- `GET /leituras` filtra por sensor e período e devolve as leituras em ordem de tempo.
- Período inválido (`fim` antes de `inicio`) responde `400` com mensagem.
- O tempo da consulta com ~500 mil leituras está anotado, antes e depois do índice (se houve índice).
- A tela mostra o gráfico, e o filtro fica na URL (F5 mantém o filtro).
- `LeituraServiceTest` cobre a validação do período.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
