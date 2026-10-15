package br.com.conde.impasse;

/**
 * POR CICLO NO GRAFO DE ESPERA.
 *
 * <p>Monta o grafo em que o processo i aponta para o processo j quando i ainda vai pedir um
 * tipo que nao tem unidade sobrando e j esta com uma unidade desse tipo. Se ha ciclo, acusa.
 *
 * <p>E o detector que todo mundo escreve, e ele e exato quando ha UMA unidade de cada tipo:
 * nesse caso o ciclo e condicao necessaria e suficiente. Com duas unidades do mesmo tipo
 * ele deixa de ser suficiente, porque o processo pode estar esperando por um recurso que
 * tanto faz de quem venha, e basta um dos donos soltar.
 *
 * <p>A tabela da deteccao mede isso em vez de citar: quantos estados ele acusa, e quantos
 * deles andam normalmente.
 */
public final class PorCiclo implements Detector {

    @Override
    public String nome() {
        return "ciclo no grafo";
    }

    @Override
    public boolean acusa(Sistema.Estado estado) {
        int processos = estado.sistema().processos();
        int tipos = estado.sistema().tipos();

        int[] sobra = estado.disponivel();

        boolean[][] espera = new boolean[processos][processos];

        for (int quem = 0; quem < processos; quem++) {
            if (estado.terminou(quem)) continue;

            for (int tipo = 0; tipo < tipos; tipo++) {
                if (estado.aindaVaiPedir(quem, tipo) == 0) continue;

                if (sobra[tipo] > 0) continue;

                for (int dono = 0; dono < processos; dono++)
                    if (dono != quem && estado.tem(dono, tipo) > 0) espera[quem][dono] = true;
            }
        }

        return temCiclo(espera, processos);
    }

    /** Fecho transitivo por Warshall: ha ciclo quando alguem alcanca a si mesmo. */
    private static boolean temCiclo(boolean[][] espera, int processos) {
        for (int meio = 0; meio < processos; meio++)
            for (int de = 0; de < processos; de++)
                for (int para = 0; para < processos; para++)
                    if (espera[de][meio] && espera[meio][para]) espera[de][para] = true;

        for (int quem = 0; quem < processos; quem++)
            if (espera[quem][quem]) return true;

        return false;
    }
}
