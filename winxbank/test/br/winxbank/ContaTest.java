package br.winxbank;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Classe de testes unitários para Conta.
// Objetivo: validar regras de negócio relacionadas a depósitos, saques, transferências, empréstimos e extrato.

class ContaTest {

    private Conta conta;

    // Implementação fake para testar classe abstrata.
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

    // Inicializa uma conta padrão antes de cada teste.
    @BeforeEach
    void setUp() {
        conta = new ContaFake(1, 100.0, null, 50.0);
    }

    // Testa se o método depositar adiciona corretamente um valor ao saldo.
    @Test
    void deveDepositarValorCorretamente() {
        conta.depositar(50.0);
        assertEquals(150.0, conta.getSaldo());
    }

    // Testa se o método sacar subtrai corretamente um valor do saldo.
    @Test
    void deveSacarValorCorretamente() {
        conta.sacar(30.0);
        assertEquals(70.0, conta.getSaldo());
    }

    // Testa se a transferência via Pix credita corretamente o valor na conta destino.
    @Test
    void deveTransferirPixParaOutraConta() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);
        conta.fazerPix(contaDestino, 50.0);
        assertEquals(50.0, contaDestino.getSaldo());
    }

    // Testa se o método de requisitar empréstimo aumenta a dívida da conta.
    @Test
    void deveAumentarDividaAoRequisitarEmprestimo() {
        conta.requisitarEmprestimo(100.0);
        assertEquals(150.0, conta.getDividaDeEmprestimo());
    }

    // Testa se o pagamento de uma parcela reduz corretamente o valor da dívida de empréstimo.
    @Test
    void deveDiminuirDividaAoPagarParcela() {
        conta.pagarParcelaDeEmprestimo(20.0);
        assertEquals(30.0, conta.getDividaDeEmprestimo());
    }

    // Testa se os juros são aplicados corretamente quando a conta possui uma dívida de empréstimo ativa.
    @Test
    void deveCobrarJurosQuandoDividaMaiorQueZero() {
        double dividaInicial = conta.getDividaDeEmprestimo();
        conta.cobrarJurusEmprestimo();
        assertTrue(conta.getDividaDeEmprestimo() < dividaInicial);
    }

    // Testa se NÃO são cobrados juros quando a conta não possui dívida de empréstimo.
    @Test
    void naoDeveCobrarJurosQuandoDividaZero() {
        Conta contaSemDivida = new ContaFake(3, 100.0, null, 0.0);
        contaSemDivida.cobrarJurusEmprestimo();
        assertEquals(0.0, contaSemDivida.getDividaDeEmprestimo());
    }

    // Testa se uma nova movimentação é adicionada corretamente ao extrato da conta.
    @Test
    void deveAdicionarMovimentacaoAoExtrato() {
        Movimentacao mov = new Movimentacao(
                100.0,
                Movimentacao.TipoDaMovimentacao.ENTRADA
        );
        conta.setExtrato(mov);
        assertEquals(1, conta.getExtrato().size());
    }

    // Testa se o saque permite que o saldo da conta fique negativo.
    @Test
    void saquePodeDeixarSaldoNegativo() {
        conta.sacar(200.0);
        assertEquals(-100.0, conta.getSaldo());
    }

    // Testa se o sistema impede depósitos com valores negativos.
    // Modificado propositalmente para falhar e expor o bug (Issue a ser aberta no GitHub).
    @Test
    void depositoComValorNegativoNaoEhValidado() {
        conta.depositar(-50.0);

        // O saldo inicial é 100.0. Um depósito negativo não deveria ser processado.
        // O teste falhará aqui pois o comportamento atual aplica o valor e deixa o saldo em 50.0.
        assertEquals(100.0, conta.getSaldo(), "BUG: O sistema não validou o depósito negativo e o saldo foi subtraído.");
    }
}