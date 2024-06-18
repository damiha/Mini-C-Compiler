import java.util.List;
import java.util.ArrayList;

public abstract class Stmt {

    abstract <T> T accept(Stmt.Visitor<T> visitor);
    static class ExpressionStatement extends Stmt{

        Expr expr;

        public ExpressionStatement(Expr expr){
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    static class BlockStatement extends Stmt {
        List<Stmt> statements;

        public BlockStatement(List<Stmt> statements){

            // make all lists mutable
            this.statements = new ArrayList<>(statements);
        }


        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    static class VariableDeclaration extends Stmt{


        String variableName;

        // can be int[] or int or name of a struct
        String type;
        int nElements;

        public VariableDeclaration(String type, String variableName){
            this.type = type;
            this.variableName = variableName;
            nElements = 1;
        }

        public VariableDeclaration(String type, String variableName, int nElements){
            this(type, variableName);
            this.nElements = nElements;

            if(nElements > 1 && !type.endsWith("[]")){
                throw new RuntimeException("Non-array type can't have more than one element");
            }
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariableDeclaration(this);
        }
    }

    static class FunctionDeclaration extends Stmt{

        String returnType;
        String functionName;
        List<VariableDeclaration> parameters;
        BlockStatement body;

        public FunctionDeclaration(String returnType, String functionName, List<VariableDeclaration> parameters, BlockStatement body){
            this.returnType = returnType;
            this.functionName = functionName;
            this.parameters = parameters;
            this.body = body;

            // adding one more return statement at the end can't hurt
            // if there's a return before, this is never reached
            // if there's no return, we add it
            this.body.statements.add(new ReturnStatement(null));
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFunctionDeclaration(this);
        }
    }

    static class IfStatement extends Stmt {

        // there are no booleans in C (we just check if something is zero or not)
        Expr condition;

        // if there is no else branch, else branch = null
        Stmt ifBranch;
        Stmt elseBranch;

        public IfStatement(Expr condition, Stmt ifBranch, Stmt elseBranch){
            this.condition = condition;
            this.ifBranch = ifBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    static class WhileStatement extends Stmt{

        Expr condition;

        Stmt body;

        public WhileStatement(Expr condition, Stmt body){
            this.condition = condition;
            this.body = body;
        }


        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    // we bake print statements into the language, so we can test
    static class PrintStatement extends Stmt{
        Expr expr;

        public PrintStatement(Expr expr){
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    static class ReturnStatement extends Stmt{

        Expr expr;

        public ReturnStatement(Expr expr){
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }

    public interface Visitor<T>{
        T visitExpressionStatement(ExpressionStatement e);
        T visitIfStatement(IfStatement ifStatement);
        T visitBlockStatement(BlockStatement blockStatement);
        T visitPrintStatement(PrintStatement printStatement);
        T visitWhileStatement(WhileStatement whileStatement);
        T visitVariableDeclaration(VariableDeclaration variableDeclaration);
        T visitFunctionDeclaration(FunctionDeclaration functionDeclaration);
        T visitReturnStatement(ReturnStatement returnStatement);
    }
}
