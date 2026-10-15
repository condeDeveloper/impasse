package br.com.conde.impasse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** As pecas, uma a uma, antes de qualquer medida. */
class ImpasseTest {

    // ───────────────────── o sorteio ─────────────────────

    @Nested
    @DisplayName("o sorteio da casa")
    class OSorteio {

        private long estado;

        private int proximoSemDeslocamento() {
            estado = estado * 6364136223846793005L + 1442695040888963407L;

            return (int) estado;
        }

        private int ateSemDeslocamento(int quantos) {
            return Math.floorMod(proximoSemDeslocamento(), quantos);
        }

        @Test
        @DisplayName("o sorteio da os mesmos numeros em qualquer maquina")
        void oSorteioEhFixo() {
            Sorteio sorteio = new Sorteio(1);

            assertThat(List.of(sorteio.proximo(), sorteio.proximo(), sorteio.proximo()))
                    .containsExactly(1817669548, -2107078989, -1510284903);
        }

        /**
         * O SORTEIO DE SISTEMAS USA {@code ate(2)} E {@code ate(3)}.
         *
         * <p>O primeiro e o bit zero, que alterna a cada passo. Com os bits de baixo, o
         * total de cada tipo sai 1, 2, 1, 2, para sempre, e nenhum sistema sorteado teria
         * dois tipos com o mesmo total.
         */
        @Test
        @DisplayName("sem o deslocamento, o total de cada tipo so alterna")
        void semODeslocamentoOTotalAlterna() {
            estado = Medidas.SEMENTE;

            StringBuilder saida = new StringBuilder();

            for (int i = 0; i < 16; i++) saida.append(1 + ateSemDeslocamento(2));

            assertThat(saida.toString()).isEqualTo("1212121212121212");

            Sorteio sorteio = new Sorteio(Medidas.SEMENTE);

            StringBuilder bom = new StringBuilder();

            for (int i = 0; i < 16; i++) bom.append(1 + sorteio.ate(2));

            assertThat(bom.toString()).isNotEqualTo("1212121212121212");
        }

        /**
         * E O QUE ISSO FARIA COM O LOTE.
         *
         * <p>Os sistemas sorteados teriam poucas formas distintas, repetidas centenas de
         * vezes, e a conta de quantos deles dao impasse estaria medindo essas poucas formas.
         */
        @Test
        @DisplayName("e com isso o lote inteiro teria pouquissimas formas distintas")
        void semODeslocamentoOLoteSeRepete() {
            estado = Medidas.SEMENTE;

            Set<String> semDeslocamento = new LinkedHashSet<>();

            for (int numero = 0; numero < Medidas.LOTE; numero++)
                semDeslocamento.add(formaSemDeslocamento());

            Set<String> comDeslocamento = new LinkedHashSet<>();

            for (Sistema sistema : Medidas.lote()) comDeslocamento.add(forma(sistema));

            assertThat(semDeslocamento.size())
                    .as("o sorteio estragado se repete muito mais")
                    .isLessThan(comDeslocamento.size() / 2);
        }

        private String formaSemDeslocamento() {
            int[] total = new int[Sistemas.TIPOS];

            for (int tipo = 0; tipo < Sistemas.TIPOS; tipo++)
                total[tipo] = 1 + ateSemDeslocamento(2);

            StringBuilder saida = new StringBuilder();

            for (int quanto : total) saida.append(quanto).append(':');

            for (int processo = 0; processo < Sistemas.PROCESSOS; processo++)
                for (int tipo = 0; tipo < Sistemas.TIPOS; tipo++)
                    saida.append(ateSemDeslocamento(total[tipo] + 1)).append(',');

            return saida.toString();
        }

        private String forma(Sistema sistema) {
            StringBuilder saida = new StringBuilder();

            for (int quanto : sistema.total()) saida.append(quanto).append(':');

            for (int[] linha : sistema.maximo())
                for (int quanto : linha) saida.append(quanto).append(',');

            return saida.toString();
        }
    }

    // ───────────────────── o sistema ─────────────────────

    @Nested
    @DisplayName("o sistema e o estado")
    class OSistema {

        @Test
        @DisplayName("no comeco ninguem tem nada e tudo esta disponivel")
        void noComecoTudoEstaLivre() {
            Sistema sistema = Sistemas.banco();

            Sistema.Estado estado = sistema.inicial();

            assertThat(estado.disponivel()).containsExactly(4);
            assertThat(estado.todosTerminaram()).isFalse();
            assertThat(estado.impasse()).isFalse();
        }

