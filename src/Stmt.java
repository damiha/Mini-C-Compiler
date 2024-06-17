import java.util.List;

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
            this.statements = statements;
        }


        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlockStatement(this);
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

    public interface Visitor<T>{
        T visitExpressionStatement(ExpressionStatement e);
        T visitIfStatement(IfStatement ifStatement);
        T visitBlockStatement(BlockStatement blockStatement);
        T visitPrintStatement(PrintStatement printStatement);
        T visitWhileStatement(WhileStatement whileStatement);
    }
}
