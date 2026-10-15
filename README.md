# impasse

Impasse do zero em Java 21: **ordenar os recursos não previne impasse nenhum quando quem
ordena é o sistema, e não o programa.**

Seis políticas de alocação, dois detectores e um juiz que não é algoritmo: a varredura de
**todos os estados alcançáveis**.

## O juiz é o espaço de estados inteiro

Impasse aqui não é "ciclo no grafo de espera" nem "estado inseguro". É o que acontece: não
sobrou transição nenhuma e ainda há processo por terminar. Ninguém consegue nada do que falta,
ninguém pode acabar, e esperar mais não resolve porque nada vai mudar sozinho.

O juiz parte do estado em que ninguém pegou nada, aplica **toda** transição que a política
deixa, e guarda tudo o que aparece. Se existe uma sequência de pedidos e términos que leva ao
impasse, ela está na conta: não há amostragem, não há sorteio, não há caso interessante
escolhido por mim.

É por isso que os sistemas medidos são pequenos. Três processos e dois tipos de recurso já dão
umas cinquenta transições por estado, e o que se ganha em tamanho se perde em certeza.

```
$ mvn exec:java -Dexec.args="correcao"

  politica             sistemas com impasse travados pela politica        estados
  sem controle                          150                      0          20689
  banqueiro                               0                      0          19026
  ordem total                           150                    273          20689
  ordem no programa                      54                     54          14423
  tudo de uma vez                        99                      9          19686
  um por vez                              0                      0          13792
```

Quatrocentos sistemas sorteados. A coluna do meio conta outra coisa: estados em que o sistema
aceitaria um pedido e a **política** recusou todos. Não é impasse do sistema; é a política
travando o que queria proteger.

## Ordenar recursos: a regra está no lugar errado

A ordem total é a prevenção mais usada de verdade, e a prova dela cabe numa linha: num ciclo de
espera, alguém teria de estar esperando por um número menor do que o que já tem.

Só que isso supõe que o processo **pede** em ordem crescente. Se o sistema apenas recusa o
pedido fora de ordem, o recurso que o processo já pegou continua na mão dele: quem agarrou o
tipo 1 primeiro fica esperando pelo tipo 0 para sempre.

```
$ mvn exec:java -Dexec.args="correcao"

  sistema                  sem controle          banqueiro        ordem total  ordem no programa    tudo de uma vez         um por vez
  dois garfos                     2 / 0              0 / 0              2 / 2              0 / 0              0 / 0              0 / 0
  garfos em dobro                 0 / 0              0 / 0              0 / 3              0 / 0              0 / 0              0 / 0
  roda de tres                    2 / 0              0 / 0              2 / 8              0 / 0              0 / 0              0 / 0
  banco                           1 / 0              0 / 0              1 / 0              1 / 0              1 / 0              0 / 0
  duas de cada                   10 / 0              0 / 0            10 / 14              0 / 0              8 / 0              0 / 0
  um guloso                       0 / 0              0 / 0              0 / 4              0 / 0              0 / 0              0 / 0
```

Como filtro do sistema, a ordem total trava **exatamente os mesmos 150 sistemas** que a
política que não faz nada, e ainda trava 273 outros por conta própria. Como regra de escrita do
programa, ela cai para 54.

## Com um tipo só de recurso, não há o que ordenar

```
$ mvn exec:java -Dexec.args="condicoes"

  politica                 sistemas com impasse
  sem controle                       222 de 400
  banqueiro                            0 de 400
  ordem total                        222 de 400
  ordem no programa                  222 de 400
  tudo de uma vez                    134 de 400
  um por vez                           0 de 400
```

Quatrocentos sistemas com **um tipo de recurso** e várias unidades. A ordem total previne a
espera circular entre tipos; quando há um tipo só, o ciclo acontece entre processos disputando
unidades do mesmo tipo, e nenhuma numeração desfaz isso.

## O detector de ciclo contra a definição

```
$ mvn exec:java -Dexec.args="deteccao"

  detector           familia               estados     impasses     acusou     alarme falso    deixou passar
  ciclo no grafo     uma unidade                94            4          4                0                0
  ciclo no grafo     varias unidades           312           11         15                4                0
  estado inseguro    uma unidade                94            4          4                0                0
  estado inseguro    varias unidades           312           11         22               11                0
```

Com **uma unidade de cada tipo**, o ciclo no grafo de espera é condição necessária e suficiente:
zero erro dos dois lados, em todos os 94 estados. É o teorema do livro, medido em vez de citado.

