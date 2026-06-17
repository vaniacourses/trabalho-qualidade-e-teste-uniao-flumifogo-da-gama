package br.winxbank.sistemabancario;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * @author Dani
 * Esta classe é responsável por representar uma entidade ContaCorrente.
 */
public class ContaCorrente extends Conta implements OperacoesAutomaticas{

    private CartaoCredito cartaoCredito;
    private static final Logger logger = Logger.getLogger(ContaCorrente.class.getName());

    /**
     * Construtor padrão da classe conta.
     *
     * @param saldo
     * @param cartaoDebito
     * @param dividaDeEmprestimo
     */
    public ContaCorrente(int numeroConta, double saldo, Cartao cartaoDebito, double dividaDeEmprestimo, CartaoCredito cartaoCredito) {
        super(numeroConta, saldo, cartaoDebito, dividaDeEmprestimo);
        this.cartaoCredito = cartaoCredito;
    }
    

    /**
     * Método responsável por pagar fatura com o saldo da conta.
     * @param valor
     */
    public void pagarFatura(double valor){
        this.saldo-=valor;
        this.cartaoCredito.setFatura(-valor);

    }

    /**
     * Método responsável por descontar a taxa de uma conta corrente.
     */
    public void descontarTaxa(){
        this.saldo -= taxaManutencaoConta;
        movimentacaoBancaria(taxaManutencaoConta);
        Movimentacao movimentacao = new Movimentacao(taxaManutencaoConta, Movimentacao.TipoDaMovimentacao.SAIDA);
        this.setExtrato(movimentacao);

    }

    /**
     * Método da interface MovimentacaoBancaria sobrescrito responsável por movimentar dinheiro ao banco.
     * @param valor
     */
    @Override
    public void movimentacaoBancaria(double valor) {
        Banco.getInstancia().setReceitas(valor);
    }


    public CartaoCredito getCartaoCredito() {
        return cartaoCredito;
    }

    public String getTipoDaConta() {
        return "Corrente";
    }

    /**
     * Método abstrato da classe conta sobrescrito responsável por realizar uma compra.
     * @param valor
     */
    @Override
    public void comprar(double valor) {
        Scanner sc = new Scanner(System.in);
        logger.info("Você deseja pagar no debito ou no credito? 1 (debito) ou 2 (credito)");
        int decisao = sc.nextInt();
        String divisoria = "------------------------------------------------";
        if (decisao == 1){
            logger.info(divisoria);
            String dadosCartao = this.cartao.getNumero() + "\n" + this.cartao.csv;
            logger.info(dadosCartao);
            logger.info(divisoria);
            logger.info("Este e o cartao que deseja utilizar? Digite 1 (confirmar)");
            int decisao2 = sc.nextInt();
            if(decisao2 == 1){
                cartao.debitar(this, valor);
                logger.info("Valor debitado.");
            }
            else{
                logger.warning("Compra cancelada. Efetue a compra novamente.");
            }
        }
        else if(decisao == 2){
            logger.info(divisoria);
            String dadosCartaoCredito = this.cartaoCredito.getNumero() + "\n" + this.cartaoCredito.csv;
            logger.info(dadosCartaoCredito);
            logger.info(divisoria);
            logger.info("Este e o cartao que deseja utilizar? Digite 1 (confirmar)");
            int decisao2 = sc.nextInt();
            if(decisao2 == 1){
                this.cartaoCredito.creditar(valor);
                logger.info("Valor creditado.");
            }
            else{
                logger.warning("Compra cancelada. Efetue a compra novamente.");
            }
        }
    }
}
