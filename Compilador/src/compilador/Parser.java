package compilador;


import java.io.*;
import java.util.Stack;

public class Parser {
    static Scanner scanner = new Scanner();
    static Token token;
    static PushbackReader leitor;
    static Stack<Simbolos> pilha = new Stack();
    int escopo = 0;
    int palavraCont = 0;
    int labelCont = 0; 
    
    
    
    /**
     * O começo do parser. Esse método testa se o programa começa no formato:   INT MAIN () { }
     * @param leitor
     * @throws IOException 
     */
    public void iniciarParser(PushbackReader leitor) throws IOException{
        
        token = scanner.gerarToken(leitor);
        if(token.getCodigo().equals(Codigos.IDENTIFICADOR_INT)){
            token = scanner.gerarToken(leitor);
            if(token.getCodigo().equals(Codigos.IDENTIFICADOR_MAIN)){
                token = scanner.gerarToken(leitor);
                if(token.getCodigo().equals(Codigos.ABRE_PARENTESES)){
                    token = scanner.gerarToken(leitor);
                    if(token.getCodigo().equals(Codigos.FECHA_PARENTESES)){
                        token = scanner.gerarToken(leitor);
                        funcaoBloco(leitor);
                        if(!token.getCodigo().equals(Codigos.FIM_DE_ARQUIVO)){
                            throw new IOException("Instrucao fora de bloco. \nErro na linha "+Scanner.getLinha()+" Coluna " +Scanner.getColuna());
                        }
                    }
                    else{
                        throw new IOException("Fecha parenteses nao encontrado apos abre parenteses. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                    }
                }
                else{
                    throw new IOException("Abre parenteses nao encontrado apos o main. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
            }
            else{                
                throw new IOException("Main nao encontrado apos identificador Int. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
        }
        else{
            throw new IOException("O programa deve comecar com um Int. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }        
    }
    
    /**
     * Testa se o bloco começa com um abre chaves e o que tem dentro do bloco. 
     * <p> Este método não tem dependência de tokens anteriores.
     * @param leitor
     * @throws IOException 
     */
    public void funcaoBloco(PushbackReader leitor) throws IOException {
        if(token.codigo.equals(Codigos.ABRE_CHAVES)){
            escopo++;
            token = scanner.gerarToken(leitor);
            while(token.getCodigo().equals(Codigos.IDENTIFICADOR_INT) || 
               token.getCodigo().equals(Codigos.IDENTIFICADOR_FLOAT) ||
               token.getCodigo().equals(Codigos.IDENTIFICADOR_CHAR)){                
                Codigos tipoVar = token.getCodigo();
                Variavel(leitor, tipoVar);
            }
            
            while(token.getCodigo().equals(Codigos.IDENTIFICADOR) || 
               token.getCodigo().equals(Codigos.IDENTIFICADOR_IF) || 
               token.getCodigo().equals(Codigos.IDENTIFICADOR_DO) || 
               token.getCodigo().equals(Codigos.IDENTIFICADOR_WHILE) ||
               token.getCodigo().equals(Codigos.ABRE_CHAVES)){
                Comando(leitor);
            }
            
            if(token.getCodigo().equals(Codigos.IDENTIFICADOR_INT) ||
               token.getCodigo().equals(Codigos.IDENTIFICADOR_FLOAT) ||
               token.getCodigo().equals(Codigos.IDENTIFICADOR_CHAR)){
                throw new IOException("As variaveis so podem ser declaradas no inicio do programa. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            if(token.getCodigo().equals(Codigos.IDENTIFICADOR_ELSE)){
                throw new IOException("Encontrado um ELSE sem um IF correspondente. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            if(!token.getCodigo().equals(Codigos.FECHA_CHAVES)){
                throw new IOException("Fecha chaves não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            else{
                token = scanner.gerarToken(leitor);                
            }
        }
        else{
            throw new IOException("O bloco do programa deve comecar com um abre chaves. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
        removeEscopo(escopo);
        escopo--;
    }
    
    /**
     * Declaracao de variavel. Esse método pode chamar comandoBasico.
     * @param leitor
     * @param tipoVar
     * @throws IOException 
     * @see compilador.Parser#ComandoBasico(java.io.PushbackReader) 
     */
    public void Variavel(PushbackReader leitor, Codigos tipoVar) throws IOException {
        
        Simbolos aux2;        
        token = scanner.gerarToken(leitor);
        if(token.getCodigo().equals(Codigos.IDENTIFICADOR)){                        
            Simbolos novo = new Simbolos(escopo, token.getPalavra(), tipoVar);
            if(buscar(novo.getLex(), escopo) == null){
                pilha.push(novo);
            }
            else{
                throw new IOException("Variavel '" +novo.getLex()+"' ja definida. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }    
            token = scanner.gerarToken(leitor);
            while(token.getCodigo().equals(Codigos.VIRGULA)){
                token = scanner.gerarToken(leitor);
                if(!token.getCodigo().equals(Codigos.IDENTIFICADOR)){
                    throw new IOException("Declaracao de variavel termina com ponto-e-virgula. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
                novo = new Simbolos(escopo, token.getPalavra(), tipoVar);
                if(buscar(novo.getLex(), escopo) == null){
                    pilha.push(novo);
                }
                else{
                    throw new IOException("Variavel '"+novo.getLex()+"' ja definida. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
                token = scanner.gerarToken(leitor);
            }
            if(!token.getCodigo().equals(Codigos.PONTO_E_VIRGULA)){
                if(!token.getCodigo().equals(Codigos.OPERADOR_IGUALDADE )){
                    throw new IOException("Nao foi encontrado virgula ou ponto-e-virgula no fim de declaracao de variavel. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
                else{
                    token = scanner.gerarToken(leitor);
                    aux2 = geradorExpressaoAritmetica(leitor);
                    checarTiposA(novo, aux2);                
                    if(novo.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
                       aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
                        aux2.setLex("*FLOAT* " + aux2.getLex());
                    }
                    CodigoIntermediario.gerarCodigo(Codigos.OPERADOR_IGUALDADE, novo, aux2);
                    if(!token.getCodigo().equals(Codigos.PONTO_E_VIRGULA)){
                        throw new IOException("Nao foi encontrado virgula ou ponto-e-virgula no fim de declaracao de variavel. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                    }
                }
            }
        }
        else{
            throw new IOException("Nome de variavel nao encontrado apos tipo de variavel. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
        token = scanner.gerarToken(leitor);
    }
    
    /**
     * Se o token lido for um abre chaves ou um identificador, chamará o método <b>COMANDO BASICO</b>.
     * <p>Se o token for um while ou um do, significa que é uma iteração e chama o método <b>ITERACAO</b>
     * <p> Caso não seja nenhum, a única possibilidade é que seja um if/else, então chama o metodo <b>CASO_IF_ELSE</b>
     * 
     * @throws java.io.IOException
     * @see #ComandoBasico(java.io.PushbackReader)
     * @see #Iteracao(java.io.PushbackReader) 
     * @see #caso_IF_ELSE(java.io.PushbackReader)
     * @param leitor 
     */
    public void Comando(PushbackReader leitor) throws IOException { 
        if(token.getCodigo().equals(Codigos.IDENTIFICADOR) ||
           token.getCodigo().equals(Codigos.ABRE_CHAVES)){
            ComandoBasico(leitor);
        }
        else if (token.getCodigo().equals(Codigos.IDENTIFICADOR_WHILE) ||
                token.getCodigo().equals(Codigos.IDENTIFICADOR_DO)){
            Iteracao(leitor);
        }
        else if(token.getCodigo().equals(Codigos.IDENTIFICADOR_IF)){
            caso_IF_ELSE(leitor);            
        }
        else{
            throw new IOException("Nao foi encontrado um ABRE-CHAVES, IF, WHILE, DO ou IDENTIFICADOR. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
    }
    
    /**
     * Se começar com um identificador, é uma atribuição, do tipo a = b. Chamará o método <b>ATRIBUICAO</b>
     * <p>Senão, a única opção é um bloco de comandos. Nesse caso, chamará o método <b>FUNCAOBLOCO</b>
     * @param leitor
     * @throws java.io.IOException
     *@see #Atribuicao(java.io.PushbackReader) 
     * @see #funcaoBloco(java.io.PushbackReader) 
     */
    public void ComandoBasico(PushbackReader leitor) throws IOException {
        if(token.getCodigo().equals(Codigos.IDENTIFICADOR)){
            Atribuicao(leitor);
        }
        else if(token.getCodigo().equals(Codigos.ABRE_CHAVES)){
            funcaoBloco(leitor);
        }
        else{
            throw new IOException("Nao foi encontrado ABRE CHAVES ou IDENTIFICADOR. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
    }
    
    /**
     * Se após o token identificador tiver um operador de igualdade '=', chamará o método <b>GERADOREXPRESSAOARITMETICA</b>
     * <p>Adimite que antes de chamar esse método, um identificador foi lido antes.
     * @param leitor
     * @throws java.io.IOException
     * @see #geradorExpressaoAritmetica(java.io.PushbackReader) 
     */
    public void Atribuicao(PushbackReader leitor) throws IOException {
        
        Simbolos aux1, aux2, simboloTemporario;
        if(token.getCodigo().equals(Codigos.IDENTIFICADOR)){
            simboloTemporario = buscar(token.getPalavra(), 0);
            if(simboloTemporario != null){
                aux1 = new Simbolos(simboloTemporario.getTipoSimbolo(), simboloTemporario.getLex());
            }
            else{
                throw new IOException("Variavel '"+token.getPalavra()+"' nao declarada. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            
            token = scanner.gerarToken(leitor);
            if(token.getCodigo().equals(Codigos.OPERADOR_IGUALDADE)){
                token = scanner.gerarToken(leitor);
                aux2 = geradorExpressaoAritmetica(leitor);
                checarTiposA(aux1, aux2);
                if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
                   aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
                    aux2.setLex("*FLOAT* " + aux2.getLex());
                }
                CodigoIntermediario.gerarCodigo(Codigos.OPERADOR_IGUALDADE, aux1, aux2);
                if(!token.getCodigo().equals(Codigos.PONTO_E_VIRGULA)) {
                    throw new IOException("Ponto-e-virgula não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
            token = scanner.gerarToken(leitor);
            }
            else{
                throw new IOException("Operador aritmetico de igual não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }          
        }
        else{
            throw new IOException("Identificador nao encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
    }
    
    /**
     * Espera que apareca um while e um abre parenteses. Caso sim, chamará o mpetodo de <b>OPERADORRELACIONAL</b> para testar a condicao dentro do parenteses.
     * <p>Depois de testar a condicao e encontrar o abre chaves, chama o metodo de comando para verificacao dentro do bloco.
     * <p>O else desse método faz a mesma coisa, mas invertido para testar o DO, já que antes do operadaor relacional de =ntro dos parenteses, tem o comando do DO.
     * @param leitor
     * @throws java.io.IOException
     * @see #operadorRelacional(java.io.PushbackReader) 
     * @see #Comando(java.io.PushbackReader) 
     */
    public void Iteracao(PushbackReader leitor) throws IOException {
        
        String inicioLabel, fimLabel, relacional, inicioLabelDO, relacionalDO;
        switch (token.getCodigo()) {
            case IDENTIFICADOR_WHILE:
                inicioLabel = Label();
                fimLabel = Label();
                token = scanner.gerarToken(leitor);
                if(token.getCodigo().equals(Codigos.ABRE_PARENTESES)){
                    
                    CodigoIntermediario.gerarLabel(inicioLabel);
                    token = scanner.gerarToken(leitor);
                    relacional = ExpressaoRelacional(leitor);
                    CodigoIntermediario.gerarCondicional(fimLabel, relacional, Codigos.RELACIONAL_COMPARACAO);
                    if(token.getCodigo().equals(Codigos.FECHA_PARENTESES)){
                        token = scanner.gerarToken(leitor);
                        Comando(leitor);
                    }
                    else{
                        throw new IOException("Fecha parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                    }
                    CodigoIntermediario.gerarJUMP(inicioLabel);
                    CodigoIntermediario.gerarLabel(fimLabel);
                }
                else{
                    throw new IOException("Abre parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }
                break;
            case IDENTIFICADOR_DO:
                inicioLabelDO = Label();
                CodigoIntermediario.gerarLabel(inicioLabelDO);
                token = scanner.gerarToken(leitor);
                Comando(leitor);
                if(token.getCodigo().equals(Codigos.IDENTIFICADOR_WHILE)){
                    token = scanner.gerarToken(leitor);
                    if(token.getCodigo().equals(Codigos.ABRE_PARENTESES)){
                        token = scanner.gerarToken(leitor);
                        relacionalDO = ExpressaoRelacional(leitor);
                        CodigoIntermediario.gerarCondicional(inicioLabelDO, relacionalDO, Codigos.RELACIONAL_COMPARACAO);
                        if(token.getCodigo().equals(Codigos.FECHA_PARENTESES)){
                            token = scanner.gerarToken(leitor);
                            if(!token.getCodigo().equals(Codigos.PONTO_E_VIRGULA)){
                                throw new IOException("Ponto-e-virgula não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                            }
                        }
                        else{
                            throw new IOException("Fecha parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                        }
                    }
                    else{
                        throw new IOException("Abre parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                    }
                }
                else{
                    throw new IOException("While apos Do nao encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                }   token = scanner.gerarToken(leitor);
                break;
            default:
                throw new IOException("Do ou While não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
    }
    
    /**
     * Antes desse método, já foi lido o identofocador do IF. Testa para encontrar o  abre parenteses, e chama o método de operador relacional para averiguar a condicao.
     * <p>Se encontrado o identificador do ELSE, chama o método de comando para o ELSE.
     * @param leitor
     * @throws java.io.IOException
     *@see #operadorRelacional(java.io.PushbackReader) 
     * @see #Comando(java.io.PushbackReader) 
     */
    public void caso_IF_ELSE(PushbackReader leitor) throws IOException {

        String relacional;   
        String ifLabel = Label();
        String elseLabel = Label();
        token = scanner.gerarToken(leitor);
        
        if(token.getCodigo().equals(Codigos.ABRE_PARENTESES)){
            token = scanner.gerarToken(leitor);
            relacional = ExpressaoRelacional(leitor);
            if(token.getCodigo().equals(Codigos.FECHA_PARENTESES)){
                CodigoIntermediario.gerarCondicional(elseLabel, relacional, Codigos.RELACIONAL_COMPARACAO);
                token = scanner.gerarToken(leitor);
                Comando(leitor);                
                
                if(token.getCodigo().equals(Codigos.IDENTIFICADOR_ELSE)){
                    CodigoIntermediario.gerarJUMP(ifLabel);
                    CodigoIntermediario.gerarLabel(elseLabel);
                    token = scanner.gerarToken(leitor);
                    Comando(leitor);
                    CodigoIntermediario.gerarLabel(ifLabel);
                }
                else{
                    CodigoIntermediario.gerarLabel(elseLabel);
                }
            }
            else{
                throw new IOException("Fecha parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
        }
        else{
            throw new IOException("Fecha parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
    }
    
    /**
     * O termo é do tipo <code> a * b, c / d</code> ou apenas um fator.
     * <p>Diferente da ExpressaoAritmetica, o termo apenas verifica multiplicação e divisão. Adicão e Subtraçãé responsabilidade de ExpressaoAritmetica.
     * @param leitor
     * @return 
     * @throws java.io.IOException
     * @see #Fator(java.io.PushbackReader) 
     * @see #ExpressaoAritmetica(java.io.PushbackReader) 
     */
    public Simbolos Termo(PushbackReader leitor) throws IOException{
        Simbolos aux1, aux2, palavra;
        Codigos operador, tipoFim;
        
        aux1 = Fator(leitor);        
        while(token.getCodigo().equals(Codigos.OPERADOR_DIVISAO) ||
              token.getCodigo().equals(Codigos.OPERADOR_MULTIPLICACAO)){
            operador = token.getCodigo();
            token = scanner.gerarToken(leitor);
            aux2 = Fator(leitor);
            tipoFim = checarTiposT(aux1, aux2, operador);
            
            if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT) && 
               aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT)){
                aux1.setLex("*FLOAT* "+aux1.getLex());
            }
            else if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) &&
                    aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
                aux2.setLex("*FLOAT* " + aux2.getLex());
            }
            
            palavra = new Simbolos(tipoFim, palavraNova());
            CodigoIntermediario.gerar(palavra, aux1, aux2, operador);
            aux1 = palavra;            
        }
        return aux1;
    }
    
    /**
     * Do tipo ( expr_arit ), onde pode ter uma expressão dentro de parênteses, como uma divisão com adição. Pode também ser apenas um número.
     * <p> Chama a função de geracao de expressao aritmetica para testar o que tem no parenteses.
     * @param leitor
     * @return 
     * @throws java.io.IOException 
     * @see #geradorExpressaoAritmetica(java.io.PushbackReader) 
     */
    public Simbolos Fator(PushbackReader leitor) throws IOException {
        Simbolos aux;
        Codigos tipo;
        Simbolos simboloAux;
        
        if(token.getCodigo().equals(Codigos.ABRE_PARENTESES)){
            token = scanner.gerarToken(leitor);
            aux = geradorExpressaoAritmetica(leitor);
            if(token.getCodigo().equals(Codigos.FECHA_PARENTESES)){
                token = scanner.gerarToken(leitor);
            }
            else{
                throw new IOException("Fecha parenteses não encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            return aux;
        }
        
        else{
            aux = new Simbolos(token.getPalavra());
            switch (token.getCodigo()) {
                case IDENTIFICADOR:
                    simboloAux = buscar(token.getPalavra(), 0);
                    if(simboloAux != null){
                        tipo = simboloAux.getTipoSimbolo();
                    }
                    else{
                        throw new IOException("Variavel '"+token.getPalavra()+"' nao declarada! \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
                    }   token = scanner.gerarToken(leitor);
                    break;
                case NUMERO_INTEIRO:
                    tipo = Codigos.IDENTIFICADOR_INT;
                    token = scanner.gerarToken(leitor);
                    break;
                case NUMERO_FLOAT:
                    tipo = Codigos.IDENTIFICADOR_FLOAT;
                    token = scanner.gerarToken(leitor);
                    break;
                case CHAR:
                    tipo = Codigos.IDENTIFICADOR_CHAR;
                    token = scanner.gerarToken(leitor);
                    break;
                default:
                    throw new IOException("Fator nao encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
        }
        aux.setTipoSimbolo(tipo);
        return aux;
    }
    
    /**
     *Esse método testa se a expressao aritmética é um fator, já que pode ser apenas um número ou um parênteses, com uma expressão dentro dele.
     * <p>Chama o método ExpressaoAritmetica, caso seja uma soma ou subtração.
     * @return 
     * @throws java.io.IOException
     * @see #ExpressaoAritmetica(java.io.PushbackReader) 
     * @param leitor 
     */
    public Simbolos geradorExpressaoAritmetica(PushbackReader leitor) throws IOException {
        
        Simbolos aux1, aux2, palavra;
        Codigos tipoFim;
        aux1 = Termo(leitor);
        aux2 = ExpressaoAritmetica(leitor);
        if(aux2 != null){
            tipoFim = checarTiposT(aux1, aux2, aux2.getOperacao());
            if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT) && 
               aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT)){
                aux1.setLex("*FLOAT* " + aux1.getLex());
            }
            else if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
                    aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
                aux2.setLex("*FLOAT* " + aux2.getLex());
            } 
            palavra = new Simbolos(tipoFim, palavraNova());
            palavra.setOperacao(aux2.getOperacao());  
            CodigoIntermediario.gerar(palavra, aux1, aux2, palavra.getOperacao());
        }
        else{
            return aux1;
        }
        return palavra;
    }
    
    private String ExpressaoRelacional (PushbackReader leitor) throws IOException{
        Simbolos aux1, aux2, palavra;
        Codigos operador;
        aux1 = geradorExpressaoAritmetica(leitor);
        operador = operadorRelacional(leitor);
        aux2 = geradorExpressaoAritmetica(leitor); 
        checarTiposER(aux1.getTipoSimbolo(), aux2.getTipoSimbolo());        
        if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT) &&
           aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT)){
            aux1.setLex("*FLOAT* " + aux1.getLex());
        }
        else if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
                aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
            aux2.setLex("*FLOAT* " + aux2.getLex());
        }
        palavra = new Simbolos(palavraNova()); 
        CodigoIntermediario.gerar(palavra, aux1, aux2, operador);
        return palavra.getLex();
    }
    
    /**
     *Em iterações, se o que tiver dentro do parênteses for uma expressão relacional, do tipo !=, ==, então será testado qual o relacional.
     * <p>Se não for nenhum dos 6, então lançará uma exceção.
     * @param leitor
     * @return 
     * @throws java.io.IOException 
     * @see #geradorExpressaoAritmetica(java.io.PushbackReader) 
     */
    public Codigos operadorRelacional(PushbackReader leitor) throws IOException{
        
        Codigos operador;
        if(!token.getCodigo().equals(Codigos.RELACIONAL_MAIORQUE) && 
           !token.getCodigo().equals(Codigos.RELACIONAL_MENORQUE) && 
           !token.getCodigo().equals(Codigos.RELACIONAL_MENORIGUAL) && 
           !token.getCodigo().equals(Codigos.RELACIONAL_MAIORIGUAL) && 
           !token.getCodigo().equals(Codigos.RELACIONAL_DIFERENTE) && 
           !token.getCodigo().equals(Codigos.RELACIONAL_COMPARACAO)){
            throw new IOException("Operador relacional nao encontrado. \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
        }
        operador = token.getCodigo();
        token = scanner.gerarToken(leitor);
        return operador;
    }
    
    public Simbolos ExpressaoAritmetica(PushbackReader leitor) throws IOException {
        
        Simbolos aux1, aux2, palavra;
        Codigos operador, tipoFim;        
        if(token.getCodigo().equals(Codigos.OPERADOR_SOMA) || 
           token.getCodigo().equals(Codigos.OPERADOR_SUBTRACAO)){
            operador = token.getCodigo();
            token = scanner.gerarToken(leitor);
            aux1 = Termo(leitor);
            aux2 = ExpressaoAritmetica(leitor);
        }
        else{
            return null;
        }
        
        tipoFim = checarTiposT(aux1, aux2, operador);
        if(aux2 != null){
             if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT) && 
               aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT)){
                aux1.setLex("*FLOAT* " + aux1.getLex());
            }
            else if(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
                    aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT)){
                aux2.setLex("*FLOAT* " + aux2.getLex());
            }
            
            palavra = new Simbolos(tipoFim, palavraNova());
            palavra.setOperacao(operador);
            CodigoIntermediario.gerar(palavra, aux1, aux2, palavra.getOperacao());
        }
        else{
            aux1.setOperacao(operador);
            return aux1;
        }
        return palavra;
    }        

    private Simbolos buscar(String palavra, int escopo) {
        Simbolos temp;
        int tamanho = pilha.size() - 1;
        while(tamanho >= 0){
            temp = pilha.get(tamanho);
            if(temp.getLex().equals(palavra)){
                if(escopo == 0){
                    return temp;
                }
                else if(temp.getEscopo() == escopo){
                    return temp;
                }
            }
            tamanho--;
        }
        return null;
    }
 

    private Codigos checarTiposT(Simbolos aux1, Simbolos aux2, Codigos operador) throws IOException {
        Codigos tipo1, tipo2;
        if(aux2 != null){
            tipo1 = aux1.getTipoSimbolo();
            tipo2 = aux2.getTipoSimbolo();
            
            if(tipo1 == tipo2 && tipo1.equals(Codigos.IDENTIFICADOR_CHAR)){
                return aux1.getTipoSimbolo();
            }      
            else if(tipo1.equals(Codigos.IDENTIFICADOR_CHAR) || tipo2.equals(Codigos.IDENTIFICADOR_CHAR)){
                throw new IOException("Char nao pode operar com outros tipos! \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
            else if (operador.equals(Codigos.OPERADOR_DIVISAO)){
                return Codigos.IDENTIFICADOR_FLOAT;
            }
            else if (tipo1.equals(Codigos.IDENTIFICADOR_FLOAT) || tipo2.equals(Codigos.IDENTIFICADOR_FLOAT)){
                return Codigos.IDENTIFICADOR_FLOAT;
            }
            return Codigos.IDENTIFICADOR_INT;
        }
        else{
            return aux1.getTipoSimbolo();
        }
    }
    
    private void checarTiposA(Simbolos aux1, Simbolos aux2) throws IOException {
        if(aux1.getTipoSimbolo() != aux2.getTipoSimbolo()){
            if(!(aux1.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_FLOAT) && 
               aux2.getTipoSimbolo().equals(Codigos.IDENTIFICADOR_INT))){
                throw new IOException("Tipos incompativeis: "+aux1.getTipoSimbolo()+" com "+aux2.getTipoSimbolo()+"\nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
        }
    }

    private String palavraNova() {
        return "T" + palavraCont++;
    }

    private String Label() {
        return "L" + labelCont++;
    }

    private void checarTiposER(Codigos tipo1, Codigos tipo2) throws IOException {
        if(tipo1 != tipo2){
            if(tipo1.equals(Codigos.IDENTIFICADOR_CHAR) || tipo2.equals(Codigos.IDENTIFICADOR_CHAR)){
                throw new IOException("Char nao pode operar com outros tipos! \nErro na linha "+Scanner.getLinha()+" Coluna "+Scanner.getColuna());
            }
        }
    }   
    
    private void removeEscopo(int escopo){
        Simbolos aux;
        if(!pilha.isEmpty()){
            do{
                aux = pilha.peek();
                if(aux.getEscopo() == escopo){
                    pilha.pop();
                }
            }while(!pilha.isEmpty() && aux.getEscopo() == escopo);
        }
    }
}