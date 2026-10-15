package br.com.conde.impasse;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("deteccao") || comando.equals("tudo")) deteccao();
        if (comando.equals("custo") || comando.equals("tudo")) custo();
        if (comando.equals("seguranca") || comando.equals("tudo")) seguranca();
        if (comando.equals("condicoes") || comando.equals("tudo")) condicoes();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("NENHUMA POLITICA DEIXA CHEGAR A UM IMPASSE?");
        System.out.println(Medidas.LOTE + " sistemas sorteados de " + Sistemas.PROCESSOS
                + " processos e " + Sistemas.TIPOS + " tipos de recurso.");
        System.out.println("O juiz varre TODOS os estados alcancaveis de cada um: se existe"
                + " caminho ate o impasse, ele esta na conta.");
        System.out.println();

        System.out.printf("  %-20s %20s %22s %14s%n",
                "politica", "sistemas com impasse", "travados pela politica", "estados");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("  %-20s %20d %22d %14d%n",
                    linha.politica(), linha.comImpasse(), linha.comTravamentoDaPolitica(),
                    linha.estados());

        System.out.println();
        System.out.println("OS SISTEMAS ESCRITOS A MAO (impasses / travados pela politica)");
        System.out.println();

        System.out.printf("  %-18s", "sistema");

        for (Politica politica : Politica.todas())
            System.out.printf(" %18s", politica.nome());

        System.out.println();

        for (Sistema sistema : Sistemas.aMao()) {
            System.out.printf("  %-18s", sistema.nome());

            for (Politica politica : Politica.todas()) {
                Medidas.AMao linha = Medidas.aMao(sistema.nome(), politica.nome());

                System.out.printf(" %18s",
                        linha.impasses() + " / " + linha.travamentosDaPolitica());
            }

            System.out.println();
        }

        System.out.println();
    }

    private static void deteccao() {
        System.out.println();
        System.out.println("O DETECTOR DE CICLO CONTRA A DEFINICAO");
        System.out.println("Todos os estados alcancaveis dos sistemas escritos a mao,"
                + " separados por familia.");
        System.out.println();

        System.out.printf("  %-18s %-18s %10s %12s %10s %16s %16s%n",
                "detector", "familia", "estados", "impasses", "acusou", "alarme falso",
                "deixou passar");

        for (Medidas.Deteccao linha : Medidas.deteccao())
            System.out.printf("  %-18s %-18s %10d %12d %10d %16d %16d%n",
                    linha.detector(), linha.familia(), linha.estados(), linha.impasses(),
                    linha.acusou(), linha.falsoPositivo(), linha.falsoNegativo());

        System.out.println();
        System.out.println("  Com uma unidade de cada tipo, o ciclo e necessario E"
                + " suficiente: zero erro dos dois lados.");
        System.out.println();
    }

    private static void custo() {
        System.out.println();
        System.out.println("QUANTO CADA PREVENCAO CUSTA EM CONCORRENCIA");
        System.out.println("A fracao de pedidos atendidos conta, sobre todos os estados,"
                + " quantos pedidos que o");
        System.out.println("sistema aceitaria a politica deixou passar.");
        System.out.println();

        System.out.printf("  %-20s %12s %22s %18s%n",
                "politica", "previne?", "pedidos atendidos", "estados alcancaveis");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf(Locale.ROOT, "  %-20s %12s %21.1f%% %18d%n",
                    linha.politica(), linha.previne() ? "sim" : "NAO",
                    100 * linha.fracaoAtendida(), linha.estados());

        System.out.println();
    }

    private static void seguranca() {
        Medidas.Seguranca medida = Medidas.seguranca();

        System.out.println();
        System.out.println("ESTADO SEGURO NAO E ESTADO SEM IMPASSE");
        System.out.println("Todos os estados alcancaveis dos " + Medidas.LOTE
                + " sistemas sorteados, sem politica nenhuma.");
        System.out.println();

        System.out.printf("  %-40s %12d%n", "estados alcancaveis", medida.estados());
        System.out.printf("  %-40s %12d%n", "estados de impasse", medida.impasses());
        System.out.printf("  %-40s %12d%n", "estados inseguros", medida.inseguros());

        System.out.println();
        System.out.printf("  %-40s %12d%n", "inseguros que terminam bem",
                medida.insegurosQueAndam());
        System.out.printf("  %-40s %12d%n", "seguros que dao impasse",
                medida.segurosComImpasse());

        System.out.println();
        System.out.printf(Locale.ROOT,
                "  %.1f%% dos estados inseguros nunca travam. A garantia vale so no outro"
                        + " sentido.%n", 100 * medida.fracaoDeAlarmeFalso());
        System.out.println();
    }

    private static void condicoes() {
        System.out.println();
        System.out.println("QUEBRAR CADA UMA DAS QUATRO CONDICOES");
        System.out.println();

        System.out.printf("  %-20s %-24s %14s%n",
                "politica", "condicao quebrada", "funciona?");

        for (Politica politica : Politica.todas()) {
            Medidas.Correcao linha = Medidas.correcao(politica.nome());

            System.out.printf("  %-20s %-24s %14s%n",
                    politica.nome(), politica.condicaoQuebrada(),
                    linha.previne() ? "sim" : linha.comImpasse() + " sistemas travam");
        }

        System.out.println();
        System.out.println("E COM UM TIPO SO DE RECURSO, COM VARIAS UNIDADES");
        System.out.println("Nao ha ordem entre tipos para impor, porque ha um tipo so.");
        System.out.println();

        System.out.printf("  %-20s %24s%n", "politica", "sistemas com impasse");

        for (Medidas.UmTipoSo linha : Medidas.umTipoSo())
            System.out.printf("  %-20s %24s%n", linha.politica(),
                    linha.comImpasse() + " de " + linha.sistemas());

        System.out.println();
    }
}
