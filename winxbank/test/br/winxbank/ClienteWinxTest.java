package br.winxbank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.ClienteWinx;

public class ClienteWinxTest {

    private ClienteWinx cliente;
    private Conta contaMock;

    @BeforeEach
    void setup() {
        cliente = new ClienteWinx("Ana", "123", 0);
        // Criação do mock da dependência Conta
        contaMock = mock(Conta.class);
    }

    @Test
    void construtor_inicializaPontosCorretamente() {
        ClienteWinx clienteComPontos = new ClienteWinx("Bob", "789", 10);
        assertEquals(10, clienteComPontos.getPontosDeCompra());
    }

    @Test
    void obterPontosDeCompra_incrementaEm1() {
        cliente.obterPontosDeCompra();
        assertEquals(1, cliente.getPontosDeCompra());
    }

    @Test
    void converterPontosEmSaldo_converteZeraPontosERegistraExtrato() {
        // Preparação
        for (int i = 0; i < 5; i++) {
            cliente.obterPontosDeCompra();
        }
        
        // Execução
        cliente.converterPontosEmSaldo(contaMock);

        // Verificações
        assertEquals(0, cliente.getPontosDeCompra());
        
        // Verifica se o método setSaldo foi chamado com o valor 15.0 (5 pontos * 3 bônus)
        verify(contaMock).setSaldo(15.0);
        
        // Captura o objeto passado para setExtrato para validar se é uma Movimentação de entrada
        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(contaMock).setExtrato(captor.capture());
        
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, captor.getValue().getTipoDaMovimentacao());
        assertEquals(15.0, captor.getValue().getDinheiroMovimentado(), 0.0001);
    }

    @Test
    void converterPontosEmSaldo_comZeroPontos() {
        cliente.converterPontosEmSaldo(contaMock);
        
        verify(contaMock).setSaldo(0.0);
        assertEquals(0, cliente.getPontosDeCompra());
    }

    @Test
    void getters_devemRetornarValoresCorretos() {
        ClienteWinx clienteGetters = new ClienteWinx("Carlos", "456", 0);
        assertEquals("Carlos", clienteGetters.getNome());
        assertEquals("456", clienteGetters.getCpf());
    }

    @Test
    void construtor_comClienteWinx_deveCopiarDados() {
        ClienteWinx clienteOriginal = new ClienteWinx("Diana", "999", 5);
        ClienteWinx clienteCopiado = new ClienteWinx(clienteOriginal);

        assertEquals("Diana", clienteCopiado.getNome());
        assertEquals("999", clienteCopiado.getCpf());
        assertEquals(5, clienteCopiado.getPontosDeCompra());
    }
}