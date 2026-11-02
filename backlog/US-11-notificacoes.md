# US-11: Notificações de alerta

- **Fase:** E. Acesso e aviso
- **Status:** pendente
- **Depende de:** US-06 (o alerta é o que dispara a notificação) e US-10 (para saber **quem** notificar)
- **Área:** backend e frontend
- **Rota da tela:** nenhuma nova obrigatória. Talvez um indicador na navbar.
- **Endpoints:** a decidir, conforme o canal

## A dor

A fila (US-07) só funciona se alguém estiver olhando para ela. Às 2h da manhã, na noite do compressor, não tem ninguém olhando. O alerta seria criado às 2h e visto às 7h, e o estoque estaria perdido do mesmo jeito.

Esta é a última parte da dor número 1: **o sistema precisa ir até a pessoa**, não esperar que a pessoa venha até ele.

## O que entrega

> Como operador de plantão, quero ser avisado quando surgir um alerta grave, para agir sem precisar ficar olhando a fila.

- Quando um alerta é criado com criticidade a partir de um nível (a decidir), o sistema avisa as pessoas certas.
- O aviso diz o essencial: local, valor e criticidade, e leva ao detalhe do alerta (US-08).
- A mesma situação não gera uma avalanche de avisos (volte à pergunta 7 da US-06).

## Onde resolver

Depende muito do canal (decisão 1). O que existe em qualquer caminho:
- **Back:** o ponto onde o alerta é criado (o que você fez na US-06) precisa disparar o aviso. Pense se a criação do alerta deve esperar o aviso terminar.
- **Front:** se o canal for dentro do sistema, a navbar (`components/layout/Navbar.tsx`) provavelmente ganha um indicador.

## Como resolver

1. **Escolha o canal** (decisão 1) e escreva por quê.
2. **Separe "criar o alerta" de "avisar".** Pense: se o envio do aviso falhar (o servidor de e-mail caiu), o alerta deixa de existir? Não deveria. Como garantir isso?
3. **Faça o caminho mais simples funcionar ponta a ponta** com um alerta, antes de pensar em volume.
4. **Rode o simulador** e veja quantos avisos ele gera. Se for uma avalanche, ajuste a regra de quando avisar.

## O que você vai aprender

- Separar o fato principal (o alerta) do efeito colateral (o aviso), e o que acontece quando o efeito colateral falha.
- Como o servidor faz chegar uma informação ao usuário sem que ele peça (se o canal for dentro do sistema).
- Integração com um serviço externo (se o canal for e-mail ou outro).

## Decisões suas

1. **Qual canal?**
   - **Dentro do sistema** (um indicador na navbar). Pesquise como o front fica sabendo de algo novo: perguntar ao back de tempos em tempos (*polling*) ou o back empurrar (*Server-Sent Events*, *WebSocket*). Lembre que o projeto não tem biblioteca de ícones, então um sino seria dependência nova.
   - **Fora do sistema** (e-mail, mensagem). Pesquise o que é preciso para enviar e como testar sem mandar e-mail de verdade.
   - Cada opção traz tecnologia nova, e a escolha é sua.
2. **A partir de qual criticidade avisa?**
3. **Quem recebe?** Todos os operadores? Só os de um local? Só quem está de plantão (e quem define o plantão)?
4. **O aviso tem estado** (lido e não lido)? Se sim, ele é uma tabela nova.

## Bugs e pendências que aparecem aqui

- Nenhum bug do backlog mexe neste código.

## Fora de escopo

- Escala de plantão completa.
- Escalonamento automático ("se ninguém tratar em 15 minutos, avisa o gerente"). Pode virar uma US própria depois.
- Preferências de notificação por usuário.

## Critério de pronto

- Um alerta com a criticidade escolhida gera um aviso para quem você decidiu, pelo canal escolhido.
- Se o envio do aviso falhar, o alerta continua gravado. Provado por teste ou por um roteiro manual escrito no PR.
- Rodando o simulador, a quantidade de avisos é razoável e você sabe explicar o número.
- `npm run build`, `npm run lint` e `./mvnw verify` passam.
