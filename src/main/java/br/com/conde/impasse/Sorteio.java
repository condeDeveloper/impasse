package br.com.conde.impasse;

/**
 * O SORTEIO DA CASA, escrito aqui em vez de usar o da biblioteca.
 *
 * <p>E um gerador congruente linear com as constantes de Knuth. A razao de ser proprio e
 * medida: o teste roda com semente fixa em tres sistemas operacionais e precisa dar o mesmo
 * numero nos tres, e a implementacao da biblioteca nao e contrato entre versoes.
 *
 * <h2>O deslocamento de 32</h2>
 *
 * <p>O estado tem 64 bits e o que sai sao os 32 DE CIMA. Nos bits de baixo, o bit zero
 * alterna a cada passo e os dois ultimos tem periodo quatro, e o sorteio de sistemas usa
 * {@code ate(2)} para o total de cada tipo e {@code ate(4)} para o maximo de cada processo.
 *
 * <p>Com os bits de baixo, os totais sairiam alternando 1, 2, 1, 2 e os maximos girariam em
 * quatro valores fixos: todo sistema sorteado teria a MESMA forma, so com os papeis
 * trocados. A tabela diria que o impasse e raro ou comum conforme a sorte de uma forma so.
 */
public final class Sorteio {

    private long estado;

    public Sorteio(long semente) {
        this.estado = semente;
    }

    /** Um inteiro qualquer, dos 32 bits de cima do estado. */
    public int proximo() {
        estado = estado * 6364136223846793005L + 1442695040888963407L;

        return (int) (estado >>> 32);
    }

    /** Um valor de 0 ate quantos menos um. */
    public int ate(int quantos) {
        return Math.floorMod(proximo(), quantos);
    }
}
