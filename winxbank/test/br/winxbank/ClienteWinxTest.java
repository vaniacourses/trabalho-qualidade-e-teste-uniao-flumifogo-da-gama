package br.winxbank;

import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void converterPontosEmSaldo_converteZeraPontosERegistraExtrato() {
        for (int i = 0; i < 5; i++) {
            cliente.obterPontosDeCompra();
        }

        cliente.converterPontosEmSaldo(conta);

        assertEquals(0, cliente.getPontosDeCompra());
        assertEquals(115.0, conta.getSaldo(), 0.0001);

        assertFalse(conta.getExtrato().isEmpty());
        Movimentacao ultima = conta.getExtrato().get(conta.getExtrato().size() - 1);
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, ultima.getTipoDaMovimentacao());
        assertEquals(15.0, ultima.getDinheiroMovimentado(), 0.0001);
    }
}