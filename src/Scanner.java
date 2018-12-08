package compilador;

import java.io.*;

public class Scanner {
    static int coluna = 1;
    static int linha = 1;
    int carac;
    

    static int getColuna() {
        return Scanner.coluna;
    }

    static int getLinha() {
        return Scanner.linha;
    }

    public Token gerarToken(PushbackReader leitor) throws IOException {

        while (true) {
            carac = leitor.read();
            if (Character.isDigit(carac) || carac == '.') {     // SE TIVERMOS UM NUMERO OU UM PONTO, SERA UM INTEIRO OU FLOAT
                if (carac == '.') {
                    return formaNumero(leitor, true);           // chama formaNumero com primeiro numero sendo ponto true
                } 
                else {
                    return formaNumero(leitor, false);
                }                
            }             
            else if (Character.isLetter(carac) || carac == '_') {         //  SE COMECAR COM UMA LETRA, PODE SER UMA VARIAVEL OU UM IDENTIFICADOR
                return formaIdentificador(leitor);
            }             
            else if (carac == '(' || carac == '{' || carac == '}' || carac == ';' || carac == ',' || carac == ')') {                    // SE O CARACTER ESPECIAL FOR UMA OPERACAO ARITMETICA OU PARENTESES OU ASPAS DUPLAS
                return formaCaracterEspecial(carac);
            }             
            else if (carac == '+' || carac == '-' || carac == '*' || carac == '=' || carac == '<' || carac == '>' || carac == '!') {
                return formaOperador(leitor, carac);
            }            
            else if (carac == '/'){
                return formaComentario(leitor, carac);
            }            
            else if (carac == 39) {
                return formaChar(leitor, carac);
            }
            else if (carac == 9){
                coluna = coluna + 4;
            }
            else if (carac == ' '){
                coluna++;
            }
            else if (carac == 13) {
            } 
            else if (carac == 10) {
                linha++;
                coluna = 1;
            } 
            else if (carac == -1) {
                return new Token(Codigos.FIM_DE_ARQUIVO);
            }
            else{
                throw new IOException("Caractere nao faz parte da linguagem. \nErro na linha " + getLinha() + " Coluna " + getColuna());
            }
        }
    }

    private Token formaNumero(PushbackReader leitor, boolean ponto) throws IOException { // ENTRA NESSA FUNCAO SE ELE ACHAR UM NUMERO OU PONTO

        String palavra = "";
        while (Character.isDigit(carac)) {
            ponto = false;
            palavra += (char) carac;
            coluna++;
            carac = leitor.read();
        }
        if (carac == '.') {
            if(ponto){
                palavra += 0;
            }
            palavra += (char) carac;
            coluna++;
            carac = leitor.read();
            while (Character.isDigit(carac)) {
                palavra += (char) carac;
                coluna++;
                carac = leitor.read();
            }
            if (Character.isLetter(carac)) {
                leitor.unread(carac);
                throw new IOException("Numero float malformado. \nErro na linha " + getLinha() + " Coluna " + getColuna());
            } else if (carac == '.') {
                System.out.println("NÃºmero float malformado Dois pontos encontrados. \nErro na linha " + getLinha() + " Coluna " + getColuna());
                return new Token(Codigos.FLOAT_MALFORMADO);
            }
            leitor.unread(carac);
            return new Token(palavra, Codigos.NUMERO_FLOAT);
        }   
        else{
            leitor.unread(carac);
            return new Token(palavra, Codigos.NUMERO_INTEIRO);
        }
    }
    
    private Token formaIdentificador(PushbackReader leitor) throws IOException {

        String palavra = "";
        while (Character.isLetterOrDigit(carac) || carac == '_') {
            palavra += (char) carac;
            coluna++;
            carac = leitor.read();
        }
        leitor.unread(carac);
            switch (palavra) {
                case "int":
                    return new Token(Codigos.IDENTIFICADOR_INT);
                case "main":
                    return new Token(Codigos.IDENTIFICADOR_MAIN);
                case "if":
                    return new Token(Codigos.IDENTIFICADOR_IF);
                case "while":
                    return new Token(Codigos.IDENTIFICADOR_WHILE);
                case "do":
                    return new Token(Codigos.IDENTIFICADOR_DO);
                case "for":
                    return new Token(Codigos.IDENTIFICADOR_FOR);
                case "else":
                    return new Token(Codigos.IDENTIFICADOR_ELSE);
                case "switch":
                    return new Token(Codigos.IDENTIFICADOR_SWITCH);
                case "float":
                    return new Token(Codigos.IDENTIFICADOR_FLOAT);
                case "char":
                    return new Token(Codigos.IDENTIFICADOR_CHAR);
                default:
                    return new Token(palavra, Codigos.IDENTIFICADOR);
            }        
    }

