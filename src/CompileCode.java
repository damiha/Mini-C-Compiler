import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CompileCode {

    public static void main(String[] args) {

        String filePath = "pointers.c";

        String source = getSource(filePath);

        // Lexer
        Lexer lexer = new Lexer(source);

        List<Token> tokens = lexer.getTokens();

        for(Token token : tokens){
            System.out.println(token);
        }

        // Parser
        Parser parser = new Parser(tokens);

        Program program = parser.parse();
        // Code generator
    }

    public static String getSource(String filePath) {

        try {
            // Read the entire content of the file as a single string
            String content = Files.readString(Path.of(filePath));
            return content;
        } catch (IOException e) {
            System.out.println("Could not open file at " + filePath);
        }
        System.exit(1);
        return "";
    }
}
