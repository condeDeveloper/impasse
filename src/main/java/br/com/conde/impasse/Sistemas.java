package br.com.conde.impasse;

import java.util.ArrayList;
import java.util.List;

/**
 * OS SISTEMAS: os escritos a mao e os sorteados.
 *
 * <p>Os escritos a mao estao aqui porque cada um separa duas coisas. Os sorteados estao aqui
 * porque os escritos a mao foram escolhidos por mim, e contar impasses entre eles mediria a
 * minha escolha.
 */
public final class Sistemas {

    private Sistemas() {}

    /** O classico: duas pessoas, dois garfos, um garfo para cada mao. */
    public static Sistema doisGarfos() {
        return new Sistema("dois garfos", new int[] {1, 1},
                new int[][] {{1, 1}, {1, 1}});
    }

    /** O mesmo com uma unidade a mais de cada tipo: o ciclo aparece e nao trava. */
    public static Sistema doisGarfosEmDobro() {
        return new Sistema("garfos em dobro", new int[] {2, 2},
                new int[][] {{1, 1}, {1, 1}});
    }

    /** Tres processos em roda: cada um quer o que o seguinte tem. */
    public static Sistema rodaDeTres() {
        return new Sistema("roda de tres", new int[] {1, 1, 1},
                new int[][] {{1, 1, 0}, {0, 1, 1}, {1, 0, 1}});
    }

    /** O exemplo do banqueiro: um tipo so, com varias unidades. */
    public static Sistema banco() {
        return new Sistema("banco", new int[] {4},
                new int[][] {{2}, {2}, {3}});
    }

    /** Dois tipos com duas unidades cada, que e onde o ciclo deixa de provar. */
    public static Sistema duasDeCada() {
        return new Sistema("duas de cada", new int[] {2, 2},
                new int[][] {{2, 1}, {1, 2}, {1, 1}});
    }

    /** Um processo que precisa de tudo, e dois que precisam de pouco. */
    public static Sistema umGuloso() {
        return new Sistema("um guloso", new int[] {2, 2},
                new int[][] {{2, 2}, {1, 0}, {0, 1}});
    }

    /** Os seis escritos a mao, na ordem das tabelas. */
    public static List<Sistema> aMao() {
        return List.of(doisGarfos(), doisGarfosEmDobro(), rodaDeTres(), banco(), duasDeCada(),
                umGuloso());
    }

    /** Os que tem uma unidade de cada tipo, que e onde o ciclo prova impasse. */
    public static List<Sistema> deUmaUnidade() {
        return aMao().stream().filter(Sistemas::unidadeUnica).toList();
    }

    public static List<Sistema> deVariasUnidades() {
        return aMao().stream().filter(sistema -> !unidadeUnica(sistema)).toList();
    }

    public static boolean unidadeUnica(Sistema sistema) {
        for (int quanto : sistema.total())
            if (quanto > 1) return false;

        return true;
    }

    /** Quantos processos e tipos os sistemas sorteados tem. */
    public static final int PROCESSOS = 3;
    public static final int TIPOS = 2;

    /**
     * Um sistema sorteado, pequeno o bastante para o juiz varrer inteiro.
     *
     * <p>O total de cada tipo sai de 1 a 2 e o maximo de cada processo sai de 0 ao total.
     * Processo que nao pede nada e inofensivo e so engorda a varredura, entao todo processo
     * pede pelo menos uma unidade de alguma coisa.
     */
    public static Sistema sorteado(Sorteio sorteio, int numero) {
        int[] total = new int[TIPOS];

        for (int tipo = 0; tipo < TIPOS; tipo++) total[tipo] = 1 + sorteio.ate(2);

        int[][] maximo = new int[PROCESSOS][TIPOS];

        for (int processo = 0; processo < PROCESSOS; processo++) {
            boolean pediuAlgo = false;

            for (int tipo = 0; tipo < TIPOS; tipo++) {
                maximo[processo][tipo] = sorteio.ate(total[tipo] + 1);

                if (maximo[processo][tipo] > 0) pediuAlgo = true;
            }

            if (!pediuAlgo) maximo[processo][sorteio.ate(TIPOS)] = 1;
        }

        return new Sistema("sorteado " + numero, total, maximo);
    }

    public static List<Sistema> sorteados(long semente, int quantos) {
        Sorteio sorteio = new Sorteio(semente);

        List<Sistema> saida = new ArrayList<>();

        for (int i = 0; i < quantos; i++) saida.add(sorteado(sorteio, i));

        return saida;
    }
}
