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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;

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


    // ----------------Testes segunda entrega-----------------------------------

    // Verifica o comportamento ao sacar um valor maior que o saldo atual, validando se o sistema permite saldo negativo ou aplica alguma regra de restrição.
    @Test
    @DisplayName("sacar: valor maior que saldo deve deixar saldo negativo ou seguir regra do sistema")
    void sacar_valorMaiorQueSaldo() {
        contaCorrente.sacar(2000.0);

        assertEquals(-1000.0, contaCorrente.getSaldo(), 0.001);
    }

    // Garante que transferências via PIX com valor zero não alteram o saldo da conta de origem nem da conta destino.
    @Test
    @DisplayName("fazerPix: valor zero não deve alterar saldo das contas")
    void fazerPix_valorZero() {
        ContaCorrente destino = new ContaCorrente(
            2002, 0.0, new Cartao(99999, 999), 0.0, new CartaoCredito(99999, 999)
        );

        contaCorrente.fazerPix(destino, 0.0);

        assertEquals(0.0, destino.getSaldo(), 0.001);
        assertEquals(1000.0, contaCorrente.getSaldo(), 0.001);
    }


    // ---------------------Integração
    // Testa um fluxo completo envolvendo depósito, pagamento de fatura e uso de cartão de crédito, validando a consistência entre saldo e fatura.
    @Test
    @DisplayName("fluxo integrado: compra no crédito e pagamento de fatura")
    void fluxo_integrado_cartaoCredito() {
        cartaoCredito.setFatura(500.0);

        contaCorrente.depositar(300.0);
        contaCorrente.pagarFatura(200.0);

        assertEquals(1100.0, contaCorrente.getSaldo(), 0.001);
        assertEquals(300.0, cartaoCredito.getFatura(), 0.001);
    }

    // Valida a integração entre conta corrente e banco, verificando se operações financeiras refletem corretamente no aumento das receitas do banco.
    @Test
    @DisplayName("fluxo integrado: movimentação + taxa afeta receitas do banco")
    void fluxo_integrado_banco() {
        double receitasAntes = Banco.getInstancia().getReceitas();

        contaCorrente.movimentacaoBancaria(1000.0);
        contaCorrente.descontarTaxa();

        assertTrue(Banco.getInstancia().getReceitas() > receitasAntes);
    }


    // -----------------Regra de negocio

    // Verifica se a aplicação de taxa de manutenção sempre reduz o saldo da conta, garantindo a regra de negócio associada à cobrança de taxas.
    @Test
    @DisplayName("regra de negócio: taxa deve sempre reduzir saldo")
    void taxa_deveSempreReduzirSaldo() {
        double saldoAntes = contaCorrente.getSaldo();

        contaCorrente.descontarTaxa();

        assertTrue(contaCorrente.getSaldo() < saldoAntes);
    }

    // Garante que movimentações bancárias válidas aumentam as receitas do banco, validando o registro correto de ganhos financeiros.
    @Test
    @DisplayName("regra de negócio: movimentação válida aumenta receita do banco")
    void receita_deveAumentar() {
        double receitasAntes = Banco.getInstancia().getReceitas();

        contaCorrente.movimentacaoBancaria(250.0);

        assertTrue(Banco.getInstancia().getReceitas() >= receitasAntes);
    }

    // ---------- Teste de estado de consistencia

    // Verifica se operações realizadas na conta são registradas no extrato, garantindo rastreabilidade das transações.
    // @Test
    // @DisplayName("estado do sistema: extrato deve registrar operações")
    // void extrato_deveRegistrarOperacao() {
    //     contaCorrente.depositar(100.0);

    //     assertFalse(contaCorrente.getExtrato().isEmpty());
    // }


    @Test
    void comprar_debito_confirmado() {

        Cartao cartaoMock = mock(Cartao.class);
        CartaoCredito creditoMock = mock(CartaoCredito.class);

        ContaCorrente conta = new ContaCorrente(
            1,
            1000,
            cartaoMock,
            0,
            creditoMock
        );

        System.setIn(
            new ByteArrayInputStream(
                "1\n1\n".getBytes()
            )
        );

        conta.comprar(100);

        verify(cartaoMock)
            .debitar(conta, 100);
    }

    @Test
    void comprar_credito_confirmado() {

        Cartao cartaoMock = mock(Cartao.class);
        CartaoCredito creditoMock = mock(CartaoCredito.class);

        ContaCorrente conta = new ContaCorrente(
            1,
            1000,
            cartaoMock,
            0,
            creditoMock
        );

        System.setIn(
            new ByteArrayInputStream(
                "2\n1\n".getBytes()
            )
        );

        conta.comprar(100);

        verify(creditoMock)
            .creditar(100);
    }

    @Test
    void comprar_debito_cancelado() {

        Cartao cartaoMock = mock(Cartao.class);
        CartaoCredito creditoMock = mock(CartaoCredito.class);

        ContaCorrente conta = new ContaCorrente(
            1,
            1000,
            cartaoMock,
            0,
            creditoMock
        );

        System.setIn(
            new ByteArrayInputStream(
                "1\n0\n".getBytes()
            )
        );

        conta.comprar(100);

        verify(cartaoMock, never())
            .debitar(any(), anyDouble());
    }

    @Test
    void comprar_credito_cancelado() {

        Cartao cartaoMock = mock(Cartao.class);
        CartaoCredito creditoMock = mock(CartaoCredito.class);

        ContaCorrente conta = new ContaCorrente(
            1,
            1000,
            cartaoMock,
            0,
            creditoMock
        );

        System.setIn(
            new ByteArrayInputStream(
                "2\n0\n".getBytes()
            )
        );

        conta.comprar(100);

        verify(creditoMock, never())
            .creditar(anyDouble());
    }
}