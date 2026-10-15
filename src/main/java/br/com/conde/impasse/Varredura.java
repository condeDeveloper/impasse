package br.com.conde.impasse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * O JUIZ: todos os estados que o sistema consegue alcancar, sem excecao.
 *
 * <p>Ele e o juiz certo e nao so o conveniente porque nao e um algoritmo de deteccao nem de
 * prevencao. E a definicao: parte do estado em que ninguem pegou nada, aplica TODA transicao
 * que a politica deixa, e guarda tudo o que aparece.
 *
 * <p>Se existe uma sequencia de pedidos e terminos que leva a um impasse, ela esta aqui. Nao
 * ha amostragem, nao ha sorteio, nao ha caso interessante escolhido: ha o espaco inteiro.
 *
 * <p>E por isso que os sistemas medidos sao pequenos. Tres processos e dois tipos de recurso
 * com tres unidades cada ja dao milhares de estados, e o que se ganha em tamanho se perde em
 * certeza.
 */
public final class Varredura {

    /**
     * Teto de estados. Passar dele quer dizer que o sistema e grande demais para o juiz.
     *
     * <p>Os sistemas medidos aqui tem algumas centenas de estados cada; o teto existe para
     * que um sistema maior pare depressa em vez de comer a memoria da maquina.
     */
    public static final int ORCAMENTO = 200_000;

    public record Resultado(Sistema sistema, String politica, int estados, int impasses,
            int travamentosParciais, int terminaisBons, String primeiroImpasse,
            int pedidosOferecidos, int pedidosAtendidos) {

        public boolean temImpasse() {
            return impasses > 0;
        }

        /** Que fracao dos pedidos que caberiam a politica deixou passar. */
        public double fracaoAtendida() {
            return pedidosOferecidos == 0 ? 1 : pedidosAtendidos / (double) pedidosOferecidos;
        }
    }

    private Varredura() {}

    public static Resultado rodar(Sistema sistema, Politica politica) {
        Map<String, Sistema.Estado> vistos = new LinkedHashMap<>();

        Deque<Sistema.Estado> fila = new ArrayDeque<>();

        Sistema.Estado inicial = sistema.inicial();

        vistos.put(inicial.chave(), inicial);
        fila.add(inicial);

        int impasses = 0;
        int travamentosParciais = 0;
        int terminaisBons = 0;

        int oferecidos = 0;
        int atendidos = 0;

        String primeiro = "";

        while (!fila.isEmpty()) {
            if (vistos.size() > ORCAMENTO)
                throw new IllegalStateException("espaco de estados grande demais");

            Sistema.Estado estado = fila.poll();

            List<Sistema.Estado> seguintes = new ArrayList<>();

            for (Sistema.Pedido pedido : estado.pedidosPossiveis()) {
                oferecidos++;

                if (!politica.permite(estado, pedido)) continue;

                atendidos++;

                seguintes.add(estado.depoisDoPedido(pedido));
            }

            for (int processo : estado.prontosParaTerminar())
                seguintes.add(estado.depoisDeTerminar(processo));

            if (seguintes.isEmpty()) {
                if (estado.todosTerminaram()) terminaisBons++;
                else {
                    if (estado.impasse()) impasses++;
                    else travamentosParciais++;

                    if (primeiro.isEmpty()) primeiro = estado.chave();
                }
            }

            for (Sistema.Estado seguinte : seguintes)
                if (!vistos.containsKey(seguinte.chave())) {
                    vistos.put(seguinte.chave(), seguinte);
                    fila.add(seguinte);
                }
        }

        return new Resultado(sistema, politica.nome(), vistos.size(), impasses,
                travamentosParciais, terminaisBons, primeiro, oferecidos, atendidos);
    }

    /** Todos os estados alcancaveis sem politica nenhuma, para os detectores enfrentarem. */
    public static List<Sistema.Estado> todosOsEstados(Sistema sistema) {
        Set<String> vistos = new LinkedHashSet<>();

        List<Sistema.Estado> saida = new ArrayList<>();

        Deque<Sistema.Estado> fila = new ArrayDeque<>();

        Sistema.Estado inicial = sistema.inicial();

        vistos.add(inicial.chave());
        fila.add(inicial);
        saida.add(inicial);

        while (!fila.isEmpty()) {
            Sistema.Estado estado = fila.poll();

            List<Sistema.Estado> seguintes = new ArrayList<>();

            for (Sistema.Pedido pedido : estado.pedidosPossiveis())
                seguintes.add(estado.depoisDoPedido(pedido));

            for (int processo : estado.prontosParaTerminar())
                seguintes.add(estado.depoisDeTerminar(processo));

            for (Sistema.Estado seguinte : seguintes)
                if (vistos.add(seguinte.chave())) {
                    fila.add(seguinte);
                    saida.add(seguinte);
                }
        }

        return saida;
    }
}
