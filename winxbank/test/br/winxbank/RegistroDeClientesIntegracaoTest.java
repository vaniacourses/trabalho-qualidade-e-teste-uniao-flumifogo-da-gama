package br.winxbank;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import br.winxbank.sistemaclientes.*;
import br.winxbank.sistemabancario.*;

/**
 * Testes de integração para RegistroDeClientes
 * Validam fluxos completos de negócio com múltiplos componentes
 */
public class RegistroDeClientesIntegracaoTest {

    private RegistroDeClientes registro;
    private static final String CPF_TESTE_1 = "11122233344";
    private static final String CPF_TESTE_2 = "99988877766";
    private static final String CPF_TESTE_3 = "12312312312";
    private static final String NOME_TESTE_1 = "João Silva";
    private static final String NOME_TESTE_2 = "Maria Santos";
    private static final String NOME_TESTE_3 = "Pedro Oliveira";

    @BeforeEach
    void setup() {
        registro = RegistroDeClientes.getInstancia();
        registro.limparListaDeClientes();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Cliente criarClienteComCpf(String nome, String cpf) throws NoSuchFieldException, IllegalAccessException {
        Cliente cliente = new Cliente(nome, cpf);
        // Injetar CPF real via reflection
        Field campoCpf = cliente.getClass().getDeclaredField("cpf");
        campoCpf.setAccessible(true);
        campoCpf.set(cliente, cpf);
        return cliente;
    }

    private ClienteWinx criarClienteWinxComCpf(String nome, String cpf, int pontos) throws NoSuchFieldException, IllegalAccessException {
        ClienteWinx cliente = new ClienteWinx(nome, cpf, pontos);
        return cliente;
    }

    private void injetarClientesAoRegistro(Cliente... clientes) {
        ArrayList<Cliente> lista = new ArrayList<>();
        for (Cliente cliente : clientes) {
            lista.add(cliente);
        }
        registro.setClientes(lista);
    }

    // ==========================================
    // TESTES DE INTEGRAÇÃO - FLUXO COMPLETO
    // ==========================================

    @Test
    void integracao_cadastroRemocaoVerificacao_fluxoCompleto() throws Exception {
        // Setup: criar cliente
        Cliente cliente = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        
        // Act: adicionar ao registro
        injetarClientesAoRegistro(cliente);
        
        // Assert: verificar presença
        assertEquals(1, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(cliente));
        assertFalse(registro.checarCpf(CPF_TESTE_1)); // CPF já existe
        assertTrue(registro.checarCpf(CPF_TESTE_2));  // CPF não existe
        
        // Act: remover cliente
        registro.removerCliente(cliente);
        
        // Assert: verificar remoção
        assertEquals(0, registro.getClientes().size());
        assertFalse(registro.getClientes().contains(cliente));
        assertTrue(registro.checarCpf(CPF_TESTE_1)); // CPF agora está disponível
    }

    @Test
    void integracao_multiplasCadastrosAtualizacaoRemocao() throws Exception {
        // Setup: criar múltiplos clientes
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2);
        Cliente cliente3 = criarClienteComCpf(NOME_TESTE_3, CPF_TESTE_3);
        
        // Act: adicionar todos
        injetarClientesAoRegistro(cliente1, cliente2, cliente3);
        
        // Assert: verificar lista está completa
        assertEquals(3, registro.getClientes().size());
        
        // Act: atualizar cliente 2
        Cliente cliente2Atualizado = criarClienteComCpf("Maria Santos Atualizada", CPF_TESTE_2);
        registro.atualizarCliente(cliente2Atualizado);
        
        // Assert: tamanho deve manter 3
        assertEquals(3, registro.getClientes().size());
        
        // Act: remover cliente 1
        registro.removerCliente(cliente1);
        
        // Assert: deve ter 2 clientes
        assertEquals(2, registro.getClientes().size());
        assertFalse(registro.getClientes().contains(cliente1));
        assertTrue(registro.getClientes().contains(cliente2Atualizado));
        assertTrue(registro.getClientes().contains(cliente3));
    }

