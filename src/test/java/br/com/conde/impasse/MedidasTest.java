package br.com.conde.impasse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * TODO NUMERO DO README ESTA AQUI.
 *
 * <p>Texto nao quebra quando deixa de ser verdade. Teste quebra. Se alguma medida mudar,
 * este arquivo e a tabela do README caem juntos, e nenhum numero fica no texto sozinho.
 */
class MedidasTest {

    private static final List<Medidas.Correcao> CORRECAO = Medidas.correcao();
    private static final List<Medidas.Deteccao> DETECCAO = Medidas.deteccao();
    private static final Medidas.Seguranca SEGURANCA = Medidas.seguranca();
    private static final List<Medidas.UmTipoSo> UM_TIPO = Medidas.umTipoSo();

    // ───────────────────── 1. correcao ─────────────────────

    @Nested
    @DisplayName("tabela 1: nenhuma politica deixa chegar a um impasse?")
    class ACorrecao {

        @Test
        @DisplayName("sao 400 sistemas sorteados e 6 politicas")
        void oLote() {
            assertThat(Medidas.lote()).hasSize(400);
            assertThat(CORRECAO).hasSize(6);

            for (Medidas.Correcao linha : CORRECAO)
                assertThat(linha.sistemas()).isEqualTo(400);
        }

        @Test
        @DisplayName("sem controle, 150 dos 400 sistemas chegam a um impasse")
        void semControleCentoECinquenta() {
            assertThat(Medidas.correcao("sem controle").comImpasse()).isEqualTo(150);
            assertThat(Medidas.correcao("sem controle").estados()).isEqualTo(20689);
            assertThat(Medidas.correcao("sem controle").fracaoAtendida())
                    .isCloseTo(1.0, within(1e-9));
        }

        @Test
        @DisplayName("so o banqueiro e o um por vez previnem os 400")
        void soDuasPrevinem() {
            assertThat(CORRECAO.stream().filter(Medidas.Correcao::previne)
                    .map(Medidas.Correcao::politica).toList())
                    .containsExactly("banqueiro", "um por vez");
        }

        /**
         * A ORDEM TOTAL APLICADA COMO FILTRO NAO PREVINE NADA.
         *
         * <p>Ela recusa o pedido fora de ordem, e isso nao desfaz o recurso que o processo
         * ja pegou: quem agarrou o tipo 1 primeiro fica esperando pelo tipo 0 para sempre.
         * Os mesmos 150 sistemas travam, mais 273 que travam por causa dela.
         */
        @Test
        @DisplayName("a ordem total como filtro trava os mesmos 150, mais 273 por conta dela")
        void aOrdemTotalComoFiltroNaoPrevine() {
            assertThat(Medidas.correcao("ordem total").comImpasse()).isEqualTo(150);
            assertThat(Medidas.correcao("ordem total").comTravamentoDaPolitica())
                    .isEqualTo(273);

            assertThat(Medidas.correcao("ordem total").comImpasse())
                    .isEqualTo(Medidas.correcao("sem controle").comImpasse());
        }

        @Test
        @DisplayName("como regra do programa ela cai de 150 para 54, e nao chega a zero")
        void aOrdemNoProgramaMelhoraENaoResolve() {
            assertThat(Medidas.correcao("ordem no programa").comImpasse()).isEqualTo(54);
        }

        @Test
        @DisplayName("pedir tudo de uma vez sem atomicidade deixa 99 sistemas travarem")
        void tudoDeUmaVezNaoBasta() {
            assertThat(Medidas.correcao("tudo de uma vez").comImpasse()).isEqualTo(99);
        }

        @Test
        @DisplayName("nos sistemas escritos a mao, os dois garfos travam e os quatro nao")
        void osSistemasEscritosAMao() {
            assertThat(Medidas.aMao("dois garfos", "sem controle").impasses()).isEqualTo(2);
            assertThat(Medidas.aMao("garfos em dobro", "sem controle").impasses()).isZero();

            assertThat(Medidas.aMao("duas de cada", "sem controle").impasses()).isEqualTo(10);
            assertThat(Medidas.aMao("duas de cada", "tudo de uma vez").impasses()).isEqualTo(8);
            assertThat(Medidas.aMao("duas de cada", "banqueiro").impasses()).isZero();
        }

