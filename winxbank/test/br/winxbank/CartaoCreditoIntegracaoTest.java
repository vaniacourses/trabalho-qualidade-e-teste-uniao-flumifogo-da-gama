package br.winxbank;

import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.tempo.Ano;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class CartaoCreditoIntegracaoTest {

    // Guarda o console padrão para não bugar outros testes
    private final InputStream systemInOriginal = System.in;

    @AfterEach
    void restauraSystemIn() {
        System.setIn(systemInOriginal);
    }

    // =========================================================
    // 1. O TRUQUE DO SCANNER (Para bater 100% de linhas)
    // =========================================================
    @Test
    void deveCobrirOAjusteDeLimitePeloScanner_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        
        // Simula o usuário digitando "2500" no teclado e apertando Enter
        String input = "2500\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Agora o método não vai mais travar esperando o usuário!
        cartao.ajustarLimite();
        
        // Prova: O limite original era 1000. Se a gente tentar creditar 2000, 
        // agora tem que passar.
        cartao.creditar(2000);
        assertEquals(2000, cartao.getFatura());
    }


    // =========================================================
    // 2. TESTES DE CARACTERIZAÇÃO (Matadores de Mutantes)
    // =========================================================

    @Test
    void devePermitirCreditoExatamenteNoValorDoLimite_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        // Testa exatamente a borda do "<=" (mata o mutante que troca pra "<")
        cartao.creditar(1000); 
        assertEquals(1000, cartao.getFatura());
    }

    @Test
    void naoDeveCreditarSePassarDoLimite_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        cartao.creditar(1001); // Ultrapassa
        assertEquals(0, cartao.getFatura()); // A fatura ignora a soma
    }

    @Test
    void documentaBug_sistemaAceitaCreditoNegativo_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        
        // Como o setFatura não valida negativo, a fatura absorve o valor negativo.
        // Nosso teste ESPERA isso, provando o comportamento atual do código.
        cartao.creditar(-100);
        assertEquals(-100, cartao.getFatura());
    }

    @Test
    void faturaMenorOuIgualAZeroMarcaComoPaga_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        CartaoCredito cartao = new CartaoCredito(100, mesAtual - 1, false, 1000, 1111, 123);
        
        // Pagamos exatamente o que devemos (100). Fatura vira 0.
        // O código de produção deve cair no if(this.fatura <= 0) e setar faturaPaga = true
        cartao.setFatura(-100); 
        assertEquals(0, cartao.getFatura());
        
        // Se a faturaPaga virou true de verdade, os juros NÃO PODEM rodar.
        cartao.cobrarJurus();
        assertEquals(0, cartao.getFatura()); 
    }

    @Test
    void creditarFaturaFazElaVoltarASerNaoPaga_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        // Fatura zerada, supostamente paga (true)
        CartaoCredito cartao = new CartaoCredito(0, mesAtual - 1, true, 1000, 1111, 123);
        
        // Cai no 'else' do setFatura e seta faturaPaga para FALSE
        cartao.setFatura(50);
        
        // Prova: Como agora é false e o mês já virou, os juros VÃO ser aplicados
        double antes = cartao.getFatura();
        cartao.cobrarJurus();
        assertTrue(cartao.getFatura() > antes);
    }

    @Test
    void naoDeveCobrarJurosNoMesmoMes_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        CartaoCredito cartao = new CartaoCredito(100, mesAtual, false, 1000, 1111, 123);
        
        // Mata o mutante que troca ">" por ">=" no IF do mês
        cartao.cobrarJurus();
        assertEquals(100, cartao.getFatura());
    }

    @Test
    void testaVoidMethodMovimentacaoBancaria_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        // Garante que a linha que gera receita para o banco não está vazia e roda sem dar NullPointer
        assertDoesNotThrow(() -> cartao.movimentacaoBancaria(50.0));
    }
 // =========================================================
    // 3. TESTES SNIPER (Para caçar os últimos 6 mutantes)
    // =========================================================

    // Mata o mutante Matemático que troca "valor + fatura" por "valor - fatura"
    @Test
    void naoDevePermitirCreditoSeASomaDaFaturaComOValorUltrapassarOLimite_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        
        // Consome 600 do limite de 1000. Fatura atual = 600.
        cartao.creditar(600);
        
        // Tenta consumir mais 500. A soma dá 1100. O sistema tem que barrar!
        // Se o Pitest trocar "+" por "-", a conta do if vira: 500 - 600 = -100 (que é menor que 1000).
        // Aí o mutante deixa a compra passar. Esse teste impede isso!
        cartao.creditar(500);
        
        assertEquals(600, cartao.getFatura());
    }

    // Mata o mutante de Fronteira que troca "this.fatura <= 0" por "this.fatura < 0"
    @Test
    void faturaExatamenteZeroMarcaComoPaga_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        // Cartão devendo 100, do mês passado
        CartaoCredito cartao = new CartaoCredito(100, mesAtual - 1, false, 1000, 1111, 123);
        
        // Em vez de deixar negativo, pagamos o valor CRAVADO para zerar
        cartao.setFatura(-100); 
        
        // Aqui a fatura é 0. A regra deve marcar como paga (true).
        // Se o mutante trocou pra "< 0", ele não vai marcar como paga, e o juros vai rodar.
        // Vamos forçar o juros. Se a fatura continuar 0, o mutante morre.
        cartao.cobrarJurus();
        
        assertEquals(0, cartao.getFatura());
    }

    // Mata o mutante Void Call que apaga a linha "this.indexMesDaFatura = ..." no método creditar
    @Test
    void creditarDeveAtualizarOMesDaFaturaEvitandoJurosImediatos_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        
        // Cria um cartão do mês passado
        CartaoCredito cartao = new CartaoCredito(0, mesAtual - 1, true, 1000, 1111, 123);
        
        // Fazemos uma nova compra HOJE (mês atual)
        cartao.creditar(100);
        
        // Se a linha que atualiza o mês foi apagada pelo Pitest, o mês vai continuar sendo o passado,
        // e o sistema vai cobrar juros indevidos na mesma hora!
        cartao.cobrarJurus();
        
        // Como o mês foi atualizado para hoje, não pode ter juros ainda
        assertEquals(100, cartao.getFatura()); 
    }
 // Mata o mutante que apaga a linha "System.out.println"
    @Test
    void deveImprimirMensagemAoAjustarLimite_integracao() {
        CartaoCredito cartao = new CartaoCredito(1111, 123);
        
        // Vamos interceptar o que o sistema tenta "printar" na tela
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));
        
        // Simula o input do usuário
        String input = "2500\n";
        java.io.InputStream originalIn = System.in;
        System.setIn(new java.io.ByteArrayInputStream(input.getBytes()));
        
        cartao.ajustarLimite();
        
        // Restaura o console original pra não bugar o resto do sistema
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Prova que a linha do print NÃO foi apagada pelo Pitest
        assertTrue(outContent.toString().contains("Digite o valor do limite"));
    }

    // Mata o mutante que apaga o envio de dinheiro pro banco ou troca o sinal da matemática
    @Test
    void cobrarJurosDeveEnviarReceitaCorretaParaOBanco_integracao() {
        int mesAtual = Ano.getInstancia().getIndexMesAtual();
        CartaoCredito cartao = new CartaoCredito(100, mesAtual - 1, false, 1000, 1111, 123);
        
        // Vamos espionar o cofre do banco antes da cobrança
        double receitasAntes = br.winxbank.sistemabancario.Banco.getInstancia().getReceitas();
        
        // A mágica acontece: o juros deve ser cobrado e enviado pro banco
        cartao.cobrarJurus();
        
        // Olhamos o cofre do banco de novo
        double receitasDepois = br.winxbank.sistemabancario.Banco.getInstancia().getReceitas();
        
        // O banco TEM que estar mais rico. Se o mutante apagou o método "movimentacaoBancaria"
        // ou trocou o "+" por "-", a receita não vai subir e o teste mata o mutante!
        assertTrue(receitasDepois > receitasAntes);
    }
}