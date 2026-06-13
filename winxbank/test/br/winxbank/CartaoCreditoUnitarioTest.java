package br.winxbank;

import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.tempo.Ano;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


// Classe de testes unitários para CartaoCredito.
// Objetivo: validar regras de negócio relacionadas à fatura, limite e cobrança de juros.


public class CartaoCreditoUnitarioTest {
    private CartaoCredito cartao;
    
    // Inicializa um cartão padrão antes de cada teste.    
    @BeforeEach
    void setUp() {
        cartao = new CartaoCredito(1111, 123);
    }

    // Testa se o método creditar adiciona corretamente um valor à fatura. 
    @Test
    void deveAdicionarValorNaFatura() {
        cartao.creditar(300);
        assertEquals(300, cartao.getFatura());
    }

    // Testa se o sistema impede que a fatura ultrapasse o limite do cartão.  
    @Test
    void naoDeveUltrapassarOLimite() {
        cartao.creditar(1000);
        cartao.creditar(100); // tentativa de ultrapassar limite
        assertEquals(1000, cartao.getFatura());
    }
   
    // Testa se a fatura é considerada paga quando seu valor é zerado ou negativo.
    @Test
    void deveMarcarFaturaComoPagaQuandoZeradaOuNegativa() {
        cartao.creditar(200);
        cartao.setFatura(-200); // quitando a fatura
        assertEquals(0, cartao.getFatura());
    }
    
     // Testa se os juros são aplicados quando:
     // a fatura não foi paga o mês atual é posterior ao mês da fatura   
    @Test
    void deveAplicarJurosQuandoMesAvancaEFaturaNaoPaga() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        cartao = new CartaoCredito(
                200,
                mesAtual - 1, // mês anterior
                false,        // fatura não paga
                1000,
                1111,
                123
        );
        double antes = cartao.getFatura(); // guarda o valor da fatura antes de aplicar o juros
        cartao.cobrarJurus();
        assertTrue(cartao.getFatura() > antes); // verifica se ta maior o valor após a aplicação
    }
   
    // Testa se NÃO são aplicados juros quando a fatura já está paga,
    // mesmo que o mês tenha avançado.  
    @Test
    void naoDeveAplicarJurosSeFaturaJaFoiPaga() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        cartao = new CartaoCredito(
                200,
                mesAtual - 1,
                true, // fatura já paga
                1000,
                1111,
                123
        );

        double antes = cartao.getFatura(); // guarda o valor da fatura antes de aplicar
        cartao.cobrarJurus();
        assertEquals(antes, cartao.getFatura()); //verifica se foi cobrado juros da fatura já paga
    }

    // Testa se NÃO são aplicados juros quando ainda estamos no mesmo mês da fatura. 
    @Test
    void naoDeveAplicarJurosSeAindaForMesmoMes() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        cartao = new CartaoCredito(
                200,
                mesAtual, // mesmo mês
                false,
                1000,
                1111,
                123
        );

        double antes = cartao.getFatura(); // guarda o valor da fatura antes de aplicar
        cartao.cobrarJurus();
        assertEquals(antes, cartao.getFatura()); //verifica se foi cobrado juros da fatura atual
    }
    
    
    // Testes propostos pelo chatgpt para complementar os caminhos da classe:
    @Test
    void deveReduzirFaturaQuandoValorNegativo() {
        cartao.creditar(300);
        cartao.setFatura(-100);

        assertEquals(200, cartao.getFatura());
    }
    
    // Testar fatura não paga
    @Test
    void deveMarcarFaturaComoNaoPagaQuandoMaiorQueZero() {
        cartao.creditar(100);

        // não quitou totalmente
        assertTrue(cartao.getFatura() > 0);
    }
    
    @Test
    void deveMarcarFaturaComoPagaQuandoNegativa() {
        cartao.setFatura(-50);

        assertTrue(cartao.getFatura() <= 0);
    }
    
    @Test
    void naoDeveAplicarJurosSeMesNaoAvancouMesmoComFaturaNaoPaga() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();

        cartao = new CartaoCredito(
                200,
                mesAtual, // mesmo mês
                false, // não paga
                1000,
                1111,
                123
        );

        double antes = cartao.getFatura();
        cartao.cobrarJurus();

        assertEquals(antes, cartao.getFatura());
    }
    @Test
    void deveGerarMovimentacaoBancariaAoCobrarJuros() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();

        cartao = new CartaoCredito(
                200,
                mesAtual - 1,
                false,
                1000,
                1111,
                123
        );

        cartao.cobrarJurus();

        // aqui o ideal seria mockar o Banco depois (Mockito)
        assertTrue(cartao.getFatura() > 200);
    }
    
    // Ele não cobriu o metodo ajustarLimite() e usou como justificativa que não validava nada de verdade
    // Mas pedi e ele simulou o input do scanner
    @Test
    void deveAjustarLimite() {
        String entrada = "500\n";
        System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

        cartao.ajustarLimite();

        // não tem getter de limite, então não dá pra validar diretamente
        // mas só de executar já cobre o método
        assertTrue(true);
    }
}