package compilador;
import java.io.*;

public class main {
    static Token token;
    static Parser parser;
    static PushbackReader leitor;
    

    public static void main(String[] args) throws IOException {
        
        FileReader arquivo;
        
        if(args.length == 0){
            System.out.println("E obrigatorio fornecer o nome do arquivo que deseja abrir! Exemplo: java compilador.main teste.txt");;
            return;
        }
        
        try {
            arquivo = new FileReader(args[0]);
            leitor = new PushbackReader(arquivo);
        } 
        catch (FileNotFoundException e) {
            System.out.println("ERRO! - Arquivo nao encontrado!");
            return;
        }   
        
        try{
            Parser parser = new Parser();
            parser.iniciarParser(leitor);
        }
        catch(IOException e) {   
            System.err.println(e.getMessage());
            //e.printStackTrace();       
        }       
    }
}