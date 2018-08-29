package compilador;    

public class CodigoIntermediario {
    
    public static String gerarCodigo(Codigos operador, Simbolos e1, Simbolos e2){
        StringBuilder codigo = new StringBuilder();
        codigo.append("    ");
        codigo.append(e1.getLex());
        codigo.append(CodigoIntermediario.operador(operador));
        codigo.append(e2.getLex());        
        System.out.println(codigo.toString());
        return codigo.toString();
    }
    
    public static String operador(Codigos operador){
        switch(operador){
            case OPERADOR_IGUALDADE:
                return " = ";
            case OPERADOR_SOMA:
                return " + ";
            case OPERADOR_SUBTRACAO:
                return " - ";
            case OPERADOR_MULTIPLICACAO:
                return " * ";
            case OPERADOR_DIVISAO:
                return " / ";
            case RELACIONAL_COMPARACAO:
                return " == ";
            case RELACIONAL_DIFERENTE:
                return " != ";
            case RELACIONAL_MAIORIGUAL:
                return " >= ";
            case RELACIONAL_MENORIGUAL:
                return " <= ";
            case RELACIONAL_MAIORQUE:
                return " > ";
            case RELACIONAL_MENORQUE:
                return " < ";
        }
        return null;
    }
    
    public static void gerar(Simbolos atrib, Simbolos arg1, Simbolos arg2, Codigos operador){
        StringBuilder codigo = new StringBuilder();
        codigo.append("    ");
        codigo.append(atrib.getLex());
        codigo.append(" = ");
        codigo.append(arg1.getLex());        
        codigo.append(CodigoIntermediario.operador(operador));        
        codigo.append(arg2.getLex());
        System.out.println(codigo.toString());
    }
    
    public static void gerarCondicional(String ifLabel, String relacional, Codigos operador){
        StringBuilder codigo = new StringBuilder();
        codigo.append("    ");
        codigo.append("if ");
        codigo.append(relacional);
        codigo.append(CodigoIntermediario.operador(operador));
        codigo.append(" 0 JMP ");
        codigo.append(ifLabel);
        System.out.println(codigo.toString());
    }
    
    public static void gerarLabel(String label){
        StringBuilder codigo = new StringBuilder();
        codigo.append(label);
        codigo.append(":");
        System.out.println(codigo.toString());
    }
    
    public static void gerarJUMP(String label){
        StringBuilder codigo = new StringBuilder();
        codigo.append("    ");
        codigo.append("JMP ");
        codigo.append(label);
        System.out.println(codigo.toString());
    }
    
}
