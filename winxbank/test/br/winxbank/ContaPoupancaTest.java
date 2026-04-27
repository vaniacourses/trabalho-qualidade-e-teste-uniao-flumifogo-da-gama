package br.winxbank;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários da ContaPoupanca")
class ContaPoupancaTest {

    private ContaPoupanca contaPoupanca;
    private Cartao cartao;

    private static final int NUMERO_CONTA = 5001;
    private static final double SALDO_INICIAL = 1000.0;
    private static final double DIVIDA = 0.0;
    private static final int NUMERO_CARTAO = 54321;
    private static final int CSV = 987;
    private static final double RENDIMENTO_MENSAL = 0.8;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0;
        Banco.getInstancia().despesas = 0;
        cartao = new Cartao(NUMERO_CARTAO, CSV);
        contaPoupanca = new ContaPoupanca(NUMERO_CONTA, SALDO_INICIAL, cartao, DIVIDA);
    }

    @Test
    @DisplayName("acrescentarRendimento: deve aumentar o saldo")
    void testAcrescentarRendimento_DeveAumentarSaldo() {
        double saldoAntes = contaPoupanca.getSaldo();
        contaPoupanca.acrescentarRendimento();

        double saldoEsperado = saldoAntes + (saldoAntes / RENDIMENTO_MENSAL);
        assertEquals(saldoEsperado, contaPoupanca.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("acrescentarRendimento: deve criar movimentação de tipo ENTRADA")
    void testAcrescentarRendimento_DeveRegistrarMovimentacao() {
        contaPoupanca.acrescentarRendimento();

        assertEquals(1, contaPoupanca.getInformeRendimento().size());
        assertEquals(
            Movimentacao.TipoDaMovimentacao.ENTRADA,
            contaPoupanca.getInformeRendimento().get(0).getTipoDaMovimentacao()
        );
    }

    @Test
    @DisplayName("setInformeRendimento: deve adicionar movimentação à lista")
    void testSetInformeRendimento_DeveAdicionarMovimentacao() {
        Movimentacao mov = new Movimentacao(100.0, Movimentacao.TipoDaMovimentacao.ENTRADA);
        contaPoupanca.setInformeRendimento(mov);

        assertEquals(1, contaPoupanca.getInformeRendimento().size());
    }

    @Test
    @DisplayName("getInformeRendimento: deve retornar lista vazia inicialmente")
    void testGetInformeRendimento_ListaVaziaNoInicio() {
        assertTrue(contaPoupanca.getInformeRendimento().isEmpty());
    }

    @Test
    @DisplayName("getTipoDaConta: deve retornar 'Poupanca'")
    void testGetTipoDaConta_DeveRetornarPoupanca() {
        assertEquals("Poupanca", contaPoupanca.getTipoDaConta());
    }

    @Test
    @DisplayName("movimentacaoBancaria: deve registrar despesas no banco")
    void testMovimentacaoBancaria_DeveRegistrarDespesas() {
        double despesasAntes = Banco.getInstancia().getDespesas();
        contaPoupanca.movimentacaoBancaria(100.0);

        assertEquals(despesasAntes + 100.0, Banco.getInstancia().getDespesas(), 0.001);
    }

    @Test
    @DisplayName("BORDA - acrescentarRendimento com saldo ZERO")
    void testBorda_AcrescentarRendimento_SaldoZero() {
        ContaPoupanca contaZero = new ContaPoupanca(5003, 0.0, cartao, 0.0);
        contaZero.acrescentarRendimento();

        assertEquals(0.0, contaZero.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("BORDA - acrescentarRendimento com saldo MUITO GRANDE")
    void testBorda_AcrescentarRendimento_SaldoGrande() {
        ContaPoupanca contaGrande = new ContaPoupanca(5004, 1000000.0, cartao, 0.0);
        double saldoAntes = contaGrande.getSaldo();

        contaGrande.acrescentarRendimento();

        double saldoEsperado = saldoAntes + (saldoAntes / RENDIMENTO_MENSAL);
        assertEquals(saldoEsperado, contaGrande.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("BORDA - acrescentarRendimento com saldo NEGATIVO")
    void testBorda_AcrescentarRendimento_SaldoNegativo() {
        ContaPoupanca contaNegativa = new ContaPoupanca(5006, -500.0, cartao, 0.0);
        double saldoAntes = contaNegativa.getSaldo();

        contaNegativa.acrescentarRendimento();

        // Saldo não deve mudar (ou aumentar, nunca ficar mais negativo)
        assertEquals(saldoAntes, contaNegativa.getSaldo(), 0.001,
                "Saldo negativo não deveria receber rendimento!");
    }

    @Test
    @DisplayName("BORDA - movimentacaoBancaria com valor ZERO")
    void testBorda_MovimentacaoBancaria_ValorZero() {
        double despesasAntes = Banco.getInstancia().getDespesas();
        contaPoupanca.movimentacaoBancaria(0.0);

        assertEquals(despesasAntes, Banco.getInstancia().getDespesas(), 0.001);
    }

    @Test
    @DisplayName("Construtor com saldo ZERO")
    void testBorda_Construtor_SaldoZero() {
        ContaPoupanca contaZero = new ContaPoupanca(9999, 0.0, cartao, 0.0);
        assertEquals(0.0, contaZero.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("Construtor com dívida NEGATIVA")
    void testBorda_Construtor_DividaNegativa() {
        ContaPoupanca contaNegativa = new ContaPoupanca(9998, 1000.0, cartao, -500.0);
        assertEquals(-500.0, contaNegativa.getDividaDeEmprestimo(), 0.001);
    }

    @Test
    @DisplayName("Múltiplas chamadas acumulam despesas")
    void testBorda_MultiplosAcumuloDespesas() {
        contaPoupanca.movimentacaoBancaria(50.0);
        contaPoupanca.movimentacaoBancaria(75.0);
        contaPoupanca.movimentacaoBancaria(100.0);

        assertEquals(225.0, Banco.getInstancia().getDespesas(), 0.001);
    }

    @Test
    @DisplayName("AcrescentarRendimento consecutivos")
    void testBorda_AcrescentarRendimentoConsecutivos() {
        contaPoupanca.acrescentarRendimento();
        contaPoupanca.acrescentarRendimento();
        contaPoupanca.acrescentarRendimento();

        assertEquals(3, contaPoupanca.getInformeRendimento().size());
    }
}
