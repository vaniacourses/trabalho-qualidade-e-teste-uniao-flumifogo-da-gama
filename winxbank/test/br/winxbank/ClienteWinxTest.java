package br.winxbank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.ClienteWinx;


public class ClienteWinxTest {

    private ClienteWinx cliente;
    private Conta conta;

    static class ContaFake extends Conta {
        ContaFake(int numeroConta, double saldo) {
            super(numeroConta, saldo, new Cartao(1234, 123), 0);
        }

        @Override
        public void comprar(double valor) {
        }

        @Override
        public void movimentacaoBancaria(double valor) {
        }
    }

    @BeforeEach
    void setup() {
        cliente = new ClienteWinx("Ana", "123", 0);
        conta = new ContaFake(1, 100.0);
    }

    @Test
    void obterPontosDeCompra_incrementaEm1() {
        cliente.obterPontosDeCompra();
        assertEquals(1, cliente.getPontosDeCompra());
    }

    @Test
    void obterPontosDeCompra_acumulaCorretamente() {
        for (int i = 0; i < 5; i++) {
            cliente.obterPontosDeCompra();
        }
        assertEquals(5, cliente.getPontosDeCompra());
    }


    @Test
    void converterPontosEmSaldo_converteZeraPontosERegistraExtrato() {
        for (int i = 0; i < 5; i++) {
            cliente.obterPontosDeCompra();
        }
        assertEquals(5, cliente.getPontosDeCompra());
        cliente.converterPontosEmSaldo(conta);

        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(115.0, conta.getSaldo(), 0.0001);

        assertFalse(conta.getExtrato().isEmpty());
        Movimentacao ultima = conta.getExtrato().get(conta.getExtrato().size() - 1);
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, ultima.getTipoDaMovimentacao());
        assertEquals(15.0, ultima.getDinheiroMovimentado(), 0.0001);
    }

    @Test
    void converterPontosEmSaldo_comPontosNegativos() {
    ClienteWinx clienteComPontosNegativos = new ClienteWinx("Ana", "123", -10);

    double saldoAntes = conta.getSaldo();
    clienteComPontosNegativos.converterPontosEmSaldo(conta);

    assertTrue(saldoAntes > conta.getSaldo(),
        "BUG: pontos negativos geraram conversão negativa e diminuíram o saldo.");
}
} 