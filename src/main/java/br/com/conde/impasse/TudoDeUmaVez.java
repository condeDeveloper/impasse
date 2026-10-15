package br.com.conde.impasse;

/**
 * TUDO DE UMA VEZ: so comeca a pegar quem consegue pegar tudo.
 *
 * <p>A ideia e quebrar a condicao de SEGURAR E ESPERAR: se ninguem fica com recurso na mao
 * esperando por outro, nao ha o que travar. O processo olha o que sobrou, ve que da para
 * tudo o que ele declarou, e so entao comeca.
 *
 * <p>So que ele comeca pegando UMA unidade, nao todas. Entre a primeira e a ultima, outro
 * processo pode olhar o que sobrou, achar que tambem da, e comecar tambem. Essa politica
 * esta aqui porque e exatamente o tipo de raciocinio que parece certo e nao e, e o juiz
 * varre todos os estados alcancaveis para descobrir se ela aguenta.
 */
public final class TudoDeUmaVez implements Politica {

    @Override
    public String nome() {
        return "tudo de uma vez";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        if (jaComecou(estado, pedido.processo())) return true;

        int[] sobra = estado.disponivel();

        for (int tipo = 0; tipo < estado.sistema().tipos(); tipo++)
            if (estado.aindaVaiPedir(pedido.processo(), tipo) > sobra[tipo]) return false;

        return true;
    }

    private boolean jaComecou(Sistema.Estado estado, int processo) {
        for (int tipo = 0; tipo < estado.sistema().tipos(); tipo++)
            if (estado.tem(processo, tipo) > 0) return true;

        return false;
    }

    @Override
    public String condicaoQuebrada() {
        return "segurar e esperar";
    }
}
