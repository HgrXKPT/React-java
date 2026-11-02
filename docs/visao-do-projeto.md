# Visão do projeto

Leia este arquivo quando se perder. Ele explica que sistema é este, qual dor ele resolve e onde cada parte da solução vai morar. As tarefas estão em [`backlog/`](../backlog/), uma US por arquivo.

---

## 1. Em uma frase

Um sistema de monitoramento em que **uma leitura de sensor que viola uma regra vira um alerta, e alguém trata esse alerta**.

---

## 2. A dor

> Cenário fictício, criado para dar motivo a cada parte do sistema.

Um mercado tem câmaras frias para carnes, laticínios e congelados, e cada câmara precisa ficar numa faixa de temperatura. Hoje um funcionário confere o termômetro algumas vezes por dia e anota numa planilha.

Uma noite, o compressor da câmara de carnes para às 2h. A temperatura sobe a madrugada inteira e ninguém vê. Às 7h o estoque está perdido, e ninguém sabe dizer quando o problema começou.

Essa noite mostra quatro dores diferentes, e cada uma vira uma parte do sistema:

| # | Dor | O que resolve | US |
|---|---|---|---|
| 1 | **Ninguém vê a tempo.** O problema só aparece na próxima conferência manual. | Regras de alerta: a leitura que fura o limite vira alerta na hora e entra numa fila. | [US-05](../backlog/US-05-regras-de-alerta.md), [US-06](../backlog/US-06-leitura-que-viola-regra-vira-alerta.md), [US-07](../backlog/US-07-fila-de-alertas.md), [US-11](../backlog/US-11-notificacoes.md) |
| 2 | **Não há histórico confiável.** A planilha tem 3 pontos por dia e ninguém sabe quando a temperatura começou a subir. | Os sensores mandam leituras sozinhos o tempo todo, e o histórico pode ser consultado num gráfico. | [US-03](../backlog/US-03-recebimento-de-leituras-e-simulador.md), [US-04](../backlog/US-04-historico-de-leituras-com-grafico.md) |
| 3 | **Ninguém sabe quem fez o quê.** Alguém viu o problema? Chamou a manutenção? Descartou o estoque? | Tratativa: cada alerta é concluído ou desconsiderado, com registro. | [US-08](../backlog/US-08-detalhe-e-tratativa-do-alerta.md) |
| 4 | **O gerente não enxerga o padrão.** Qual câmara dá mais problema? Está piorando? | Dashboard com indicadores por período e por local. | [US-09](../backlog/US-09-dashboard-de-indicadores.md) |

Antes de tudo isso, o sistema precisa saber **o que** está monitorando: quais locais existem e quais sensores ficam em cada um ([US-01](../backlog/US-01-cadastro-de-locais.md), [US-02](../backlog/US-02-cadastro-de-sensores.md)). E, no fim, precisa saber **quem** pode fazer o quê ([US-10](../backlog/US-10-perfis-de-acesso.md)).

### E a dor de quem constrói

O sistema é um pretexto. O objetivo do repositório é **aprender React e Java/Spring construindo algo real**, no mesmo padrão de camadas usado no trabalho. Um produto realista dá motivo para cada peça técnica: a tela de histórico pede filtro de período, a fila pede paginação, a tratativa pede uma rota com `:id`. Você aprende cada coisa quando o produto precisa dela, não antes.

---

## 3. Como o sistema resolve

```
  CADASTROS (alguém configura)                  OPERAÇÃO (o sistema trabalha sozinho)

  Local  ◄──  Sensor  ◄──  Leitura ──► viola uma Regra vigente? ──sim──► Alerta ──► Tratativa
  US-01       US-02        US-03                US-05 / US-06                US-07      US-08
                             │                                                 │
                             ▼                                                 ▼
                         Histórico                                         Dashboard
                         US-04                                             US-09
```

