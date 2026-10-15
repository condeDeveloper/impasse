package br.com.conde.impasse;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositorio afirma, calculado aqui e em nenhum outro lugar.
 *
 * <p>O Medidor imprime o que sai daqui e o teste confere o que sai daqui. Os dois leem a
 * MESMA funcao, entao nao existe a situacao de o texto dizer uma coisa e o teste conferir
 * outra: se um numero mudar, a tabela muda e o teste quebra junto.
 *
 * <p>A varredura de todos os estados de 400 sistemas por 6 politicas e guardada na primeira
 * vez. Sem isso, o teste faria a mesma varredura cinco vezes.
 */
public final class Medidas {

    /** Quantos sistemas sorteados entram no lote. */
    public static final int LOTE = 400;

    /** A semente, fixa para o numero sair igual nos tres sistemas operacionais. */
    public static final long SEMENTE = 1;

    private Medidas() {}

    private static List<Sistema> lote;

    public static List<Sistema> lote() {
        if (lote != null) return lote;

        return lote = Sistemas.sorteados(SEMENTE, LOTE);
    }

    /* ===================== 1. correcao ===================== */

    public record Correcao(String politica, int sistemas, int comImpasse,
            int comTravamentoDaPolitica, long estados, long pedidosOferecidos,
            long pedidosAtendidos) {

        public double fracaoAtendida() {
            return pedidosAtendidos / (double) pedidosOferecidos;
        }

        public boolean previne() {
            return comImpasse == 0;
        }
    }

    private static List<Correcao> correcao;

    /**
     * CADA POLITICA CONTRA A VARREDURA DE TODOS OS ESTADOS ALCANCAVEIS.
     *
     * <p>Nao e uma simulacao com sorteio de quem pede o que: e o espaco inteiro. Se existe
     * uma sequencia de pedidos e terminos que leva ao impasse, ela esta na conta.
     *
     * <p>A coluna do travamento da politica e separada de proposito. Um estado em que o
     * sistema aceitaria um pedido e a politica recusa TODOS nao e um impasse do sistema: e a
     * politica tendo travado o que queria proteger.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (Politica politica : Politica.todas()) {
            int comImpasse = 0;
            int comTravamento = 0;

            long estados = 0;
            long oferecidos = 0;
            long atendidos = 0;

            for (Sistema sistema : lote()) {
                Varredura.Resultado resultado = Varredura.rodar(sistema, politica);

                if (resultado.temImpasse()) comImpasse++;
                if (resultado.travamentosParciais() > 0) comTravamento++;

                estados += resultado.estados();
                oferecidos += resultado.pedidosOferecidos();
                atendidos += resultado.pedidosAtendidos();
            }

            saida.add(new Correcao(politica.nome(), lote().size(), comImpasse, comTravamento,
                    estados, oferecidos, atendidos));
        }

        return correcao = List.copyOf(saida);
    }

    public static Correcao correcao(String politica) {
        return correcao().stream().filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }

    /* ===================== 2. deteccao ===================== */

    public record Deteccao(String detector, String familia, int estados, int impasses,
            int acusou, int falsoPositivo, int falsoNegativo) {}

    private static List<Deteccao> deteccao;

    /**
     * OS DETECTORES CONTRA A DEFINICAO, EM TODOS OS ESTADOS ALCANCAVEIS.
     *
     * <p>A separacao por familia e o ponto da tabela. Com uma unidade de cada tipo, o ciclo
     * no grafo de espera e condicao necessaria E suficiente, e a medida mostra zero erro dos
     * dois lados. Com varias unidades, o ciclo continua necessario e deixa de ser
     * suficiente.
     */
    public static List<Deteccao> deteccao() {
        if (deteccao != null) return deteccao;

        List<Deteccao> saida = new ArrayList<>();

        for (Detector detector : Detector.todos())
            for (String familia : List.of("uma unidade", "varias unidades")) {
                List<Sistema> sistemas = familia.equals("uma unidade")
                        ? Sistemas.deUmaUnidade() : Sistemas.deVariasUnidades();

                int estados = 0;
                int impasses = 0;
                int acusou = 0;
                int falsoPositivo = 0;
                int falsoNegativo = 0;

                for (Sistema sistema : sistemas)
                    for (Sistema.Estado estado : Varredura.todosOsEstados(sistema)) {
                        estados++;

                        boolean travou = estado.impasse();
                        boolean acusa = detector.acusa(estado);

                        if (travou) impasses++;
                        if (acusa) acusou++;
                        if (acusa && !travou) falsoPositivo++;
                        if (!acusa && travou) falsoNegativo++;
                    }

                saida.add(new Deteccao(detector.nome(), familia, estados, impasses, acusou,
                        falsoPositivo, falsoNegativo));
            }

        return deteccao = List.copyOf(saida);
    }

