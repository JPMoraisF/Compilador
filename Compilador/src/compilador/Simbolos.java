
package compilador;


public class Simbolos {
    private int escopo;
    private String lex;
    private Codigos tipoSimbolo;
    private Codigos operador;

    public Codigos getOperacao() {
        return operador;
    }

    public void setOperacao(Codigos operacao) {
        this.operador = operacao;
    }
    
    public Simbolos(int escopo, String lex, Codigos tipoSimbolo){
        this.tipoSimbolo = tipoSimbolo;
        this.escopo = escopo;
        this.lex = lex;
    }
    
    public Simbolos(String lex){
        this.lex = lex;
    }
    
    public Simbolos(Codigos tipo){
        this.tipoSimbolo = tipo;
    }
    
    public Simbolos (Codigos tipo, String lex){
        this.tipoSimbolo = tipo;
        this.lex = lex;
    }

    public int getEscopo() {
        return escopo;
    }

    public void setEscopo(int escopo) {
        this.escopo = escopo;
    }

    public String getLex() {
        return lex;
    }

    public void setLex(String lex) {
        this.lex = lex;
    }

    public Codigos getTipoSimbolo() {
        return tipoSimbolo;
    }

    public void setTipoSimbolo(Codigos tipoSimbolo) {
        this.tipoSimbolo = tipoSimbolo;
    }
    
}