1. **Local**: onde algo é monitorado ("Câmara fria de carnes").
2. **Sensor**: o equipamento que mede, instalado num local, com tipo e status.
3. **Leitura**: um número que o sensor mandou num instante. Sozinha, ela ainda não é problema.
4. **Regra**: o limite aceitável, com criticidade e período de vigência, cadastrado por uma pessoa.
5. **Alerta**: o que o sistema cria quando uma leitura viola uma regra vigente. Ninguém digita um alerta: ele é **calculado**.
6. **Tratativa**: o que uma pessoa decidiu sobre o alerta (concluir ou desconsiderar).
7. **Histórico e Dashboard**: as leituras e os alertas vistos no tempo e agrupados.

---

## 4. Onde cada parte mora

### Back (Spring Boot, `src/main/java/com/higor/React_java/`)

Cada camada só conhece a de baixo:

```
controller/   recebe a requisição HTTP e devolve a resposta. Sem regra de negócio.
    ▼
service/      a regra de negócio. Valida, decide, lança RegraDeNegocioException.
    ▼
repository/   fala com o banco (JpaRepository). Você só declara os métodos.
```

Ao lado das camadas:
- `model/domain/`: as `@Entity`, que espelham as tabelas;
- `model/dto/input|output/`: o que entra e o que sai da API (`OutputDTO.of(...)`);
- `exception/`: as exceções de negócio e o `GlobalExceptionHandler`;
- `config/`: configuração (`CorsConfig`, `PasswordEncoderConfig`);
- `src/main/resources/db/migration/`: as migrations Flyway em T-SQL (SQL Server).

### Front (React, `meu-projeto/src/`)

```
pages/<Area>/<Tela>/index.tsx       desenha a tela. Só consome o hook.
pages/<Area>/<Tela>/hooks/use-*.ts  o cérebro da tela: estado, chamadas, ações.
    ▼
components/                         peças reutilizáveis (layout, tabela, card).
    ▼
services/*.service.ts               a única camada que fala HTTP. Traduz DTO → model.
    ▼
models/                             os tipos que o front usa.
```

Transversais: `contexts/` (usuário logado), `hooks/` (`use-auth`), `config/env.ts` (URL da API), `routes.tsx` (rotas).

### O banco (planejado)

```
locais 1 ─── N sensores 1 ─── N leituras
                                  │ é avaliada contra
                            regras_alerta
                                  │ quando viola
                                  ▼
                               alertas ─── tratativa
```

Os nomes, as colunas e a ligação exata da regra (com sensor, local ou tipo) são decididos em cada US. A tabela `usuarios` já existe e será usada na US-10.

### As telas e as rotas (planejadas)

| Tela | Rota no front | Endpoints | US |
|---|---|---|---|
| Cadastro de locais | `/admin/locais` | `GET` e `POST /locais` | US-01 |
| Cadastro de sensores | `/admin/sensores` | `GET` e `POST /sensores` | US-02 |
| (sem tela) | — | `POST /leituras` | US-03 |
| Histórico de leituras | `/historico-leituras` | `GET /leituras?...` | US-04 |
| Regras de alerta | `/admin/regras-alerta` | `GET`, `POST` e `PUT /regras-alerta` | US-05 |
| (sem tela) | — | (dentro do `POST /leituras`, a decidir) | US-06 |
| Fila de alertas | `/alertas` | `GET /alertas?...` paginado | US-07 |
| Detalhe do alerta | `/alertas/:id` | `GET /alertas/{id}` + ação de tratativa | US-08 |
| Dashboard | `/` | endpoint de indicadores | US-09 |

---

## 5. Decisões de caminho (e por quê)

1. **A leitura chega por `POST /leituras`, enviada por um simulador.** Ninguém digita leitura. Na vida real, quem chama esse endpoint é o sensor ou um gateway. Aqui, um programa simulador faz esse papel e gera volume (meta: ~500 mil leituras). O motivo principal é a US-06: o sistema precisa reagir **no momento em que a leitura chega**, e quando ela chega por uma chamada à sua API, esse momento é uma chamada ao seu código.
   - *Alternativa considerada:* ler as leituras de um banco que outro sistema alimenta, só para leitura. É um padrão real, mas exige dois bancos e descobrir "o que chegou de novo". Pode virar uma US própria no futuro.
