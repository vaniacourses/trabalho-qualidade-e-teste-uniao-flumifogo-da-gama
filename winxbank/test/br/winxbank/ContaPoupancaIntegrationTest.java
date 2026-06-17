package br.winxbank;
import br.winxbank.geradordedocumentos.ArquivoInformeRendimento;
import br.winxbank.sistemabancario.*;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Testes de Integração da ContaPoupanca")
class ContaPoupancaIntegrationTest {
    private ContaPoupanca contaPoupanca;
    private Cartao cartao;
    private Banco banco;
    private static final int NUMERO_CONTA = 6001;
    private static final double SALDO_INICIAL = 1000.0;
    private static final int NUMERO_CARTAO = 54321;
    private static final int CSV = 987;
    @BeforeEach
    void setUp() {
        banco = Banco.getInstancia();
        banco.receitas = 0;
        banco.despesas = 0;
        cartao = new Cartao(NUMERO_CARTAO, CSV);
        contaPoupanca = new ContaPoupanca(NUMERO_CONTA, SALDO_INICIAL, cartao, 0.0);
    }
    @AfterEach
    void tearDown() {
        // Limpa arquivo gerado nos testes
        File arquivo = new File(NUMERO_CONTA + "informe.txt");
        if (arquivo.exists()) {
            arquivo.delete();
        }
    }

    @Test
    @DisplayName("acrescentarRendimento deve incrementar despesas do Banco")
    void testAcrescentarRendimento_IntegracaoBanco() {
        double despesasAntes = banco.getDespesas();
        contaPoupanca.acrescentarRendimento();
        double rendimentoEsperado = SALDO_INICIAL / 0.8;
        assertEquals(despesasAntes + rendimentoEsperado, banco.getDespesas(), 0.001);
    }

    @Test
    @DisplayName("depositar + acrescentarRendimento: saldo e despesas do banco consistentes")
    void testFluxoDepositoERendimento() {
        contaPoupanca.depositar(500.0);
        assertEquals(1500.0, contaPoupanca.getSaldo(), 0.001);
        contaPoupanca.acrescentarRendimento();
        double rendimentoEsperado = 1500.0 / 0.8;
        assertEquals(1500.0 + rendimentoEsperado, contaPoupanca.getSaldo(), 0.001);
        assertEquals(rendimentoEsperado, banco.getDespesas(), 0.001);
    }

    @Test
    @DisplayName("gerarInformeRendimento deve criar arquivo no disco")
    void testGerarInformeRendimento_IntegracaoArquivo() throws Exception {
        contaPoupanca.acrescentarRendimento();
        contaPoupanca.gerarInformeRendimento();
        File arquivo = new File(NUMERO_CONTA + "informe.txt");
        assertTrue(arquivo.exists());
        assertTrue(arquivo.length() > 0);
    }

    @Test
    @DisplayName("múltiplos acrescentarRendimento acumulam despesas no Banco")
    void testMultiplosRendimentos_AcumulamDespesas() {
        contaPoupanca.acrescentarRendimento();
        contaPoupanca.acrescentarRendimento();
        contaPoupanca.acrescentarRendimento();
        double rendimentoBase = SALDO_INICIAL / 0.8;
        double rendimento2 = (SALDO_INICIAL + rendimentoBase) / 0.8;
        double rendimento3 = (SALDO_INICIAL + rendimentoBase + rendimento2) / 0.8;
        double totalDespesas = rendimentoBase + rendimento2 + rendimento3;
        assertEquals(totalDespesas, banco.getDespesas(), 0.001);
    }

    @Test
    @DisplayName("acrescentarRendimento com saldo ZERO não altera despesas do Banco")
    void testRendimentoSaldoZero_NaoGeraDespesa() {
        double despesasAntes = banco.getDespesas();
        ContaPoupanca contaZero = new ContaPoupanca(6002, 0.0, cartao, 0.0);
        contaZero.acrescentarRendimento();
        assertEquals(despesasAntes, banco.getDespesas(), 0.001);
    }
}