    public static Deteccao deteccao(String detector, String familia) {
        return deteccao().stream()
                .filter(linha -> linha.detector().equals(detector))
                .filter(linha -> linha.familia().equals(familia))
                .findFirst().orElseThrow();
    }

    /* ===================== 3. seguranca ===================== */

    public record Seguranca(int estados, int inseguros, int impasses, int insegurosQueAndam,
            int segurosComImpasse) {

        /** Que fracao dos estados inseguros termina bem mesmo assim. */
        public double fracaoDeAlarmeFalso() {
            return insegurosQueAndam / (double) inseguros;
        }
    }

    private static Seguranca seguranca;

    /**
     * SEGURO NAO E O MESMO QUE SEM IMPASSE.
     *
     * <p>Estado inseguro quer dizer "existe um futuro ruim SE cada um pedir o maximo que
     * declarou". Ninguem e obrigado a pedir o maximo, entao muito estado inseguro termina
     * bem.
     *
     * <p>A tabela conta as duas direcoes. A direcao que importa para a correcao e a outra: o
     * numero de estados seguros que dao impasse tem de ser zero, e e.
     */
    public static Seguranca seguranca() {
        if (seguranca != null) return seguranca;

        int estados = 0;
        int inseguros = 0;
        int impasses = 0;
        int insegurosQueAndam = 0;
        int segurosComImpasse = 0;

        for (Sistema sistema : lote())
            for (Sistema.Estado estado : Varredura.todosOsEstados(sistema)) {
                estados++;

                boolean seguro = Banqueiro.seguro(estado);
                boolean travou = estado.impasse();

                if (!seguro) inseguros++;
                if (travou) impasses++;
                if (!seguro && !travou) insegurosQueAndam++;
                if (seguro && travou) segurosComImpasse++;
            }

        return seguranca = new Seguranca(estados, inseguros, impasses, insegurosQueAndam,
                segurosComImpasse);
    }

    /* ===================== 4. os sistemas escritos a mao ===================== */

    public record AMao(String sistema, String politica, int estados, int impasses,
            int travamentosDaPolitica, double fracaoAtendida) {}

    private static List<AMao> aMao;

    public static List<AMao> aMao() {
        if (aMao != null) return aMao;

        List<AMao> saida = new ArrayList<>();

        for (Sistema sistema : Sistemas.aMao())
            for (Politica politica : Politica.todas()) {
                Varredura.Resultado resultado = Varredura.rodar(sistema, politica);

                saida.add(new AMao(sistema.nome(), politica.nome(), resultado.estados(),
                        resultado.impasses(), resultado.travamentosParciais(),
                        resultado.fracaoAtendida()));
            }

        return aMao = List.copyOf(saida);
    }

    public static AMao aMao(String sistema, String politica) {
        return aMao().stream()
                .filter(linha -> linha.sistema().equals(sistema))
                .filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }

    /* ===================== 5. um tipo so de recurso ===================== */

    public record UmTipoSo(String politica, int sistemas, int comImpasse) {}

    private static List<UmTipoSo> umTipoSo;

    /** Os sistemas sorteados que tem um tipo de recurso so, com varias unidades. */
    public static List<Sistema> comUmTipoSo() {
        List<Sistema> saida = new ArrayList<>();

        Sorteio sorteio = new Sorteio(SEMENTE + 1);

        for (int numero = 0; numero < LOTE; numero++) {
            int total = 2 + sorteio.ate(3);

            int[][] maximo = new int[Sistemas.PROCESSOS][1];

            for (int processo = 0; processo < Sistemas.PROCESSOS; processo++)
                maximo[processo][0] = 1 + sorteio.ate(total);

            saida.add(new Sistema("um tipo " + numero, new int[] {total}, maximo));
        }

        return saida;
    }

    /**
     * COM UM TIPO SO DE RECURSO, ORDENAR NAO TEM O QUE ORDENAR.
     *
     * <p>A ordem total previne a espera circular ENTRE tipos. Quando ha um tipo so, com
     * varias unidades, o ciclo acontece entre processos disputando unidades do mesmo tipo, e
     * nao ha ordem que desfaca isso.
     */
    public static List<UmTipoSo> umTipoSo() {
        if (umTipoSo != null) return umTipoSo;

        List<Sistema> sistemas = comUmTipoSo();

        List<UmTipoSo> saida = new ArrayList<>();

        for (Politica politica : Politica.todas()) {
            int comImpasse = 0;

            for (Sistema sistema : sistemas)
                if (Varredura.rodar(sistema, politica).temImpasse()) comImpasse++;

            saida.add(new UmTipoSo(politica.nome(), sistemas.size(), comImpasse));
        }

        return umTipoSo = List.copyOf(saida);
    }

    public static UmTipoSo umTipoSo(String politica) {
        return umTipoSo().stream().filter(linha -> linha.politica().equals(politica))
                .findFirst().orElseThrow();
    }
}
