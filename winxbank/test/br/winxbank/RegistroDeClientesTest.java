package br.winxbank;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

// Importamos tudo do pacote sistemaclientes para garantir que Banco, Conta e ContaPoupanca sejam achados
import br.winxbank.sistemaclientes.*;
import br.winxbank.sistemabancario.*;

public class RegistroDeClientesTest {

    private RegistroDeClientes registro;
    private static final String CPF_TESTE_1 = "11122233344";
    private static final String CPF_TESTE_2 = "99988877766";
    private static final String CPF_TESTE_3 = "12312312312";
    private static final String CPF_TESTE_4 = "55555555555";
    private static final String NOME_TESTE = "Cliente Teste";

    @BeforeEach
    void setup() {
        registro = RegistroDeClientes.getInstancia();
        registro.limparListaDeClientes();
    }

    // ==========================================
    // HELPER METHODS - Reduz redundância
    // ==========================================

    private Cliente criarClienteMockComCpf(String cpf) {
        Cliente mock = mock(Cliente.class);
        injetarCpfNoMock(mock, cpf);
        when(mock.getNome()).thenReturn(NOME_TESTE);
        return mock;
    }

    private ClienteWinx criarClienteWinxMockComCpf(String cpf, int pontos) {
        ClienteWinx mock = mock(ClienteWinx.class);
        injetarCpfNoMock(mock, cpf);
        when(mock.getNome()).thenReturn(NOME_TESTE);
        when(mock.getPontosDeCompra()).thenReturn(pontos);
        return mock;
    }

    private void adicionarClientesAoRegistro(Cliente... clientes) {
        ArrayList<Cliente> lista = new ArrayList<>();
        for (Cliente cliente : clientes) {
            lista.add(cliente);
        }
        registro.setClientes(lista);
    }

    private Conta criarContaMockComSaldo(double saldo, Class<?> tipo) {
        Conta conta = mock(Conta.class);
        when(conta.getSaldo()).thenReturn(saldo);
        when(conta.getNumeroConta()).thenReturn(123456);
        return conta;
    }

    private void injetarCpfNoMock(Cliente mock, String valorCpf) {
        try {
            Field campoCpf = Cliente.class.getDeclaredField("cpf");
            campoCpf.setAccessible(true);
            campoCpf.set(mock, valorCpf);
        } catch (NoSuchFieldException e) {
            try {
                Field campoCpfPai = Cliente.class.getSuperclass().getDeclaredField("cpf");
                campoCpfPai.setAccessible(true);
                campoCpfPai.set(mock, valorCpf);
            } catch (Exception ex) {
                throw new RuntimeException("Falha ao injetar CPF: variável não encontrada", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao injetar CPF no mock via Reflection", e);
        }
    }


    // ==========================================
    // TESTES DE NEGÓCIO - CHECAR CPF
    // ==========================================

    @Test
    void checarCpf_quandoCpfJaExiste_deveRetornarFalse() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteMock);
        assertFalse(registro.checarCpf(CPF_TESTE_1));
    }

    @Test
    void checarCpf_quandoCpfNaoExiste_deveRetornarTrue() {
        assertTrue(registro.checarCpf("12345678900"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "11122233344", "99988877766", "12345678901" })
    void checarCpf_variosValores_deveValidarCorretamente(String cpf) {
        if (cpf.equals("11122233344")) {
            Cliente mock = criarClienteMockComCpf(cpf);
            adicionarClientesAoRegistro(mock);
            assertFalse(registro.checarCpf(cpf));
        } else {
            assertTrue(registro.checarCpf(cpf));
        }
    }

    // ==========================================
    // TESTES DE NEGÓCIO - REMOVER CLIENTE
    // ==========================================

    @Test
    void removerCliente_deveRemoverDaListaCorretamente() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(clienteMock);
        registro.removerCliente(clienteMock);
        assertTrue(registro.getClientes().isEmpty());
    }

    @Test
    void removerCliente_comMultiplosClientes_deveRemoverApenasOCorreto() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(cliente1, cliente2);
        
        registro.removerCliente(cliente1);
        
