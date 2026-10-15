package br.com.conde.impasse;

/**
 * UM POR VEZ: so um processo pode estar pegando recursos de cada vez.
 *
 * <p>E o TUDO DE UMA VEZ com o buraco tapado. Ninguem comeca enquanto outro estiver no meio
 * de uma aquisicao, entao a sequencia de pedidos de um processo vira atomica de verdade.
 *
 * <p>Ela funciona, e e a politica mais cara que existe: o sistema inteiro passa a ser uma
 * fila. A tabela do custo mede o tamanho desse preco, e ele e o motivo de ninguem fazer isso.
 */
public final class UmPorVez implements Politica {

    @Override
    public String nome() {
        return "um por vez";
    }

    @Override
    public boolean permite(Sistema.Estado estado, Sistema.Pedido pedido) {
        for (int processo = 0; processo < estado.sistema().processos(); processo++) {
            if (processo == pedido.processo()) continue;

            if (estado.terminou(processo)) continue;

            for (int tipo = 0; tipo < estado.sistema().tipos(); tipo++)
                if (estado.tem(processo, tipo) > 0) return false;
        }

        return true;
    }

    @Override
    public String condicaoQuebrada() {
        return "segurar e esperar";
    }
}
