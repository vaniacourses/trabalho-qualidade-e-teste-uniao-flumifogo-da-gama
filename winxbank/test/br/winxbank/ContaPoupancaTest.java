package br.winxbank;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import br.winxbank.geradordedocumentos.ArquivoInformeRendimento;
import static org.mockito.Mockito.*;

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
        cartao = new Cartao(NUMERO_CARTAO, CSV);
        contaPoupanca = new ContaPoupanca(NUMERO_CONTA, SALDO_INICIAL, cartao, DIVIDA);
    }

    @Test
    @DisplayName("acrescentarRendimento: deve aumentar o saldo")
    void testAcrescentarRendimento_DeveAumentarSaldo() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            double saldoAntes = contaPoupanca.getSaldo();
            contaPoupanca.acrescentarRendimento();
            double saldoEsperado = saldoAntes + (saldoAntes / RENDIMENTO_MENSAL);
            assertEquals(saldoEsperado, contaPoupanca.getSaldo(), 0.001);
        }
    }

    @Test
    @DisplayName("acrescentarRendimento: deve criar movimentação de tipo ENTRADA")
    void testAcrescentarRendimento_DeveRegistrarMovimentacao() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            contaPoupanca.acrescentarRendimento();
            assertEquals(1, contaPoupanca.getInformeRendimento().size());
            assertEquals(
                    Movimentacao.TipoDaMovimentacao.ENTRADA,
                    contaPoupanca.getInformeRendimento().get(0).getTipoDaMovimentacao()
            );
        }
    }

    @Test
    @DisplayName("setInformeRendimento: deve adicionar movimentação à lista")
    void testSetInformeRendimento_DeveAdicionarMovimentacao() {
        Movimentacao mov = new Movimentacao(100.0, Movimentacao.TipoDaMovimentacao.ENTRADA);
        contaPoupanca.setInformeRendimento(mov);

        assertEquals(1, contaPoupanca.getInformeRendimento().size());
    }

    @Test
    @DisplayName("setInformeRendimento com NULL deve lançar exceção")
    void testValidacao_SetInformeRendimento_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            contaPoupanca.setInformeRendimento(null);
        }, "Deveria lançar exceção ao adicionar movimentação nula!");
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
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            contaPoupanca.movimentacaoBancaria(100.0);
            verify(bancoMockInstance).setDespesas(100.0);
        }
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
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            ContaPoupanca contaGrande = new ContaPoupanca(5004, 1000000.0, cartao, 0.0);
            double saldoAntes = contaGrande.getSaldo();
            contaGrande.acrescentarRendimento();
            double saldoEsperado = saldoAntes + (saldoAntes / RENDIMENTO_MENSAL);
            assertEquals(saldoEsperado, contaGrande.getSaldo(), 0.001);
        }
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
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            contaPoupanca.movimentacaoBancaria(0.0);
            verify(bancoMockInstance).setDespesas(0.0);
        }
    }

    @Test
    @DisplayName("movimentacaoBancaria com valor NEGATIVO (validação no Banco)")
    void testValidacao_MovimentacaoBancaria_ValorNegativo() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            contaPoupanca.movimentacaoBancaria(-100.0);
            verify(bancoMockInstance).setDespesas(-100.0);
        }
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
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            contaPoupanca.movimentacaoBancaria(50.0);
            contaPoupanca.movimentacaoBancaria(75.0);
            contaPoupanca.movimentacaoBancaria(100.0);
            verify(bancoMockInstance, times(3)).setDespesas(anyDouble());
            verify(bancoMockInstance).setDespesas(50.0);
            verify(bancoMockInstance).setDespesas(75.0);
            verify(bancoMockInstance).setDespesas(100.0);
        }
    }

    @Test
    @DisplayName("AcrescentarRendimento consecutivos")
    void testBorda_AcrescentarRendimentoConsecutivos() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            contaPoupanca.acrescentarRendimento();
            contaPoupanca.acrescentarRendimento();
            contaPoupanca.acrescentarRendimento();
            assertEquals(3, contaPoupanca.getInformeRendimento().size());
        }
    }

    @Test
    @DisplayName("acrescentarRendimento com saldo ZERO não deve chamar Banco")
    void testAcrescentarRendimento_NaoDeveChamarBanco_QuandoSaldoZero() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            ContaPoupanca contaZero = new ContaPoupanca(5003, 0.0, cartao, 0.0);
            contaZero.acrescentarRendimento();
            verify(bancoMockInstance, never()).setDespesas(anyDouble());
        }
    }

    @Test
    @DisplayName("acrescentarRendimento com saldo NEGATIVO não deve chamar Banco")
    void testAcrescentarRendimento_NaoDeveChamarBanco_QuandoSaldoNegativo() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            ContaPoupanca contaNegativa = new ContaPoupanca(5006, -500.0, cartao, 0.0);
            contaNegativa.acrescentarRendimento();
            verify(bancoMockInstance, never()).setDespesas(anyDouble());
        }
    }

    @Test
    @DisplayName("gerarInformeRendimento: deve chamar ArquivoInformeRendimento")
    void testGerarInformeRendimento_DeveChamarArquivo() throws Exception {
        try (MockedStatic<ArquivoInformeRendimento> arquivoMock = mockStatic(ArquivoInformeRendimento.class)) {
            ArquivoInformeRendimento arquivoMockInstance = mock(ArquivoInformeRendimento.class);
            arquivoMock.when(ArquivoInformeRendimento::getInstancia).thenReturn(arquivoMockInstance);
            contaPoupanca.gerarInformeRendimento();
            verify(arquivoMockInstance).gerarDocumento(contaPoupanca);
        }
    }

    @Test
    @DisplayName("acrescentarRendimento com saldo ZERO não deve registrar no informe")
    void testAcrescentarRendimento_NaoDeveRegistrarMovimentacao_QuandoSaldoZero() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            ContaPoupanca contaZero = new ContaPoupanca(5003, 0.0, cartao, 0.0);
            contaZero.acrescentarRendimento();
            assertTrue(contaZero.getInformeRendimento().isEmpty());
        }
    }

    @Test
    @DisplayName("acrescentarRendimento com saldo válido deve chamar movimentacaoBancaria")
    void testAcrescentarRendimento_DeveChamarMovimentacaoBancaria() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            Banco bancoMockInstance = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(bancoMockInstance);
            contaPoupanca.acrescentarRendimento();
            double rendimentoEsperado = SALDO_INICIAL / RENDIMENTO_MENSAL;
            verify(bancoMockInstance).setDespesas(rendimentoEsperado);
        }
    }

    @Test
    @DisplayName("comprar com confirmação (decisao=1) deve debitar o cartão")
    void testComprar_Confirmado_DeveDebitar() {
        double saldoAntes = contaPoupanca.getSaldo();
        contaPoupanca.executarCompra(150.0, 1);
        assertEquals(saldoAntes - 150.0, contaPoupanca.getSaldo(), 0.001);
    }
    @Test
    @DisplayName("comprar cancelado (decisao=2) não deve alterar saldo")
    void testComprar_Cancelado_NaoAlteraSaldo() {
        double saldoAntes = contaPoupanca.getSaldo();
        contaPoupanca.executarCompra(150.0, 2);
        assertEquals(saldoAntes, contaPoupanca.getSaldo(), 0.001);
    }

    @Test
    @DisplayName("comprar com entrada do usuário (1 - confirmar) deve debitar")
    void testComprar_ComInput_Confirmado() throws Exception {
        System.setIn(new java.io.ByteArrayInputStream("1\n".getBytes()));
        double saldoAntes = contaPoupanca.getSaldo();
        contaPoupanca.comprar(150.0);
        assertEquals(saldoAntes - 150.0, contaPoupanca.getSaldo(), 0.001);
        System.setIn(System.in);
    }
    @Test
    @DisplayName("comprar com entrada do usuário (2 - cancelar) não altera saldo")
    void testComprar_ComInput_Cancelado() throws Exception {
        System.setIn(new java.io.ByteArrayInputStream("2\n".getBytes()));
        double saldoAntes = contaPoupanca.getSaldo();
        contaPoupanca.comprar(150.0);
        assertEquals(saldoAntes, contaPoupanca.getSaldo(), 0.001);
        System.setIn(System.in);
    }

    @Test
    @DisplayName("acrescentarRendimento: movimentacao deve ter valor correto (diferença rendimento - saldo)")
    void testAcrescentarRendimento_ValorMovimentacaoCorreto() {
        try (MockedStatic<Banco> bancoMock = mockStatic(Banco.class)) {
            bancoMock.when(Banco::getInstancia).thenReturn(mock(Banco.class));
            contaPoupanca.acrescentarRendimento();
            double rendimentoEsperado = SALDO_INICIAL / RENDIMENTO_MENSAL;
            double valorMovimentacaoEsperado = rendimentoEsperado - SALDO_INICIAL;
            assertEquals(valorMovimentacaoEsperado,
                    contaPoupanca.getInformeRendimento().get(0).getDinheiroMovimentado(), 0.001);
        }
    }

    @Test
    @DisplayName("comprar confirmado deve imprimir mensagem de débito")
    void testComprar_ComInput_ImprimeMensagemDebito() throws Exception {
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));
        try {
            System.setIn(new java.io.ByteArrayInputStream("1\n".getBytes()));
            contaPoupanca.comprar(150.0);
            assertTrue(outContent.toString().contains("A conta sera debitada..."));
            assertTrue(outContent.toString().contains(String.valueOf(cartao.getNumero())));
            assertTrue(outContent.toString().contains("Valor debitado."));
        } finally {
            System.setOut(System.out);
        }
    }
    @Test
    @DisplayName("comprar cancelado deve imprimir mensagem de cancelamento")
    void testComprar_ComInput_ImprimeMensagemCancelamento() throws Exception {
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));
        try {
            System.setIn(new java.io.ByteArrayInputStream("2\n".getBytes()));
            contaPoupanca.comprar(150.0);
            assertTrue(outContent.toString().contains("A conta sera debitada..."));
            assertTrue(outContent.toString().contains(String.valueOf(cartao.getNumero())));
            assertTrue(outContent.toString().contains("Compra cancelada."));
        } finally {
            System.setOut(System.out);
        }
    }
}