    @Test
    void integracao_checarCpfComMultiplosClientes_validaCorracao() throws Exception {
        // Setup: criar 5 clientes com CPFs distintos
        String[] cpfs = {"11111111111", "22222222222", "33333333333", "44444444444", "55555555555"};
        Cliente[] clientes = new Cliente[cpfs.length];
        
        for (int i = 0; i < cpfs.length; i++) {
            clientes[i] = criarClienteComCpf("Cliente " + i, cpfs[i]);
        }
        
        // Act: adicionar todos
        injetarClientesAoRegistro(clientes);
        
        // Assert: verificar cada CPF
        for (String cpf : cpfs) {
            assertFalse(registro.checarCpf(cpf), "CPF " + cpf + " deveria estar cadastrado");
        }
        
        // Assert: CPFs não cadastrados devem retornar true
        assertTrue(registro.checarCpf("99999999999"));
        assertTrue(registro.checarCpf("88888888888"));
    }

    @Test
    void integracao_retornarClienteComLista_encontraCorreto() throws Exception {
        // Setup: criar 3 clientes
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2);
        Cliente cliente3 = criarClienteComCpf(NOME_TESTE_3, CPF_TESTE_3);
        
        // Act: adicionar ao registro
        injetarClientesAoRegistro(cliente1, cliente2, cliente3);
        
        // Assert: buscar cada um
        Cliente resultado1 = registro.retornarCliente(CPF_TESTE_1);
        Cliente resultado2 = registro.retornarCliente(CPF_TESTE_2);
        Cliente resultado3 = registro.retornarCliente(CPF_TESTE_3);
        
        assertNotNull(resultado1);
        assertNotNull(resultado2);
        assertNotNull(resultado3);
        
        assertEquals(cliente1, resultado1);
        assertEquals(cliente2, resultado2);
        assertEquals(cliente3, resultado3);
        
