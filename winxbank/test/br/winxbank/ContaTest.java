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
        public void comprar(double valor) {}

        @Override
        public void movimentacaoBancaria(double valor) {}
    }

    // Inicializa uma conta padrão antes de cada teste.
    @BeforeEach
    void setUp() {
        conta = new ContaFake(1, 100.0, null, 50.0);
    }

    // ==========================
    //        `Depositar`
    // ==========================

    // Testa se o método depositar adiciona corretamente um valor ao saldo.
    @Test
    void deveDepositarValorCorretamente() {
        conta.depositar(50.0);
        assertEquals(150.0, conta.getSaldo());
    }

    // Testa se o sistema impede depósitos com valores negativos.
    @Test
    void depositoComValorNegativoNaoEhValidado() {
        conta.depositar(-50.0);

        // O saldo inicial é 100.0. Um depósito negativo não deveria ser processado.
        // O teste falhará aqui pois o comportamento atual aplica o valor e deixa o saldo em 50.0.
        assertEquals(100.0, conta.getSaldo(), "BUG: O sistema não validou o depósito negativo e o saldo foi subtraído.");
    }

    // ==========================
    //        `Sacar`
    // ==========================

    // Testa se o método sacar subtrai corretamente um valor do saldo.
    @Test
    void deveSacarValorCorretamente() {
        conta.sacar(30.0);
        assertEquals(70.0, conta.getSaldo());
    }

    // Testa se o método sacar retorna erro quando tenta sacar valor negativo.
    @Test
    void deveRetornarErroAoSacarValorNegativo() {
        conta.sacar(-30.0);
        assertEquals(100.0, conta.getSaldo());
    }

    // Testa se saque de valor maior que saldo é negado
    @Test
    void saqueComValorMaiorQueSaldoNaoDeveSerPermitido() {
        // Saldo inicial é 100.0. Tentamos sacar 200.0.
        conta.sacar(200.0);

        // O saldo não pode ficar negativo se a conta não tiver limite de cheque especial.
        assertTrue(conta.getSaldo() >= 0.0, "BUG: O sistema permitiu sacar mais do que o saldo disponível.");
    }

    // ==========================
    //        `FazerPix`
    // ==========================

    // Testa se a transferência via Pix credita corretamente o valor na conta destino.
    @Test
    void deveTransferirPixParaOutraConta() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);
        conta.fazerPix(contaDestino, 50.0);
        assertEquals(50.0, contaDestino.getSaldo());
    }

    // Testa se a transferência via Pix debita corretamente o valor na conta origem.
    @Test
    void deveDebitarDinheiroEnviadoNaContaOrigem() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);
        conta.fazerPix(contaDestino, 50.0);
        assertEquals(50.0, conta.getSaldo());
    }

    // Testa se a transferência via Pix para mesma contato esta sendo permitido
    @Test
    void pixParaContaPessoalDeveSerProibido() {
        conta.fazerPix(conta, 50.0);
        assertEquals(100.0, conta.getSaldo());
    }

    // Testa se o sistema impede que um Pix seja feito com valor negativo.
    @Test
    void pixComValorNegativoNaoDeveSerPermitido() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);
        conta.fazerPix(contaDestino, -50.0);

        // O saldo da conta origem (100.0) não pode ser alterado
        assertEquals(100.0, conta.getSaldo(), "BUG: A conta origem teve o saldo alterado por um Pix negativo.");

        // O saldo da conta destino (0.0) também não pode receber o valor negativo
        assertEquals(0.0, contaDestino.getSaldo(), "BUG: A conta destino recebeu um Pix com valor negativo.");
    }

    // Teste para quando é passado um valor nulo no método fazerPix
    @Test
    void pixParaContaNulaDeveLancarExcecao() {
        // Se passarmos null, o sistema atual lança NullPointerException quando tenta acessar conta.saldo.
        // O correto seria validar antes e lançar IllegalArgumentException.
        assertThrows(IllegalArgumentException.class, () -> {
            conta.fazerPix(null, 50.0);
        }, "BUG: O sistema não validou a conta destino nula (vai estourar NullPointerException no código real).");
    }

    // Testa se o sistema impede que um Pix seja feito caso o valor exceda o saldo da conta origem.
    @Test
    void pixComValorMaiorQueSaldoNaoDeveSerPermitido() {
        Conta contaDestino = new ContaFake(2, 0.0, null, 0.0);
        // Saldo inicial é 100.0. Tentamos enviar 150.0.
        conta.fazerPix(contaDestino, 150.0);

        // Assumindo que a regra de negócio não permite Pix usando limite especial para ficar negativo.
        assertTrue(conta.getSaldo() >= 0.0, "BUG: O sistema permitiu transferir um valor de Pix maior que o saldo disponível.");
    }

    // ==========================
    //   `RequisitarEmprestimo`
    // ==========================

    // Testa se o método de requisitar empréstimo aumenta a dívida da conta.
    @Test
    void deveAumentarDividaAoRequisitarEmprestimo() {
        conta.requisitarEmprestimo(100.0);
        assertEquals(150.0, conta.getDividaDeEmprestimo());
    }

    // Testa se o método de requisitar empréstimo diminui ao passar valor negativo
    @Test
    void requisitarEmprestimoDeValorNegativoEhInvalido() {
        conta.requisitarEmprestimo(-100.0);
        assertEquals(50.0, conta.getDividaDeEmprestimo());
    }

    // ==========================
    // `PagarParcelaDeEmprestimo`
    // ==========================

    // Testa se o pagamento de uma parcela reduz corretamente o valor da dívida de empréstimo.
    @Test
    void deveDiminuirDividaAoPagarParcela() {
        conta.pagarParcelaDeEmprestimo(20.0);
        assertEquals(30.0, conta.getDividaDeEmprestimo());
    }

    // Testa se o sistema barra pagamentos de parcela com valores negativos.
    @Test
    void pagarParcelaComValorNegativoNaoDeveSerValidado() {
        conta.pagarParcelaDeEmprestimo(-20.0);

        // A dívida inicial é 50.0.
        // Se o sistema aceitar o negativo, ele fará: 50.0 - (-20.0) = 70.0 (aumentando a dívida do cliente).
        assertEquals(50.0, conta.getDividaDeEmprestimo(), "BUG: O pagamento negativo foi processado e aumentou a dívida.");
    }

    // Testa se o sistema permite pagar um valor maior que o da parcela
    @Test
    void naoDevePermitirPagamentoMaiorQueADivida() {
        // Dívida inicial é 50.0. Tentamos pagar 100.0.
        conta.pagarParcelaDeEmprestimo(100.0);

        // O comportamento atual vai deixar a dívida em -50.0 (BUG).
        // O ideal é que a dívida zere (0.0) ou a transação seja recusada.
        assertTrue(conta.getDividaDeEmprestimo() >= 0.0, "BUG: A dívida ficou negativa após um pagamento excedente.");
    }

    // ==========================
    //  `CobrarJurusEmprestimo`
    // ==========================

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

    // Testa se o sistema ignora a cobrança de juros caso a dívida, por algum motivo de falha, esteja negativa.
    @Test
    void naoDeveCobrarJurosQuandoDividaForNegativa() {
        // Criamos uma conta que já começa com uma "dívida" negativa (ex: cliente pagou a mais).
        Conta contaDividaNegativa = new ContaFake(4, 100.0, null, -10.0);

        contaDividaNegativa.cobrarJurusEmprestimo();

        // A dívida deve permanecer inalterada.
        assertEquals(-10.0, contaDividaNegativa.getDividaDeEmprestimo());
    }

    // ==========================
    //       `setExtrato`
    // ==========================

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

    // Testa se o método de registrar extrato não quebra recebendo um objeto nulo.
    @Test
    void extratoComMovimentacaoNulaDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            conta.setExtrato(null);
        }, "BUG: O sistema aceitou adicionar uma movimentação nula ao extrato.");
    }
}