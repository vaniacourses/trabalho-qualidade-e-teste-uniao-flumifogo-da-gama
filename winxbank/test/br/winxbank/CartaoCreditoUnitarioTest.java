package br.winxbank;

import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Banco;
import br.winxbank.tempo.Ano;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartaoCreditoUnitarioTest {

    private CartaoCredito cartao;
    private Ano mockAno;
    private Banco mockBanco;
    private Object anoOriginal;
    private Object bancoOriginal;

    @BeforeEach
    void setUp() throws Exception {
        // Guarda as instâncias originais para não quebrar outros testes
        anoOriginal = Ano.getInstancia();
        bancoOriginal = Banco.getInstancia();

        // Cria os Mocks convencionais (sem MockedStatic)
        mockAno = mock(Ano.class);
        mockBanco = mock(Banco.class);

        // Injeta os Mocks à força dentro do sistema usando Reflection
        injetarMockSingleton(Ano.class, mockAno);
        injetarMockSingleton(Banco.class, mockBanco);

        cartao = new CartaoCredito(1111, 123);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restaura o sistema ao normal pós-teste
        injetarMockSingleton(Ano.class, anoOriginal);
        injetarMockSingleton(Banco.class, bancoOriginal);
    }

    // Método Hacker para invadir o Singleton e trocar a instância interna
    private void injetarMockSingleton(Class<?> classe, Object mock) throws Exception {
        for (Field field : classe.getDeclaredFields()) {
            if (field.getType().equals(classe) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                field.set(null, mock);
                return;
            }
        }
    }

    @Test
    void deveAdicionarValorNaFatura() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        when(mockAno.getMesAtual()).thenReturn("Maio");

        cartao.creditar(300);
        assertEquals(300, cartao.getFatura());
    }

    @Test
    void naoDeveUltrapassarOLimite() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        when(mockAno.getMesAtual()).thenReturn("Maio");

        cartao.creditar(1000);
        cartao.creditar(100); 
        assertEquals(1000, cartao.getFatura());
    }

    @Test
    void deveMarcarFaturaComoPagaQuandoZeradaOuNegativa() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        when(mockAno.getMesAtual()).thenReturn("Maio");

        cartao.creditar(200);
        cartao.setFatura(-200);
        assertEquals(0, cartao.getFatura());
    }

    @Test
    void deveAplicarJurosQuandoMesAvancaEFaturaNaoPaga() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);

        cartao = new CartaoCredito(200, 4, false, 1000, 1111, 123);
        double antes = cartao.getFatura();

        cartao.cobrarJurus();
        double depois = cartao.getFatura();

        assertTrue(depois > antes); 
        
        // MATADOR DA LINHA 98: Exige a matemática exata da subtração em vez de anyDouble()!
        verify(mockBanco, times(1)).setReceitas(depois - antes);
    }

    @Test
    void naoDeveAplicarJurosSeFaturaJaFoiPaga() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);

        cartao = new CartaoCredito(200, 4, true, 1000, 1111, 123);
        double antes = cartao.getFatura(); 

        cartao.cobrarJurus();
        
        assertEquals(antes, cartao.getFatura()); 
    }

    @Test
    void naoDeveAplicarJurosSeAindaForMesmoMes() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);

        cartao = new CartaoCredito(200, 5, false, 1000, 1111, 123);
        double antes = cartao.getFatura(); 

        cartao.cobrarJurus();
        
        assertEquals(antes, cartao.getFatura()); 
    }
 // =========================================================
    // TESTES FRANCO-ATIRADORES (Caçadores de Mutantes de Fronteira)
    // =========================================================

   @Test
    void deveAceitarCreditoExatamenteNoValorMaximoDoLimite() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        when(mockAno.getMesAtual()).thenReturn("Maio");

        // O limite padrão instanciado no setUp é 1000.
        // Alvo: Mutantes que alteram if(fatura + valor <= limite) para if(fatura + valor < limite)
        cartao.creditar(999);
        cartao.creditar(1);
        assertEquals(1000, cartao.getFatura(), "Mutante: Fronteira exata do limite (<=)");
        
        // Alvo: Mutantes matemáticos de estouro mínimo
        cartao.creditar(0.01);
        assertEquals(1000, cartao.getFatura(), "Mutante: Estouro de limite por um centavo");
    }

    @Test
    void cobrarJurosNaoDeveAlterarFaturaSeForOMesmoMes_FronteiraExata() {
        // Alvo: Mutantes que alteram if(mesAtual > mesFatura) para if(mesAtual >= mesFatura)
        cartao = new CartaoCredito(200, 4, false, 1000, 1111, 123);
        
        when(mockAno.getIndexMesAtual()).thenReturn(4); // Exatamente o mesmo mês (Fronteira)
        cartao.cobrarJurus();
        
        assertEquals(200, cartao.getFatura(), "Mutante: Juros aplicados erroneamente no mesmo mês");
    }
 // =========================================================
    // CAÇADORES DE MUTANTES DO RELATÓRIO
    // =========================================================

    @Test
    void mutanteLinha79_fronteiraFaturaExatamenteZeroImpedeJuros() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        cartao = new CartaoCredito(200, 4, false, 1000, 1111, 123);
        
        // Zera a fatura na fronteira exata (200 + (-200) = 0)
        cartao.setFatura(-200); 
        
        cartao.cobrarJurus();
        
        // Se a fronteira <= 0 funcionou, a fatura foi dada como paga e a cobrança é abortada.
        // Logo, o método do banco NUNCA deve ser chamado.
        verify(mockBanco, never()).setReceitas(anyDouble());
    }

    @Test
    void mutanteLinha79_faturaMaiorQueZeroPermiteJuros() {
        when(mockAno.getIndexMesAtual()).thenReturn(5);
        cartao = new CartaoCredito(200, 4, false, 1000, 1111, 123);
        
        // Fatura fica positiva (200 + (-199) = 1)
        cartao.setFatura(-199); 
        
        cartao.cobrarJurus();
        
        // Como a fatura não foi quitada, o método do banco DEVE ser chamado 1 vez.
        verify(mockBanco, times(1)).setReceitas(anyDouble());
    }
/*
    @Test
    void mutanteLinha64_deveAjustarLimiteViaConsole() {
        // Cobre a linha do Scanner simulando a digitação "2500" no terminal do sistema
        String entrada = "2500\n";
        java.io.InputStream inOriginal = System.in;
        System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

        try {
            cartao = new CartaoCredito(1111, 123); // Limite inicial é 1000
            
            // Vai ler o "2500" que injetamos acima
            cartao.ajustarLimite(); 
            
            // Prova que o limite subiu para 2500 (o crédito de 2000 tem que ser aceito)
            cartao.creditar(2000); 
            assertEquals(2000, cartao.getFatura());
        } finally {
            // Devolve o console ao normal para não quebrar outros testes
            System.setIn(inOriginal);
        }
    }
    */
}