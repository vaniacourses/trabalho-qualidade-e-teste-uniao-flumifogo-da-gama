package br.winxbank;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

// Classe de testes unitários para Conta.
// Objetivo: validar regras de negócio relacionadas a depósitos, saques, transferências, empréstimos e extrato.

@ExtendWith(MockitoExtension.class) // Habilita o uso do Mockito nesta classe de teste
class ContaUnitarioTest {

    private Conta conta;

    // ==========================
    //          MOCKS
    // ==========================
    
    @Mock
    private Cartao cartaoMock;

    @Mock
    private Movimentacao movimentacaoMock;

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
        // Agora injetamos o mock de Cartao em vez de 'null'. 
        // Isso previne NullPointerExceptions caso a classe Conta tente acessar o cartão no futuro.
        conta = new ContaFake(1, 100.0, cartaoMock, 50.0);
    }

    // ==========================
    //        `Depositar`
    // ==========================

    @Test
    void deveDepositarValorCorretamente() {
        conta.depositar(50.0);
        assertEquals(150.0, conta.getSaldo());
    }

    @Test
    void depositoComValorNegativoNaoEhValidado() {
        conta.depositar(-50.0);
        assertEquals(100.0, conta.getSaldo(), "BUG: O sistema não validou o depósito negativo e o saldo foi subtraído.");
    }

    // ==========================
    //        `Sacar`
    // ==========================

    @Test
    void deveSacarValorCorretamente() {
        conta.sacar(30.0);
        assertEquals(70.0, conta.getSaldo());
    }

    @Test
    void deveRetornarErroAoSacarValorNegativo() {
        conta.sacar(-30.0);
        assertEquals(100.0, conta.getSaldo());
    }

    @Test
    void saqueComValorMaiorQueSaldoNaoDeveSerPermitido() {
        conta.sacar(200.0);
        assertTrue(conta.getSaldo() >= 0.0, "BUG: O sistema permitiu sacar mais do que o saldo disponível.");
    }

    // ==========================
    //        `FazerPix`
    // ==========================

    @Test
    void deveTransferirPixParaOutraConta() {
        // Utilizando o mock do cartão também na conta de destino para manter o isolamento
        Conta contaDestino = new ContaFake(2, 0.0, cartaoMock, 0.0);
        conta.fazerPix(contaDestino, 50.0);
        assertEquals(50.0, contaDestino.getSaldo());
    }

    @Test
    void deveDebitarDinheiroEnviadoNaContaOrigem() {
        Conta contaDestino = new ContaFake(2, 0.0, cartaoMock, 0.0);
        conta.fazerPix(contaDestino, 50.0);
        assertEquals(50.0, conta.getSaldo());
    }

    @Test
    void pixParaContaPessoalDeveSerProibido() {
        conta.fazerPix(conta, 50.0);
        assertEquals(100.0, conta.getSaldo());
    }

    @Test
    void pixComValorNegativoNaoDeveSerPermitido() {
        Conta contaDestino = new ContaFake(2, 0.0, cartaoMock, 0.0);
        conta.fazerPix(contaDestino, -50.0);

        assertEquals(100.0, conta.getSaldo(), "BUG: A conta origem teve o saldo alterado por um Pix negativo.");
        assertEquals(0.0, contaDestino.getSaldo(), "BUG: A conta destino recebeu um Pix com valor negativo.");
    }

    @Test
    void pixParaContaNulaDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            conta.fazerPix(null, 50.0);
        }, "BUG: O sistema não validou a conta destino nula (vai estourar NullPointerException no código real).");
    }

    @Test
    void pixComValorMaiorQueSaldoNaoDeveSerPermitido() {
        Conta contaDestino = new ContaFake(2, 0.0, cartaoMock, 0.0);
        conta.fazerPix(contaDestino, 150.0);

        assertTrue(conta.getSaldo() >= 0.0, "BUG: O sistema permitiu transferir um valor de Pix maior que o saldo disponível.");
    }

    // ==========================
    //   `RequisitarEmprestimo`
    // ==========================

    @Test
    void deveAumentarDividaAoRequisitarEmprestimo() {
        conta.requisitarEmprestimo(100.0);
        assertEquals(150.0, conta.getDividaDeEmprestimo());
    }

    @Test
    void requisitarEmprestimoDeValorNegativoEhInvalido() {
        conta.requisitarEmprestimo(-100.0);
        assertEquals(50.0, conta.getDividaDeEmprestimo());
    }

    // ==========================
    // `PagarParcelaDeEmprestimo`
    // ==========================

    @Test
    void deveDiminuirDividaAoPagarParcela() {
        conta.pagarParcelaDeEmprestimo(20.0);
        assertEquals(30.0, conta.getDividaDeEmprestimo());
    }

    @Test
    void pagarParcelaComValorNegativoNaoDeveSerValidado() {
        conta.pagarParcelaDeEmprestimo(-20.0);
        assertEquals(50.0, conta.getDividaDeEmprestimo(), "BUG: O pagamento negativo foi processado e aumentou a dívida.");
    }

    @Test
    void naoDevePermitirPagamentoMaiorQueADivida() {
        conta.pagarParcelaDeEmprestimo(100.0);
        assertTrue(conta.getDividaDeEmprestimo() >= 0.0, "BUG: A dívida ficou negativa após um pagamento excedente.");
    }

    // ==========================
    //  `CobrarJurusEmprestimo`
    // ==========================

    @Test
    void deveCobrarJurosQuandoDividaMaiorQueZero() {
        double dividaInicial = conta.getDividaDeEmprestimo();
        conta.cobrarJurusEmprestimo();
        assertTrue(conta.getDividaDeEmprestimo() < dividaInicial);
    }

    @Test
    void naoDeveCobrarJurosQuandoDividaZero() {
        Conta contaSemDivida = new ContaFake(3, 100.0, cartaoMock, 0.0);
        contaSemDivida.cobrarJurusEmprestimo();
        assertEquals(0.0, contaSemDivida.getDividaDeEmprestimo());
    }

    @Test
    void naoDeveCobrarJurosQuandoDividaForNegativa() {
        Conta contaDividaNegativa = new ContaFake(4, 100.0, cartaoMock, -10.0);
        contaDividaNegativa.cobrarJurusEmprestimo();
        assertEquals(-10.0, contaDividaNegativa.getDividaDeEmprestimo());
    }

    // ==========================
    //       `setExtrato`
    // ==========================

    @Test
    void deveAdicionarMovimentacaoAoExtrato() {
        // Substituindo a instância real pelo mock de Movimentacao.
        // Isso garante que não dependemos da lógica do construtor de Movimentacao.
        conta.setExtrato(movimentacaoMock);
        assertEquals(1, conta.getExtrato().size());
        assertTrue(conta.getExtrato().contains(movimentacaoMock));
    }

    @Test
    void extratoComMovimentacaoNulaDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> {
            conta.setExtrato(null);
        }, "BUG: O sistema aceitou adicionar uma movimentação nula ao extrato.");
    }
}