        @Test
        @DisplayName("o disponivel e o total menos o que esta nas maos de alguem")
        void oDisponivelFecha() {
            Sistema sistema = Sistemas.duasDeCada();

            Sistema.Estado estado = sistema.inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0))
                    .depoisDoPedido(new Sistema.Pedido(1, 1));

            assertThat(estado.disponivel()).containsExactly(1, 1);
            assertThat(estado.tem(0, 0)).isEqualTo(1);
            assertThat(estado.aindaVaiPedir(0, 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("quem ja tem o maximo que declarou pode terminar")
        void quemCompletouTermina() {
            Sistema sistema = Sistemas.doisGarfos();

            Sistema.Estado estado = sistema.inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0))
                    .depoisDoPedido(new Sistema.Pedido(0, 1));

            assertThat(estado.completo(0)).isTrue();
            assertThat(estado.prontosParaTerminar()).containsExactly(0);

            Sistema.Estado depois = estado.depoisDeTerminar(0);

            assertThat(depois.terminou(0)).isTrue();
            assertThat(depois.disponivel()).containsExactly(1, 1);
        }

        /**
         * O IMPASSE CLASSICO, MONTADO A MAO.
         *
         * <p>Dois processos, dois recursos, um de cada. Cada um pegou um e precisa do outro.
         * Ninguem termina, ninguem consegue nada, e esperar nao resolve.
         */
        @Test
        @DisplayName("um de cada lado da mesa: o impasse dos dois garfos")
        void oImpasseDosDoisGarfos() {
            Sistema.Estado estado = Sistemas.doisGarfos().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0))
                    .depoisDoPedido(new Sistema.Pedido(1, 1));

            assertThat(estado.impasse()).isTrue();
            assertThat(estado.travados()).containsExactly(0, 1);
            assertThat(estado.pedidosPossiveis()).isEmpty();
            assertThat(estado.prontosParaTerminar()).isEmpty();
        }

        @Test
        @DisplayName("com dois de cada, o mesmo estado nao e impasse nenhum")
        void comDoisDeCadaNaoTrava() {
            Sistema.Estado estado = Sistemas.doisGarfosEmDobro().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0))
                    .depoisDoPedido(new Sistema.Pedido(1, 1));

            assertThat(estado.impasse()).isFalse();
            assertThat(estado.pedidosPossiveis()).isNotEmpty();
        }
    }

    // ───────────────────── a varredura ─────────────────────

    @Nested
    @DisplayName("a varredura, que e o juiz")
    class AVarredura {

        @Test
        @DisplayName("ela acha o impasse dos dois garfos sem ninguem contar onde")
        void elaAchaOImpasse() {
            Varredura.Resultado resultado =
                    Varredura.rodar(Sistemas.doisGarfos(), new SemControle());

            assertThat(resultado.impasses()).isEqualTo(2);
            assertThat(resultado.terminaisBons()).isEqualTo(1);
            assertThat(resultado.primeiroImpasse()).isNotEmpty();
        }

        @Test
        @DisplayName("todo estado alcancavel aparece uma vez so")
        void cadaEstadoUmaVez() {
            List<Sistema.Estado> estados = Varredura.todosOsEstados(Sistemas.rodaDeTres());

            Set<String> chaves = new LinkedHashSet<>();

            for (Sistema.Estado estado : estados) chaves.add(estado.chave());

            assertThat(chaves).hasSize(estados.size());
        }

        @Test
        @DisplayName("sem politica, a varredura sempre chega ao fim bom")
        void semPoliticaExisteCaminhoBom() {
            for (Sistema sistema : Sistemas.aMao())
                assertThat(Varredura.rodar(sistema, new SemControle()).terminaisBons())
                        .as(sistema.nome())
                        .isPositive();
        }

        @Test
        @DisplayName("sistema grande demais e recusado em vez de travar a maquina")
        void oEspacoTemTeto() {
            int[] total = new int[6];
            int[][] maximo = new int[6][6];

            for (int i = 0; i < 6; i++) {
                total[i] = 6;

                for (int j = 0; j < 6; j++) maximo[i][j] = 6;
            }

            Sistema enorme = new Sistema("enorme", total, maximo);

            assertThatThrownBy(() -> Varredura.rodar(enorme, new SemControle()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ───────────────────── as politicas ─────────────────────

    @Nested
    @DisplayName("as politicas")
    class AsPoliticas {

        @Test
        @DisplayName("o banqueiro recusa o pedido que deixaria o estado inseguro")
        void oBanqueiroRecusa() {
            Sistema.Estado estado = Sistemas.doisGarfos().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0));

            assertThat(new Banqueiro().permite(estado, new Sistema.Pedido(1, 1)))
                    .as("dar o outro garfo ao segundo trava os dois")
                    .isFalse();

            assertThat(new Banqueiro().permite(estado, new Sistema.Pedido(0, 1)))
                    .as("completar o primeiro e seguro")
                    .isTrue();
        }

        @Test
        @DisplayName("o estado inicial e sempre seguro, e o de impasse nunca e")
        void seguroNoComecoInseguroNoImpasse() {
            for (Sistema sistema : Sistemas.aMao()) {
                assertThat(Banqueiro.seguro(sistema.inicial()))
                        .as(sistema.nome())
                        .isTrue();

                for (Sistema.Estado estado : Varredura.todosOsEstados(sistema))
                    if (estado.impasse())
                        assertThat(Banqueiro.seguro(estado))
                                .as("impasse em " + sistema.nome())
                                .isFalse();
            }
        }

        @Test
        @DisplayName("a ordem total recusa pedir para tras")
        void aOrdemTotalRecusaParaTras() {
            Sistema.Estado estado = Sistemas.duasDeCada().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 1));

            assertThat(new OrdemTotal().permite(estado, new Sistema.Pedido(0, 0))).isFalse();
            assertThat(new OrdemTotal().permite(estado, new Sistema.Pedido(1, 0))).isTrue();
        }

        @Test
        @DisplayName("a ordem no programa so deixa pedir o menor que ainda falta")
        void aOrdemNoProgramaPedeOMenor() {
            Sistema.Estado estado = Sistemas.duasDeCada().inicial();

            assertThat(new OrdemNoPrograma().permite(estado, new Sistema.Pedido(0, 1)))
                    .as("ele ainda vai precisar do tipo 0, entao nao comeca pelo 1")
                    .isFalse();

            assertThat(new OrdemNoPrograma().permite(estado, new Sistema.Pedido(0, 0)))
                    .isTrue();
        }

        @Test
        @DisplayName("o um por vez nao deixa dois processos segurando recurso ao mesmo tempo")
        void oUmPorVezSerializa() {
            Sistema.Estado estado = Sistemas.duasDeCada().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0));

            assertThat(new UmPorVez().permite(estado, new Sistema.Pedido(1, 1))).isFalse();
            assertThat(new UmPorVez().permite(estado, new Sistema.Pedido(0, 1))).isTrue();
        }
    }

    // ───────────────────── os detectores ─────────────────────

    @Nested
    @DisplayName("os detectores")
    class OsDetectores {

        @Test
        @DisplayName("o ciclo acusa o impasse dos dois garfos")
        void oCicloAcusaOImpasse() {
            Sistema.Estado estado = Sistemas.doisGarfos().inicial()
                    .depoisDoPedido(new Sistema.Pedido(0, 0))
                    .depoisDoPedido(new Sistema.Pedido(1, 1));

            assertThat(new PorCiclo().acusa(estado)).isTrue();
        }

        /**
         * O MESMO CICLO, SEM IMPASSE NENHUM.
         *
         * <p>Com duas unidades de cada tipo, os dois processos esperam um pelo outro no
         * grafo e mesmo assim ha unidade sobrando para os dois andarem. O ciclo continua
         * sendo necessario e deixa de ser suficiente.
         */
        @Test
        @DisplayName("e acusa tambem um estado que anda normalmente, com duas unidades")
        void oCicloAcusaQuemAnda() {
            List<Sistema.Estado> acusadosQueAndam = new ArrayList<>();

            for (Sistema.Estado estado : Varredura.todosOsEstados(Sistemas.duasDeCada()))
                if (new PorCiclo().acusa(estado) && !estado.impasse())
                    acusadosQueAndam.add(estado);

            assertThat(acusadosQueAndam).isNotEmpty();
        }

        @Test
        @DisplayName("nenhum dos dois deixa um impasse passar, em nenhum estado")
        void nenhumDeixaPassar() {
            for (Detector detector : Detector.todos())
                for (Sistema sistema : Sistemas.aMao())
                    for (Sistema.Estado estado : Varredura.todosOsEstados(sistema))
                        if (estado.impasse())
                            assertThat(detector.acusa(estado))
                                    .as(detector.nome() + " em " + sistema.nome())
                                    .isTrue();
        }
    }
}
