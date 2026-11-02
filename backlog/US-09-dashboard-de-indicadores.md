# US-09: Dashboard com indicadores por período e local

- **Fase:** D. Operação
- **Status:** pendente
- **Depende de:** US-08 (os indicadores contam alertas e tratativas)
- **Área:** backend e frontend
- **Rota da tela:** `/` (substitui a `DashboardPage` provisória)
- **Endpoints:** um endpoint de indicadores com filtro de período e local (formato a decidir)

## A dor

A fila resolve o turno de hoje. O gerente da loja tem outra pergunta: **qual câmara dá mais problema? Está melhorando ou piorando? A equipe trata rápido?** Para responder isso hoje, alguém teria que contar alertas na fila, um por um.

Essa é a dor número 4: ninguém enxerga o padrão. Os dados já existem desde a US-03. Falta agrupar.

## O que entrega

> Como gerente, quero ver indicadores por período e por local, para saber onde investir em manutenção e se a operação está respondendo a tempo.

- A tela inicial (`/`) mostra cards com números do período escolhido e pelo menos um gráfico.
- Filtro de período e, opcionalmente, de local.
- Os números são calculados **no back** e chegam prontos. O front não recebe 500 mil linhas para somar.
- A `DashboardPage` provisória (`pages/Dashboard/index.tsx`) deixa de ser só um título.

## Onde resolver

**Back:**
- um controller e um service de indicadores (o nome é seu)
- consultas de agregação no repository de alertas (e talvez no de leituras)
- um DTO de saída com os indicadores

**Front:**
- `pages/Dashboard/index.tsx` (existe, só com um `<h1>`) e `pages/Dashboard/hooks/use-dashboard-page.ts` (novo)
- `services/indicadores.service.ts` e `models/indicadores.ts` (novos)
- um componente de card em `components/`, se ele se repetir
- o gráfico com a mesma biblioteca da US-04

## Como resolver

1. **Escolha os indicadores primeiro** (decisão 1). Para cada um, escreva a pergunta de negócio que ele responde. Indicador sem pergunta é enfeite.
2. **Back: agregação.** Contar e agrupar é trabalho do banco. Pesquise `GROUP BY`, `COUNT` e `AVG` em JPQL, e como o Spring Data devolve um resultado que não é uma entidade (pesquise *projections* ou um `record` no `SELECT new ...`).
3. **Meça.** Com o volume do simulador, cronometre cada consulta de indicador. O dashboard é a tela que todo mundo abre primeiro: se ela demora, todo mundo sofre.
4. **Front: cards e gráfico.** A tela faz uma chamada ou uma por indicador? Pense no que aparece enquanto carrega e se um indicador com erro derruba os outros.
5. **Front: filtro.** Período e local, de novo na URL (padrão das US-04 e US-07).

## O que você vai aprender

- **Back:** agregação no banco, resultado de consulta que não é entidade e o custo de uma consulta que lê muita linha.
- **Front:** compor uma tela com várias peças independentes (cards + gráfico) e reaproveitar a biblioteca de gráfico.

## Decisões suas

1. **Quais indicadores?** Algumas ideias, escolha poucas: total de alertas no período; alertas por criticidade; alertas por local; pendentes × tratados; tempo médio entre o alerta e a tratativa; desconsiderados ÷ total (regra mal calibrada gera muito falso alarme).
2. **Um endpoint com tudo, ou um endpoint por indicador?**
3. **O período padrão** ao abrir: hoje, últimos 7 dias, mês atual?
4. **Se uma consulta ficar lenta**, o que é aceitável: calcular na hora mesmo assim, ou guardar um resultado pronto? Só decida isso **depois** de medir.

## Bugs e pendências que aparecem aqui

- O título da navbar: se a US-01 resolveu com `handle` + `useMatches()`, esta tela já mostra "Dashboard" pela rota.

## Fora de escopo

- Exportar o dashboard.
- Dashboard personalizado por usuário.
- Indicadores em tempo real (atualizando sozinhos).

## Critério de pronto

- A tela inicial mostra os indicadores escolhidos, filtrados por período (e por local, se você decidiu ter esse filtro).
- Cada indicador tem a pergunta de negócio que ele responde, escrita na descrição do PR.
- O tempo de cada consulta com o volume do simulador está anotado.
- Os números batem com uma conferência manual feita no banco para um período pequeno.
- Teste de service para pelo menos um indicador.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
