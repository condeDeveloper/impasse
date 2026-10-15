package br.com.conde.impasse;

import java.util.List;

/**
 * UM DETECTOR: olhando este estado, ha impasse?
 *
 * <p>Detectar e diferente de prevenir. A politica decide antes e paga o preco de recusar; o
 * detector deixa o sistema andar e pergunta depois, para quebrar o impasse matando alguem ou
 * tirando recurso de alguma maos.
 *
 * <p>Os dois detectores daqui sao os dois que qualquer um escreve, e o juiz confere os dois
 * contra a definicao em TODOS os estados alcancaveis.
 */
public interface Detector {

    String nome();

    boolean acusa(Sistema.Estado estado);

    static List<Detector> todos() {
        return List.of(new PorCiclo(), new PorInseguranca());
    }
}
