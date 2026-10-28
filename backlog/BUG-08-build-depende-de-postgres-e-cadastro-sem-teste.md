# BUG-08: Build depende de Postgres local e o cadastro não tem nenhum teste

- **Severidade:** Média
- **Área:** backend e infra
- **Onde:**
  - `src/test/java/com/higor/React_java/ReactJavaApplicationTests.java:6-11`
  - `run.cmd:2`
- **Evidência:** relatório do surefire lido pela revisão de 2026-10-27 (`Connection to localhost:5432 refused`).

## Sintoma

- Numa máquina sem Postgres em `localhost:5432`, ou num CI, `mvnw test` e `mvnw package` falham. Sem `-DskipTests`, nem o jar é gerado.
- Com o Postgres no ar, o Flyway do teste roda contra o banco de desenvolvimento `meubanco`.
- Os erros dos BUG-01, 02 e 04 entraram no código sem nenhum teste acusar.
- Não há instrução nem `docker-compose` para subir o banco. O `run.cmd:2` fixa um `JAVA_HOME` que só existe nesta máquina.

## Como reproduzir

1. Pare o Postgres local.
2. Rode `mvnw test`. O build falha no `contextLoads`.

## Causa-raiz

- O único teste é o `contextLoads` com `@SpringBootTest` (linha 6). Ele sobe o contexto inteiro com o datasource real.
- Não existe nenhum teste de domínio, de service ou de controller.

## Direção

Teste cada regra na camada mais barata que consegue prová-la:

| Camada | O que prova | Precisa de banco? |
|---|---|---|
| `Usuario.criar` | as invariantes do domínio | não |
| `UsuarioService` com mocks (Mockito, já no classpath) | ordem das operações, duplicidade | não |
| Controller (`@WebMvcTest`) | status HTTP e corpo de erro | não |
| Integração | constraint UNIQUE, corrida | sim, isolado do banco de dev |

- Para a integração, pesquise Testcontainers. É uma dependência nova, então a decisão é sua.
- O ambiente precisa ser reproduzível: um jeito documentado de subir o banco (por exemplo, `docker-compose`) e nenhum caminho de máquina fixo em script versionado.
- O front não tem test runner. O padrão do seu contrato é Vitest com React Testing Library.

## Critério de pronto

- `mvnw test` passa numa máquina sem Postgres instalado (com Docker, se você optar por Testcontainers).
- Cada BUG corrigido deste backlog tem um teste que falharia antes da correção.
