package br.winxbank;

import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.tempo.Ano;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartaoCreditoIntegracaoTest {


// Teste 1: Integração com Ano (juros aplicados corretamente)
@Test
void deveAplicarJurosQuandoMesAvanca_integracao() {
    int mesAtual = Ano.getInstancia().getIndexMesAtual();

    CartaoCredito cartao = new CartaoCredito(
            200,
            mesAtual - 1, // mês anterior
            false, // fatura não paga
            1000,
            1111,
            123
    );

    double antes = cartao.getFatura();

    cartao.cobrarJurus();

    assertTrue(cartao.getFatura() > antes);
}

// Teste 2: Integração com Ano (não deve aplicar juros no mesmo mês)
@Test
void naoDeveAplicarJurosNoMesmoMes_integracao() {
    int mesAtual = Ano.getInstancia().getIndexMesAtual();

    CartaoCredito cartao = new CartaoCredito(
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

// Teste 3: Integração completa (CartaoCredito + Ano + fluxo real)
@Test
void fluxoCompletoCreditoEAtraso_integracao() {
    CartaoCredito cartao = new CartaoCredito(1111, 123);

    // usuário faz uma compra
    cartao.creditar(300);

    int mesAtual = Ano.getInstancia().getIndexMesAtual();

    // simula que passou um mês sem pagar
    cartao = new CartaoCredito(
            cartao.getFatura(),
            mesAtual - 1,
            false,
            1000,
            1111,
            123
    );

    double antes = cartao.getFatura();

    cartao.cobrarJurus();

    assertTrue(cartao.getFatura() > antes);
}

@Test
void naoDeveAlterarFaturaSeUltrapassarLimite_integracao() {
    CartaoCredito cartao = new CartaoCredito(1111, 123);

    cartao.creditar(1000); // limite cheio

    cartao.setFatura(100); // tenta ultrapassar

    assertEquals(1000, cartao.getFatura());
}
@Test
void deveManterFaturaNegativa_integracao() {
    CartaoCredito cartao = new CartaoCredito(1111, 123);

    cartao.setFatura(-50); // já começa negativo

    assertTrue(cartao.getFatura() <= 0);
}
}
