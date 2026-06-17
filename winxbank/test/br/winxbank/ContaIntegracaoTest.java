package br.winxbank;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ContaIntegracaoTest {

    // Guarda os fluxos originais do sistema
    private final InputStream systemInOriginal = System.in;
    private final PrintStream systemOutOriginal = System.out;
    private ByteArrayOutputStream outContent;

    // --- NOVO: Variáveis para espionar o Logger ---
    private Logger loggerContaCorrente;
    private LogCaptorHandler logCaptor;

    // Classe interna para capturar os logs na memória durante o teste
    static class LogCaptorHandler extends Handler {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage()); // Guarda a mensagem de log
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        // Método utilitário para verificar se a mensagem que queremos foi registrada
        public boolean contemMensagem(String textoEsperado) {
            return messages.stream().anyMatch(msg -> msg.contains(textoEsperado));
        }
    }

    // Classe Fake apenas para o Cartão de Débito
    static class CartaoDebitoFake extends Cartao {
        public CartaoDebitoFake(int numero, int csv) {
            super(numero, csv);
        }
        @Override
        public void debitar(Conta conta, double valor) {
            conta.setSaldo(-valor);
        }
    }

    @BeforeEach
    void preparaConsoleELogs() {
        // 1. Prepara o Scanner (System.in) e System.out
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // 2. Prepara o interceptador do Logger
        loggerContaCorrente = Logger.getLogger(ContaCorrente.class.getName());
        logCaptor = new LogCaptorHandler();
        loggerContaCorrente.addHandler(logCaptor); // Adiciona nosso espião ao Logger
    }

    @AfterEach
    void restauraTudo() {
        System.setIn(systemInOriginal);
        System.setOut(systemOutOriginal);
        
        // Remove o nosso capturador para não vazar logs para outros testes
        loggerContaCorrente.removeHandler(logCaptor);
    }

    private void simularDigitacaoNoConsole(String dados) {
        System.setIn(new ByteArrayInputStream(dados.getBytes()));
    }

    // =========================================================
    // 1. O TRUQUE DO SCANNER E DOS LOGS
    // =========================================================

    @Test
    void deveComprarNoDebitoComSucesso_integracao() {
        Cartao debito = new CartaoDebitoFake(1234, 999);
        CartaoCredito credito = new CartaoCredito(1000, 1, false, 5000, 5678, 888);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        simularDigitacaoNoConsole("1\n1\n");
        conta.comprar(20.0);

        assertEquals(80.0, conta.getSaldo());
        
        // Agora consultamos o nosso espião de logs!
        assertTrue(logCaptor.contemMensagem("Valor debitado."));
    }

    @Test
    void deveCancelarCompraNoDebitoSeUsuarioNaoConfirmar_integracao() {
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, null);

        simularDigitacaoNoConsole("1\n2\n");
        conta.comprar(20.0);

        assertEquals(100.0, conta.getSaldo());
        assertTrue(logCaptor.contemMensagem("Compra cancelada. Efetue a compra novamente."));
    }

    @Test
    void deveComprarNoCreditoComSucesso_integracao() {
        CartaoCredito credito = new CartaoCredito(0.0, 1, false, 1000.0, 5678, 888);
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        simularDigitacaoNoConsole("2\n1\n");
        conta.comprar(50.0);

        assertEquals(100.0, conta.getSaldo());
        assertEquals(50.0, conta.getCartaoCredito().getFatura());
        assertTrue(logCaptor.contemMensagem("Valor creditado."));
    }

    @Test
    void deveCancelarCompraNoCreditoSeUsuarioNaoConfirmar_integracao() {
        CartaoCredito credito = new CartaoCredito(0.0, 1, false, 1000.0, 5678, 888);
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        simularDigitacaoNoConsole("2\n2\n");
        conta.comprar(50.0);

        assertEquals(0.0, conta.getCartaoCredito().getFatura());
        assertTrue(logCaptor.contemMensagem("Compra cancelada. Efetue a compra novamente."));
    }

    // =========================================================
    // 2. INTEGRAÇÃO COM OUTROS MÓDULOS (Banco e Cartão)
    // =========================================================

    @Test
    void pagarFaturaDeveReduzirOSaldoEReduzirAFatura_integracao() {
        CartaoCredito credito = new CartaoCredito(100.0, 1, false, 1000.0, 5678, 888);
        ContaCorrente conta = new ContaCorrente(1, 500.0, null, 0.0, credito);

        conta.pagarFatura(50.0);

        assertEquals(450.0, conta.getSaldo());
        assertEquals(50.0, conta.getCartaoCredito().getFatura());
    }

    // =========================================================
    // 3. TESTE DE CARACTERIZAÇÃO (Documentando Comportamento)
    // =========================================================

    @Test
    void documentaBug_pagarFaturaSemSaldoDeixaAContaNegativa_integracao() {
        CartaoCredito credito = new CartaoCredito(500.0, 1, false, 1000.0, 5678, 888);
        ContaCorrente conta = new ContaCorrente(1, 100.0, null, 0.0, credito);

        conta.pagarFatura(500.0);

        assertEquals(-400.0, conta.getSaldo());
        assertEquals(0.0, conta.getCartaoCredito().getFatura());
    }
}