package br.winxbank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;

import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da ContaCorrente")
class ContaCorrenteTest {

    private ContaCorrente contaCorrente;
    private Cartao cartaoDebito;
    private CartaoCredito cartaoCredito;

    private static final int    NUMERO_CONTA    = 1001;
    private static final double SALDO_INICIAL   = 1000.0;
    private static final double DIVIDA          = 0.0;
    private static final int    NUMERO_CARTAO   = 12345;
    private static final int    CSV             = 123;

    @BeforeEach
    void setUp() {
        // Resetar o singleton do Banco antes de cada teste
        Banco.getInstancia().receitas = 0;
        Banco.getInstancia().despesas = 0;

        cartaoDebito  = new Cartao(NUMERO_CARTAO, CSV);
        cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV);

        contaCorrente = new ContaCorrente(
            NUMERO_CONTA,
            SALDO_INICIAL,
            cartaoDebito,
            DIVIDA,
            cartaoCredito
        );
    }

    // ─── pagarFatura ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("pagarFatura: deve deduzir o valor do saldo")
    void pagarFatura_deveReduzirSaldo() {
        contaCorrente.pagarFatura(200.0);

        assertEquals(800.0, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("pagarFatura: deve reduzir a fatura do cartão de crédito")
    void pagarFatura_deveReduzirFaturaDoCartaoCredito() {
        cartaoCredito.setFatura(300.0); // simula uma compra no crédito
        contaCorrente.pagarFatura(300.0);

        assertEquals(0.0, cartaoCredito.getFatura(), 0.001);
    }

    @Test
    @DisplayName("pagarFatura: valor zero não altera o saldo")
    void pagarFatura_valorZeroNaoAlteraSaldo() {
        contaCorrente.pagarFatura(0.0);

        assertEquals(SALDO_INICIAL, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("pagarFatura: valor maior que o saldo deixa saldo negativo")
    void pagarFatura_valorMaiorQueOSaldoResultaEmSaldoNegativo() {
        contaCorrente.pagarFatura(1500.0);

        assertEquals(-500.0, contaCorrente.getSaldo(), 0.001);
    }

    // ─── descontarTaxa ────────────────────────────────────────────────────────

    @Test
    @DisplayName("descontarTaxa: deve deduzir a taxa de manutenção do saldo")
    void descontarTaxa_deveReduzirSaldoPelaTaxa() {
        double taxaEsperada = Conta.taxaManutencaoConta;

        contaCorrente.descontarTaxa();

        assertEquals(SALDO_INICIAL - taxaEsperada, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("descontarTaxa: deve registrar movimentação no extrato")
    void descontarTaxa_deveAdicionarMovimentacaoNoExtrato() {
        contaCorrente.descontarTaxa();

        assertFalse(contaCorrente.getExtrato().isEmpty());
    }

    @Test
    @DisplayName("descontarTaxa: deve repassar a taxa às receitas do banco")
    void descontarTaxa_deveAumentarReceitasDoBanco() {
        double receitasAntes = Banco.getInstancia().getReceitas();

        contaCorrente.descontarTaxa();

        assertTrue(Banco.getInstancia().getReceitas() > receitasAntes);
    }

    // ─── movimentacaoBancaria ─────────────────────────────────────────────────

    @Test
    @DisplayName("movimentacaoBancaria: deve aumentar as receitas do banco")
    void movimentacaoBancaria_deveAumentarReceitasDoBanco() {
        contaCorrente.movimentacaoBancaria(500.0);

        assertEquals(500.0, Banco.getInstancia().getReceitas(), 0.001);
    }

    @Test
    @DisplayName("movimentacaoBancaria: valor negativo não altera receitas")
    void movimentacaoBancaria_valorNegativoNaoAlteraReceitas() {
        contaCorrente.movimentacaoBancaria(-100.0);

        assertEquals(0.0, Banco.getInstancia().getReceitas(), 0.001);
    }

    // ─── getCartaoCredito ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getCartaoCredito: deve retornar o cartão de crédito associado")
    void getCartaoCredito_deveRetornarOCartaoCorreto() {
        assertSame(cartaoCredito, contaCorrente.getCartaoCredito());
    }

    // ─── getTipoDaConta ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getTipoDaConta: deve retornar 'Corrente'")
    void getTipoDaConta_deveRetornarStringCorreta() {
        assertEquals("Corrente", contaCorrente.getTipoDaConta());
    }

    // ─── construtor / getters herdados ────────────────────────────────────────

    @Test
    @DisplayName("Construtor: deve inicializar o número da conta corretamente")
    void construtor_deveInicializarNumeroDaConta() {
        assertEquals(NUMERO_CONTA, contaCorrente.getNumeroConta());
    }

    @Test
    @DisplayName("Construtor: deve inicializar o saldo corretamente")
    void construtor_deveInicializarSaldo() {
        assertEquals(SALDO_INICIAL, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("Construtor: deve inicializar a dívida de empréstimo corretamente")
    void construtor_deveInicializarDividaDeEmprestimo() {
        assertEquals(DIVIDA, contaCorrente.getDividaDeEmprestimo(), 0.001);
    }

    // ─── depositar / sacar / pix ──────────────────────────────────────────────

    @Test
    @DisplayName("depositar: deve aumentar o saldo corretamente")
    void depositar_deveAumentarOSaldo() {
        contaCorrente.depositar(500.0);

        assertEquals(1500.0, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("sacar: deve reduzir o saldo corretamente")
    void sacar_deveReduzirOSaldo() {
        contaCorrente.sacar(300.0);

        assertEquals(700.0, contaCorrente.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("fazerPix: deve transferir o valor para outra conta")
    void fazerPix_deveTransferirValorParaOutraConta() {
        ContaCorrente contaDestino = new ContaCorrente(
            2002, 0.0, new Cartao(99999, 999), 0.0, new CartaoCredito(99999, 999)
        );

        contaCorrente.fazerPix(contaDestino, 400.0);

        assertEquals(400.0, contaDestino.getSaldo(), 0.001);
    }
}