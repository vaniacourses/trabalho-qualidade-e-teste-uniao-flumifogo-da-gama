package br.winxbank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.ClienteWinx;

public class ClienteWinxIntegracaoTest {

    @Test
    void converterPontosEmSaldo_deveAtualizarSaldoExtratoEResetarPontos() {
        ClienteWinx cliente = new ClienteWinx("Ana", "123", 5);
        ContaCorrente conta = new ContaCorrente(
                1,
                100.0,
                new Cartao(1234, 123),
                0,
                new CartaoCredito(1111, 123)
        );

        cliente.converterPontosEmSaldo(conta);

        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(115.0, conta.getSaldo(), 0.0001);
        assertFalse(conta.getExtrato().isEmpty());

        Movimentacao ultima = conta.getExtrato().get(conta.getExtrato().size() - 1);
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, ultima.getTipoDaMovimentacao());
        assertEquals(15.0, ultima.getDinheiroMovimentado(), 0.0001);
    }

    @Test
    void converterPontosEmSaldo_multiplicasConversoes() {
        ClienteWinx cliente = new ClienteWinx("Carlos", "999", 0);
        ContaCorrente conta = new ContaCorrente(
                3,
                200.0,
                new Cartao(9999, 999),
                0,
                new CartaoCredito(3333, 999)
        );

        // Primeira conversão: 2 pontos = 6.0 de crédito
        for (int i = 0; i < 2; i++) {
            cliente.obterPontosDeCompra();
        }
        cliente.converterPontosEmSaldo(conta);
        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(206.0, conta.getSaldo(), 0.0001);

        // Segunda conversão: 3 pontos = 9.0 de crédito
        for (int i = 0; i < 3; i++) {
            cliente.obterPontosDeCompra();
        }
        cliente.converterPontosEmSaldo(conta);
        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(215.0, conta.getSaldo(), 0.0001);
        assertEquals(2, conta.getExtrato().size());
    }

    @Test
    void converterPontosEmSaldo_comSaldoAlto() {
        ClienteWinx cliente = new ClienteWinx("Diana", "111", 10);
        ContaCorrente conta = new ContaCorrente(
                4,
                5000.0,
                new Cartao(1111, 111),
                0,
                new CartaoCredito(4444, 111)
        );

        cliente.converterPontosEmSaldo(conta);

        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(5030.0, conta.getSaldo(), 0.0001);
        Movimentacao ultima = conta.getExtrato().get(conta.getExtrato().size() - 1);
        assertEquals(30.0, ultima.getDinheiroMovimentado(), 0.0001);
    }

}