        @Test
        @DisplayName("o banco tem um tipo so e trava com todas as prevencoes por ordem")
        void oBancoTravaComOrdem() {
            assertThat(Medidas.aMao("banco", "ordem total").impasses()).isEqualTo(1);
            assertThat(Medidas.aMao("banco", "ordem no programa").impasses()).isEqualTo(1);
            assertThat(Medidas.aMao("banco", "tudo de uma vez").impasses()).isEqualTo(1);
            assertThat(Medidas.aMao("banco", "banqueiro").impasses()).isZero();
        }
    }

    // ───────────────────── 2. deteccao ─────────────────────

    @Nested
    @DisplayName("tabela 2: o detector de ciclo contra a definicao")
    class ADeteccao {

        /**
         * COM UMA UNIDADE DE CADA TIPO, O CICLO E EXATO.
         *
         * <p>Nos 94 estados alcancaveis dos sistemas de unidade unica ha 4 impasses, o
         * detector acusa exatamente 4, e nenhum dos dois erros aparece. E o teorema do livro,
         * medido em vez de citado.
         */
        @Test
        @DisplayName("com uma unidade de cada tipo, zero alarme falso e zero escapado")
        void comUmaUnidadeOCicloEhExato() {
            Medidas.Deteccao linha = Medidas.deteccao("ciclo no grafo", "uma unidade");

            assertThat(linha.estados()).isEqualTo(94);
            assertThat(linha.impasses()).isEqualTo(4);
            assertThat(linha.acusou()).isEqualTo(4);
            assertThat(linha.falsoPositivo()).isZero();
            assertThat(linha.falsoNegativo()).isZero();
        }

        @Test
        @DisplayName("com varias unidades ele acusa 15 onde ha 11, e nao deixa nenhum passar")
        void comVariasUnidadesEleAcusaDemais() {
            Medidas.Deteccao linha = Medidas.deteccao("ciclo no grafo", "varias unidades");

            assertThat(linha.estados()).isEqualTo(312);
            assertThat(linha.impasses()).isEqualTo(11);
            assertThat(linha.acusou()).isEqualTo(15);
            assertThat(linha.falsoPositivo()).isEqualTo(4);
            assertThat(linha.falsoNegativo()).isZero();
        }

        @Test
        @DisplayName("usar inseguranca como deteccao acusa quase o dobro: 22 onde ha 11")
        void aInsegurancaAcusaDemais() {
            Medidas.Deteccao linha = Medidas.deteccao("estado inseguro", "varias unidades");

            assertThat(linha.acusou()).isEqualTo(22);
            assertThat(linha.falsoPositivo()).isEqualTo(11);
            assertThat(linha.falsoNegativo()).isZero();
        }

        @Test
        @DisplayName("nenhum dos dois deixa um impasse passar, em nenhuma familia")
        void nenhumDeixaPassar() {
            for (Medidas.Deteccao linha : DETECCAO)
                assertThat(linha.falsoNegativo())
                        .as(linha.detector() + " em " + linha.familia())
                        .isZero();
        }
    }

    // ───────────────────── 3. custo ─────────────────────

    @Nested
    @DisplayName("tabela 3: quanto cada prevencao custa em concorrencia")
    class OCusto {

        @Test
        @DisplayName("o banqueiro atende 89,5% dos pedidos e o um por vez atende 58,4%")
        void oPrecoDasDuasQueFuncionam() {
            assertThat(Medidas.correcao("banqueiro").fracaoAtendida())
                    .isCloseTo(0.895, within(0.001));

            assertThat(Medidas.correcao("um por vez").fracaoAtendida())
                    .isCloseTo(0.584, within(0.001));
        }

        @Test
        @DisplayName("e o um por vez corta um terco dos estados alcancaveis")
        void oUmPorVezCortaOsEstados() {
            assertThat(Medidas.correcao("um por vez").estados()).isEqualTo(13792);

            assertThat(Medidas.correcao("um por vez").estados()
                    / (double) Medidas.correcao("sem controle").estados())
                    .isCloseTo(0.667, within(0.005));
        }

