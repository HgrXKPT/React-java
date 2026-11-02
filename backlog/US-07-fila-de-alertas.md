# US-07: Fila de alertas com criticidade, filtros e paginação

- **Fase:** D. Operação
- **Status:** pendente
- **Depende de:** US-06 (os alertas precisam existir)
- **Área:** backend e frontend
- **Rota da tela:** `/alertas`
- **Endpoints:** `GET /alertas?...` (paginado e com filtros)

## A dor

Os alertas agora existem, mas estão escondidos no banco. O operador do turno da noite precisa de **um lugar para olhar** que responda: o que está pendente? O que é mais grave? O que é da câmara que eu cuido?

Depois de rodar o simulador, também podem existir milhares de alertas. Uma tela que tenta mostrar todos de uma vez trava o navegador e não ajuda ninguém.

## O que entrega

> Como operador, quero uma fila de alertas filtrável, com o mais grave em destaque, para saber o que tratar primeiro.

- A tela lista os alertas em páginas, com local, sensor, valor lido, criticidade, estado e horário.
- A criticidade tem uma cor, para que o grave salte aos olhos.
- Filtros **opcionais**: estado, criticidade, local e período. Sem filtro nenhum, mostra a fila padrão (ver "Decisões suas").
- Navegação entre páginas, mostrando o total de alertas encontrados.
- Cada linha leva ao detalhe do alerta (a tela vem na US-08).

## Onde resolver

**Back:**
- `controller/AlertaController.java` (novo, ou o que você criou na US-06)
- `service/AlertaService.java`: a consulta com filtros
- `repository/AlertaRepository.java`: a consulta paginada
- `model/dto/output/AlertaOutputDTO.java`

**Front (novo):**
- `models/alerta.ts`: estado e criticidade como union, com rótulos
- `services/alerta.service.ts`
- `pages/Alertas/index.tsx` e `pages/Alertas/hooks/use-alertas-page.ts`
- talvez um componente de paginação em `components/`, se você enxergar que ele vai se repetir
- rota em `routes.tsx` e link na `Sidebar.tsx`

## Como resolver

1. **Back: paginação.** O Spring Data já resolve a paginação. Pesquise `Pageable` e `Page<T>`. Repare no que o `Page` devolve além dos itens (total, número da página) e decida o que o seu DTO de saída expõe.
2. **Back: filtros opcionais.** É a parte difícil: cada filtro pode vir ou não. Escrever um método do repository para cada combinação explode rápido (4 filtros = 16 combinações). Pesquise duas saídas: JPQL com a condição "parâmetro nulo ou campo igual ao parâmetro", e `Specification` do Spring Data. Escolha a mais simples que resolve.
3. **Back: ordem.** Qual alerta aparece primeiro? Ordenar pela criticidade exige que o banco saiba qual nível vem antes de qual (com nomes como "ALTA" e "BAIXA", por exemplo). A ordem alfabética não resolve isso.
4. **Meça com volume.** Com os alertas gerados pelo simulador, cronometre a fila padrão. Se estiver lenta, olhe o plano de execução, como na US-04.
5. **Front: filtros e página na URL.** O mesmo `useSearchParams` da US-04. Assim, F5 e "voltar" do navegador mantêm a página e os filtros.
6. **Front: cor por criticidade.** Os rótulos já mapeiam criticidade → texto. A cor é o mesmo padrão: criticidade → classe. A cor exige CSS. Peça quando chegar aqui.

## O que você vai aprender

- **Back:** paginação, filtros opcionais e ordenação por um valor de negócio.
- **Front:** listagem paginada, vários filtros na URL e mapear um valor para uma aparência.

## Decisões suas

1. **A fila padrão (sem filtro) mostra o quê?** Todos os alertas, ou só os pendentes?
2. **Qual é a ordem padrão?** Primeiro o mais grave, ou o mais recente? E entre dois de mesma criticidade?
3. **Quantos itens por página?** O usuário pode escolher? Qual é o máximo que o back aceita, para ninguém pedir 1 milhão de uma vez?
4. **JPQL com parâmetros opcionais ou `Specification`?** Ver passo 2.
5. **A fila se atualiza sozinha quando chega alerta novo?** Ou o operador recarrega? Atualizar sozinha é bem mais trabalho. Comece pelo simples e anote se faz falta.

## Bugs e pendências que aparecem aqui

- [BUG-11](BUG-11-open-in-view-segura-conexao.md): se o `AlertaOutputDTO` montar o local e o sensor de cada alerta da página no controller, o N+1 da US-02 volta, agora com 20 alertas por página.

## Fora de escopo

- Tratar o alerta (US-08).
- Atualização automática da fila. Se você decidir que faz falta, vira uma US própria ou entra na US-11.
- Exportar a fila.

## Critério de pronto

- `GET /alertas` pagina, filtra por qualquer combinação dos filtros e devolve o total.
- Página ou tamanho de página inválido responde `400`, ou é corrigido para um valor válido, conforme a sua decisão 3.
- A tela pagina, filtra, mostra a cor por criticidade, e F5 mantém a página e os filtros.
- O tempo da fila padrão com o volume do simulador está anotado.
- `AlertaServiceTest` cobre pelo menos a combinação "sem filtro" e uma "com todos os filtros".
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