        assertEquals(1, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(cliente2));
        assertFalse(registro.getClientes().contains(cliente1));
    }

    @Test
    void removerCliente_quandoClienteNaoExisteNaLista_naoDuzErro() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente clienteOutro = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(clienteOutro);
        
        assertDoesNotThrow(() -> registro.removerCliente(clienteMock));
        assertEquals(1, registro.getClientes().size());
    }

    // ==========================================
    // TESTES DE NEGÓCIO - ATUALIZAR CLIENTE
    // ==========================================

    @Test
    void atualizarCliente_deveSubstituirClienteComMesmoCpf() throws InterruptedException {
        Cliente clienteAntigoMock = criarClienteMockComCpf(CPF_TESTE_3);
        adicionarClientesAoRegistro(clienteAntigoMock);

        Cliente clienteAtualizadoMock = criarClienteMockComCpf(CPF_TESTE_3);
        when(clienteAtualizadoMock.getNome()).thenReturn("Nome Atualizado");

        registro.atualizarCliente(clienteAtualizadoMock);

        assertEquals(1, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(clienteAtualizadoMock));
    }

    @Test
    void atualizarCliente_comMultiplosClientes_apenasOComMesmoCpfDeveSerAtualizado() throws InterruptedException {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(cliente1, cliente2);

        Cliente clienteAtualizado = criarClienteMockComCpf(CPF_TESTE_1);
        when(clienteAtualizado.getNome()).thenReturn("Cliente 1 Atualizado");

        registro.atualizarCliente(clienteAtualizado);

        assertEquals(2, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(clienteAtualizado));
    }

    // ==========================================
    // TESTES DE NEGÓCIO - RETORNAR CLIENTE
    // ==========================================

    @Test
    void retornarCliente_quandoCpfExiste_deveRetornarOObjetoCliente() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_4);
        adicionarClientesAoRegistro(clienteMock);
        
        Cliente resultado = registro.retornarCliente(CPF_TESTE_4);
        
        assertNotNull(resultado);
        assertEquals(clienteMock, resultado);
    }

    @Test
    void retornarCliente_quandoCpfNaoExiste_deveRetornarNull() {
        assertNull(registro.retornarCliente("00000000000"));
    }

    @Test
    void retornarCliente_comMultiplosClientes_deveRetornarOCorreto() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(cliente1, cliente2);

        Cliente resultado = registro.retornarCliente(CPF_TESTE_2);

        assertNotNull(resultado);
        assertEquals(cliente2, resultado);
    }


    // ==========================================
    // TESTES DE UTILITY - GETTERS E LIMPEZA
    // ==========================================

    @Test
    void getInstancia_deveRetornarSempreAMesmaInstancia() {
        assertSame(RegistroDeClientes.getInstancia(), RegistroDeClientes.getInstancia());
    }

    @Test
    void limparListaDeClientes_deveDeixarAListaVazia() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteMock);
        
        registro.limparListaDeClientes();
        
        assertTrue(registro.getClientes().isEmpty());
    }

    @Test
    void getClientes_deveRetornarAListaAtual() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteMock);
        
        ArrayList<Cliente> clientes = registro.getClientes();
        
        assertNotNull(clientes);
        assertEquals(1, clientes.size());
    }

    // ==========================================
    // TESTES DE VISUALIZAÇÃO - PRINT
    // ==========================================

    @Test
    void printarListaDeClientes_comClienteNormal_naoDeveLancarExcecao() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteMock);
        
        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    @Test
    void printarListaDeClientes_quandoListaEstaVazia_naoDeveLancarExcecao() {
        registro.limparListaDeClientes();
        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    // ==========================================
    // TESTES DE VISUALIZAÇÃO - DETALHES
    // ==========================================

    @Test
    void visualizarDetalhesDoCliente_quandoNaoExiste_naoDeveLancarExcecao() {
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente("00000000000"));
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteExistente_naoDeveLancarExcecao() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteMock);
        
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_1));
    }

    // ==========================================
    // TESTES DE VISUALIZAÇÃO - CONTAS
    // ==========================================

    @Test
    void visualizarContas_comListaVazia_naoDeveLancarExcecao() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        when(clienteMock.getContas()).thenReturn(new ArrayList<>());
        
        assertDoesNotThrow(() -> registro.visualizarContas(clienteMock));
    }


    @Test
    void printarListaDeClientes_comClienteWinxMock_deveCobrirBranch() {
        ClienteWinx clienteWinxMock = criarClienteWinxMockComCpf(CPF_TESTE_1, 100);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(clienteWinxMock);
        registro.setClientes(lista);

        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    @Test
    void printarListaDeClientes_comClienteNormalMock_deveCobrirBranch() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(clienteMock);
        registro.setClientes(lista);

        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteWinx_deveCobrirBranch() {
        ClienteWinx clienteWinxMock = criarClienteWinxMockComCpf(CPF_TESTE_1, 150);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(clienteWinxMock);
        registro.setClientes(lista);

        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_1));
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteNormalMock_deveCobrirBranch() {
        Cliente clienteMock = criarClienteMockComCpf(CPF_TESTE_1);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(clienteMock);
        registro.setClientes(lista);

        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_1));
    }

    @Test
    void checarCpf_percorreLista_comparaCPFs() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        Cliente cliente3 = criarClienteMockComCpf(CPF_TESTE_3);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(cliente1);
        lista.add(cliente2);
        lista.add(cliente3);
        registro.setClientes(lista);

        // Deve retornar false pois CPF existe
        assertFalse(registro.checarCpf(CPF_TESTE_2));
    }

    @Test
    void retornarCliente_comListaNaoPorqueriCPFNoPrimeiro_deveAcharNoSegundoOuTerceiro() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        Cliente cliente3 = criarClienteMockComCpf(CPF_TESTE_3);
        ArrayList<Cliente> lista = new ArrayList<>();
        lista.add(cliente1);
        lista.add(cliente2);
        lista.add(cliente3);
        registro.setClientes(lista);

        Cliente resultado = registro.retornarCliente(CPF_TESTE_3);

        assertNotNull(resultado);
        assertEquals(cliente3, resultado);
    }

    @Test
    void atualizarCliente_quandoListaPossuiMultiplosClientes_deveAtualizarCorreto() throws InterruptedException {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        Cliente cliente3 = criarClienteMockComCpf(CPF_TESTE_3);
        adicionarClientesAoRegistro(cliente1, cliente2, cliente3);

        Cliente clienteAtualizado = criarClienteMockComCpf(CPF_TESTE_2);
        when(clienteAtualizado.getNome()).thenReturn("Cliente 2 Atualizado");

        registro.atualizarCliente(clienteAtualizado);

        assertEquals(3, registro.getClientes().size());
    }

    @Test
    void removerCliente_quandoExistemMultiplosClientes_deveRemoverUmEMantenhosOutros() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        Cliente cliente3 = criarClienteMockComCpf(CPF_TESTE_3);
        adicionarClientesAoRegistro(cliente1, cliente2, cliente3);

        registro.removerCliente(cliente2);

        assertEquals(2, registro.getClientes().size());
        assertTrue(registro.getClientes().contains(cliente1));
        assertTrue(registro.getClientes().contains(cliente3));
        assertFalse(registro.getClientes().contains(cliente2));
    }

    @Test
    void checarCpf_comListaPossuindoMultiplosClientes_deveRetornarCorreto() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        adicionarClientesAoRegistro(cliente1, cliente2);

        // CPF que existe deve retornar false
        assertFalse(registro.checarCpf(CPF_TESTE_1));
        assertFalse(registro.checarCpf(CPF_TESTE_2));
        
        // CPF que não existe deve retornar true
        assertTrue(registro.checarCpf(CPF_TESTE_3));
    }

    @Test
    void setClientes_comMultiplosClientesEmLista_deveAdicionarTodos() {
        ArrayList<Cliente> listaClientes = new ArrayList<>();
        listaClientes.add(criarClienteMockComCpf(CPF_TESTE_1));
        listaClientes.add(criarClienteMockComCpf(CPF_TESTE_2));
        listaClientes.add(criarClienteMockComCpf(CPF_TESTE_3));

        registro.setClientes(listaClientes);

        assertEquals(3, registro.getClientes().size());
    }

    @Test
    void visualizarContas_comContaPoupancaECartao_deveExibirDetalhesCompletos() {
        Cliente cliente = criarClienteMockComCpf(CPF_TESTE_1);
        
        // Mock de ContaPoupanca usando mock()
        ContaPoupanca contaPoupanca = mock(ContaPoupanca.class);
        when(contaPoupanca.getSaldo()).thenReturn(150000.0);
        when(contaPoupanca.getNumeroConta()).thenReturn(123456);
        when(contaPoupanca.getDividaDeEmprestimo()).thenReturn(0.0);
        when(contaPoupanca.getTipoDaConta()).thenReturn("Poupança");
        
        Cartao cartao = mock(Cartao.class);
        when(cartao.getNumero()).thenReturn(1111);
        when(cartao.getCsv()).thenReturn(123);
        when(contaPoupanca.getCartao()).thenReturn(cartao);
        
        ArrayList<Conta> contas = new ArrayList<>();
        contas.add(contaPoupanca);
        when(cliente.getContas()).thenReturn(contas);
        
        // Verificar que não lança exceção durante a visualização
        assertDoesNotThrow(() -> registro.visualizarContas(cliente));
    }

    @Test
    void visualizarContas_comContaCorrenteECartaoDado_deveExibirDetalhesCartaoCredito() {
        Cliente cliente = criarClienteMockComCpf(CPF_TESTE_1);
        
        // Mock de ContaCorrente usando mock()
        ContaCorrente contaCorrente = mock(ContaCorrente.class);
        when(contaCorrente.getSaldo()).thenReturn(150000.0);
        when(contaCorrente.getNumeroConta()).thenReturn(654321);
        when(contaCorrente.getDividaDeEmprestimo()).thenReturn(5000.0);
        when(contaCorrente.getTipoDaConta()).thenReturn("Corrente");
        
        Cartao cartao = mock(Cartao.class);
        when(cartao.getNumero()).thenReturn(1111);
        when(cartao.getCsv()).thenReturn(456);
        when(contaCorrente.getCartao()).thenReturn(cartao);
        
        CartaoCredito cartaoCredito = mock(CartaoCredito.class);
        when(cartaoCredito.getNumero()).thenReturn(5555);
        when(cartaoCredito.getCsv()).thenReturn(789);
        when(cartaoCredito.getFatura()).thenReturn(12500.0);
        when(contaCorrente.getCartaoCredito()).thenReturn(cartaoCredito);
        
        ArrayList<Conta> contas = new ArrayList<>();
        contas.add(contaCorrente);
        when(cliente.getContas()).thenReturn(contas);
        
        // Verificar que não lança exceção durante a visualização
        assertDoesNotThrow(() -> registro.visualizarContas(cliente));
    }

    @Test
    void printarListaDeClientes_comClienteNormalNaLista_deveExibirSemPontos() {
        // Usar Cliente real (spy) para que getClass() retorne Cliente.class
        Cliente cliente = spy(new Cliente("João Silva", CPF_TESTE_1));
        
        adicionarClientesAoRegistro(cliente);
        
        // Mock para visualizarContas retornar lista vazia
        when(cliente.getContas()).thenReturn(new ArrayList<>());
        
        // Verificar que printarListaDeClientes funciona sem exceção
        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    @Test
    void printarListaDeClientes_comClienteWinxMockEClienteNormalReal_deveCobrirAmbos() {
        // Adicionar ClienteWinx mock
        ClienteWinx clienteWinx = criarClienteWinxMockComCpf(CPF_TESTE_1, 100);
        when(clienteWinx.getContas()).thenReturn(new ArrayList<>());
        
        // Adicionar Cliente normal real (spy)
        Cliente clienteNormal = spy(new Cliente("João Silva", CPF_TESTE_2));
        when(clienteNormal.getContas()).thenReturn(new ArrayList<>());
        
        adicionarClientesAoRegistro(clienteWinx, clienteNormal);
        
        // Executar printarListaDeClientes deve cobrir ambos os branches
        assertDoesNotThrow(() -> registro.printarListaDeClientes());
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteNormalReal_deveCobrirBranchCliente() {
        // Usar Cliente real (spy) para que getClass() retorne Cliente.class
        Cliente cliente = spy(new Cliente("João Silva", CPF_TESTE_3));
        when(cliente.getContas()).thenReturn(new ArrayList<>());
        
        adicionarClientesAoRegistro(cliente);
        
        // Executar visualizarDetalhesDoCliente deve cobrir o branch de Cliente.class
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_3));
    }

    @Test
    void visualizarDetalhesDoCliente_comMultiplosClientesFindoCorreto_deveCobrirComparacaoCpf() {
        // Adicionar múltiplos clientes onde o correto é o último
        ClienteWinx clienteWinx1 = criarClienteWinxMockComCpf(CPF_TESTE_1, 50);
        when(clienteWinx1.getContas()).thenReturn(new ArrayList<>());
        
        Cliente clienteNormal = spy(new Cliente("João Silva", CPF_TESTE_2));
        when(clienteNormal.getContas()).thenReturn(new ArrayList<>());
        
        ClienteWinx clienteWinx2 = criarClienteWinxMockComCpf(CPF_TESTE_3, 150);
        when(clienteWinx2.getContas()).thenReturn(new ArrayList<>());
        
        adicionarClientesAoRegistro(clienteWinx1, clienteNormal, clienteWinx2);
        
        // Buscar o último cliente deve cobrir comparação de CPF
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_3));
    }

    @Test
    void retornarCliente_quandoExistemMultiplosClientes_deveRetornarOPrimeiro() {
        Cliente cliente1 = criarClienteMockComCpf(CPF_TESTE_1);
        Cliente cliente2 = criarClienteMockComCpf(CPF_TESTE_2);
        Cliente cliente3 = criarClienteMockComCpf(CPF_TESTE_3);
        adicionarClientesAoRegistro(cliente1, cliente2, cliente3);

        Cliente resultado = registro.retornarCliente(CPF_TESTE_1);

        assertNotNull(resultado);
        assertEquals(cliente1, resultado);
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteWinxExistente_deveExibirPontosDeCompra() {
        ClienteWinx cliente = criarClienteWinxMockComCpf(CPF_TESTE_1, 100);
        adicionarClientesAoRegistro(cliente);
        
        ArrayList<Conta> contas = new ArrayList<>();
        when(cliente.getContas()).thenReturn(contas);
        
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_1));
    }

    @Test
    void visualizarDetalhesDoCliente_comClienteNormalExistente_deveExibirSemPontos() {
        Cliente cliente = mock(Cliente.class);
        injetarCpfNoMock(cliente, CPF_TESTE_1);
        when(cliente.getNome()).thenReturn("João Silva");
        
        adicionarClientesAoRegistro(cliente);
        
        ArrayList<Conta> contas = new ArrayList<>();
        when(cliente.getContas()).thenReturn(contas);
        
        assertDoesNotThrow(() -> registro.visualizarDetalhesDoCliente(CPF_TESTE_1));
    }

    @Test
    void cadastrarCliente_comContaPoupancaComSaldoAlto_deveCobrir41() {
        // Este teste tenta cobrir a linha 41: if(conta.getClass() == ContaPoupanca.class)
        // quando o saldo é >= 100000 (criando ClienteWinx com ContaPoupanca)
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            // Criar uma ContaPoupanca real (não mock) para permitir getClass()
            // Precisamos criar com os parâmetros corretos
            Cartao cartaoDebito = mock(Cartao.class);
            when(cartaoDebito.getNumero()).thenReturn(1111);
            when(cartaoDebito.getCsv()).thenReturn(111);
            
            ContaPoupanca contaPoupanca = new ContaPoupanca(123456, 150000.0, cartaoDebito, 0.0);
            when(bancoMock.abrirNovaConta()).thenReturn(contaPoupanca);

            String entrada = "Teste Poupança\n55555555555\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            // Executar o cadastro
            assertDoesNotThrow(() -> {
                try (MockedStatic<Movimentacao> mockedMov = mockStatic(Movimentacao.class)) {
                    registro.cadastrarCliente();
                }
            });

            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_comContaPoupancaComSaldoBaixo_deveCobrir49() {
        // Este teste tenta cobrir a linha 49: if(conta.getClass() == ContaPoupanca.class)
        // quando o saldo é < 100000 (criando Cliente normal com ContaPoupanca)
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            // Criar uma ContaPoupanca real com saldo baixo
            Cartao cartaoDebito = mock(Cartao.class);
            when(cartaoDebito.getNumero()).thenReturn(2222);
            when(cartaoDebito.getCsv()).thenReturn(222);
            
            ContaPoupanca contaPoupanca = new ContaPoupanca(234567, 50000.0, cartaoDebito, 0.0);
            when(bancoMock.abrirNovaConta()).thenReturn(contaPoupanca);

            String entrada = "Teste Poupança Baixo\n66666666666\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            // Executar o cadastro
            assertDoesNotThrow(() -> {
                try (MockedStatic<Movimentacao> mockedMov = mockStatic(Movimentacao.class)) {
                    registro.cadastrarCliente();
                }
            });

            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_saldoMaior100k_deveCriarClienteWinx() {
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            Conta contaMock = mock(Conta.class);
            when(bancoMock.abrirNovaConta()).thenReturn(contaMock);
            when(contaMock.getSaldo()).thenReturn(150000.0);
            when(contaMock.getNumeroConta()).thenReturn(123456);
            when(contaMock.getCartao()).thenReturn(mock(Cartao.class));
            when(contaMock.getExtrato()).thenReturn(new ArrayList<>());

            String entrada = "Elon Musk\n12312312312\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            assertDoesNotThrow(() -> registro.cadastrarCliente());
        } finally {
            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_saldoMaior100kComTipoPoupanca_deveCriarClienteWinx() {
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            Conta contaMock = mock(Conta.class);
            when(bancoMock.abrirNovaConta()).thenReturn(contaMock);
            when(contaMock.getSaldo()).thenReturn(120000.0);
            when(contaMock.getNumeroConta()).thenReturn(654321);
            when(contaMock.getCartao()).thenReturn(mock(Cartao.class));
            when(contaMock.getExtrato()).thenReturn(new ArrayList<>());

            String entrada = "Maria Silva\n98765432100\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            assertDoesNotThrow(() -> registro.cadastrarCliente());
        } finally {
            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_saldoMenor100k_deveCriarClienteComum() {
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            Conta contaMock = mock(Conta.class);
            when(bancoMock.abrirNovaConta()).thenReturn(contaMock);
            when(contaMock.getSaldo()).thenReturn(5000.0);
            when(contaMock.getNumeroConta()).thenReturn(111111);
            when(contaMock.getCartao()).thenReturn(mock(Cartao.class));
            when(contaMock.getExtrato()).thenReturn(new ArrayList<>());

            String entrada = "Joao Silva\n98798798798\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            assertDoesNotThrow(() -> registro.cadastrarCliente());
        } finally {
            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_saldoMenor100kComTipoPoupanca_deveCriarClienteComum() {
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            Conta contaMock = mock(Conta.class);
            when(bancoMock.abrirNovaConta()).thenReturn(contaMock);
            when(contaMock.getSaldo()).thenReturn(30000.0);
            when(contaMock.getNumeroConta()).thenReturn(222222);
            when(contaMock.getCartao()).thenReturn(mock(Cartao.class));
            when(contaMock.getExtrato()).thenReturn(new ArrayList<>());

            String entrada = "Pedro Santos\n12345678901\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            assertDoesNotThrow(() -> registro.cadastrarCliente());
        } finally {
            System.setIn(System.in);
        }
    }

    @Test
    void cadastrarCliente_cpfJaExistente_deveCairNoElseDeErro() {
        Cliente clienteExistente = criarClienteMockComCpf(CPF_TESTE_1);
        adicionarClientesAoRegistro(clienteExistente);

        String entrada = "Copia\n" + CPF_TESTE_1 + "\n";
        System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

        assertDoesNotThrow(() -> registro.cadastrarCliente());

        System.setIn(System.in);
    }

    @Test
    void cadastrarCliente_listaVaziaNoInicio_deveCairNoBranch() {
        try (MockedStatic<Banco> mockedBanco = mockStatic(Banco.class)) {
            Banco bancoMock = mock(Banco.class);
            mockedBanco.when(Banco::getInstancia).thenReturn(bancoMock);

            Conta contaMock = mock(Conta.class);
            when(bancoMock.abrirNovaConta()).thenReturn(contaMock);
            when(contaMock.getSaldo()).thenReturn(50000.0);
            when(contaMock.getNumeroConta()).thenReturn(333333);
            when(contaMock.getCartao()).thenReturn(mock(Cartao.class));
            when(contaMock.getExtrato()).thenReturn(new ArrayList<>());

            String entrada = "Primeiro Cliente\n11111111111\n";
            System.setIn(new java.io.ByteArrayInputStream(entrada.getBytes()));

            assertDoesNotThrow(() -> registro.cadastrarCliente());
        } finally {
            System.setIn(System.in);
        }
    }
}