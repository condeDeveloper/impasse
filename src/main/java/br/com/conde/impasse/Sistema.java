package br.com.conde.impasse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * O SISTEMA: quantos recursos existem, e o que cada processo ainda vai pedir.
 *
 * <p>O modelo e o de Dijkstra, de 1965, e ele e proposital: cada processo DECLARA de antemao
 * o maximo que vai precisar de cada tipo de recurso. Essa declaracao e o que torna o
 * banqueiro possivel, e e tambem a hipotese que quase nenhum programa de verdade cumpre.
 *
 * <p>Um recurso aqui e reutilizavel e indivisivel: ele e tomado, usado e devolvido inteiro.
 * Memoria que se divide, processador que se fatia e mensagem que se perde sao outros
 * assuntos.
 *
 * <h2>Duas transicoes, e nada mais</h2>
 *
 * <p>Um processo PEDE uma unidade de um tipo que ele ainda vai usar, e so consegue se houver
 * unidade sobrando. Quando ja tem tudo o que declarou, ele TERMINA e devolve tudo de uma vez.
 *
 * <p>Nao ha devolucao parcial nem pedido de varias unidades de uma vez. As duas coisas
 * existem em sistemas de verdade e nenhuma delas muda a natureza do impasse; o que elas
 * mudam e o tamanho do espaco de estados, e aqui ele precisa caber inteiro na varredura.
 */
public record Sistema(String nome, int[] total, int[][] maximo) {

    public Sistema {
        total = total.clone();

        int[][] copia = new int[maximo.length][];

        for (int i = 0; i < maximo.length; i++) copia[i] = maximo[i].clone();

        maximo = copia;
    }

    public int processos() {
        return maximo.length;
    }

    public int tipos() {
        return total.length;
    }

    /** O estado em que ninguem pegou nada e ninguem terminou. */
    public Estado inicial() {
        return new Estado(new int[processos()][tipos()], new boolean[processos()]);
    }

    /** Um pedido: um processo e um tipo de recurso. */
    public record Pedido(int processo, int tipo) {

        @Override
        public String toString() {
            return "p" + processo + " pede r" + tipo;
        }
    }

    /**
     * Um estado do sistema: quem esta com o que, e quem ja acabou.
     *
     * <p>O resto se calcula disto. O disponivel e o total menos o que esta nas maos de
     * alguem, e o que cada um ainda vai pedir e o maximo dele menos o que ele ja tem.
     */
    public final class Estado {

        private final int[][] tem;
        private final boolean[] terminou;

        public Estado(int[][] tem, boolean[] terminou) {
            this.tem = tem;
            this.terminou = terminou;
        }

        public int tem(int processo, int tipo) {
            return tem[processo][tipo];
        }

        public boolean terminou(int processo) {
            return terminou[processo];
        }

        public int[] disponivel() {
            int[] sobra = total.clone();

            for (int processo = 0; processo < processos(); processo++)
                for (int tipo = 0; tipo < tipos(); tipo++) sobra[tipo] -= tem[processo][tipo];

            return sobra;
        }

        public int aindaVaiPedir(int processo, int tipo) {
            return maximo[processo][tipo] - tem[processo][tipo];
        }

        /** Verdadeiro quando este processo ja tem tudo o que declarou que ia precisar. */
        public boolean completo(int processo) {
            for (int tipo = 0; tipo < tipos(); tipo++)
                if (aindaVaiPedir(processo, tipo) > 0) return false;

            return true;
        }

        /** Os pedidos que o sistema atende agora, antes de qualquer politica opinar. */
        public List<Pedido> pedidosPossiveis() {
            List<Pedido> saida = new ArrayList<>();

            int[] sobra = disponivel();

            for (int processo = 0; processo < processos(); processo++) {
                if (terminou[processo]) continue;

                for (int tipo = 0; tipo < tipos(); tipo++)
                    if (aindaVaiPedir(processo, tipo) > 0 && sobra[tipo] > 0)
                        saida.add(new Pedido(processo, tipo));
            }

            return saida;
        }

        /** Quem pode terminar agora e devolver tudo. */
        public List<Integer> prontosParaTerminar() {
            List<Integer> saida = new ArrayList<>();

            for (int processo = 0; processo < processos(); processo++)
                if (!terminou[processo] && completo(processo)) saida.add(processo);

            return saida;
        }

        public Estado depoisDoPedido(Pedido pedido) {
            int[][] novo = copiar();

            novo[pedido.processo()][pedido.tipo()]++;

            return new Estado(novo, terminou.clone());
        }

        public Estado depoisDeTerminar(int processo) {
            int[][] novo = copiar();

            novo[processo] = new int[tipos()];

            boolean[] acabaram = terminou.clone();

            acabaram[processo] = true;

            return new Estado(novo, acabaram);
        }

        private int[][] copiar() {
            int[][] novo = new int[processos()][];

            for (int i = 0; i < processos(); i++) novo[i] = tem[i].clone();

            return novo;
        }

        public boolean todosTerminaram() {
            for (boolean acabou : terminou)
                if (!acabou) return false;

            return true;
        }

        /**
         * IMPASSE E A DEFINICAO, NAO O RESULTADO DE UM ALGORITMO.
         *
         * <p>Ha impasse quando nao sobrou transicao nenhuma e ainda ha processo por
         * terminar. Ninguem consegue nada do que falta, ninguem pode acabar, e esperar mais
         * nao resolve, porque nada vai mudar sozinho.
         *
         * <p>Repare que isto nao fala de ciclo, de grafo nem de espera circular. Fala do que
         * acontece: o sistema parou e nao volta.
         */
        public boolean impasse() {
            return !todosTerminaram() && pedidosPossiveis().isEmpty()
                    && prontosParaTerminar().isEmpty();
        }

        /** Quem esta travado: por terminar e sem conseguir nada do que ainda falta. */
        public List<Integer> travados() {
            List<Integer> saida = new ArrayList<>();

            int[] sobra = disponivel();

            for (int processo = 0; processo < processos(); processo++) {
                if (terminou[processo] || completo(processo)) continue;

                boolean consegue = false;

                for (int tipo = 0; tipo < tipos(); tipo++)
                    if (aindaVaiPedir(processo, tipo) > 0 && sobra[tipo] > 0) consegue = true;

                if (!consegue) saida.add(processo);
            }

            return saida;
        }

        public String chave() {
            StringBuilder saida = new StringBuilder();

            for (int[] linha : tem)
                for (int quanto : linha) saida.append(quanto).append(',');

            saida.append('|').append(Arrays.toString(terminou));

            return saida.toString();
        }

        public Sistema sistema() {
            return Sistema.this;
        }

        @Override
        public String toString() {
            return chave();
        }
    }
}
