package br.com.conde.impasse;

/**
 * ORDEM TOTAL, MAS DENTRO DO PROGRAMA: cada processo pede sempre o menor que ainda falta.
 *
 * <p>E a mesma ideia da ordem total, aplicada no lugar certo. A diferenca e quem obedece: la
 * o sistema RECUSA o pedido fora de ordem, aqui o processo nao faz o pedido fora de ordem.
 *
 * <p>Isso parece a mesma coisa e nao e, e a tabela mostra a diferenca. Recusar o pedido fora
 * de ordem nao desfaz o recurso que o processo ja pegou: quem agarrou o tipo 1 primeiro fica
 * esperando pelo tipo 0 para sempre, e o sistema trava do mesmo jeito. A ordem total so
 * previne impasse quando ela e uma regra de escrita do programa.
 */
public final class OrdemNoPrograma implements Politica {

    @Override
    public String nome() {
        return "ordem no programa";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        for (int tipo = 0; tipo < pedido.tipo(); tipo++)
            if (estado.aindaVaiPedir(pedido.processo(), tipo) > 0) return false;

        return true;
    }

    @Override
    public String condicaoQuebrada() {
        return "espera circular";
    }
}
