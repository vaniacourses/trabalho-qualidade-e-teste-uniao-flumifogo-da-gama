package br.winxbank.sistemabancario;

import br.winxbank.tempo.Ano;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


// Classe de testes unitários para CartaoCredito.
// Objetivo: validar regras de negócio relacionadas à fatura, limite e cobrança de juros.


class CartaoCreditoTest {
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
        double antes = cartao.getFatura();
        cartao.cobrarJurus();
        assertTrue(cartao.getFatura() > antes);
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

        double antes = cartao.getFatura();
        cartao.cobrarJurus();
        assertEquals(antes, cartao.getFatura());
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

        double antes = cartao.getFatura();
        cartao.cobrarJurus();
        assertEquals(antes, cartao.getFatura());
    }

    // Testa se o método setFatura respeita o limite do cartão,
    // impedindo incremento que ultrapasse o limite disponível.   
    @Test
    void naoDeveAlterarFaturaSeExcederLimite() {
        cartao.creditar(900);
        cartao.setFatura(200); // ultrapassa limite
        assertEquals(900, cartao.getFatura());
    }
}