        // Assert: CPF não existente retorna null
        assertNull(registro.retornarCliente("00000000000"));
    }

    @Test
    void integracao_clienteWinxComDadosCompletos() throws Exception {
        // Setup: criar ClienteWinx com pontos
        ClienteWinx clienteWinx = criarClienteWinxComCpf(NOME_TESTE_1, CPF_TESTE_1, 500);
        
        // Act: adicionar ao registro
        injectarClientesAoRegistro(clienteWinx);
        
        // Assert: verificar dados
        assertEquals(1, registro.getClientes().size());
        ClienteWinx recuperado = (ClienteWinx) registro.retornarCliente(CPF_TESTE_1);
        assertNotNull(recuperado);
        assertEquals(CPF_TESTE_1, recuperado.getCpf());
        assertEquals(500, recuperado.getPontosDeCompra());
    }

    @Test
    void integracao_getInstanciaRetornaSingleton() {
        // Act: obter instâncias
        RegistroDeClientes reg1 = RegistroDeClientes.getInstancia();
        RegistroDeClientes reg2 = RegistroDeClientes.getInstancia();
        
        // Assert: devem ser a mesma instância
        assertSame(reg1, reg2);
    }

    @Test
    void integracao_limparListaEfetuaLimpeza() throws Exception {
        // Setup: adicionar múltiplos clientes
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2);
        Cliente cliente3 = criarClienteComCpf(NOME_TESTE_3, CPF_TESTE_3);
        
        injectarClientesAoRegistro(cliente1, cliente2, cliente3);
        
        // Assert: lista tem 3 clientes
        assertEquals(3, registro.getClientes().size());
        
        // Act: limpar lista
        registro.limparListaDeClientes();
        
        // Assert: lista vazia
        assertEquals(0, registro.getClientes().size());
        assertTrue(registro.checarCpf(CPF_TESTE_1));
        assertTrue(registro.checarCpf(CPF_TESTE_2));
        assertTrue(registro.checarCpf(CPF_TESTE_3));
    }

    @ParameterizedTest
    @CsvSource({
        "CPF1,Cliente1,true",
        "CPF2,Cliente2,true",
        "CPF3,Cliente3,true",
        "CPFX,ClienteX,true",
        "CPFY,ClienteY,true"
    })
    void integracao_parametrizadoChecarCpfNovoCliente(String cpf, String nome, boolean esperado) throws Exception {
        // Setup
        Cliente cliente = criarClienteComCpf(nome, cpf);
        
        // Act & Assert: novo cliente deve ter CPF disponível
        assertEquals(esperado, registro.checarCpf(cpf));
        
        // Act: adicionar cliente
        injectarClientesAoRegistro(cliente);
        
        // Act & Assert: CPF agora não está disponível
        assertFalse(registro.checarCpf(cpf));
    }

    @Test
    void integracao_setClientesAdicionaALista() throws Exception {
        // Setup: criar clientes em lista separada
        ArrayList<Cliente> listaExterna = new ArrayList<>();
        listaExterna.add(criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1));
        listaExterna.add(criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2));
        listaExterna.add(criarClienteComCpf(NOME_TESTE_3, CPF_TESTE_3));
        
        // Act: adicionar via setClientes
        registro.setClientes(listaExterna);
        
        // Assert: verificar que foram adicionados
        assertEquals(3, registro.getClientes().size());
        assertFalse(registro.checarCpf(CPF_TESTE_1));
        assertFalse(registro.checarCpf(CPF_TESTE_2));
        assertFalse(registro.checarCpf(CPF_TESTE_3));
    }

    @Test
    void integracao_atualizarClienteAlteraRegistro() throws Exception {
        // Setup: criar e adicionar cliente
        Cliente clienteOriginal = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        injectarClientesAoRegistro(clienteOriginal);
        
        // Assert: cliente original está na lista
        assertEquals(1, registro.getClientes().size());
        Cliente recuperado = registro.retornarCliente(CPF_TESTE_1);
        assertEquals(NOME_TESTE_1, recuperado.getNome());
        
        // Act: atualizar cliente com novo nome
        Cliente clienteAtualizado = criarClienteComCpf("NOVO_NOME", CPF_TESTE_1);
        registro.atualizarCliente(clienteAtualizado);
        
        // Assert: lista tem 1 cliente (não duplicou) e nome foi atualizado
        assertEquals(1, registro.getClientes().size());
        Cliente recuperadoAposAtualizacao = registro.retornarCliente(CPF_TESTE_1);
        assertEquals("NOVO_NOME", recuperadoAposAtualizacao.getNome());
    }

    @Test
    void integracao_removerClienteNaoPresenteNaoAfeita() throws Exception {
        // Setup: criar e adicionar 2 clientes
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2);
        Cliente clienteNaoPresente = criarClienteComCpf(NOME_TESTE_3, CPF_TESTE_3);
        
        injectarClientesAoRegistro(cliente1, cliente2);
        
        // Assert: lista tem 2
        assertEquals(2, registro.getClientes().size());
        
        // Act: tentar remover cliente não presente
        registro.removerCliente(clienteNaoPresente);
        
        // Assert: lista continua com 2 (não foi afetada)
        assertEquals(2, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(cliente1));
        assertTrue(registro.getClientes().contains(cliente2));
    }

    @Test
    void integracao_checarCpfComListaVazia() {
        // Setup: lista vazia
        assertEquals(0, registro.getClientes().size());
        
        // Assert: qualquer CPF deve estar disponível
        assertTrue(registro.checarCpf(CPF_TESTE_1));
        assertTrue(registro.checarCpf(CPF_TESTE_2));
        assertTrue(registro.checarCpf(CPF_TESTE_3));
        assertTrue(registro.checarCpf("00000000000"));
    }

    @Test
    void integracao_retornarClienteComListaVazia() {
        // Setup: lista vazia
        assertEquals(0, registro.getClientes().size());
        
        // Assert: retorna null
        assertNull(registro.retornarCliente(CPF_TESTE_1));
        assertNull(registro.retornarCliente(CPF_TESTE_2));
        assertNull(registro.retornarCliente("00000000000"));
    }

    @Test
    void integracao_multiplosClientesComSameCpf_primeiroEhEncontrado() throws Exception {
        // Setup: criar dois clientes com MESMO CPF (simulando erro/conflito)
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_1); // Mesmo CPF!
        
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(cliente1);
        lista.add(cliente2);
        registro.setClientes(lista);
        
        // Assert: retorna o primeiro (ordem FIFO)
        Cliente resultado = registro.retornarCliente(CPF_TESTE_1);
        assertNotNull(resultado);
        assertEquals(NOME_TESTE_1, resultado.getNome());
    }

    @Test
    void integracao_atualizarRemoverAtualizarCiclCompleto() throws Exception {
        // Setup: criar e adicionar cliente
        Cliente cliente = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        injectarClientesAoRegistro(cliente);
        
        // Assert: inicial
        assertEquals(1, registro.getClientes().size());
        assertEquals(NOME_TESTE_1, registro.retornarCliente(CPF_TESTE_1).getNome());
        
        // Act: atualizar
        Cliente clienteAtualizado1 = criarClienteComCpf("Nome Atualizado 1", CPF_TESTE_1);
        registro.atualizarCliente(clienteAtualizado1);
        
        // Assert: após atualização
        assertEquals(1, registro.getClientes().size());
        assertEquals("Nome Atualizado 1", registro.retornarCliente(CPF_TESTE_1).getNome());
        
        // Act: remover
        registro.removerCliente(clienteAtualizado1);
        
        // Assert: após remoção
        assertEquals(0, registro.getClientes().size());
        assertNull(registro.retornarCliente(CPF_TESTE_1));
    }

    @Test
    void integracao_checarCpfComClienteWinx() throws Exception {
        // Setup: adicionar ClienteWinx
        ClienteWinx clienteWinx = criarClienteWinxComCpf(NOME_TESTE_1, CPF_TESTE_1, 100);
        injectarClientesAoRegistro(clienteWinx);
        
        // Assert: CPF não deve estar disponível
        assertFalse(registro.checarCpf(CPF_TESTE_1));
        assertTrue(registro.checarCpf(CPF_TESTE_2));
        
        // Assert: consegue recuperar ClienteWinx
        Cliente recuperado = registro.retornarCliente(CPF_TESTE_1);
        assertNotNull(recuperado);
        assertTrue(recuperado instanceof ClienteWinx);
    }

    @Test
    void integracao_getClientesRetornaListaMutativel() throws Exception {
        // Setup: adicionar clientes
        Cliente cliente1 = criarClienteComCpf(NOME_TESTE_1, CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf(NOME_TESTE_2, CPF_TESTE_2);
        injectarClientesAoRegistro(cliente1, cliente2);
        
        // Act: obter lista e modificar
        ArrayList<Cliente> lista = registro.getClientes();
        assertEquals(2, lista.size());
        
        // Assert: pode remover via lista retornada
        lista.remove(cliente1);
        assertEquals(1, lista.size());
        assertEquals(1, registro.getClientes().size());
    }

    // Helper method with typo fixed (injetar instead of injectarComo was fixed in previous code)
    private void injectarClientesAoRegistro(Cliente... clientes) {
        ArrayList<Cliente> lista = new ArrayList<>();
        for (Cliente cliente : clientes) {
            lista.add(cliente);
        }
        registro.setClientes(lista);
    }

    // ==========================================
    // TESTES ADICIONAIS - CENÁRIOS CRÍTICOS
    // ==========================================

    @Test
    void integracao_operacoesMultiplasPreservamIntegridade() throws Exception {
        // Setup: criar 10 clientes
        Cliente[] clientes = new Cliente[10];
        for (int i = 0; i < 10; i++) {
            String cpf = String.format("%0" + 11 + "d", i);
            clientes[i] = criarClienteComCpf("Cliente" + i, cpf);
        }
        
        // Act: adicionar todos
        injectarClientesAoRegistro(clientes);
        
        // Assert: todos presentes
        assertEquals(10, registro.getClientes().size());
        
        // Act: remover alternados (0, 2, 4, 6, 8)
        for (int i = 0; i < 10; i += 2) {
            registro.removerCliente(clientes[i]);
        }
        
        // Assert: restam 5
        assertEquals(5, registro.getClientes().size());
        
        // Assert: verifica quais permaneceram
        for (int i = 1; i < 10; i += 2) {
            assertNotNull(registro.retornarCliente(String.format("%0" + 11 + "d", i)));
        }
    }

    @Test
    void integracao_atualizarMultiplosClientesSequencialmente() throws Exception {
        // Setup: criar 3 clientes
        Cliente cliente1 = criarClienteComCpf("Nome1", CPF_TESTE_1);
        Cliente cliente2 = criarClienteComCpf("Nome2", CPF_TESTE_2);
        Cliente cliente3 = criarClienteComCpf("Nome3", CPF_TESTE_3);
        
        injectarClientesAoRegistro(cliente1, cliente2, cliente3);
        
        // Act & Assert: atualizar sequencialmente
        Cliente atualizado1 = criarClienteComCpf("UPDATED1", CPF_TESTE_1);
        registro.atualizarCliente(atualizado1);
        assertEquals("UPDATED1", registro.retornarCliente(CPF_TESTE_1).getNome());
        assertEquals(3, registro.getClientes().size());
        
        Cliente atualizado2 = criarClienteComCpf("UPDATED2", CPF_TESTE_2);
        registro.atualizarCliente(atualizado2);
        assertEquals("UPDATED2", registro.retornarCliente(CPF_TESTE_2).getNome());
        assertEquals(3, registro.getClientes().size());
        
        Cliente atualizado3 = criarClienteComCpf("UPDATED3", CPF_TESTE_3);
        registro.atualizarCliente(atualizado3);
        assertEquals("UPDATED3", registro.retornarCliente(CPF_TESTE_3).getNome());
        assertEquals(3, registro.getClientes().size());
    }

    @Test
    void integracao_checarCpfMantemEstadoAposOperacoes() throws Exception {
        // Setup
        Cliente cliente = criarClienteComCpf("TestClient", CPF_TESTE_1);
        
        // Assert: antes de adicionar, CPF disponível
        assertTrue(registro.checarCpf(CPF_TESTE_1));
        
        // Act: adicionar
        injectarClientesAoRegistro(cliente);
        
        // Assert: CPF não disponível
        assertFalse(registro.checarCpf(CPF_TESTE_1));
        
        // Act: atualizar (não muda status do CPF)
        Cliente atualizado = criarClienteComCpf("UpdatedClient", CPF_TESTE_1);
        registro.atualizarCliente(atualizado);
        
        // Assert: CPF ainda não disponível
        assertFalse(registro.checarCpf(CPF_TESTE_1));
        
        // Act: remover
        registro.removerCliente(atualizado);
        
        // Assert: CPF volta a estar disponível
        assertTrue(registro.checarCpf(CPF_TESTE_1));
    }

    @Test
    void integracao_buscaSemFalsoPositivo() throws Exception {
        // Setup: criar clientes com CPFs similares
        Cliente cliente1 = criarClienteComCpf("Cliente", "11111111111");
        Cliente cliente2 = criarClienteComCpf("Cliente", "11111111112");
        Cliente cliente3 = criarClienteComCpf("Cliente", "11111111113");
        
        injectarClientesAoRegistro(cliente1, cliente2, cliente3);
        
        // Act & Assert: buscar cada um sem confundi-los
        Cliente res1 = registro.retornarCliente("11111111111");
        Cliente res2 = registro.retornarCliente("11111111112");
        Cliente res3 = registro.retornarCliente("11111111113");
        
        assertNotNull(res1);
        assertNotNull(res2);
        assertNotNull(res3);
        
        // Valida que são clientes diferentes
        assertEquals(cliente1, res1);
        assertEquals(cliente2, res2);
        assertEquals(cliente3, res3);
        
        // Valida ordem de retorno (FIFO)
        assertEquals("11111111111", res1.getCpf());
        assertEquals("11111111112", res2.getCpf());
        assertEquals("11111111113", res3.getCpf());
    }

    @Test
    void integracao_misturaTiposClienteValidaComportamento() throws Exception {
        // Setup: criar mistura de Cliente e ClienteWinx
        Cliente clienteNormal = criarClienteComCpf("Normal", CPF_TESTE_1);
        ClienteWinx clienteWinx1 = criarClienteWinxComCpf("Winx1", CPF_TESTE_2, 100);
        Cliente clienteNormal2 = criarClienteComCpf("Normal2", CPF_TESTE_3);
        ClienteWinx clienteWinx2 = criarClienteWinxComCpf("Winx2", "44444444444", 200);
        
        injectarClientesAoRegistro(clienteNormal, clienteWinx1, clienteNormal2, clienteWinx2);
        
        // Assert: todos presentes
        assertEquals(4, registro.getClientes().size());
        
        // Assert: checarCpf funciona para ambos tipos
        assertFalse(registro.checarCpf(CPF_TESTE_1)); // Normal
        assertFalse(registro.checarCpf(CPF_TESTE_2)); // Winx
        assertFalse(registro.checarCpf(CPF_TESTE_3)); // Normal
        assertFalse(registro.checarCpf("44444444444")); // Winx
        
        // Act: remover ClienteWinx
        registro.removerCliente(clienteWinx1);
        
        // Assert: conta reduz mas tipos mistos continuam
        assertEquals(3, registro.getClientes().size());
        
        // Valida que removeu correto
        assertNull(registro.retornarCliente(CPF_TESTE_2));
        assertNotNull(registro.retornarCliente(CPF_TESTE_1));
    }
}
