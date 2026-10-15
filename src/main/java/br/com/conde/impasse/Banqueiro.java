package br.com.conde.impasse;

/**
 * O BANQUEIRO, de Dijkstra, 1965: so atende o pedido que deixa o estado SEGURO.
 *
 * <p>Um estado e seguro quando existe uma ordem de terminar todo mundo: alguem consegue tudo
 * o que ainda vai pedir com o que esta sobrando, termina, devolve o que tinha, e com isso
 * sobra mais para o proximo, e assim por diante ate acabarem todos.
 *
 * <p>Repare no que isso exige: saber de antemao o MAXIMO que cada processo vai pedir. E a
 * hipotese que faz o banqueiro funcionar e e tambem a razao de ele quase nunca ser usado,
 * porque quase nenhum programa sabe declarar isso.
 *
 * <h2>Seguro nao e o mesmo que sem impasse</h2>
 *
 * <p>Um estado inseguro NAO e um estado de impasse: e um estado a partir do qual o impasse e
 * possivel, se cada um pedir o maximo que declarou. Como ninguem e obrigado a pedir o
 * maximo, muitos estados inseguros terminam bem.
 *
 * <p>O banqueiro recusa todos eles do mesmo jeito, e a tabela da seguranca mede quantos
 * pedidos perfeitamente inofensivos ele manda esperar.
 */
public final class Banqueiro implements Politica {

    @Override
    public String nome() {
        return "banqueiro";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        return seguro(estado.depoisDoPedido(pedido));
    }

    /** Existe ordem em que todos terminam, supondo que cada um peca o maximo que declarou? */
    public static boolean seguro(Sistema.Estado estado) {
        Sistema sistema = estado.sistema();

        int processos = sistema.processos();
        int tipos = sistema.tipos();

        int[] sobra = estado.disponivel();

        boolean[] jaTerminou = new boolean[processos];

        for (int processo = 0; processo < processos; processo++)
            jaTerminou[processo] = estado.terminou(processo);

        boolean andou = true;

        while (andou) {
            andou = false;

            for (int processo = 0; processo < processos; processo++) {
                if (jaTerminou[processo]) continue;

                boolean cabe = true;

                for (int tipo = 0; tipo < tipos; tipo++)
                    if (estado.aindaVaiPedir(processo, tipo) > sobra[tipo]) cabe = false;

                if (!cabe) continue;

                /* Ele termina e devolve tudo o que tinha, que e o que destrava os outros. */
                for (int tipo = 0; tipo < tipos; tipo++)
                    sobra[tipo] += estado.tem(processo, tipo);

                jaTerminou[processo] = true;
                andou = true;
            }
        }

        for (boolean terminou : jaTerminou)
            if (!terminou) return false;

        return true;
    }

    @Override
    public String condicaoQuebrada() {
        return "nenhuma, ele desvia";
    }
}
