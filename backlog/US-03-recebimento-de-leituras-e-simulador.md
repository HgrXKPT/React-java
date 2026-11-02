# US-03: Receber leituras dos sensores + simulador que gera leituras

- **Fase:** B. Dados entrando
- **Status:** pendente
- **Depende de:** US-02 (toda leitura pertence a um sensor)
- **Área:** backend + um programa simulador. Sem tela.
- **Rota da tela:** nenhuma
- **Endpoints:** `POST /leituras`

## A dor

Os cadastros dizem **o que** existe, mas o sistema ainda não sabe **o que está acontecendo**. Lembre a noite do compressor: a temperatura subiu por horas e não havia nenhum registro disso. O sistema precisa receber medições o tempo todo, sem ninguém digitar.

Também não existe sensor de verdade aqui. Sem volume de dados, as US-04 (histórico) e US-09 (dashboard) seriam testadas com 10 linhas, e nada do que é difícil em volume apareceria.

## O que entrega

> Como sensor, quero enviar cada medição para o sistema, para que ela fique registrada e possa ser avaliada.

- `POST /leituras` recebe uma leitura (de qual sensor, o valor e quando foi medida) e grava.
- Leitura de sensor inexistente é rejeitada.
- Um programa **simulador** faz o papel dos sensores: gera leituras realistas para os sensores cadastrados e as envia para o `POST /leituras`.
- O simulador consegue gerar a massa de **~500 mil leituras** e você sabe quanto tempo isso leva.

## Onde resolver

**Back (novo):**
- migration `Vn__criar_tabela_leituras.sql`, com chave estrangeira para `sensores`
- `model/domain/Leitura.java`, `repository/LeituraRepository.java`, `service/LeituraService.java`, `controller/LeituraController.java`
- `model/dto/input/LeituraInputDTO.java`, `model/dto/output/LeituraOutputDTO.java`
- `src/test/.../service/LeituraServiceTest.java`

**Simulador (novo):** fora da aplicação Spring. Onde ele mora e em que linguagem é decisão sua (ver abaixo).

## Como resolver

1. **Migration e entidade.** A leitura guarda o sensor, o valor e o instante da medição. Para o instante, olhe como a `V1` e a `V2` usam `DATETIMEOFFSET`. Pesquise a diferença entre `Instant`, `LocalDateTime` e `OffsetDateTime` no Java.
2. **Service.** Valida o sensor e grava. Esta é a primeira escrita que vai acontecer **milhares de vezes**, então tudo o que roda por leitura custa caro. Conte quantas consultas o banco recebe por `POST` (o console mostra).
3. **Teste no Swagger** com uma leitura manual, antes de escrever o simulador.
4. **Simulador, versão pequena.** Primeiro, um programa que busca os sensores (`GET /sensores`) e manda 100 leituras. Os valores devem parecer reais: uma câmara de congelados fica em torno de -18 °C, com pequena variação. Inclua de propósito alguns valores fora da faixa, porque a US-06 vai precisar deles.
5. **Simulador, volume.** Gere ~500 mil leituras e **meça**: quanto tempo levou? Quantas leituras por segundo? Anote o número. Ele é a linha de base para qualquer melhoria.
6. **Só depois de medir**, pense se precisa melhorar e como (ver "Decisões suas").

## O que você vai aprender

- **Back:** endpoint de escrita, datas e fuso horário, custo por requisição e o que acontece com o banco sob volume.
- **Geral:** gerar massa de dados realista, medir antes de otimizar e escrever um cliente HTTP fora do navegador.

## Decisões suas

1. **O simulador é escrito em quê, e mora onde?** Um programa Java separado? Um script Node? Uma ferramenta de carga? Cada opção é uma dependência ou ferramenta nova, e a escolha é sua. Pergunte-se qual delas você já sabe rodar e versionar.
2. **Uma leitura por requisição, ou um lote por requisição?** Com uma por vez, 500 mil leituras são 500 mil chamadas HTTP. Meça primeiro, e decida depois.
3. **Quem define o instante da leitura?** O sensor (o horário da medição) ou o servidor (o horário em que recebeu)? Se a rede atrasar 10 minutos, qual dos dois está certo? Vale guardar os dois?
4. **Leitura de sensor `INATIVO` ou em `MANUTENCAO`:** aceita, grava e ignora depois, ou rejeita?
5. **O `POST` responde com o quê?** O sensor precisa de corpo na resposta, ou basta o status?

## Bugs e pendências que aparecem aqui

- [BUG-11](BUG-11-open-in-view-segura-conexao.md): com o simulador mandando muitas requisições ao mesmo tempo, cada uma segura uma conexão do pool durante a requisição inteira. Se o tempo por leitura piorar sob carga, este é um suspeito. Meça antes de mexer.
- [BUG-08](BUG-08-build-depende-de-postgres-e-cadastro-sem-teste.md): o `contextLoads` ainda precisa do banco no ar. Os testes de service (com Mockito) não precisam.

## Fora de escopo

- Avaliar a leitura contra uma regra e gerar alerta (US-06). Aqui a leitura só é gravada.
- Consultar as leituras (US-04).
- Índice na tabela de leituras. Ele entra na US-04, **depois** de medir a consulta lenta.
- Fila de mensagens entre o sensor e a API. Aqui o `POST` faz esse papel.

## Critério de pronto

- `POST /leituras` grava uma leitura de um sensor existente e rejeita a de um sensor inexistente, no formato de erro da API.
- O simulador gera ~500 mil leituras para os sensores cadastrados, com alguns valores fora da faixa.
- O tempo total e as leituras por segundo estão anotados (em `docs/` ou na descrição do PR).
- `LeituraServiceTest` cobre sensor inexistente e gravação válida.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
