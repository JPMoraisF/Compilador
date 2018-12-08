
package compilador;


public class Token{
    
    String palavra;
    Codigos codigo;
    
    
    public Token(String palavra, Codigos codigo){
        this.palavra = palavra;
        this.codigo = codigo;
    }
    
    public Token (Codigos codigoToken){
        this.codigo = codigoToken;
    }
            
    public String getPalavra(){
        return this.palavra;
    }
    
    /**
     * O código de '==' é RELACIONAL_COMPARACAO, pois é uma expressao relaciona e está comparando dois números.     
     * <p>O código para '=' é OPERADOR.IGUALDADE, para atribuir um valor a outra variável.
     * @return O código de acordo com a classe de códigos que representa o token lido.
     */
    public Codigos getCodigo(){
        return this.codigo;
    }
    
    
    
}