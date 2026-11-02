# US-06: Leitura que viola uma regra vigente vira alerta

- **Fase:** C. O núcleo
- **Status:** pendente
- **Depende de:** US-03 (leituras chegando) e US-05 (regras cadastradas)
- **Área:** backend. Sem tela.
- **Rota da tela:** nenhuma (os alertas aparecem na US-07)
- **Endpoints:** nenhum novo. A avaliação acontece quando a leitura chega.

> **Esta US é sua.** É a regra de negócio central do sistema. Ela traz só a dor, as perguntas e a direção. O código e o desenho da solução são seus. O assistente revisa o que você escrever, mas não escreve por você.

## A dor

Até aqui o sistema **guarda** a temperatura subindo, mas não **avisa** ninguém. Na noite do compressor, as leituras estariam todas no banco às 2h, 3h e 4h, e ninguém saberia de nada até abrir o histórico de manhã. Guardar sem avaliar não resolve a dor número 1: ninguém vê a tempo.

Ninguém digita um alerta. Ele é **calculado**: a leitura chega, o sistema compara com as regras vigentes e decide se aquilo é um alerta e com qual criticidade.

## O que entrega

> Como operador, quero que toda leitura que viola uma regra vigente vire um alerta automaticamente, para não depender de alguém olhando o gráfico.

- Uma leitura que viola uma regra vigente gera um alerta, com a criticidade da regra.
- Uma leitura dentro do limite não gera nada.
- Uma regra fora da vigência não gera alerta, mesmo que a leitura viole o limite.
- O alerta guarda o suficiente para explicar por que existe: qual leitura, qual regra e quando.
- O alerta nasce num estado inicial que a US-08 vai mudar (concluído ou desconsiderado).

## Onde resolver

Você decide. Algumas pistas sobre o que provavelmente vai existir:
- uma migration para a tabela de alertas;
- uma entidade, um repository e um tipo para o estado do alerta;
- o lugar onde a avaliação acontece. Responder **onde** é parte do exercício.

## Perguntas que o seu desenho precisa responder

Responda por escrito (na descrição do PR, por exemplo) antes de codar.

**Quando e onde**
1. A avaliação acontece dentro do `POST /leituras` ou depois, separada? O que o sensor espera enquanto isso?
2. Se gravar a leitura deu certo e gravar o alerta falhou, o que fica no banco? Isso é aceitável?

**O que é violar**
3. O valor igual ao limite viola ou não?
4. O que é "vigente": a vigência é comparada com o horário da medição ou com o horário em que a leitura chegou? (Volte à decisão 3 da US-03.)
5. Leitura de sensor em `MANUTENCAO` gera alerta?

**Mais de um**
6. Duas regras vigentes são violadas pela mesma leitura. Um alerta ou dois? Se for um, com qual criticidade?
7. A câmara está quente e o sensor manda uma leitura por minuto. Em uma hora, são 60 alertas para o mesmo problema? Se não, o que define que é "o mesmo problema"? E quando o problema acaba?

**Volume**
8. Com o simulador mandando milhares de leituras, quantas consultas ao banco a avaliação faz **por leitura**? Meça. Esse número multiplicado por 500 mil é aceitável?

## Como resolver

1. Responda as perguntas acima. Se não conseguir responder alguma, leve o seu desenho para o assistente e peça para ele questionar a abordagem antes do código.
2. Escreva os testes de service **antes** do código, a partir das respostas: dentro do limite, fora do limite, fora da vigência, valor igual ao limite.
3. Implemente.
4. Rode o simulador da US-03 e confira se o número de alertas gerados faz sentido para os valores fora da faixa que você injetou.

## O que você vai aprender

- A diferença entre **guardar um dado** e **derivar um fato** dele.
- Transação: o que fica no banco quando metade do trabalho falha.
- Desenhar uma regra de negócio antes de escrever o código, a partir dos casos de borda.
- O custo de uma regra que roda em cada requisição.

## Bugs e pendências que aparecem aqui

- Nenhum bug do backlog mexe neste código.

## Fora de escopo

- Mostrar os alertas numa tela (US-07).
- Tratar um alerta (US-08).
- Avisar alguém (US-11).
- Reavaliar leituras antigas quando uma regra é criada ou editada.

## Critério de pronto

- As 8 perguntas estão respondidas por escrito.
- Existem testes de service para: dentro do limite, fora do limite, fora da vigência, valor igual ao limite e a sua resposta à pergunta 7.
- Depois de rodar o simulador, o número de alertas bate com o que você esperava, e você sabe explicar a diferença, se houver.
- `./mvnw verify` passa.
