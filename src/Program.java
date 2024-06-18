import javax.management.relation.RelationNotFoundException;
import java.util.List;

public class Program {

    // global variables + function definitions (one of them must be main)
    List<Stmt.VariableDeclaration> globalDeclarations;
    List<Stmt.FunctionDeclaration> functionDeclarations;

    public Program(List<Stmt.VariableDeclaration> globalDeclarations, List<Stmt.FunctionDeclaration> functionDeclarations){
        this.globalDeclarations = globalDeclarations;
        this.functionDeclarations = functionDeclarations;

        checkMainFunctionPresent();
    }

    private void checkMainFunctionPresent(){

        for(Stmt.FunctionDeclaration funDecl : functionDeclarations){
            if(funDecl.functionName.equals("main")){
                return;
            }
        }

        throw new RuntimeException("Program lacks entry point 'main'");
    }
}
