package compilador;
public enum Codigos {  
    INTEIRO_MALFORMADO(-5),
    SIMBOLO_FORA_LINGUAGEM(-4), 
    COMENTARIO_MALFORMADO(-3), 
    CHAR_MALFORMADO(-2),
    FLOAT_MALFORMADO(-1),
    FIM_DE_ARQUIVO(0),
    NUMERO_INTEIRO(1),
    NUMERO_FLOAT(2),
    PONTO_E_VIRGULA(3),
    IDENTIFICADOR(6),
    IDENTIFICADOR_MAIN(7), 
    IDENTIFICADOR_IF(8), 
    IDENTIFICADOR_WHILE(9), 
    IDENTIFICADOR_DO(10), 
    IDENTIFICADOR_FOR(11), 
    IDENTIFICADOR_ELSE(12), 
    IDENTIFICADOR_SWITCH(13), 
    IDENTIFICADOR_INT(14),
    CHAR(15), 
    ABRE_PARENTESES(17),
    FECHA_PARENTESES(18),
    ABRE_CHAVES(19),
    FECHA_CHAVES(20), 
    OPERADOR_SOMA(21), 
    OPERADOR_SUBTRACAO(22), 
    OPERADOR_MULTIPLICACAO(23), 
    OPERADOR_DIVISAO(24), 
    VIRGULA(25), 
    /**
     * ==
     */
    RELACIONAL_COMPARACAO(26), 
    /**
     * =
     */
    OPERADOR_IGUALDADE(27), 
    /**
     * "<="
     */
    RELACIONAL_MENORIGUAL(28), 
    /**
     * <
     */
    RELACIONAL_MENORQUE(29), 
    /**
     * >=
     */
    RELACIONAL_MAIORIGUAL(30), 
    /**
     * >
     */
    RELACIONAL_MAIORQUE(31), 
    /**
     * !=
     */
    RELACIONAL_DIFERENTE(32), 
    /**
     * <!@#=
     */
    RELACIONAL_MALFORMADO(33), 
    IDENTIFICADOR_FLOAT(38), 
    IDENTIFICADOR_CHAR(39),
    QUEBRA_LINHA(40);
    
    public int valorCodigo;
    
    Codigos(int i){
        valorCodigo = i;
    }
    
    public int getvalorCodigo(){
        return valorCodigo;
    }

}
