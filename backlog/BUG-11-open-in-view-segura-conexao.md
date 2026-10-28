# BUG-11: `open-in-view` ligado segura a conexão do banco durante o BCrypt

- **Severidade:** Baixa
- **Área:** backend
- **Onde:** `src/main/resources/application.properties` (a propriedade `spring.jpa.open-in-view` não está definida)
- **Evidência:** inferido, não medido. O WARN de startup é o comportamento padrão do Spring Boot quando a propriedade não está definida.

## Sintoma

- No startup aparece o WARN `spring.jpa.open-in-view is enabled by default...`.
- Cada cadastro segura uma conexão do pool (Hikari, 10 conexões por padrão) desde o `existsByEmail` até o fim da resposta, inclusive durante o BCrypt. A revisão mediu cerca de 44 ms de BCrypt nesta máquina.
- Com mais cadastros simultâneos do que conexões no pool, as outras requisições ao banco esperam até 30 s e falham.

## Como reproduzir

1. Suba a aplicação e procure o WARN de `open-in-view` no log.
2. Para ver o esgotamento do pool, dispare mais de 10 cadastros simultâneos (com k6, JMeter ou um script) e acompanhe os tempos de resposta.

## Causa-raiz

- *Open Session in View* (OSIV) vem ligado por padrão: o Spring mantém a sessão do JPA, e com ela a conexão, aberta durante toda a requisição web.

## Direção

- Pesquise o que é OSIV e por que ele existe (lazy loading na camada de view). Em API REST, o padrão de mercado é `spring.jpa.open-in-view=false`.
- Meça antes e depois da mudança: um teste de carga simples mostra se o pool de fato esgota.

## Critério de pronto

- O WARN de `open-in-view` não aparece no startup.
- O cadastro continua funcionando, com os testes do BUG-08 passando.