        /**
         * A POLITICA QUE RECUSA MAIS NAO E A QUE PREVINE MELHOR.
         *
         * <p>A ordem no programa recusa um quarto dos pedidos, mais do que o banqueiro
         * recusa, e ainda deixa 54 sistemas travarem. Recusar nao e a medida do que se ganha.
         */
        @Test
        @DisplayName("a ordem no programa recusa mais que o banqueiro e previne menos")
        void recusarMaisNaoEhPrevenirMais() {
            assertThat(Medidas.correcao("ordem no programa").fracaoAtendida())
                    .isLessThan(Medidas.correcao("banqueiro").fracaoAtendida());

            assertThat(Medidas.correcao("ordem no programa").comImpasse())
                    .isGreaterThan(Medidas.correcao("banqueiro").comImpasse());
        }
    }

    // ───────────────────── 4. seguranca ─────────────────────

    @Nested
    @DisplayName("tabela 4: estado seguro nao e estado sem impasse")
    class ASeguranca {

        @Test
        @DisplayName("sao 20.689 estados, com 814 impasses e 1.663 inseguros")
        void asContagens() {
            assertThat(SEGURANCA.estados()).isEqualTo(20689);
            assertThat(SEGURANCA.impasses()).isEqualTo(814);
            assertThat(SEGURANCA.inseguros()).isEqualTo(1663);
        }

        /**
         * A GARANTIA VALE NUM SENTIDO SO, E A MEDIDA MOSTRA OS DOIS.
         *
         * <p>Nenhum estado seguro da impasse: essa e a promessa, e ela se cumpre nos 20.689.
         * Mas 849 dos 1.663 estados inseguros, mais da metade, terminam bem, e o banqueiro
         * recusa todos eles do mesmo jeito.
         */
        @Test
        @DisplayName("nenhum estado seguro trava, e metade dos inseguros termina bem")
        void aGarantiaValeNumSentidoSo() {
            assertThat(SEGURANCA.segurosComImpasse()).isZero();

            assertThat(SEGURANCA.insegurosQueAndam()).isEqualTo(849);
            assertThat(SEGURANCA.fracaoDeAlarmeFalso()).isCloseTo(0.511, within(0.001));
        }

        @Test
        @DisplayName("todo estado de impasse e inseguro, que e a outra metade da garantia")
        void todoImpasseEhInseguro() {
            assertThat(SEGURANCA.inseguros()).isGreaterThanOrEqualTo(SEGURANCA.impasses());

            assertThat(SEGURANCA.inseguros() - SEGURANCA.insegurosQueAndam())
                    .isEqualTo(SEGURANCA.impasses());
        }
    }

    // ───────────────────── 5. um tipo so ─────────────────────

    @Nested
    @DisplayName("tabela 5: com um tipo so de recurso")
    class UmTipoSo {

        /**
         * COM UM TIPO SO, ORDENAR NAO TEM O QUE ORDENAR.
         *
         * <p>As duas politicas de ordem travam exatamente os mesmos 222 sistemas que a
         * politica que nao faz nada. A espera circular continua existindo, so que entre
         * unidades do mesmo tipo.
         */
        @Test
        @DisplayName("as duas ordens travam os mesmos 222 que o sem controle")
        void ordenarNaoAjudaComUmTipoSo() {
            assertThat(Medidas.umTipoSo("sem controle").comImpasse()).isEqualTo(222);
            assertThat(Medidas.umTipoSo("ordem total").comImpasse()).isEqualTo(222);
            assertThat(Medidas.umTipoSo("ordem no programa").comImpasse()).isEqualTo(222);
        }

        @Test
        @DisplayName("e o banqueiro e o um por vez continuam prevenindo os 400")
        void oBanqueiroContinuaPrevenindo() {
            assertThat(Medidas.umTipoSo("banqueiro").comImpasse()).isZero();
            assertThat(Medidas.umTipoSo("um por vez").comImpasse()).isZero();
        }

        @Test
        @DisplayName("sao 400 sistemas de um tipo so, com 3 processos cada")
        void oLoteDeUmTipo() {
            for (Medidas.UmTipoSo linha : UM_TIPO)
                assertThat(linha.sistemas()).isEqualTo(400);

            for (Sistema sistema : Medidas.comUmTipoSo()) {
                assertThat(sistema.tipos()).isEqualTo(1);
                assertThat(sistema.processos()).isEqualTo(3);
            }
        }
    }
}
