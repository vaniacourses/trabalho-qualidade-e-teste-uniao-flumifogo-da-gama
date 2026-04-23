package br.winxbank;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private Conta conta;

    // Implementação fake para testar classe abstrata
    static class ContaFake extends Conta {

        public ContaFake(int numeroConta, double saldo, Cartao cartao, double divida) {
            super(numeroConta, saldo, cartao, divida);
        }

        @Override
        public void comprar(double valor) {
            this.saldo -= valor;
        }

        @Override
        public void movimentacaoBancaria(double valor) {

        }
    }

    @BeforeEach
    void setUp() {
        conta = new ContaFake(1, 100.0, null, 50.0);
    }

    // =========================
    // 💰 DEPÓSITO
    // =========================
    @Test
    void deveDepositarValorCorretamente() {
        conta.depositar(50.0);

        assertEquals(150.0, conta.getSaldo());
    }

    // =========================
    // 💸 SAQUE
    // =========================
    @Test
    void deveSacarValorCorretamente() {
        conta.sacar(30.0);

        assertEquals(70.0, conta.getSaldo());
    }

    // =========================
    // 🔁 PIX
    // =========================
    @Test
    void deveTransferirPixParaOutraConta() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);

        conta.fazerPix(contaDestino, 50.0);

        assertEquals(50.0, contaDestino.getSaldo());
    }

    // =========================
    // 💳 EMPRÉSTIMO
    // =========================
    @Test
    void deveAumentarDividaAoRequisitarEmprestimo() {
        conta.requisitarEmprestimo(100.0);

        assertEquals(150.0, conta.getDividaDeEmprestimo());
    }

    @Test
    void deveDiminuirDividaAoPagarParcela() {
        conta.pagarParcelaDeEmprestimo(20.0);

        assertEquals(30.0, conta.getDividaDeEmprestimo());
    }

    // =========================
    // 📉 JUROS
    // =========================
    @Test
    void deveCobrarJurosQuandoDividaMaiorQueZero() {
        double dividaInicial = conta.getDividaDeEmprestimo();

        conta.cobrarJurusEmprestimo();

        assertTrue(conta.getDividaDeEmprestimo() < dividaInicial);
    }

    @Test
    void naoDeveCobrarJurosQuandoDividaZero() {
        Conta contaSemDivida = new ContaFake(3, 100.0, null, 0.0);

        contaSemDivida.cobrarJurusEmprestimo();

        assertEquals(0.0, contaSemDivida.getDividaDeEmprestimo());
    }

    // =========================
    // ➕ EXTRATO
    // =========================
    @Test
    void deveAdicionarMovimentacaoAoExtrato() {
        Movimentacao mov = new Movimentacao(
                100.0,
                Movimentacao.TipoDaMovimentacao.ENTRADA
        );

        conta.setExtrato(mov);

        assertEquals(1, conta.getExtrato().size());
    }

    // =========================
    // ⚠️ CASOS DE BORDA
    // =========================
    @Test
    void saquePodeDeixarSaldoNegativo() {
        conta.sacar(200.0);

        assertEquals(-100.0, conta.getSaldo());
    }

    @Test
    void depositoComValorNegativoNaoEhValidado() {
        conta.depositar(-50.0);

        assertEquals(50.0, conta.getSaldo()); // comportamento atual (bug potencial)
    }
}