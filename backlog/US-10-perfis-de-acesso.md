# US-10: Perfis de acesso (quem vê, quem trata, quem configura)

- **Fase:** E. Acesso e aviso
- **Status:** pendente
- **Depende de:** US-05 (regras) e US-08 (tratativa), as ações que os perfis vão proteger
- **Área:** backend e frontend
- **Rota da tela:** nenhuma nova. Muda o que cada tela mostra e permite.
- **Endpoints:** muda o login (`POST /auth/login`) e passa a proteger os endpoints existentes

> **Esta US provavelmente é grande demais.** Antes de proteger por perfil, o back precisa saber de verdade quem está chamando, e hoje ele não sabe. Quando chegar a vez dela, avalie quebrá-la em duas: (a) autenticação de verdade e (b) perfis.

## A dor

Hoje qualquer pessoa logada pode fazer tudo: o estagiário do turno da noite pode mudar o limite da regra de congelados, e o alerta some.

Pior, o "logado" nem é de verdade. O back não emite token nem sessão: o `POST /auth/login` só confere a senha e devolve o usuário (`AuthService.java:22-33`). O front guarda esse usuário no `localStorage`, e qualquer um pode editá-lo no DevTools. Qualquer endpoint pode ser chamado direto, sem login nenhum, pelo Swagger ou por um `curl`.

E a US-08 deixou uma pendência: o sistema não consegue registrar com confiança **quem** tratou um alerta.

## O que entrega

> Como responsável pela operação, quero que cada pessoa só possa fazer o que o perfil dela permite, para que a configuração e o histórico de tratativas sejam confiáveis.

Três perfis:

| Perfil | Pode |
|---|---|
| **Leitura** | ver telas e dados |
| **Operador** | o mesmo que Leitura, e tratar alertas |
| **Admin** | o mesmo que Operador, e configurar locais, sensores e regras |

- O back sabe quem está chamando em **toda** requisição, sem depender do que o front diz.
- Um endpoint chamado sem login responde `401`. Um endpoint chamado com login, mas sem o perfil necessário, responde `403`.
- O front esconde ou desabilita o que o perfil não permite. Isso é conforto, não segurança: quem protege é o back.
- A tratativa (US-08) passa a registrar o usuário autenticado.

## Onde resolver

**Back:**
- `pom.xml`: hoje só tem o `spring-security-crypto` (usado no `PasswordEncoderConfig`). Autenticação de verdade provavelmente pede mais dependência (ver "Decisões suas").
- `service/AuthService.java` e `controller/AuthController.java`: o login passa a emitir uma credencial
- uma migration para guardar o perfil do usuário
- `model/domain/Usuario.java`: o perfil
- uma configuração que diz qual perfil acessa qual endpoint

**Front:**
- `services/auth.service.ts`: guardar a credencial recebida no login
- todos os `services/*.service.ts`: enviar a credencial em toda chamada
- `contexts/AuthProvider.tsx`: o que fica guardado no `localStorage` muda
- as telas: esconder ou desabilitar ações por perfil

## Como resolver

1. **Entenda o problema antes da ferramenta.** Explique com as suas palavras: por que o `localStorage` com o usuário não é autenticação? O que o back precisa receber em cada requisição para confiar em quem está chamando?
2. **Escolha o mecanismo** (decisão 1).
3. **Proteja primeiro um endpoint só** (ex.: `POST /regras-alerta` só para Admin) e prove com `401` e `403` no Swagger ou no `curl`. Depois estenda para os outros.
4. **Front: a credencial em toda chamada.** Hoje cada service monta o seu `fetch`. Repetir o mesmo cabeçalho em todos é a terceira vez que um código se repete. Talvez agora valha um lugar só.
5. **Front: o que acontece quando a credencial expira** no meio do uso?
6. **Volte à US-08** e troque o "quem tratou" provisório pelo usuário autenticado.

## O que você vai aprender

- A diferença entre **autenticação** (quem é você) e **autorização** (o que você pode fazer).
- `401` × `403`.
- Por que a checagem no front não protege nada.
- Como uma credencial viaja em cada requisição.

## Decisões suas

1. **Sessão no servidor ou token?** Se for token, qual? Onde o front guarda? Cada opção tem custo e risco diferentes. Pesquise os dois antes de escolher.
2. **Usar o Spring Security completo?** É uma dependência nova e grande, com muita coisa automática. A decisão é sua, mas faça o passo 1 antes.
3. **Um usuário tem um perfil ou vários?**
4. **Quem dá o perfil?** O cadastro público (`/criar`) continua existindo? Com qual perfil o usuário nasce?
5. **Quebrar esta US em duas?** Ver o aviso no topo.

## Bugs e pendências que aparecem aqui

- [BUG-03](BUG-03-senha-em-texto-puro-no-log.md): o login mexe com senha. Garanta que nenhum log nem resposta de erro mostre a senha.
- [BUG-06](BUG-06-email-sem-normalizacao-duplica-conta.md): o cadastro já normaliza o e-mail (`UsuarioService.java:25`), mas o login não normaliza (`AuthService.java:25`). Quem digita o e-mail com maiúscula no login recebe "Credenciais inválidas". O login de usuário também é gravado sem `trim` (`UsuarioService.java:36`).
- [BUG-02](BUG-02-senha-acima-de-72-bytes-gera-500.md): o limite de senha continua em caracteres (`UsuarioInputDTO.java:10`, `UsuarioService.java:68`), e o BCrypt limita em bytes.
- [BUG-01](BUG-01-cadastro-duplicado-nao-retorna-409.md): o cadastro de usuário ainda responde `400` para e-mail duplicado (`UsuarioService.java:27-28`).

## Fora de escopo

- Login com conta externa (Google, Microsoft).
- Recuperação de senha.
- Tela de administração de usuários e perfis, a menos que a decisão 4 exija.

## Critério de pronto

- Sem login, um endpoint protegido responde `401`. Com login sem o perfil, responde `403`. Com o perfil, funciona. Provado para cada um dos três perfis.
- Editar o `localStorage` no DevTools não dá a ninguém um perfil que ele não tem.
- A tratativa registra o usuário autenticado.
- Testes cobrem a regra de perfil de pelo menos um endpoint de cada nível.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
