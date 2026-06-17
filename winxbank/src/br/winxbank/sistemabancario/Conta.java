package br.winxbank.sistemabancario;

import br.winxbank.geradordedocumentos.ArquivoExtrato;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Natália
 * Esta classe é responsável por representar uma entidade abstrata Conta.
 */
public abstract class Conta implements OperacoesAutomaticas {
	
	private static final Logger logger = Logger.getLogger(Conta.class.getName());

    protected int numeroConta;
    protected double saldo;
    protected Cartao cartao;
    protected double dividaDeEmprestimo;
    protected ArrayList<Movimentacao> extrato = new ArrayList<>();

    /**
     * Construtor padrão da classe conta.
     */
    protected Conta(int numeroConta, double saldo, Cartao cartao, double dividaDeEmprestimo) {
        this.numeroConta = numeroConta;
        this.saldo = saldo;
        this.cartao = cartao;
        this.dividaDeEmprestimo = dividaDeEmprestimo;
    }

    /**
     * Construtor alternativo para leitura de arquivo json.
     */
    protected Conta(int numeroConta, double saldo, Cartao cartao, double dividaDeEmprestimo, List<Movimentacao> movimentacoes) {
        this.numeroConta = numeroConta;
        this.saldo = saldo;
        this.cartao = cartao;
        this.dividaDeEmprestimo = dividaDeEmprestimo;
        if (movimentacoes != null) {
            this.extrato.addAll(movimentacoes);
        }
    }

    /**
     * Método responsável por subtrair o valor da dívida de empréstimo.
     */
    public void pagarParcelaDeEmprestimo(double valor) {
        // Validação: Ignora valores negativos e pagamentos acima da dívida
        if (valor <= 0) return;
        if (valor > this.dividaDeEmprestimo) return;
        
        this.dividaDeEmprestimo -= valor;
    }

    /**
     * Método responsável por somar um valor à dívida de empréstimo.
     */
    public void requisitarEmprestimo(double valor) {
        // Validação: Ignora empréstimos negativos
        if (valor <= 0) return;
        
        this.dividaDeEmprestimo += valor;
    }

    /**
     * Método responsável por cobrar juros de um empréstimo conforme meses passados.
     */
    public void cobrarJurusEmprestimo() {
        if (this.dividaDeEmprestimo > 0) {
            double resultado = dividaDeEmprestimo / taxaJurus;
            this.dividaDeEmprestimo -= resultado;
        }
    }

    /**
     * Método responsável por gerar um extrato.
     */
    public void gerarExtrato() throws FileNotFoundException {
        ArquivoExtrato.getInstancia().gerarDocumento(this);
    }

    /**
     * Método responsável por realizar uma transferência via pix a uma conta.
     */
    public void fazerPix(Conta contaDestino, double valor) {
        // Validação: Lança exceção para conta nula (exigido pelo assertThrows no teste)
        if (contaDestino == null) {
            throw new IllegalArgumentException("A conta de destino não pode ser nula.");
        }
        
        // Validação: Ignora pix para si mesmo, valores negativos ou sem saldo suficiente
        if (contaDestino == this) return;
        if (valor <= 0) return;
        if (valor > this.saldo) return;

        // Processa o PIX debitando da origem e creditando no destino
        this.saldo -= valor;
        contaDestino.saldo += valor;
    }

    /**
     * Método responsável por realizar uma compra.
     */
    public abstract void comprar(double valor);

    /**
     * Método responsável por sacar um valor da conta.
     */
    public void sacar(double valor) {
        // Validação: Ignora saques negativos e valores acima do saldo
        if (valor <= 0) return;
        if (valor > this.saldo) return;
        
        this.saldo -= valor;
        logger.log(Level.INFO, "Você está sacando o valor de: {0}", valor);
    }

    /**
     * Método responsável por depositar um valor na conta.
     */
    public double depositar(double valor) {
        // Validação: Ignora depósitos negativos
        if (valor <= 0) return 0;
        
        setSaldo(valor);
        return valor;
    }

    // ==========================
    //      GETTERS & SETTERS
    // ==========================

    public double getSaldo() {
        return saldo;
    }

    public int getNumeroConta() {
        return numeroConta;
    }

    public double getDividaDeEmprestimo() {
        return dividaDeEmprestimo;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public List<Movimentacao> getExtrato() {
        return extrato;
    }

    public void setSaldo(double valor) {
        this.saldo += valor;
    }

    public void setExtrato(Movimentacao movimentacao) {
        // Validação: Lança exceção para movimentação nula (exigido pelo assertThrows no teste)
        if (movimentacao == null) {
            throw new IllegalArgumentException("A movimentação não pode ser nula.");
        }
        this.extrato.add(movimentacao);
    }
}