package br.com.conde.impasse;

/**
 * POR INSEGURANCA: usa o criterio do banqueiro como se fosse deteccao.
 *
 * <p>Este detector acusa todo estado inseguro, e inseguro quer dizer "nao existe ordem em
 * que todos terminem SE cada um pedir o maximo que declarou".
 *
 * <p>E a confusao mais comum do assunto, e ela e facil de cometer porque o criterio do
 * banqueiro e de fato uma garantia: estado seguro nunca vai dar impasse. O contrario nao
 * vale, e a tabela da seguranca mostra o tamanho da diferenca.
 */
public final class PorInseguranca implements Detector {

    @Override
    public String nome() {
        return "estado inseguro";
    }

    @Override
    public boolean acusa(Sistema.Estado estado) {
        return !Banqueiro.seguro(estado);
    }
}
