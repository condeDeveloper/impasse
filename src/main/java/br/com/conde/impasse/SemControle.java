package br.com.conde.impasse;

/**
 * SEM CONTROLE: se cabe, pode.
 *
 * <p>Ela e a referencia e nao uma opcao. Enquanto houver uma unidade sobrando do que o
 * processo pediu, ele leva. E o comportamento de qualquer sistema que nao faca nada a
 * respeito, que e a maioria deles.
 *
 * <p>O que ela mede e onde o impasse mora: todo estado de impasse que o juiz encontra e
 * alcancavel desta forma, e e dele que as outras politicas precisam fugir.
 */
public final class SemControle implements Politica {

    @Override
    public String nome() {
        return "sem controle";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        return true;
    }

    @Override
    public String condicaoQuebrada() {
        return "nenhuma";
    }
}
