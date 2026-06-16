package br.winxbank;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Movimentacao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ContaIntegracaoTest {

    // Guarda os fluxos originais do sistema para não quebrar outros testes
    private final InputStream systemInOriginal = System.in;
    private final PrintStream systemOutOriginal = System.out;
    private ByteArrayOutputStream outContent;

    // Classe Fake apenas para o Cartão de Débito (pois Cartao costuma ser abstrato)
    static class CartaoDebitoFake extends Cartao {
        public CartaoDebitoFake(int numero, int csv) {
            super(numero, csv); // Assumindo que Cartao tem esse construtor
        }
        @Override
        public void debitar(Conta conta, double valor) {
            conta.setSaldo(-valor); // Simula o débito tirando do saldo
        }
    }

    @BeforeEach
    void preparaConsole() {
        // Intercepta tudo que o sistema tentar "printar" na tela
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restauraConsoleEScanner() {
        System.setIn(systemInOriginal);
        System.setOut(systemOutOriginal);
    }

    // Método auxiliar para simular o usuário digitando no console
    private void simularDigitacaoNoConsole(String dados) {
        System.setIn(new ByteArrayInputStream(dados.getBytes()));
    }

    // =========================================================
    // 1. O TRUQUE DO SCANNER (Cobrança e Fluxos de Compra)
    // =========================================================

    @Test
    void deveComprarNoDebitoComSucesso_integracao() {
        Cartao debito = new CartaoDebitoFake(1234, 999);
        CartaoCredito credito = new CartaoCredito(1000, 1, false, 5000, 5678, 888);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        // O usuário vai digitar "1" (Débito) e depois "1" (Confirmar)
        simularDigitacaoNoConsole("1\n1\n");

        conta.comprar(20.0);

        // O saldo inicial era 100. Como debitou 20, tem que sobrar 80.
        assertEquals(80.0, conta.getSaldo());
        assertTrue(outContent.toString().contains("Valor debitado."));
    }

    @Test
    void deveCancelarCompraNoDebitoSeUsuarioNaoConfirmar_integracao() {
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, null);

        // O usuário vai digitar "1" (Débito) e depois "2" (Cancelar)
        simularDigitacaoNoConsole("1\n2\n");

        conta.comprar(20.0);

        // O saldo deve permanecer intacto e a mensagem de cancelamento deve aparecer
        assertEquals(100.0, conta.getSaldo());
        assertTrue(outContent.toString().contains("Compra cancelada. Efetue a compra novamente."));
    }

    @Test
    void deveComprarNoCreditoComSucesso_integracao() {
        // O construtor do CartaoCredito costuma ser: (fatura, mes, faturaPaga, limite, numero, csv)
        CartaoCredito credito = new CartaoCredito(0.0, 1, false, 1000.0, 5678, 888);
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        // O usuário vai digitar "2" (Crédito) e depois "1" (Confirmar)
        simularDigitacaoNoConsole("2\n1\n");

        conta.comprar(50.0);

        // O saldo da conta não muda no crédito
        assertEquals(100.0, conta.getSaldo());
        
        // Mas a fatura do cartão TEM que subir
        assertEquals(50.0, conta.getCartaoCredito().getFatura());
        assertTrue(outContent.toString().contains("Valor creditado."));
    }

    @Test
    void deveCancelarCompraNoCreditoSeUsuarioNaoConfirmar_integracao() {
        CartaoCredito credito = new CartaoCredito(0.0, 1, false, 1000.0, 5678, 888);
        Cartao debito = new CartaoDebitoFake(1234, 999);
        ContaCorrente conta = new ContaCorrente(1, 100.0, debito, 0.0, credito);

        // O usuário vai digitar "2" (Crédito) e depois "2" (Cancelar)
        simularDigitacaoNoConsole("2\n2\n");

        conta.comprar(50.0);

        // A fatura não pode ter sido alterada
        assertEquals(0.0, conta.getCartaoCredito().getFatura());
        assertTrue(outContent.toString().contains("Compra cancelada. Efetue a compra novamente."));
    }

    // =========================================================
    // 2. INTEGRAÇÃO COM OUTROS MÓDULOS (Banco e Cartão)
    // =========================================================
    
    // Mata o mutante Matemático que troca o "-" por "+" no saldo ou na fatura
    @Test
    void pagarFaturaDeveReduzirOSaldoEReduzirAFatura_integracao() {
        // Inicializamos um cartão já com 100 de fatura
        CartaoCredito credito = new CartaoCredito(100.0, 1, false, 1000.0, 5678, 888);
        ContaCorrente conta = new ContaCorrente(1, 500.0, null, 0.0, credito);

        // Pagamos 50
        conta.pagarFatura(50.0);

        // O Saldo deve cair de 500 para 450
        assertEquals(450.0, conta.getSaldo());
        
        // A Fatura deve cair de 100 para 50
        assertEquals(50.0, conta.getCartaoCredito().getFatura());
    }

    // =========================================================
    // 3. TESTE DE CARACTERIZAÇÃO (Documentando Comportamento)
    // =========================================================

    @Test
    void documentaBug_pagarFaturaSemSaldoDeixaAContaNegativa_integracao() {
        CartaoCredito credito = new CartaoCredito(500.0, 1, false, 1000.0, 5678, 888);
        // Conta tem apenas 100 de saldo
        ContaCorrente conta = new ContaCorrente(1, 100.0, null, 0.0, credito);

        // O usuário tenta pagar 500 reais de fatura, mesmo sem saldo
        conta.pagarFatura(500.0);

        // Documenta que o sistema atual não tem validação e permite o saldo ficar negativo
        // O esperado num sistema real seria barrar ou usar cheque especial.
        assertEquals(-400.0, conta.getSaldo());
        assertEquals(0.0, conta.getCartaoCredito().getFatura());
    }
}