    private Token formaCaracterEspecial(int carac) throws IOException{
        coluna++;
        switch (carac) {
            case '(':
                return new Token(Codigos.ABRE_PARENTESES);
            case ')':
                return new Token(Codigos.FECHA_PARENTESES);
            case ',':
                return new Token(Codigos.VIRGULA);
            case ';':
                return new Token(Codigos.PONTO_E_VIRGULA);
            case '{':
                return new Token(Codigos.ABRE_CHAVES);              
            default:
                return new Token(Codigos.FECHA_CHAVES);     /* de acordo com o if, so 6 possibilidades existem
                                                            portanto se nao for nenhuma das 5 anteriores, 
                                                            so pode ser essa
                                                            */
        }
    }

    private Token formaOperador(PushbackReader leitor, int carac) throws IOException {
        coluna++;      
        switch (carac) {
            case '+':
                return new Token(Codigos.OPERADOR_SOMA);
            case '-':
                return new Token(Codigos.OPERADOR_SUBTRACAO);
            case '=': // caso encontre um sinal de igualdade
                carac = leitor.read();
                if(carac == '='){
                    return new Token(Codigos.RELACIONAL_COMPARACAO);
                }
                else{
                    leitor.unread(carac);
                    return new Token(Codigos.OPERADOR_IGUALDADE);
                }                
            case '<': // caso encontre um sinal de menor <
                carac = leitor.read();
                if(carac == '='){
                    return new Token(Codigos.RELACIONAL_MENORIGUAL);
                }
                else{
                    leitor.unread(carac);
                    return new Token(Codigos.RELACIONAL_MENORQUE);
                }                
            case '>': //caso encontre um sinal de maior >
                carac = leitor.read();
                if(carac == '='){
                    return new Token(Codigos.RELACIONAL_MAIORIGUAL);
                }
                else{
                    leitor.unread(carac);
                    return new Token(Codigos.RELACIONAL_MAIORQUE);
                }                
             case '!': // caso encontre um sinal de negacao !
                carac = leitor.read();
                if(carac == '='){
                    return new Token(Codigos.RELACIONAL_DIFERENTE);
                }
                else{
                    throw new IOException("Sinal de diferença não pode estar sozinho. \nErro na linha " + getLinha() + " Coluna " + getColuna());
                }
             default: // se nao for nenhuma dos outros cases, entao e multiplicacao
                 return new Token(Codigos.OPERADOR_MULTIPLICACAO);
        }
    }    
    
    private Token formaComentario(PushbackReader leitor, int carac) throws IOException {
        
        carac = leitor.read();        
        if(carac == '/'){                                       // COMENTARIO LINHA UNICA ENCONTRADO!
            carac = leitor.read();
            while(carac != 10 && carac != -1){                //  ENQUANTO FOR DIFERENTE DE FIM DE LINHA OU FIM DE ARQUIVO
                carac = leitor.read();
            }
            leitor.unread(carac);
            return gerarToken(leitor);        
        }   
        else if (carac == '*'){                                 //COMENTARIO MULTIPLA LINHA ENCONTRADO
            carac = leitor.read();            
            while(carac != -1){                                 //ENQUANTO NAO FOR FIM DE ARQUIVO               
                if(carac == '*'){                    
                    carac = leitor.read();
                    if(carac == '/'){
                        return gerarToken(leitor);                        
                    }
                    else{
                        leitor.unread(carac);
                    }
                }
                if(carac == 10){
                    linha++;
                    coluna = 1;
                }
                carac = leitor.read();                
            }  
            /*
            //SE CHEGOU AQUI E PORQUE ACABOU O ARQUIVO ANTES DO COMENTARIO
            System.out.println("Arquivo chega ao fim antes do comentario de multiplas linhas ser fechado. \nErro na linha " + getLinha() + " Coluna " + getColuna());
            return new Token(Codigos.COMENTARIO_MALFORMADO);            
            */
            throw new IOException("Arquivo chega ao fim antes do comentario de multiplas linhas ser fechado. \nErro na linha " + getLinha() + " Coluna " + getColuna());
        }
        else{
           leitor.unread(carac);
           return new Token(Codigos.OPERADOR_DIVISAO);
        }        
    }

    private Token formaChar(PushbackReader leitor, int carac) throws IOException {
        String palavra = "";
        coluna++;
        carac = leitor.read();            
        if(Character.isLetterOrDigit(carac)){
            palavra += (char) carac;
            coluna++;
            carac = leitor.read();
            if(carac == 39){
                coluna++;
                return new Token(palavra, Codigos.CHAR);
            }
            else{
                throw new IOException("Char pode ter apenas uma letra no seu identificador. \nErro na linha " + getLinha() + " Coluna " + getColuna());
            }
        }
        throw new IOException("Caractere desconhecido encontrado!. \nErro na linha " + getLinha() + " Coluna " + getColuna());
    }
}