Com várias unidades ele continua necessário e deixa de ser suficiente: acusa 15 onde há 11,
porque o processo pode estar esperando por um recurso que tanto faz de quem venha, e basta um
dos donos soltar.

## Seguro não é o mesmo que sem impasse

```
$ mvn exec:java -Dexec.args="seguranca"

  estados alcancaveis                             20689
  estados de impasse                                814
  estados inseguros                                1663

  inseguros que terminam bem                        849
  seguros que dao impasse                             0

  51.1% dos estados inseguros nunca travam.
```

Estado inseguro quer dizer "existe um futuro ruim **se** cada um pedir o máximo que declarou".
Ninguém é obrigado a pedir o máximo, então mais da metade dos estados inseguros termina bem.

A garantia vale num sentido só, e ela se cumpre: nenhum dos 20.689 estados seguros dá impasse. O
banqueiro recusa os 849 inofensivos do mesmo jeito, porque não tem como distinguir.

## O preço de cada prevenção

```
$ mvn exec:java -Dexec.args="custo"

  politica                 previne?      pedidos atendidos estados alcancaveis
  sem controle                  NAO                 100.0%              20689
  banqueiro                     sim                  89.5%              19026
  ordem total                   NAO                  84.8%              20689
  ordem no programa             NAO                  75.1%              14423
  tudo de uma vez               NAO                  81.4%              19686
  um por vez                    sim                  58.4%              13792
```

Duas políticas previnem os 400 sistemas. O banqueiro recusa 10,5% dos pedidos; o um por vez, que
serializa o sistema inteiro, recusa 41,6% e corta um terço dos estados alcançáveis.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `deteccao`, `custo`, `seguranca` e `condicoes`.

## O que as medidas me corrigiram

**Eu ia escrever que ordenar recursos previne impasse. Previne, quando o programa obedece.**
Aplicada como filtro do sistema, a ordem total deixa passar os mesmos 150 impasses em 400
sistemas e cria 273 travamentos novos. A diferença entre "o sistema recusa o pedido fora de
ordem" e "o processo não faz o pedido fora de ordem" parece não existir, e é a diferença entre
uma política inútil e uma política que funciona na maioria dos casos.

**E mesmo como regra do programa ela não resolve tudo.** Ficam 54 sistemas de 400, e com um tipo
só de recurso ela volta a prevenir exatamente nada: os mesmos 222 de 400 que travam sem controle
nenhum. Numerar os recursos quebra o ciclo entre tipos, e o ciclo também acontece entre unidades
do mesmo tipo.

**Pedir tudo de uma vez não basta se o pedido não for atômico.** A política parecia certa: só
começa a pegar quem consegue pegar tudo. Entre a primeira unidade e a última, outro processo
olha o que sobrou, acha que também dá, e começa também. Noventa e nove sistemas travam. Tapar o
buraco custa serializar o sistema inteiro, e aí o preço é recusar 41,6% dos pedidos.

**Inseguro não é travado, e eu usei um pelo outro.** Mais da metade dos estados inseguros,
849 de 1.663, termina bem. Usar o critério do banqueiro como detector de impasse acusa 22
estados onde há 11.

**O detector de ciclo não erra do lado que eu temia.** Ele nunca deixou um impasse passar, em
nenhum dos 406 estados. O erro dele é todo para o outro lado, e só aparece com várias unidades
do mesmo tipo.

## O que não tem aqui

**Preempção.** Nenhuma política daqui tira recurso de quem já tem. Tirar é a quarta condição de
Coffman e é o que um banco de dados faz ao abortar uma transação, com o custo de desfazer
trabalho.

**Recuperação.** Os detectores acusam e param por aí. Escolher a vítima, desfazer o que ela fez
e evitar que a mesma seja escolhida para sempre é outro assunto, e é onde mora a inanição.

**Pedidos de várias unidades de uma vez.** Aqui cada pedido é de uma unidade. Pedir duas de uma
vez não muda a natureza do impasse e multiplica o espaço de estados, que precisa caber inteiro
na varredura.

**Impasse de comunicação.** Dois processos esperando mensagem um do outro travam igual, sem
recurso nenhum envolvido. O modelo daqui é todo de recursos reutilizáveis.

**Espera com prazo.** Desistir depois de um tempo quebra o impasse na prática e troca o problema
por outro: o processo que desistiu tenta de novo, e todos podem ficar tentando para sempre. É um
assunto de [`retentativa`](https://github.com/condeDeveloper/retentativa).

## Licença

MIT.
