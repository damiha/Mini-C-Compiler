import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CompileCode {

    public static void main(String[] args) {

        String filePath = "primes.c";

        String source = getSource(filePath);

        // Lexer
        Lexer lexer = new Lexer(source);

        List<Token> tokens = lexer.getTokens();

        // Parser
        Parser parser = new Parser(tokens);

        Program program = parser.parse();

        // Code generator
        CodeGenerator codeGenerator = new CodeGenerator();

        Code code = codeGenerator.generateCode(program);

        // strip away the .c at the end
        String rawName = filePath.substring(0, filePath.length() - 2);
        String outputPath = rawName + ".cma";

        code.saveToFile(outputPath);
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
