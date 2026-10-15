package br.com.conde.impasse;

import java.util.List;

/**
 * UMA POLITICA: dado o estado e um pedido que cabe, atender ou mandar esperar.
 *
 * <p>Toda politica daqui so pode dizer sim ou nao a um pedido. Nenhuma delas tira recurso de
 * quem ja tem, nenhuma mata processo, nenhuma reordena nada. E a forma mais fraca de
 * controle que existe, e a pergunta e quanto ela consegue com tao pouco.
 *
 * <p>A politica que diz sim a tudo e a referencia: e ela que mostra onde o impasse mora.
 */
public interface Politica {

    String nome();

    /** Verdadeiro quando o pedido pode ser atendido agora. */
    boolean permite(Sistema.Estado estado, Sistema.Pedido pedido);

    /** Qual das quatro condicoes de Coffman esta politica quebra, ou "nenhuma". */
    String condicaoQuebrada();

    static List<Politica> todas() {
        return List.of(new SemControle(), new Banqueiro(), new OrdemTotal(),
                new OrdemNoPrograma(), new TudoDeUmaVez(), new UmPorVez());
    }
}