2. **Um banco só** (SQL Server + Flyway). Tudo o que o sistema cria mora nele.
3. **Os cadastros pertencem ao sistema.** Locais, sensores e regras são telas de administração deste sistema, mesmo que as leituras venham de fora.
4. **A ordem é a do produto, não a da lição.** Cada US depende da anterior, porque o dado de uma é o insumo da próxima. O dashboard vem por último porque só agrega o que as outras criaram.
5. **Ferramenta nova só quando uma US pedir, e a decisão é sua.** Pontos já conhecidos: biblioteca de gráfico (US-04), validação de schema no front (US-05), segurança/token (US-10), entrega de notificação (US-11).
6. **A regra central é sua.** A US-06 (leitura que viola regra vira alerta) traz só perguntas e direção. O código é escrito por você.

---

## 6. O roteiro

| Fase | US | O que você constrói | O que aprende de novo |
|---|---|---|---|
| **A. Cadastros** | [US-01 Locais](../backlog/US-01-cadastro-de-locais.md) | listar e criar locais | primeiro recurso completo no back; primeira tela que busca dado da API |
| | [US-02 Sensores](../backlog/US-02-cadastro-de-sensores.md) | sensores ligados a um local | relação entre tabelas; status como union + rótulos; `<select>` carregado da API |
| **B. Dados entrando** | [US-03 Leituras + simulador](../backlog/US-03-recebimento-de-leituras-e-simulador.md) | o sistema recebe leituras em volume | endpoint de escrita; massa de dados |
| | [US-04 Histórico](../backlog/US-04-historico-de-leituras-com-grafico.md) | leituras por sensor e período num gráfico | filtro por query string; datas; biblioteca de gráfico; consulta em volume |
| **C. O núcleo** | [US-05 Regras](../backlog/US-05-regras-de-alerta.md) | cadastrar e editar regras | formulário de verdade com validação; edição; vigência com datas |
| | [US-06 Leitura vira alerta](../backlog/US-06-leitura-que-viola-regra-vira-alerta.md) | a regra de negócio central | escrita por você |
| **D. Operação** | [US-07 Fila](../backlog/US-07-fila-de-alertas.md) | a fila de alertas | paginação; filtros opcionais; cor por criticidade |
| | [US-08 Detalhe + tratativa](../backlog/US-08-detalhe-e-tratativa-do-alerta.md) | abrir e tratar um alerta | rota com `:id`; ação que muda o estado e atualiza a fila |
| | [US-09 Dashboard](../backlog/US-09-dashboard-de-indicadores.md) | indicadores por período e local | agregação no back; cards e gráfico |
| **E. Acesso e aviso** | [US-10 Perfis](../backlog/US-10-perfis-de-acesso.md) | quem vê, quem trata, quem configura | autenticação de verdade; roles |
| | [US-11 Notificações](../backlog/US-11-notificacoes.md) | avisar sem precisar olhar a fila | entrega de evento para o usuário |

O status de cada US fica no cabeçalho dela.

Os bugs (`backlog/BUG-*`) não são etapas do roteiro. Cada um é resolvido quando a US mexe no código perto dele, e a seção "Bugs e pendências" de cada US diz quais aparecem ali.

---

## 7. Como uma US vira PR

1. Leia a US inteira, principalmente "Decisões suas". Responda as decisões **antes** de escrever código.
2. Crie a branch a partir da `develop`, com o mesmo prefixo das branches que você já usa: `feat/us-02-cadastro-de-sensores`.
3. Back primeiro (migration → entidade → repository → service → controller), testando no Swagger. Depois o front (model → service → hook → tela → rota → link na sidebar).
4. Critério de pronto comum a todas as US, além do específico de cada uma:
   - `npm run build` e `npm run lint` em `meu-projeto/` passam;
   - `./mvnw verify` passa (precisa do SQL Server no ar);
   - a regra nova do service tem teste (padrão de `src/test/.../service/UsuarioServiceTest.java`: JUnit 5 + Mockito + AssertJ);
   - o roteiro manual da US funciona no navegador.
5. Commits no padrão `tipo(escopo): descrição`. Uma mudança lógica por commit.
