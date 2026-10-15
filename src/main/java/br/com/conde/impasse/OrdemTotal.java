package br.com.conde.impasse;

/**
 * ORDEM TOTAL: os recursos sao numerados, e ninguem pede para tras.
 *
 * <p>Um processo que ja tem uma unidade do tipo 2 nao pode pedir do tipo 0 nem do 1. Se
 * precisar, devolve tudo e recomeca, o que aqui quer dizer que ele simplesmente nao pode
 * fazer isso.
 *
 * <p>Ela quebra a ESPERA CIRCULAR, e a prova cabe numa linha: num ciclo de espera, alguem
 * teria de estar esperando por um numero menor do que o que ja tem. E a prevencao mais usada
 * de verdade, porque nao precisa de declaracao nenhuma do futuro: basta todo mundo tomar os
 * cadeados na mesma ordem.
 */
public final class OrdemTotal implements Politica {

    @Override
    public String nome() {
        return "ordem total";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        for (int tipo = pedido.tipo() + 1; tipo < estado.sistema().tipos(); tipo++)
            if (estado.tem(pedido.processo(), tipo) > 0) return false;

        return true;
    }

    @Override
    public String condicaoQuebrada() {
        return "espera circular";
    }
}
